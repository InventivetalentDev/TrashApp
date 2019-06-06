package org.inventivetalent.trashapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.hardware.GeomagneticField;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.RemoteViews;
import org.inventivetalent.trashapp.common.*;
import org.inventivetalent.trashapp.common.db.AppDatabase;

import java.util.ArrayList;
import java.util.List;

import static org.inventivetalent.trashapp.common.Constants.DEFAULT_SEARCH_RADIUS;
import static org.inventivetalent.trashapp.common.Constants.ONE_METER_DEG;

/**
 * Implementation of App Widget functionality.
 */
public class CompassWidget extends AppWidgetProvider implements TrashCanResultHandler {

	private       LocationManager  mLocationManager;
	public static Location         lastKnownLocation;
	public static GeomagneticField geoField;

	private       SensorManager mSensorManager;
	private       float[]       mGravity          = new float[3];
	private       boolean       gravitySet;
	private       float[]       mGeomagnetic      = new float[3];
	private       boolean       magneticSet;
	public static float[]       lastKnownRotation = new float[3];

	public static RotationBuffer rotationBuffer = new RotationBuffer();

	public static List<LatLon> nearbyTrashCans = new ArrayList<>();
	public static LatLon       closestTrashCan;

//	private final LocationListener    mLocationListener = new LocationListener() {
//		@Override
//		public void onLocationChanged(final Location location) {
//			Log.i("CompassWidget", "onLocationChanged");
//			Log.i("CompassWidget", location.toString());
//
//			setLastKnownLocation(location);
//		}
//
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//		}
//
//		@Override
//		public void onProviderEnabled(String provider) {
//		}
//
//		@Override
//		public void onProviderDisabled(String provider) {
//		}
//	};
//	private final SensorEventListener mSensorListener   = new SensorEventListener() {
//		@Override
//		public void onSensorChanged(SensorEvent event) {
//			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//				System.arraycopy(event.values, 0, mGravity, 0, event.values.length);
//				gravitySet = true;
//			}
//			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//				System.arraycopy(event.values, 0, mGeomagnetic, 0, event.values.length);
//				magneticSet = true;
//			}
//
//			if (gravitySet && magneticSet) {
//				float[] r = new float[9];
//				float[] i = new float[9];
//
//				if (SensorManager.getRotationMatrix(r, i, mGravity, mGeomagnetic)) {
//					SensorManager.getOrientation(r, lastKnownRotation);
//					rotationBuffer.add(lastKnownRotation[0]);
//
//					updatePointer();
//				}
//			}
//		}
//
//		@Override
//		public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		}
//	};

	private        float lastPointerRotation;
	public static int   pointerResId = -1;

