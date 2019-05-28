package org.inventivetalent.trashapp.common;

import java.util.Locale;

public class OverpassBoundingBox {

	public final double south, west, north, east;

	// south = min latitude
	// west = min longitude
	// north = max latitude
	// east = max longitude
	public OverpassBoundingBox(double south, double west, double north, double east) {
		this.south = south;
		this.west = west;
		this.north = north;
		this.east = east;
	}

	public String toCoordString() {
		return String.format(Locale.ENGLISH, "%.6f,%.6f,%.6f,%.6f", this.south, this.west, this.north, this.east);
	}

}
