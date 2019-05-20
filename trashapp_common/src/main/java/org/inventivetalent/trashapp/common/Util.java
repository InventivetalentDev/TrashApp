package org.inventivetalent.trashapp.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

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

}
