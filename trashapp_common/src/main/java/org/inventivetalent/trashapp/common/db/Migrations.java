package org.inventivetalent.trashapp.common.db;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations {

	public static final Migration MIGRATION_1_2 = new Migration(1, 2) {

		@Override
		public void migrate(@NonNull SupportSQLiteDatabase database) {
			Log.i("Migrations", "Migrating trashcans table from " + startVersion + "->" + endVersion);

			database.execSQL("ALTER TABLE trashcans ADD COLUMN types VARCHAR");// create new types column
			database.execSQL("UPDATE trashcans SET types = 'general' WHERE types IS NULL OR types = ''");// fill empty types fields with 'general'
		}
	};
}
