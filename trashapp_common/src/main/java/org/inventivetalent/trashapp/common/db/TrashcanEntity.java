package org.inventivetalent.trashapp.common.db;

import android.location.Location;
import androidx.room.*;
import org.inventivetalent.trashapp.common.LatLon;
import org.inventivetalent.trashapp.common.TrashType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity(tableName = "trashcans")
@TypeConverters({ Converters.class })
public class TrashcanEntity implements LatLon, TrashType {

	@PrimaryKey
	public long id;

	@ColumnInfo(name = "lat")
	public double lat;

	@ColumnInfo(name = "lon")
	public double lon;

	@ColumnInfo(name = "types")
	public List<String> types = new ArrayList<>();

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

	@Override
	public List<String> getTypes() {
		if (types != null && !types.isEmpty()) {
			return types;
		}
		return Collections.singletonList("general");
	}

	public Location toLocation() {
		if (location == null) {
			location = new Location(String.valueOf(id));
			location.setLatitude(lat);
			location.setLongitude(lon);
		}
		return location;
	}

	@Override
	public String toString() {
		return "TrashcanEntity{" +
				"id=" + id +
				", lat=" + lat +
				", lon=" + lon +
				", types=" + types +
				", location=" + location +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		TrashcanEntity that = (TrashcanEntity) o;
		return id == that.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
