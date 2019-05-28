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
	protected List<TrashcanEntity> doInBackground(OverpassBoundingBox... overpassBoundingBoxes) {
		if (overpassBoundingBoxes.length > 0) {
			AppDatabase database = handler.getDatabase();
			if (database != null) {
				OverpassBoundingBox box = overpassBoundingBoxes[0];
				return database.trashcanDao().getAllInArea(box.south, box.north, box.west, box.east);
			}
		}
		return null;
	}

}
