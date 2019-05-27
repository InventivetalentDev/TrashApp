package org.inventivetalent.trashapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

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
			case "grass":
			default:
				rId = R.style.GrassTheme;
				break;
//			default:
//				rId = R.style.AppTheme;
//				break;
		}
		context.setTheme(rId);
	}



}
