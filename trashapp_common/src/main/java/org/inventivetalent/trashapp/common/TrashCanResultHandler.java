package org.inventivetalent.trashapp.common;

import org.inventivetalent.trashapp.common.db.AppDatabase;

import java.io.File;

public interface TrashCanResultHandler {

	void handleTrashCanLocations(OverpassResponse response, boolean isCached);

	boolean shouldCacheResults();

	File getCacheFile();

	AppDatabase getDatabase();

}
