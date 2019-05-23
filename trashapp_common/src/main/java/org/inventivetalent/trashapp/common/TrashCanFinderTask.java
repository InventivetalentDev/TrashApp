package org.inventivetalent.trashapp.common;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;

import java.io.*;

public class TrashCanFinderTask extends AsyncTask<OverpassBoundingBox, Void, OverpassResponse> {

	private static OverpassAPI overpassAPI = new OverpassAPI();

	private OverpassQuery         query;
	private TrashCanResultHandler handler;

	public TrashCanFinderTask(Activity activity, TrashCanResultHandler handler) {
		this.query = new OverpassQuery(activity, R.raw.waste_basket_query);
		this.handler = handler;
	}

	@Override
	protected void onPreExecute() {
		File cacheFile = handler.getCacheFile();
		if (cacheFile.exists()) {
			try (Reader reader = new FileReader(cacheFile)) {
				OverpassResponse response = new Gson().fromJson(reader, OverpassResponse.class);
				handler.handleTrashCanLocations(response, true);
			} catch (IOException e) {
				Log.w("TrashCanFinderTask", "failed to read response from cache file", e);
			}
		}
	}

	@Override
	protected OverpassResponse doInBackground(OverpassBoundingBox... overpassBoundingBoxes) {
		if (overpassBoundingBoxes.length > 0) {
			try {
				return overpassAPI.query(this.query, overpassBoundingBoxes[0]);
			} catch (IOException e) {
				Log.e("TrashCanFinderTask", "Overpass Query failed", e);
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(OverpassResponse overpassResponse) {
		if (overpassResponse != null) {
			handler.handleTrashCanLocations(overpassResponse, false);

			if (handler.shouldCacheResults()) {
				File cacheFile = handler.getCacheFile();
				try (Writer writer = new FileWriter(cacheFile)) {
					new Gson().toJson(overpassResponse, writer);
				} catch (IOException e) {
					Log.w("TrashCanFinderTask", "failed to write response to cache file", e);
				}
			}
		}
	}
}
