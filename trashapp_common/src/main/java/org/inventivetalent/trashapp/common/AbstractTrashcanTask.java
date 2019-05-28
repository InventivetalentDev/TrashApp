package org.inventivetalent.trashapp.common;

import android.os.AsyncTask;

import java.util.List;

public abstract class AbstractTrashcanTask<T extends LatLon> extends AsyncTask<OverpassBoundingBox, Void, List<T>> {

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

		if (ts != null) {
			ts = sanitize(ts);
			handler.handleTrashCanLocations(ts, isCaching());
		}
	}

}
