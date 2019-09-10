package org.inventivetalent.trashapp.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.inventivetalent.trashapp.common.Util.readLines;

public class OsmBridgeClient {

	public static final  int    AUTH_REQUEST_CODE = 7881;
	private static final String BRIDGE_URL        = "https://osmbridge.trashapp.cc";

	private Gson gson = new Gson();

	// Session ID used for API auth
	private String sid;
	private long   sidSaveTime;

	private JsonObject userInfoJson;

	/**
	 * Creates a new client without session id
	 */
	public OsmBridgeClient() {
	}

	/**
	 * Creates a new client with the specified session id
	 *
	 * @param sid session id
	 */
	public OsmBridgeClient(String sid) {
		this.sid = sid;
	}

	/**
	 * Creates a new client and attempts to get the session id from the shared preferences
	 *
	 * @param preferences
	 */
	public OsmBridgeClient(SharedPreferences preferences) {
		String sidPref = preferences.getString("osmbridge_sid", "");
		if (sidPref != null && !sidPref.isEmpty()) {
			this.sid = sidPref;
			sidSaveTime = preferences.getLong("osmbridge_sid_saved", 0L);
		}
	}

	public boolean hasSessionId() {
		return this.sid != null && !this.sid.isEmpty();
	}

	/**
	 * Saves the session id to the app's preferences
	 *
	 * @param preferences
	 */
	public void storeSessionId(SharedPreferences preferences) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("osmbridge_sid", this.sid);
		editor.putLong("osmbridge_sid_saved", this.sidSaveTime = System.currentTimeMillis());
		editor.apply();

