package org.inventivetalent.trashapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import org.inventivetalent.trashapp.common.*;
import org.inventivetalent.trashapp.common.db.AppDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.inventivetalent.trashapp.common.Constants.*;
import static org.inventivetalent.trashapp.common.OverpassResponse.convertElementsToPoints;

public class MainActivity extends WearableActivity implements TrashCanResultHandler {

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

	public static List<OverpassResponse.Element> nearbyTrashCans = new ArrayList<>();
	public static OverpassResponse.Element       closestTrashCan;

	private final LocationListener    mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			Log.i("TrashApp", "onLocationChanged");
			Log.i("TrashApp", location.toString());

			setLastKnownLocation(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	};
	private final SensorEventListener mSensorListener   = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				System.arraycopy(event.values, 0, mGravity, 0, event.values.length);
				gravitySet = true;
			}
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				System.arraycopy(event.values, 0, mGeomagnetic, 0, event.values.length);
				magneticSet = true;
			}

			if (gravitySet && magneticSet) {
				float[] r = new float[9];
				float[] i = new float[9];

				if (SensorManager.getRotationMatrix(r, i, mGravity, mGeomagnetic)) {
					SensorManager.getOrientation(r, lastKnownRotation);
					rotationBuffer.add(lastKnownRotation[0]);

					updatePointer();
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	private TextView distanceTextView;
	private TextView rangeTextView;

	private ProgressBar searchProgress;

	private ImageView pointerView;

	private float lastPointerRotation;

	private AppDatabase appDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		distanceTextView = findViewById(R.id.distanceTextView);
		pointerView = findViewById(R.id.pointer);
		searchProgress = findViewById(R.id.progressBar);

		pointerView.setBackground(getResources().getDrawable(R.drawable.ic_pointer_24dp));

		appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "trashapp").build();

		// Enables Always-on
		setAmbientEnabled();
	}

	@Override
	protected void onResume() {
		super.onResume();

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		if (mSensorManager == null) {
			Toast.makeText(this, "Your device doesn't support sensors", Toast.LENGTH_LONG).show();
			exitApp();
			return;
		}
		Sensor magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(mSensorListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (requestLocationUpdates(true)) {
			lookForTrashCans();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		mSensorManager.unregisterListener(mSensorListener);
		mLocationManager.removeUpdates(mLocationListener);
	}

	void updatePointer() {
		if (closestTrashCan == null) {
			searchProgress.setVisibility(View.VISIBLE);
			pointerView.setVisibility(View.INVISIBLE);
			//			distanceTextView.setText(R.string.searching_cans);
			return;
		}
		searchProgress.setVisibility(View.INVISIBLE);
		pointerView.setVisibility(View.VISIBLE);

		double distance = distance(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), closestTrashCan.lat, closestTrashCan.lon) / ONE_METER_DEG;
		distanceTextView.setText(Math.round(distance) + "m");

		//		double heading = (Math.toDegrees(lastKnownRotation[0]) + 360) % 360;
		//		if (geoField != null) {
		//			heading -= geoField.getDeclination();
		//		}
		//		if (nortRotation < 0) {
		//			nortRotation += 180;
		//		}

		Location canLocation = closestTrashCan.toLocation();
		float bearing = lastKnownLocation.bearingTo(canLocation);

		//		float angle = (float) (bearing - heading)*-1;
		float azimuth = rotationBuffer.getAverageAzimuth();
		//		 azimuth = (float) Math.toDegrees(azimuth);
		if (geoField != null) {
			azimuth += geoField.getDeclination();
		}
		float angle = (float) (azimuth - bearing);
		if (angle < 0) { angle += 360f; }

		//double canAngle =Math.toDegrees(angleTo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), closestTrashCan.lat, closestTrashCan.lon))%360;
		//
		//		double angle = (-nortRotation)%360;

		float imageRotation = -angle;
		RotateAnimation animation = new RotateAnimation(lastPointerRotation, imageRotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());

		pointerView.startAnimation(animation);
		//		pointerView.setRotation((float) nortRotation);

		lastPointerRotation = imageRotation;
	}

	private double normalizeDegree(double value) {
		if (value >= 0.0f && value <= 180.0f) {
			return value;
		} else {
			return 180 + (180 + value);
		}
	}

	double distance(double lat1, double lon1, double lat2, double lon2) {
		return Math.sqrt(Math.abs(((lat2 - lat1) * (lat2 - lat1)) - ((lon2 - lon1) * (lon2 - lon1))));
	}

	double angleTo(double selfLat, double selfLon, double otherLat, double otherLon) {
		return Math.atan2(otherLat - selfLat, otherLon - selfLon);
	}

	double radToDeg(double rad) {
		return rad * 180 / Math.PI;
	}

	void setLastKnownLocation(Location location) {
		if (location != null) {
			geoField = new GeomagneticField(
					(float) location.getLatitude(),
					(float) location.getLongitude(),
					(float) location.getAltitude(),
					System.currentTimeMillis()
			);
			lastKnownLocation = location;

			updatePointer();
		}
	}

	boolean requestLocationUpdates(boolean ask) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Log.i("TrashApp", "Location permissions not granted");
			if (ask) {
				Log.i("TrashApp", "Requesting location permissions");
				ActivityCompat.requestPermissions(this, new String[] {
						Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_LOCATION_PERMS_CODE);
				return false;
			}
			Log.i("TrashApp", "Location permissions not granted and can't ask - exiting!");

			Toast.makeText(this, "This app requires location permissions", Toast.LENGTH_LONG).show();
			exitApp();
			return false;
		}

		Log.i("TrashApp", "Location permissions granted!");
		// has permission, request!
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
				LOCATION_REFRESH_DISTANCE, mLocationListener);

		Log.i("TrashApp", "Trying to get last known location from providers");
		Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null) {
			location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (location == null) {
			location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		setLastKnownLocation(location);
		Log.i("TrashApp", lastKnownLocation != null ? lastKnownLocation.toString() : "n/a");

		return true;
	}

	@Override
	public boolean shouldCacheResults() {
		return true;
	}

	@Override
	public File getCacheFile() {
		return new File(getFilesDir(), "last_osm_query.json");
	}

	@Override
	public AppDatabase getDatabase() {
		return appDatabase;
	}

	void lookForTrashCans() {
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
		Log.i("TrashApp", boundingBox.toCoordString());
		new TrashCanFinderTask(this, this).execute(boundingBox);
	}

	@Override
	public void handleTrashCanLocations(OverpassResponse response, boolean isCached) {
		Log.i("TrashApp", response.toString());


		List<OverpassResponse.Element> elements = response.elements;
		elements = convertElementsToPoints(elements);
		Log.i("TrashApp", elements.toString());

		nearbyTrashCans.clear();
		nearbyTrashCans.addAll(elements);

		if (elements.size() > 0) {
			OverpassResponse.Element closest = elements.get(0);
			closestTrashCan = closest;
			updatePointer();
		} else {
			Util.insertTrashcanResult(appDatabase, elements);

			closestTrashCan = null;
			updatePointer();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		if (requestCode == REQUEST_LOCATION_PERMS_CODE) {
			// just try to init the updates again
			if (requestLocationUpdates(false)) {
				lookForTrashCans();
			}
		}
	}

	void exitApp() {
		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
	}

}
