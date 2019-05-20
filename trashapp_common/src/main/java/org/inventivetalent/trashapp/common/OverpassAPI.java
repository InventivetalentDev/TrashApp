package org.inventivetalent.trashapp.common;

import android.util.Log;
import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class OverpassAPI {

	private static final String OVERPASS_URL = "http://www.overpass-api.de/api/interpreter";

	private Gson gson = new Gson();

	public OverpassAPI() {
	}

	public OverpassResponse query(OverpassQuery query, OverpassBoundingBox boundingBox) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL(OVERPASS_URL).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setRequestProperty("User-Agent", "TrashApp");
		connection.setRequestProperty("Referer", "https://trashapp.inventivetalent.org");
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
		}

		String rawResponse = readLines(new GZIPInputStream(connection.getInputStream()));
		Log.i("OverpassAPI", rawResponse);

		return gson.fromJson(rawResponse, OverpassResponse.class);
	}

	static String readLines(InputStream inputStream) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		return  builder.toString();
	}

}
