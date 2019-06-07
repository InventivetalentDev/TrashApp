package org.inventivetalent.trashapp.common.db;

import androidx.room.TypeConverter;

import java.util.Arrays;
import java.util.List;

public class Converters {

	@TypeConverter
	public static List<String> fromString(String value) {
		if (value == null) { return null; }
		return Arrays.asList(value.split(","));
	}

	@TypeConverter
	public static String fromList(List<String> list) {
		if (list == null) { return null; }
		StringBuilder stringBuilder = new StringBuilder();
		boolean first = true;
		for (String string : list) {
			if (!first) {
				stringBuilder.append(',');
			}
			stringBuilder.append(string);

			first = false;
		}
		return stringBuilder.toString();
	}

}
