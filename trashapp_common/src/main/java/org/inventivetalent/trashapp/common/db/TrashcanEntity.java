package org.inventivetalent.trashapp.common.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.inventivetalent.trashapp.common.LatLon;

@Entity(tableName = "trashcans")
public class TrashcanEntity implements LatLon {

	@PrimaryKey
	public long id;

	@ColumnInfo(name = "lat")
	public double lat;

	@ColumnInfo(name = "lon")
	public double lon;

	@Override
	public double getLat() {
		return lat;
	}

	@Override
	public double getLon() {
		return lon;
	}
}
