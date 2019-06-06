package org.inventivetalent.trashapp.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TrashcanQuery {

	public OverpassBoundingBox boundingBox;
	public List<String> types = new ArrayList<>();

	public TrashcanQuery(OverpassBoundingBox boundingBox, List<String> types) {
		this.boundingBox = boundingBox;
		this.types = types;
	}

	public TrashcanQuery(OverpassBoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	public TrashcanQuery withTypes(List<String> types) {
		this.types.addAll(types);
		return this;
	}

	public TrashcanQuery withTypes(String... types) {
		this.types.addAll(Arrays.asList(types));
		return this;
	}

}
