package org.inventivetalent.trashapp.common;

import android.location.Location;
import android.util.Log;
import android.util.LongSparseArray;
import com.google.gson.annotations.SerializedName;

import java.util.*;

public class OverpassResponse {

	@SerializedName("version")
	public double        version;
	@SerializedName("generator")
	public String        generator;
	@SerializedName("elements")
	public List<Element> elements = new ArrayList<>();

	@Deprecated
	public List<Element> elementsSortedByDistanceFrom(final double lat, final double lon) {
		List<Element> sorted = new ArrayList<>(this.elements);
		Collections.sort(sorted, new Comparator<Element>() {
			@Override
			public int compare(Element o1, Element o2) {
				double d1 = (((lon - o1.lon) * (lon - o1.lon)) + ((lat - o1.lat) * (lat - o1.lat)));
				double d2 = (((lon - o2.lon) * (lon - o2.lon)) + ((lat - o2.lat) * (lat - o2.lat)));

				return (int) (d2 - d1);
			}
		});
		return sorted;
	}

	public static List<? extends LatLon> elementsSortedByDistanceFrom(Collection<? extends LatLon> elements, final Location location) {
		List<? extends LatLon> sorted = new ArrayList<>(elements);
		Collections.sort(sorted, new Comparator<LatLon>() {
			@Override
			public int compare(LatLon o1, LatLon o2) {
				Float d1 = location.distanceTo(o1.toLocation());
				Float d2 = location.distanceTo(o2.toLocation());

				return d1.compareTo(d2);
			}
		});
		return sorted;
	}

	public static List<Element> convertElementsToPoints(Collection<Element> elements) {
		List<Element> pointElements = new ArrayList<>();

		LongSparseArray<Element> referencedNodes = new LongSparseArray<>();

		for (Element element : elements) {// filter nodes
			if ("node".equals(element.type)) {
				if (element.tags.isEmpty()) {// referenced element
					referencedNodes.put(element.id, element);
				} else {// standalone element
					pointElements.add(element);
				}
			} else if ("way".equals(element.type)) {
				// handle below
			} else {
				Log.w("OverpassResponse", "Unhandled element type: " + element.type);
			}
		}

		for (Element element : elements) {// handle ways
			if ("way".equals(element.type)) {
				List<Element> wayNodes = new ArrayList<>();
				for (long l : element.nodes) {
					Element node = referencedNodes.get(l);
					if (node == null) {
						Log.w("OverpassResponse", "Way references node #" + l + " but it's not in the response");
						continue;
					}
					wayNodes.add(node);
				}
				double[] wayCenter = Util.findPolygonCenter(wayNodes);

				element.lat = wayCenter[0];
				element.lon = wayCenter[1];

				pointElements.add(element);
			}
		}

		return pointElements;
	}

	@Override
	public String toString() {
		return "OverpassResponse{" +
				"version=" + version +
				", generator='" + generator + '\'' +
				", elements=" + elements +
				'}';
	}

	public static class Element implements LatLon, TrashType {

		@SerializedName("type")
		public String type;
		@SerializedName("id")
		public long   id;
		@SerializedName("lat")
		public double lat;
		@SerializedName("lon")
		public double lon;
		@SerializedName("nodes")
		List<Long>          nodes = new ArrayList<>();
		@SerializedName("tags")
		Map<String, String> tags  = new HashMap<>();

		private Location location;

		@Override
		public List<String> getTypes() {
			if (tags.containsKey("amenity")) {
				if ("waste_basket".equals(tags.get("amenity"))) {
					if (tags.containsKey("waste")) {
						return Collections.singletonList(tags.get("waste"));
					}
					return Collections.singletonList("general");
				}
				if("waste_disposal".equals(tags.get("amenity"))) {
					if (tags.containsKey("waste")) {
						return Collections.singletonList(tags.get("waste"));
					}
					return Collections.singletonList("waste_disposal");
				}
				if ("recycling".equals(tags.get("amenity"))) {
					List<String> types = new ArrayList<>();
					for (Map.Entry<String, String> entry : tags.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						if (key.startsWith("recycling:") && "yes".equals(value)) {
							String type = key.substring("recycling:".length());
							types.add(type);
						}
					}
					return types;
				}
			}
			if (tags.containsKey("bin") && "yes".equals(tags.get("bin"))) {
				return Collections.singletonList("bin");
			}

			return Collections.singletonList("general");
		}

		@Override
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
			return "Element{" +
					"type='" + type + '\'' +
					", id=" + id +
					", lat=" + lat +
					", lon=" + lon +
					'}';
		}

		@Override
		public double getLat() {
			return lat;
		}

		@Override
		public double getLon() {
			return lon;
		}
	}

}
