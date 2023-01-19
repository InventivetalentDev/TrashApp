package org.inventivetalent.trashapp.common;

import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public abstract class AbstractTrashcanTask<T extends LatLon> extends AsyncTask<TrashcanQuery, Void, List<T>> {

	protected TrashCanResultHandler handler;

	public AbstractTrashcanTask(TrashCanResultHandler handler) {
		this.handler = handler;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	abstract boolean isCaching();

	protected List<T> sanitize(List<T> ts) {
		return ts;
	}

	@Override
	protected void onPostExecute(List<T> ts) {
		super.onPostExecute(ts);

		Log.d(getClass().getName(), "onPostExecute");
		Log.d(getClass().getName(), "" + ts);

		if (ts != null) {
			ts = sanitize(ts);
			Log.i(getClass().getName(), ts.size() + " elements remaining after #sanitize");
			handler.handleTrashCanLocations(ts, isCaching());
		}
	}

}
