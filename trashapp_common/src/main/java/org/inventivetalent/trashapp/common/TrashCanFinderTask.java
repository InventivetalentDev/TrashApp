package org.inventivetalent.trashapp.common;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class TrashCanFinderTask extends AbstractTrashcanTask<OverpassResponse.Element> {

	private static OverpassAPI overpassAPI = new OverpassAPI();

	private OverpassQuery         query;
	private TrashCanResultHandler handler;

	public TrashCanFinderTask(Context activity, TrashCanResultHandler handler) {
		super(handler);
		this.query = new OverpassQuery(activity, R.raw.waste_basket_query);
	}

//	@Override
//	protected void onPreExecute() {
////		File cacheFile = handler.getCacheFile();
////		if (cacheFile!=null&&cacheFile.exists()) {
////			try (Reader reader = new FileReader(cacheFile)) {
////				OverpassResponse response = new Gson().fromJson(reader, OverpassResponse.class);
////				handler.handleTrashCanLocations(response, true);
////			} catch (IOException e) {
////				Log.w("TrashCanFinderTask", "failed to read response from cache file", e);
////			}
////		}
//	}

	@Override
	boolean isCaching() {
		return false;
	}

	@Override
	protected List<OverpassResponse.Element> sanitize(List<OverpassResponse.Element> elements) {
		return OverpassResponse.convertElementsToPoints(elements);
	}

	@Override
	protected List<OverpassResponse.Element> doInBackground(TrashcanQuery... queries) {
		if (queries.length > 0) {
			try {
				OverpassResponse response =  overpassAPI.query(this.query, queries[0].boundingBox);
				if (response != null) {
					return response.elements;
				}
			} catch (IOException e) {
				Log.e("TrashCanFinderTask", "Overpass Query failed", e);
				e.printStackTrace();
			}
		}
		return null;
	}



//	@Override
//	protected void onPostExecute(Collection<OverpassResponse.Element> elements) {
//		if (elements != null) {
//			handler.handleTrashCanLocations(overpassResponse, false);
//
//			if (handler.shouldCacheResults()) {
//				File cacheFile = handler.getCacheFile();
//				if(cacheFile!=null) {
//					try (Writer writer = new FileWriter(cacheFile)) {
//						new Gson().toJson(overpassResponse, writer);
//					} catch (IOException e) {
//						Log.w("TrashCanFinderTask", "failed to write response to cache file", e);
//					}
//				}
//
//				AppDatabase appDatabase = handler.getDatabase();
//				if (appDatabase != null) {
//					//TODO
//				}
//			}
//		}
//	}
}
