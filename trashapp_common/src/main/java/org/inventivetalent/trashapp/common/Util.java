package org.inventivetalent.trashapp.common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.inventivetalent.trashapp.common.db.AppDatabase;
import org.inventivetalent.trashapp.common.db.TrashcanDao;
import org.inventivetalent.trashapp.common.db.TrashcanEntity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Util {

	public static String APP_VERSION_NAME = "0.0.0";
	public static int    APP_VERSION_ID   = 0;

	public static String SPLIT_SEMICOLON_OR_COMMA = "[,;]";

	public static       String[] FILTER_WASTE     = new String[] {
			"oil",
			"drugs",
			"organic",
			"plastic",
			"rubble",
			"dog_excrement",
			"cigarettes"
	};
	/* Generated Array @ Sun Sep 15 18:19:55 CEST 2019 */
	public static final String[] FILTER_RECYCLING = new String[] {
			"aerosol_cans",
			"animal_waste",
			"aluminium",
			"bags",
			"batteries",
			"belts",
			"beverage_cartons",
			"bicycles",
			"books",
			"cans",
			"car_batteries",
			"cardboard",
			"cartons",
			"cds",
			"chipboard",
			"christmas_trees",
			"clothes",
			"coffee_capsules",
			"computers",
			"cooking_oil",
			"cork",
			"drugs",
			"electrical_items",
			"engine_oil",
			"fluorescent_tubes",
			"foil",
			"furniture",
			"gas_bottles",
			"glass",
			"glass_bottles",
			"green_waste",
			"garden_waste",
			"hazardous_waste",
			"hardcore",
			"low_energy_bulbs",
			"magazines",
			"metal",
			"mobile_phones",
			"newspaper",
			"organic",
			"paint",
			"pallets",
			"paper",
			"paper_packaging",
			"pens",
			"PET",
			"plasterboard",
			"plastic",
			"plastic_bags",
			"plastic_bottles",
			"plastic_packaging",
			"polyester",
			"polystyrene_foam",
			"printer_cartridges",
			"printer_toner_cartridges",
			"printer_inkjet_cartridges",
			"rubble",
			"scrap_metal",
			"sheet_metal",
			"small_appliances",
			"small_electrical_appliances",
			"styrofoam",
			"tyres",
			"tv_monitor",
			"waste",
			"white_goods",
			"wood"
	};
	/* /Generated Array */

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
			} catch (NumberFormatException e) {
				Log.w("TrashAppUtil", "Failed to parse int from preferences", e);
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
			} catch (NumberFormatException e) {
				Log.w("TrashAppUtil", "Failed to parse float from preferences", e);
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

		FirebaseAnalytics.getInstance(context).setUserProperty("app_theme", themeKey);
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

	public static List<TrashcanEntity> getAllTrashcansOfTypesInArea(TrashcanDao dao, Collection<String> types, double minLat, double maxLat, double minLon, double maxLon) {
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

		SimpleSQLiteQuery query = new SimpleSQLiteQuery(stringBuilder.toString(), args.toArray(new Object[0]));
		Log.i("Util", "Query: " + query.getSql());
		return dao.getAllOfTypesInArea(query);
	}

	public static List<String> createFilterFromPreferences(SharedPreferences preferences) {
		List<String> types = new ArrayList<>();

		// Misc
		if (preferences.getBoolean("filter_general", true)) {
			types.add("general");
		}
		if (preferences.getBoolean("filter_bins", true)) {
			types.add("bin");
		}
		if (preferences.getBoolean("filter_waste_disposal", true)) {
			types.add("waste_disposal");
		}

		// Waste (https://wiki.openstreetmap.org/wiki/Key:waste) 06.06.19
		if (preferences.getBoolean("filter_waste_trash", true)) {
			types.add("trash");
		}
		if (preferences.getBoolean("filter_waste_oil", true)) {
			types.add("oil");
		}
		if (preferences.getBoolean("filter_waste_drugs", true)) {
			types.add("drugs");
		}
		if (preferences.getBoolean("filter_waste_organic", true)) {
			types.add("organic");
		}
		if (preferences.getBoolean("filter_waste_plastic", true)) {
			types.add("plastic");
		}
		if (preferences.getBoolean("filter_waste_rubble", true)) {
			types.add("rubble");
		}
		if (preferences.getBoolean("filter_waste_dog_excrement", true)) {
			types.add("dog_excrement");
		}
		if (preferences.getBoolean("cigarettes", true)) {
			types.add("cigarettes");
		}

		/// Recycling
		/* Generated Code @ Thu Jun 06 18:52:57 CEST 2019 */
		if (preferences.getBoolean("filter_recycling_aerosol_cans", true)) {
			types.add("aerosol_cans");
		}
		if (preferences.getBoolean("filter_recycling_animal_waste", true)) {
			types.add("animal_waste");
		}
		if (preferences.getBoolean("filter_recycling_aluminium", true)) {
			types.add("aluminium");
		}
		if (preferences.getBoolean("filter_recycling_bags", true)) {
			types.add("bags");
		}
		if (preferences.getBoolean("filter_recycling_batteries", true)) {
			types.add("batteries");
		}
		if (preferences.getBoolean("filter_recycling_belts", true)) {
			types.add("belts");
		}
		if (preferences.getBoolean("filter_recycling_beverage_cartons", true)) {
			types.add("beverage_cartons");
		}
		if (preferences.getBoolean("filter_recycling_bicycles", true)) {
			types.add("bicycles");
		}
		if (preferences.getBoolean("filter_recycling_books", true)) {
			types.add("books");
		}
		if (preferences.getBoolean("filter_recycling_cans", true)) {
			types.add("cans");
		}
		if (preferences.getBoolean("filter_recycling_car_batteries", true)) {
			types.add("car_batteries");
		}
		if (preferences.getBoolean("filter_recycling_cardboard", true)) {
			types.add("cardboard");
		}
		if (preferences.getBoolean("filter_recycling_cartons", true)) {
			types.add("cartons");
		}
		if (preferences.getBoolean("filter_recycling_cds", true)) {
			types.add("cds");
		}
		if (preferences.getBoolean("filter_recycling_chipboard", true)) {
			types.add("chipboard");
		}
		if (preferences.getBoolean("filter_recycling_christmas_trees", true)) {
			types.add("christmas_trees");
		}
		if (preferences.getBoolean("filter_recycling_clothes", true)) {
			types.add("clothes");
		}
		if (preferences.getBoolean("filter_recycling_coffee_capsules", true)) {
			types.add("coffee_capsules");
		}
		if (preferences.getBoolean("filter_recycling_computers", true)) {
			types.add("computers");
		}
		if (preferences.getBoolean("filter_recycling_cooking_oil", true)) {
			types.add("cooking_oil");
		}
		if (preferences.getBoolean("filter_recycling_cork", true)) {
			types.add("cork");
		}
		if (preferences.getBoolean("filter_recycling_drugs", true)) {
			types.add("drugs");
		}
		if (preferences.getBoolean("filter_recycling_electrical_items", true)) {
			types.add("electrical_items");
		}
		if (preferences.getBoolean("filter_recycling_engine_oil", true)) {
			types.add("engine_oil");
		}
		if (preferences.getBoolean("filter_recycling_fluorescent_tubes", true)) {
			types.add("fluorescent_tubes");
		}
		if (preferences.getBoolean("filter_recycling_foil", true)) {
			types.add("foil");
		}
		if (preferences.getBoolean("filter_recycling_furniture", true)) {
			types.add("furniture");
		}
		if (preferences.getBoolean("filter_recycling_gas_bottles", true)) {
			types.add("gas_bottles");
		}
		if (preferences.getBoolean("filter_recycling_glass", true)) {
			types.add("glass");
		}
		if (preferences.getBoolean("filter_recycling_glass_bottles", true)) {
			types.add("glass_bottles");
		}
		if (preferences.getBoolean("filter_recycling_green_waste", true)) {
			types.add("green_waste");
		}
		if (preferences.getBoolean("filter_recycling_garden_waste", true)) {
			types.add("garden_waste");
		}
		if (preferences.getBoolean("filter_recycling_hazardous_waste", true)) {
			types.add("hazardous_waste");
		}
		if (preferences.getBoolean("filter_recycling_hardcore", true)) {
			types.add("hardcore");
		}
		if (preferences.getBoolean("filter_recycling_low_energy_bulbs", true)) {
			types.add("low_energy_bulbs");
		}
		if (preferences.getBoolean("filter_recycling_magazines", true)) {
			types.add("magazines");
		}
		if (preferences.getBoolean("filter_recycling_metal", true)) {
			types.add("metal");
		}
		if (preferences.getBoolean("filter_recycling_mobile_phones", true)) {
			types.add("mobile_phones");
		}
		if (preferences.getBoolean("filter_recycling_newspaper", true)) {
			types.add("newspaper");
		}
		if (preferences.getBoolean("filter_recycling_organic", true)) {
			types.add("organic");
		}
		if (preferences.getBoolean("filter_recycling_paint", true)) {
			types.add("paint");
		}
		if (preferences.getBoolean("filter_recycling_pallets", true)) {
			types.add("pallets");
		}
		if (preferences.getBoolean("filter_recycling_paper", true)) {
			types.add("paper");
		}
		if (preferences.getBoolean("filter_recycling_paper_packaging", true)) {
			types.add("paper_packaging");
		}
		if (preferences.getBoolean("filter_recycling_pens", true)) {
			types.add("pens");
		}
		if (preferences.getBoolean("filter_recycling_PET", true)) {
			types.add("PET");
		}
		if (preferences.getBoolean("filter_recycling_plasterboard", true)) {
			types.add("plasterboard");
		}
		if (preferences.getBoolean("filter_recycling_plastic", true)) {
			types.add("plastic");
		}
		if (preferences.getBoolean("filter_recycling_plastic_bags", true)) {
			types.add("plastic_bags");
		}
		if (preferences.getBoolean("filter_recycling_plastic_bottles", true)) {
			types.add("plastic_bottles");
		}
		if (preferences.getBoolean("filter_recycling_plastic_packaging", true)) {
			types.add("plastic_packaging");
		}
		if (preferences.getBoolean("filter_recycling_polyester", true)) {
			types.add("polyester");
		}
		if (preferences.getBoolean("filter_recycling_polystyrene_foam", true)) {
			types.add("polystyrene_foam");
		}
		if (preferences.getBoolean("filter_recycling_printer_cartridges", true)) {
			types.add("printer_cartridges");
		}
		if (preferences.getBoolean("filter_recycling_printer_toner_cartridges", true)) {
			types.add("printer_toner_cartridges");
		}
		if (preferences.getBoolean("filter_recycling_printer_inkjet_cartridges", true)) {
			types.add("printer_inkjet_cartridges");
		}
		if (preferences.getBoolean("filter_recycling_rubble", true)) {
			types.add("rubble");
		}
		if (preferences.getBoolean("filter_recycling_scrap_metal", true)) {
			types.add("scrap_metal");
		}
		if (preferences.getBoolean("filter_recycling_sheet_metal", true)) {
			types.add("sheet_metal");
		}
		if (preferences.getBoolean("filter_recycling_small_appliances", true)) {
			types.add("small_appliances");
		}
		if (preferences.getBoolean("filter_recycling_small_electrical_appliances", true)) {
			types.add("small_electrical_appliances");
		}
		if (preferences.getBoolean("filter_recycling_styrofoam", true)) {
			types.add("styrofoam");
		}
		if (preferences.getBoolean("filter_recycling_tyres", true)) {
			types.add("tyres");
		}
		if (preferences.getBoolean("filter_recycling_tv_monitor", true)) {
			types.add("tv_monitor");
		}
		if (preferences.getBoolean("filter_recycling_waste", true)) {
			types.add("waste");
		}
		if (preferences.getBoolean("filter_recycling_white_goods", true)) {
			types.add("white_goods");
		}
		if (preferences.getBoolean("filter_recycling_wood", true)) {
			types.add("wood");
		}
		/* /Generated Code */
		if (preferences.getBoolean("filter_recycling_shoes", true)) {
			types.add("shoes");
		}

		return types;
	}

	public static List<String> typeKeysToReadables(Context context, List<String> keys) {
		List<String> readables = new ArrayList<>();

		for (String key : keys) {
			String readable;
			if ("general".equals(key)) {
				readable = context.getString(R.string.settings_filter_general);
			} else if ("bin".equals(key)) {
				readable = context.getString(R.string.settings_filter_bins);
			} else if ("waste_disposal".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_disposal);

				// waste
			} else if ("trash".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_trash);
			} else if ("oil".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_oil);
			} else if ("drugs".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_drugs);
			} else if ("organic".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_organic);
			} else if ("plastic".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_plastic);
			} else if ("rubble".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_rubble);
			} else if ("dog_excrement".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_dog_excrement);
			} else if ("cigarettes".equals(key)) {
				readable = context.getString(R.string.settings_filter_waste_cigarettes);

				// recycling
			} else {
				readable = getStringFromKey(context, "settings_filter_recycling_" + key);
			}
			readables.add(readable);
		}
		return readables;
	}

	public static String getStringFromKey(Context context, String key) {
		@StringRes
		int id = getStringResFromKey(context, key);
		if (id == 0) {
			Exception exception = new Resources.NotFoundException("Resource ID for key " + key + " is 0!");
			Crashlytics.logException(exception);
			Log.w("Util", exception);
			return key;
		}
		return context.getString(id);
	}

	@StringRes
	public static int getStringResFromKey(Context context, String key) {
		return context.getResources().getIdentifier(key, "string", context.getPackageName());
	}

	public static boolean isMiscTrash(TrashType type) {
		return type.getTypes().contains("general") || type.getTypes().contains("bin") || type.getTypes().contains("waste_disposal") ||
				type.getTypes().contains("trash") || type.getTypes().contains("oil") || type.getTypes().contains("drugs") || type.getTypes().contains("organic") || type.getTypes().contains("plastic") || type.getTypes().contains("rubble") || type.getTypes().contains("dog_excrement") || type.getTypes().contains("cigarette");
	}

	//	public static boolean isRecycling(TrashType type) {
	//		return !type.getTypes().contains("general") && !type.getTypes().contains("bin");
	//	}

	public static <T extends LatLon> List<T> filterResponse(List<T> response, List<String> types) {
		List<T> filtered = new ArrayList<>();

		for (T t : response) {
			if (t instanceof TrashType) {
				for (String s : types) {
					if (((TrashType) t).getTypes().contains(s)) {
						filtered.add(t);
					}
				}
			}
		}

		return filtered;
	}


	public static int getStringArrayResLength(Context context, @ArrayRes int resId) {
		return context.getResources().getStringArray(resId).length;
	}

	public static String getArrayResString(Context context, @ArrayRes int resId, int index) {
		return context.getResources().getStringArray(resId)[index];
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

	public static String createPrefilledIssueUri(PackageInfo packageInfo) {
		StringBuilder bodyBuilder = new StringBuilder("++ Device Info ++").append('\n');
		bodyBuilder.append("Manufacturer: ").append(Build.MANUFACTURER).append('\n');
		bodyBuilder.append("Brand: ").append(Build.BRAND).append('\n');
		bodyBuilder.append("Device: ").append(Build.DEVICE).append('\n');
		bodyBuilder.append("Version API Level: ").append(Build.VERSION.SDK_INT).append('\n');
		bodyBuilder.append("Version Release: ").append(Build.VERSION.RELEASE).append('\n');
		bodyBuilder.append('\n');
		if (packageInfo != null) {
			bodyBuilder.append("App Version Name: ").append(packageInfo.versionName).append('\n');
			bodyBuilder.append("App Version Code: ").append(packageInfo.versionCode).append('\n');
		}
		bodyBuilder.append("App Build Type: ").append(org.inventivetalent.trashapp.common.BuildConfig.BUILD_TYPE).append('\n');
		bodyBuilder.append("++++++++++++++").append('\n');
		bodyBuilder.append('\n');
		bodyBuilder.append('\n');
		bodyBuilder.append("## What steps will reproduce the problem?").append('\n');
		bodyBuilder.append("1. \n");
		bodyBuilder.append("2. \n");
		bodyBuilder.append("3. \n");
		bodyBuilder.append('\n');
		bodyBuilder.append("## What were you expecting to happen? What happened instead?\n");
		bodyBuilder.append('\n');

		try {
			return "https://github.com/InventivetalentDev/TrashApp/issues/new?body=" + URLEncoder.encode(bodyBuilder.toString(), "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "https://github.com/InventivetalentDev/TrashApp/issues/new";
		}
	}

	/// https://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
	public static void openPlayStoreForPackage(Context context, String pckg) {
		try {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pckg)));
		} catch (android.content.ActivityNotFoundException anfe) {
			context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pckg)));
		}
	}

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

	public static String readLines(InputStream inputStream) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		return builder.toString();
	}

}