	static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
			int appWidgetId) {

		Intent intent = new Intent(context, TabActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		// Construct the RemoteViews object
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.compass_widget);
		views.setOnClickPendingIntent(R.id.trashCanImage, pendingIntent);
		views.setOnClickPendingIntent(R.id.pointerImage, pendingIntent);
		views.setImageViewBitmap(R.id.trashCanImage, Util.getBitmapFromVectorDrawable(context, R.drawable.ic_trashcan_64dp));
		Log.i("CompassWidget", "res: " + pointerResId);
		if (pointerResId != -1) { views.setImageViewBitmap(R.id.pointerImage, Util.getBitmapFromVectorDrawable(context, pointerResId)); }

		// Instruct the widget manager to update the widget
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.i("CompassWidget", "onUpdate");

//		if (pointerResId == -1) {
//			init(context);
//		}

		// There may be multiple widgets active, so update all of them
		for (int appWidgetId : appWidgetIds) {
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onEnabled(Context context) {
		Log.i("CompassWidget", "onEnabled");

//		init(context);
	}
//
//	void init(Context context) {
//		mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
//		if (mSensorManager == null) {
//			Toast.makeText(context, "Your device doesn't support sensors", Toast.LENGTH_LONG).show();
//			return;
//		}
////		Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
////		Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
////		mSensorManager.registerListener(mSensorListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
////		mSensorManager.registerListener(mSensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//
////		mLocationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
////		if (requestLocationUpdates(context, true)) {
////			lookForTrashCans(context);
////		}
//	}

	@Override
	public void onDisabled(Context context) {
		Log.i("CompassWidget", "onDisabled");
		// Enter relevant functionality for when the last widget is disabled
	}

//	void setLastKnownLocation(Location location) {
//		if (location != null) {
//			geoField = new GeomagneticField(
//					(float) location.getLatitude(),
//					(float) location.getLongitude(),
//					(float) location.getAltitude(),
//					System.currentTimeMillis()
//			);
//			lastKnownLocation = location;
//
//			updatePointer();
//		}
//	}

//	static void updatePointer() {
//		if (closestTrashCan == null) {
//			return;
//		}
//
//		//TODO
//		//		double distance = distance(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), closestTrashCan.lat, closestTrashCan.lon) / ONE_METER_DEG;
//		//		distanceTextView.setText(Math.round(distance) + "m");
//
//		//		double heading = (Math.toDegrees(lastKnownRotation[0]) + 360) % 360;
//		//		if (geoField != null) {
//		//			heading -= geoField.getDeclination();
//		//		}
//		//		if (nortRotation < 0) {
//		//			nortRotation += 180;
//		//		}
//
//		Location canLocation = closestTrashCan.toLocation();
//		float bearing = lastKnownLocation.bearingTo(canLocation);
//
//		//		float angle = (float) (bearing - heading)*-1;
//		float azimuth = rotationBuffer.getAverageAzimuth();
//		//		 azimuth = (float) Math.toDegrees(azimuth);
//		if (geoField != null) {
//			azimuth += geoField.getDeclination();
//		}
//		float angle = (float) (azimuth - bearing);
//		if (angle < 0) { angle += 360f; }
//
//		//double canAngle =Math.toDegrees(angleTo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), closestTrashCan.lat, closestTrashCan.lon))%360;
//		//
//		//		double angle = (-nortRotation)%360;
//
//		Log.i("CompassWidget", "original angle: " + angle);
//		float imageRotation = angle;
//		Log.i("CompassWidget", "rotation: " + imageRotation);
//		imageRotation = Math.abs(imageRotation);
//		Log.i("CompassWidget", "abs: " + imageRotation);
//		imageRotation = imageRotation % 360;
//		Log.i("CompassWidget", "clamped: " + imageRotation);
//
//
//		//		RotateAnimation animation = new RotateAnimation(lastPointerRotation, imageRotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//		//		animation.setDuration(500);
//		//		animation.setFillAfter(true);
//		//		animation.setInterpolator(new LinearInterpolator());
//
//		//		pointerView.startAnimation(animation);
//		//		pointerView.setRotation((float) nortRotation);
//	}

	double distance(double lat1, double lon1, double lat2, double lon2) {
		return Math.sqrt(Math.abs(((lat2 - lat1) * (lat2 - lat1)) - ((lon2 - lon1) * (lon2 - lon1))));
	}

//	boolean requestLocationUpdates(Context context, boolean ask) {
//		if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//			Log.i("CompassWidget", "Location permissions not granted");
//			if (ask) {
//				//				Log.i("CompassWidget", "Requesting location permissions");
//				//				ActivityCompat.requestPermissions(context, new String[] {
//				//						Manifest.permission.ACCESS_FINE_LOCATION,
//				//						Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_LOCATION_PERMS_CODE);
//				return false;
//			}
//			Log.i("CompassWidget", "Location permissions not granted and can't ask - exiting!");
//
//			Toast.makeText(context, "This app requires location permissions", Toast.LENGTH_LONG).show();
//			return false;
//		}
//
//		Log.i("CompassWidget", "Location permissions granted!");
//		// has permission, request!
////		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
////				LOCATION_REFRESH_DISTANCE, mLocationListener);
//
//		Log.i("CompassWidget", "Trying to get last known location from providers");
//		Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//		if (location == null) {
//			location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		}
//		if (location == null) {
//			location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
//		}
////		setLastKnownLocation(location);
//		Log.i("CompassWidget", lastKnownLocation != null ? lastKnownLocation.toString() : "n/a");
//
//		return true;
//	}

	void lookForTrashCans(Context context) {
		if (lastKnownLocation == null) {
			return;
		}
		Log.i("TrashApp", "Looking for trash cans");
		double searchRadius = DEFAULT_SEARCH_RADIUS;// meters
		//TODO: might need to steadily increase the radius if we can't find anything closer
		double searchRadiusDeg = searchRadius * ONE_METER_DEG;

		Log.i("TrashApp", "Radius: " + (searchRadius / 1000) + "km / " + searchRadius + "m / " + searchRadiusDeg + "deg");

		double lat = lastKnownLocation.getLatitude();
		double lon = lastKnownLocation.getLongitude();

		OverpassBoundingBox boundingBox = new OverpassBoundingBox(lat - searchRadiusDeg, lon - searchRadiusDeg, lat + searchRadiusDeg, lon + searchRadiusDeg);
		TrashcanQuery query = new TrashcanQuery(boundingBox);
		//TODO: might wanna filter it
		Log.i("TrashApp", boundingBox.toCoordString());
		new TrashCanFinderTask(context, this).execute(query);
	}

	@Override
	public void handleTrashCanLocations(List<? extends LatLon> elements, boolean isCached) {
//		Log.i("TrashApp", response.toString());
//		List<OverpassResponse.Element> elements = response.elementsSortedByDistanceFrom(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
		nearbyTrashCans.clear();
		nearbyTrashCans.addAll(elements);

		if (elements.size() > 0) {
			LatLon closest = elements.get(0);
			closestTrashCan = closest;
//			updatePointer();
		} else {
			closestTrashCan = null;
//			updatePointer();
		}
	}


	@Override
	public AppDatabase getDatabase() {
		//TODO
		return null;
	}
}

