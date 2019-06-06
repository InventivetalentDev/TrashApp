package org.inventivetalent.trashapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.sqlite.db.SimpleSQLiteQuery;
import org.inventivetalent.trashapp.common.db.AppDatabase;
import org.inventivetalent.trashapp.common.db.TrashcanDao;
import org.inventivetalent.trashapp.common.db.TrashcanEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {

	public static double normalizeDegree(double value) {
		if (value >= 0.0f && value <= 180.0f) {
			return value;
		} else {
			return 180 + (180 + value);
		}
	}

	public static double distance(double lat1, double lon1, double lat2, double lon2) {
		return Math.sqrt(Math.abs(((lat2 - lat1) * (lat2 - lat1)) - ((lon2 - lon1) * (lon2 - lon1))));
	}

	public static double angleTo(double selfLat, double selfLon, double otherLat, double otherLon) {
		return Math.atan2(otherLat - selfLat, otherLon - selfLon);
	}

	public static double radToDeg(double rad) {
		return rad * 180 / Math.PI;
	}

	// https://stackoverflow.com/questions/33696488/getting-bitmap-from-vector-drawable
	public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
		Drawable drawable = ContextCompat.getDrawable(context, drawableId);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			drawable = (DrawableCompat.wrap(drawable)).mutate();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	public static double[] findPolygonCenter(double[] xA, double[] zA) {
		if (xA.length != zA.length) {
			throw new IllegalArgumentException("x and z array must be of the same length");
		}

		double xAvg = 0;
		for (int i = 0; i < xA.length; i++) {
			xAvg += xA[i];
		}
		xAvg /= xA.length;

		double zAvg = 0;
		for (int i = 0; i < zA.length; i++) {
			zAvg += zA[i];
		}
		zAvg /= zA.length;

		return new double[] {
				xAvg,
				zAvg };
	}

	public static double[] findPolygonCenter(List<OverpassResponse.Element> elements) {
		double lonAvg = 0;
		double latAvg = 0;
		for (OverpassResponse.Element element : elements) {
			latAvg += element.lat;
			lonAvg += element.lon;
		}

		latAvg /= elements.size();
		lonAvg /= elements.size();

		return new double[] {
				latAvg,
				lonAvg };
	}

	public static int getInt(SharedPreferences preferences, String key, int def) {
		int i = def;
		try {
			i = preferences.getInt(key, def);
		} catch (ClassCastException ignored) {
			try {
				i = Integer.parseInt(preferences.getString(key, String.valueOf(def)));
			} catch (ClassCastException ignored1) {
			}
		}
		return i;
	}

	public static float getFloat(SharedPreferences preferences, String key, float def) {
		float f = def;
		try {
			f = preferences.getFloat(key, def);
		} catch (ClassCastException ignored) {
			try {
				f = Float.parseFloat(preferences.getString(key, String.valueOf(def)));
			} catch (ClassCastException ignored1) {
			}
		}
		return f;
	}

	public static boolean getBoolean(SharedPreferences preferences, String key, boolean def) {
		boolean b = def;
		try {
			b = preferences.getBoolean(key, def);
		} catch (ClassCastException ignored) {
			try {
				b = Boolean.parseBoolean(preferences.getString(key, String.valueOf(def)));
			} catch (ClassCastException ignored1) {
			}
		}
		return b;
	}

	@ColorInt
	public static int getAttrColor(Context context, int attr) {
		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = context.getTheme();
		theme.resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	public static void applyTheme(Context context) {
		applyTheme(context, PreferenceManager.getDefaultSharedPreferences(context));
	}

	public static void applyTheme(Context context, SharedPreferences preferences) {
		String themeKey = preferences.getString("app_theme", "");
		if (themeKey == null) { themeKey = ""; }
		int rId;
		switch (themeKey) {
			//TODO
			case "classic":
				rId = R.style.AppTheme;
				break;
			case "dark_grass":
				rId = R.style.DarkGrassTheme;
				break;
			case "grass":
			default:
				rId = R.style.GrassTheme;
				break;
		}
		context.setTheme(rId);
	}

	public static void insertTrashcanResult(final AppDatabase database, List<? extends LatLon> sanitizedElements) {
		final TrashcanEntity[] entities = new TrashcanEntity[sanitizedElements.size()];
		for (int i = 0; i < sanitizedElements.size(); i++) {
			LatLon element = sanitizedElements.get(i);
			TrashcanEntity entity = new TrashcanEntity();
			if (element instanceof TrashcanEntity) { entity.id = ((TrashcanEntity) element).id; }
			if (element instanceof OverpassResponse.Element) { entity.id = ((OverpassResponse.Element) element).id; }

			entity.lat = element.getLat();
			entity.lon = element.getLon();

			if (element instanceof TrashType) { entity.types = ((TrashType) element).getTypes(); }

			entities[i] = entity;
		}
		AsyncTask.execute(new Runnable() {
			@Override
			public void run() {
				database.trashcanDao().insertAll(entities);
			}
		});
	}

	public static List<TrashcanEntity> getAllTrashcansOfTypesInArea(TrashcanDao dao,Collection<String> types, double minLat, double maxLat, double minLon, double maxLon) {
		if (types == null || types.isEmpty()) {
			// fallback to default method if we don't need to check for types
			return dao.getAllInArea(minLat, maxLat, minLon, maxLon);
		}


		StringBuilder stringBuilder = new StringBuilder();
		List<Object> args = new ArrayList<>();

		// base query, same as TrashcanDao#getAllInArea
		stringBuilder.append("SELECT * FROM trashcans WHERE (lat BETWEEN ? AND ? AND lon BETWEEN ? AND ?) AND (");
		args.add(minLat);
		args.add(maxLat);
		args.add(minLon);
		args.add(maxLon);

		boolean first = true;
		for (String type : types) {
			if (!first) {
				stringBuilder.append(" OR ");
			}
			stringBuilder.append("types LIKE ?");
			args.add(type);

			first = false;
		}

		stringBuilder.append(")");

		return dao.getAllOfTypesInArea(new SimpleSQLiteQuery(stringBuilder.toString(), args.toArray(new Object[0])));
	}

	public static List<String> createFilterFromPreferences(SharedPreferences preferences) {
		List<String> types = new ArrayList<>();

		if (preferences.getBoolean("filter_general", true)) {
			types.add("general");
		}
		if (preferences.getBoolean("filter_bins", true)) {
			types.add("bin");
		}

		//TODO: recycling stuff


		return types;
	}

	public static <T extends LatLon> List<T> filterResponse(List<T> response, List<String> types) {
		List<T> filtered = new ArrayList<>();

		for (T t : response) {
			if(t instanceof TrashType) {
				for (String s : types) {
					if (((TrashType) t).getTypes().contains(s)) {
						filtered.add(t);
					}
				}
			}
		}

		return filtered;
	}

//	public static <T extends TrashType> List<T> filterResponse(List<T> response, List<String> types) {
//		List<T> filtered = new ArrayList<>();
//
//		for (T t : response) {
//			for (String s : types) {
//				if (t.getTypes().contains(s)) {
//					filtered.add(t);
//				}
//			}
//		}
//
//		return filtered;
//	}

	public static void showDebugDBAddressLogToast(Context context) {
		if (BuildConfig.DEBUG) {
			try {
				Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
				Method getAddressLog = debugDB.getMethod("getAddressLog");
				Object value = getAddressLog.invoke(null);
				Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
			} catch (Exception ignore) {

			}
		}
	}

}
