package org.inventivetalent.trashapp.common.db;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

	public static final Migration MIGRATION_1_2 = new Migration(1, 2) {

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			database.execSQL("ALTER TABLE trashcans ADD COLUMN types VARCHAR");
		}
	};
}