		Log.i("OsmBridgeClient", "session stored to preferences");
	}

	public void invalidateSession(SharedPreferences preferences) {
		this.sid = null;
		this.sidSaveTime = 0;

		SharedPreferences.Editor editor = preferences.edit();
		editor.putString("osmbridge_sid", "");
		editor.putLong("osmbridge_sid_saved", 0L);
		editor.apply();

		Log.i("OsmBridgeClient", "session invalidated");
	}

	/**
	 * Calls the API to check if the session id is still valid
	 * Should be called from an {@link android.os.AsyncTask}
	 *
	 * @return validity
	 */
	public boolean isSessionIdValid() {
		if (!hasSessionId()) {
			Log.i("OsmBridgeClient", "isSessionIdValid: no session id");
			return false;
		}
		if (System.currentTimeMillis() - sidSaveTime > 3.154e+10/* 1 year */) {
			// invalidate after 1 year
			Log.i("OsmBridgeClient", "isSessionIdValid: session id expired");
			return false;
		}

		try {
			JsonObject response = request("GET", "/", null);
			System.out.println(response);
			if (response.has("user")) {
				JsonElement userElement = response.get("user");
				if (userElement != null && !userElement.isJsonNull()) {
					userInfoJson = response.getAsJsonObject("user");
				}
			}
			return response != null && response.has("authenticated") && response.get("authenticated").getAsBoolean();
		} catch (IOException e) {
			Log.e("OsmBridgeClient", "Failed to check API for authentication", e);
		}
		return false;
	}

	/**
	 * @return The auth url to be visited through a browser by the user
	 */
	public String getAuthUrl() {
		return BRIDGE_URL + "/auth";
	}

	/**
	 * Should only be called after making sure the session is valid
	 *
	 * @return The osm username
	 */
	public String getOsmUsername() {
		if (userInfoJson == null) {
			return null;
		}
		return userInfoJson.getAsJsonObject("osm").getAsJsonArray("user").get(0).getAsJsonObject().getAsJsonObject("$").get("display_name").getAsString();
	}

	/**
	 * Launches a {@link WebViewActivity} to start the authentication flow with OSM
	 *
	 * @param activity
	 */
	public void launchAuthWebView(Activity activity) {
		Intent intent = new Intent(activity, WebViewActivity.class);
		intent.putExtra("url", getAuthUrl());
		intent.putExtra("title", activity.getString(R.string.osm_auth_title));

		//		expireSessionCookie(activity);

		//		CookieManager cookieManager = CookieManager.getInstance();
		if (hasSessionId()) {
			intent.putExtra("sid", sid);

			//			cookieManager.setCookie(BRIDGE_URL, "connect.sid=" + this.sid + "; Domain=osmbridge.trashapp.cc; Path=/; Secure; HttpOnly");
			//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			//				cookieManager.flush();
			//			}
		}
		activity.startActivityForResult(intent, AUTH_REQUEST_CODE);
	}

	/**
	 * Method to be called when receiving an activity result from the WebViewActivity
	 */
	public boolean notifyAuthFinished(int resultCode) {
		if (resultCode != Activity.RESULT_OK) {
			Log.w("OsmBridgeClient", "Got non-ok resultCode");
			return false;
		}

		// Get session cookie
		CookieManager cookieManager = CookieManager.getInstance();
		String cookieString = cookieManager.getCookie(BRIDGE_URL);
		Log.i("OsmBridgeClient", "cookie: " + cookieString);

		// This only seems to return one cookie, for whatever reason :shrug:
		//		List<HttpCookie> cookies = HttpCookie.parse(cookieString);
		//		System.out.println(cookies);

		//		HttpCookie sessionCookie = null;
		//		for (HttpCookie cookie : cookies) {
		//			if ("connect.sid".equals(cookie.getName())) {
		//				sessionCookie = cookie;
		//				break;
		//			}
		//		}
		//		if (sessionCookie == null) {
		//			Log.w("OsmBridgeClient", "Missing session id cookie");
		//			return false;
		//		}
		//		String sid = sessionCookie.getValue();

		String sid = extractSid(cookieString);

		//cookieManager.removeSessionCookie();// Remove session cookies (hopefully this fixes duplicate sid cookies)

		if (sid == null) {
			Log.w("OsmBridgeClient", "Missing session id cookie");
			return false;
		}
		if (sid.isEmpty()) {
			Log.w("OsmBridgeClient", "Session cookie has empty value");
			return false;
		}
		this.sid = sid;
		Log.i("OsmBridgeClient", "Session id updated");
		return true;
	}

	/**
	 * Adds trashcans
	 * Should be called from an {@link android.os.AsyncTask}
	 *
	 * @param trashcans trashcans to add
	 * @param comment   Editor comment
	 * @return
	 */
	public boolean addTrashcans(String comment, Iterable<PendingTrashcan> trashcans) {
		JsonArray jsonArray = new JsonArray();
		for (PendingTrashcan trashcan : trashcans) {
			jsonArray.add(gson.toJsonTree(trashcan));
		}

		try {
			return addTrashcans(jsonArray, comment);
		} catch (IOException e) {
			Log.e("OsmBridgeClient", "Error while trying to add trashcans", e);
			return false;
		}
	}

	/**
	 * Adds trashcans
	 * Should be called from an {@link android.os.AsyncTask}
	 *
	 * @param trashcans trashcans to add
	 * @param comment   Editor comment
	 * @return
	 */
	public boolean addTrashcans(String comment, PendingTrashcan... trashcans) {
		JsonArray jsonArray = new JsonArray();
		for (PendingTrashcan trashcan : trashcans) {
			jsonArray.add(gson.toJsonTree(trashcan));
		}

		try {
			return addTrashcans(jsonArray, comment);
		} catch (IOException e) {
			Log.e("OsmBridgeClient", "Error while trying to add trashcans", e);
			return false;
		}
	}

	private boolean addTrashcans(JsonArray jsonArray, String comment) throws IOException {
		if (!hasSessionId()) {
			throw new IllegalStateException("Client has no session id");
		}

		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("comment", comment);
		jsonObject.add("trashcans", jsonArray);

		JsonObject response = request("POST", "/create", jsonObject);

		return response != null && response.has("msg") && "success".equals(response.get("msg").getAsString());
	}

	private JsonObject request(String method, String path, JsonObject data) throws IOException {
		Log.i("OsmBridgeClient", method + " " + path);

		HttpURLConnection connection = (HttpURLConnection) new URL(BRIDGE_URL + path).openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("User-Agent", "TrashApp/" + Util.APP_VERSION_NAME);
		connection.setRequestProperty("Referer", "https://trashapp.cc");
		if (hasSessionId()) {
			connection.setRequestProperty("Cookie", "connect.sid=" + sid);
		}
		connection.setConnectTimeout(2000);
		connection.setReadTimeout(10000);
		connection.setDoInput(true);
		connection.setDoOutput(data != null);

		if (data != null) {
			String dataString = data.toString();
			Log.i("OsmBridgeClient", dataString);
			connection.setRequestProperty("Content-Type", "application/json");
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
				writer.write(dataString);
			}
		}

		int responseCode = connection.getResponseCode();
		if (responseCode < 200 || responseCode > 240) {
			Log.e("OsmBridgeClient", "Got non 200 response code");
			Log.e("OsmBridgeClient", readLines(connection.getErrorStream()));
		}

		String rawResponse = readLines(connection.getInputStream());
		Log.i("OsmBridgeClient", rawResponse);

		return gson.fromJson(rawResponse, JsonObject.class);
	}

	public static String extractSid(String cookieString) {
		String[] split = cookieString.split(";");
		for (String str : split) {
			String[] split1 = str.trim().split("=");
			if (split1.length != 2) {
				throw new IllegalStateException("Cookie split is malformed (" + str + ")");
			}
			if ("connect.sid".equals(split1[0])) {
				return split1[1];
			}
		}
		return null;
	}

	void expireSessionCookie(Context context) {
		/// https://stackoverflow.com/questions/2834180/how-to-remove-cookies-using-cookiemanager-for-a-specific-domain/11621738#11621738

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.createInstance(context);
		}

		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setCookie(BRIDGE_URL, "connect.sid=" + this.sid + "; Domain=osmbridge.trashapp.cc; Path=/; Secure; HttpOnly; Expires=Expires=Wed, 31 Dec 2025 23:59:59 GMT");

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.flush();
		} else {
			CookieSyncManager.getInstance().sync();
		}
	}

	public static class PendingTrashcan {

		@SerializedName("lat")
		public double lat;
		@SerializedName("lon")
		public double lon;
		@SerializedName("amenity")
		public String amenity;

		public PendingTrashcan(double lat, double lon, String amenity) {
			this.lat = lat;
			this.lon = lon;
			this.amenity = amenity;
		}

		public PendingTrashcan(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
			this.amenity = "waste_basket";// default
		}
	}

}
