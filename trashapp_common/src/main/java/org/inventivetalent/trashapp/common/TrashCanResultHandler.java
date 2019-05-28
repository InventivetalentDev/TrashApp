package org.inventivetalent.trashapp.common;

import org.inventivetalent.trashapp.common.db.AppDatabase;

import java.io.File;
import java.util.List;

public interface TrashCanResultHandler {

	void handleTrashCanLocations(List<? extends LatLon> locations, boolean isCached);

	boolean shouldCacheResults();

	File getCacheFile();

	AppDatabase getDatabase();

}
