package org.inventivetalent.trashapp.common;

import static org.inventivetalent.trashapp.common.Util.readLines;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class OverpassAPI {

	private static final String OVERPASS_URL   = "https://www.overpass-api.de/api/interpreter";
	private static final String OSM_BRIDGE_URL = "https://osmbridge.trashapp.cc/interpreter";

	private Gson gson = new Gson();

	public OverpassAPI() {
	}

	public OverpassResponse query(OverpassQuery query, OverpassBoundingBox boundingBox) throws IOException {
		Log.i("OverpassAPI", "sending request to " + OSM_BRIDGE_URL);

		HttpURLConnection connection = (HttpURLConnection) new URL(OSM_BRIDGE_URL).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setRequestProperty("User-Agent", "TrashApp/" + Util.APP_VERSION_NAME);
		connection.setRequestProperty("Referer", "https://trashapp.cc");
		connection.setConnectTimeout(2000);
		connection.setReadTimeout(30000);
		connection.setDoInput(true);
		connection.setDoOutput(true);

		String rawQuery = query.formatBoundingBox(boundingBox);
		Log.i("OverpassAPI", rawQuery);
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
			writer.write(rawQuery);
		}

		int responseCode = connection.getResponseCode();
		if (responseCode < 200 || responseCode > 240) {
			Log.e("OverpassAPI", "Got non 200 response code");
			Log.e("OverpassAPI", readLines(new GZIPInputStream(connection.getErrorStream())));
			if (query.getContext() instanceof Activity) {
				((Activity) query.getContext())
						.runOnUiThread(() -> Toast
								.makeText(query.getContext(), "OpenStreetMap request failed - please try again later (E" + responseCode + ")", Toast.LENGTH_LONG).show());
			}
		}

		String rawResponse = readLines(new GZIPInputStream(connection.getInputStream()));
		Log.i("OverpassAPI", rawResponse);

		OverpassResponse response = gson.fromJson(rawResponse, OverpassResponse.class);
		Log.i("OverpassAPI", "" + response);
		return response;
	}

}
