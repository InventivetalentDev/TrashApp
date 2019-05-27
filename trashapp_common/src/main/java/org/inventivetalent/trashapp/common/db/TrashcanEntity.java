package org.inventivetalent.trashapp.common.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trashcans")
public class TrashcanEntity {

	@PrimaryKey
	public long id;

	@ColumnInfo(name = "lat")
	public double lat;

	@ColumnInfo(name = "lon")
	public double lon;

}
