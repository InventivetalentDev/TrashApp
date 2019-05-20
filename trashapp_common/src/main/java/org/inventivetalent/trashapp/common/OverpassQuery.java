package org.inventivetalent.trashapp.common;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class OverpassQuery {

	protected final String queryFormat;

	public OverpassQuery(Activity activity, int resourceId) {
		queryFormat = loadRawResText(activity, resourceId);
	}

	public String formatBoundingBox(OverpassBoundingBox boundingBox) {
		return this.queryFormat.replaceAll(Pattern.quote("{{bbox}}"), boundingBox.toCoordString());
	}

	private static String loadRawResText(Activity activity, int resourceId) {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(activity.getResources().openRawResource(resourceId)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} catch (IOException e) {
			Log.e("OverpassQuery", "Failed to load query from res ID " + resourceId, e);
		}
		return builder.toString();
	}

}
