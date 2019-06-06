package org.inventivetalent.trashapp.common;

import android.location.Location;

public interface LatLon {

	double getLat();

	double getLon();

	Location toLocation();

}
