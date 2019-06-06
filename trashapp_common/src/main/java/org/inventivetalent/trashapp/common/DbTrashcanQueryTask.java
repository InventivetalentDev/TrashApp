package org.inventivetalent.trashapp.common;

import org.inventivetalent.trashapp.common.db.AppDatabase;
import org.inventivetalent.trashapp.common.db.TrashcanEntity;

import java.util.List;

public class DbTrashcanQueryTask extends AbstractTrashcanTask<TrashcanEntity> {


	public DbTrashcanQueryTask(TrashCanResultHandler handler) {
		super(handler);
	}

	@Override
	boolean isCaching() {
		return true;
	}

	@Override
	protected List<TrashcanEntity> doInBackground(TrashcanQuery... queries) {
		if (queries.length > 0) {
			AppDatabase database = handler.getDatabase();
			if (database != null) {
				TrashcanQuery query = queries[0];
				OverpassBoundingBox box = query.boundingBox;
				return Util.getAllTrashcansOfTypesInArea(database.trashcanDao(), query.types, box.south, box.north, box.west, box.east);
			}
		}
		return null;
	}

}
