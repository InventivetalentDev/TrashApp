package org.inventivetalent.trashapp.common;

import java.io.File;

public interface TrashCanResultHandler {

	void handleTrashCanLocations(OverpassResponse response, boolean isCached);

	boolean shouldCacheResults();

	File getCacheFile();

}
