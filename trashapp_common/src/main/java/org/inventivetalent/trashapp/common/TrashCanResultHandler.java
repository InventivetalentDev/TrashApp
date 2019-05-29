package org.inventivetalent.trashapp.common;

import org.inventivetalent.trashapp.common.db.AppDatabase;

import java.util.List;

public interface TrashCanResultHandler {

	void handleTrashCanLocations(List<? extends LatLon> locations, boolean isCached);

	AppDatabase getDatabase();

}
