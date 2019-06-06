package org.inventivetalent.trashapp.common.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { TrashcanEntity.class }, version = 2)
public abstract class AppDatabase extends RoomDatabase {
	public abstract TrashcanDao trashcanDao();
}
