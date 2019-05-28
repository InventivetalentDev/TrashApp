package org.inventivetalent.trashapp.common.db;

import android.location.Location;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
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

	@Ignore
	private Location location;

	@Override
	public double getLat() {
		return lat;
	}

	@Override
	public double getLon() {
		return lon;
	}


	public Location toLocation() {
		if (location == null) {
			location = new Location(String.valueOf(id));
			location.setLatitude(lat);
			location.setLongitude(lon);
		}
		return location;
	}
}
