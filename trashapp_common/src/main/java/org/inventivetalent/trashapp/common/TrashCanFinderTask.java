package org.inventivetalent.trashapp.common;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public  class TrashCanFinderTask extends AsyncTask<OverpassBoundingBox, Void, OverpassResponse> {

	private static OverpassAPI overpassAPI = new OverpassAPI();

		private OverpassQuery query;
		private TrashCanResultHandler handler;

		public   TrashCanFinderTask(Activity activity,TrashCanResultHandler handler) {
			this.query = new OverpassQuery(activity, R.raw.waste_basket_query);
			this.handler = handler;
		}

		@Override
		protected OverpassResponse doInBackground(OverpassBoundingBox... overpassBoundingBoxes) {
			if (overpassBoundingBoxes.length > 0) {
				try {
					return overpassAPI.query(this.query, overpassBoundingBoxes[0]);
				} catch (IOException e) {
					Log.e("TrashCanFinderTask", "Overpass Query failed", e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(OverpassResponse overpassResponse) {
			if (overpassResponse != null) {
				handler.handleTrashCanLocations(overpassResponse);
			}
		}
	}
