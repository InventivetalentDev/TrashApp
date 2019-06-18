package org.inventivetalent.trashapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.room.Room;
import com.android.billingclient.api.*;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.location.*;
import com.google.android.material.tabs.TabLayout;
import com.kobakei.ratethisapp.RateThisApp;
import org.inventivetalent.trashapp.common.*;
import org.inventivetalent.trashapp.common.db.AppDatabase;
import org.inventivetalent.trashapp.common.db.Migrations;
import org.inventivetalent.trashapp.ui.main.PageViewModel;
import org.inventivetalent.trashapp.ui.main.SectionsPagerAdapter;

import java.util.*;

import static org.inventivetalent.trashapp.common.Constants.*;
import static org.inventivetalent.trashapp.common.OverpassResponse.elementsSortedByDistanceFrom;

public class TabActivity extends AppCompatActivity implements TrashCanResultHandler, TrashcanUpdater, PaymentHandler, BillingManager.BillingUpdatesListener {

	protected static TabActivity instance;

	private SharedPreferences sharedPreferences;
	private boolean           debug;

	private       LocationManager  mLocationManager;
	public static Location         lastKnownLocation;
	public static Location         searchCenter;
	public static GeomagneticField geoField;

	private       SensorManager mSensorManager;
	private       float[]       mGravity          = new float[3];
	private       boolean       gravitySet;
	private       float[]       mGeomagnetic      = new float[3];
	private       boolean       magneticSet;
	public static float[]       lastKnownRotation = new float[3];

	public long lastLiveUpdateTime = 0;

	public static RotationBuffer rotationBuffer = new RotationBuffer();

	boolean initialSearchCompleted = false;
	public static List<LatLon> nearbyTrashCans = new ArrayList<>();
	public static LatLon       closestTrashCan;

	private BillingManager            billingManager;
	private boolean                   billingManagerReady;
	private Set<String>               purchasedSkus         = new HashSet<>();
	private Set<PaymentReadyListener> paymentReadyListeners = new HashSet<>();

	@Deprecated
	protected static SkuInfo SKU_INFO_PREMIUM;
	protected static SkuInfo SKU_INFO_THEMES;
	protected static SkuInfo SKU_INFO_REMOVE_ADS;

	private   int         searchItaration = 0;
	protected AppDatabase appDatabase;

	private CustomViewPager viewPager;

	private       FusedLocationProviderClient fusedLocationProviderClient;
	private       LocationRequest             locationRequest   = new LocationRequest()
			.setInterval(Constants.LOCATION_INTERVAL)
			.setFastestInterval(Constants.LOCATION_INTERVAL_MIN)
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	private final LocationListener            mLocationListener = new LocationListener() {
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
	private final LocationCallback            locationCallback  = new LocationCallback() {
		@Override
		public void onLocationResult(LocationResult locationResult) {
			if (locationResult == null) {
				return;
			}

			Log.i("TrashApp", "onLocationResult");
			Log.i("TrashApp", locationResult.toString());

			setLastKnownLocation(locationResult.getLastLocation());
		}
	};
	private final SensorEventListener         mSensorListener   = new SensorEventListener() {
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

					ViewModelProviders.of(TabActivity.this).get(PageViewModel.class).mRotation.setValue(lastKnownRotation[0]);
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		//		for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
		//			System.out.println(entry.getKey() + ": " + entry.getValue() + " (" + entry.getValue().getClass() + ")");
		//		}
		debug = Util.getBoolean(sharedPreferences, "enable_debug", false);

		Util.applyTheme(this, sharedPreferences);

		setContentView(R.layout.activity_tab);
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
		viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(sectionsPagerAdapter);
		TabLayout tabs = findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		Intent intent = getIntent();
		if (intent != null) {
			Log.i("TrashApp", intent.toString());
			Log.i("TrashApp", intent.getAction());
			Uri data = intent.getData();
			Log.i("TrashApp", data != null ? data.toString() : "n/a");
		}

		billingManager = new BillingManager(this, this);

		MobileAds.initialize(this, "ca-app-pub-2604356629473365~4556622372");

		appDatabase = Room
				.databaseBuilder(getApplicationContext(), AppDatabase.class, "trashapp")
				.addMigrations(Migrations.MIGRATION_1_2)
				.fallbackToDestructiveMigration()
				.build();

		//		FloatingActionButton fab = findViewById(R.id.fab);
		//
		//		fab.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View view) {
		//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
		//						.setAction("Action", null).show();
		//			}
		//		});
		Util.showDebugDBAddressLogToast(this);

		// ATTENTION: This was auto-generated to handle app links.
		Intent appLinkIntent = getIntent();
		String appLinkAction = appLinkIntent.getAction();
		Uri appLinkData = appLinkIntent.getData();
		if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
			String tabPath = appLinkData.getLastPathSegment();
			if (tabPath != null) {
				switch (tabPath) {
					case "map":
						viewPager.setCurrentItem(1);
						break;
					case "compass":
					default:
						viewPager.setCurrentItem(0);
						break;
				}
			}
		}

		// Monitor launch times and interval from installation
		RateThisApp.onCreate(this);
		// If the condition is satisfied, "Rate this app" dialog will be shown
		RateThisApp.showRateDialogIfNeeded(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i("TabActivity", "onResume");

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
		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		if (requestLocationUpdates(true)) {
			lookForTrashCans();
		}

		if (billingManager != null
				&& billingManager.getBillingClientResponseCode() == BillingClient.BillingResponseCode.OK) {
			billingManager.queryPurchases();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSensorManager != null) { mSensorManager.unregisterListener(mSensorListener); }
		//		if (mLocationManager != null) { mLocationManager.removeUpdates(mLocationListener); }
		if (fusedLocationProviderClient != null) { fusedLocationProviderClient.removeLocationUpdates(locationCallback); }
	}

	@Override
	protected void onDestroy() {
		instance = null;
		if (billingManager != null) {
			billingManager.destroy();
		}
		super.onDestroy();
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

			if (searchCenter == null || !sharedPreferences.getBoolean("moving_search", false) || searchCenter.distanceTo(lastKnownLocation) > 1) {
				searchCenter = lastKnownLocation;
			}

			ViewModelProviders.of(this).get(PageViewModel.class).mLocation.setValue(location);

			if (!initialSearchCompleted) {
				lookForTrashCans();
			}
			updateClosestTrashcan(nearbyTrashCans);
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

		fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null/*Looper*/);

		//		//TODO: use google play services for location updates
		//		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
		//				LOCATION_REFRESH_DISTANCE, mLocationListener);
		//		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_REFRESH_TIME,
		//				LOCATION_REFRESH_DISTANCE, mLocationListener);

		Log.i("TrashApp", "Trying to get last known location from providers");
		Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null) {
			location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		} else {
			Log.i("TrashApp", "got last known location from gps provider");
		}
		if (location == null) {
			location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		} else {
			Log.i("TrashApp", "got last known location from network provider");
		}
		setLastKnownLocation(location);
		Log.i("TrashApp", lastKnownLocation != null ? lastKnownLocation.toString() : "n/a");

		mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);

		return true;
	}

	@Override
	public void lookForTrashCans() {
		if (searchCenter == null) {
			if (lastKnownLocation != null) {
				searchCenter = lastKnownLocation;
			} else {
				return;
			}
		}
		Log.i("TrashApp", "Looking for trash cans");
		Toast.makeText(this, R.string.searching, Toast.LENGTH_SHORT).show();

		double searchRadius = Util.getInt(sharedPreferences, "search_radius_start", DEFAULT_SEARCH_RADIUS) + SEARCH_STEP * searchItaration;// meters
		double searchRadiusDeg = searchRadius * ONE_METER_DEG;

		Log.i("TrashApp", "Radius: " + (searchRadius / 1000) + "km / " + searchRadius + "m / " + searchRadiusDeg + "deg");

		double lat = searchCenter.getLatitude();
		double lon = searchCenter.getLongitude();

		OverpassBoundingBox boundingBox = new OverpassBoundingBox(lat - searchRadiusDeg, lon - searchRadiusDeg, lat + searchRadiusDeg, lon + searchRadiusDeg);
		List<String> types = Util.createFilterFromPreferences(sharedPreferences);
		if (types.isEmpty()) {
			Toast.makeText(this, R.string.warn_empty_filter, Toast.LENGTH_SHORT).show();
		}
		Log.i("TrashApp", boundingBox.toCoordString());
		TrashcanQuery query = new TrashcanQuery(boundingBox, types);

		//TODO: make this more efficient, i.e. don't run both
		new DbTrashcanQueryTask(this).execute(query);

		if (System.currentTimeMillis() - lastLiveUpdateTime > 10000) {
			new TrashCanFinderTask(this, this).execute(query);
			lastLiveUpdateTime = System.currentTimeMillis();
		}
	}

	@Override
	public AppDatabase getDatabase() {
		return appDatabase;
	}

	@Override
	public void handleTrashCanLocations(List<? extends LatLon> elements, boolean isCached) {
		Log.i("TrashApp", "Got trashcan locations (cached: " + isCached + ")");

		initialSearchCompleted = true;

		//		elements = convertElementsToPoints(elements);
		Log.i("TrashApp", elements.toString());

		if (elements.isEmpty()) {
			if (!isCached && Util.getInt(sharedPreferences, "search_radius_start", DEFAULT_SEARCH_RADIUS) + SEARCH_STEP * searchItaration < Util.getInt(sharedPreferences, "search_radius_max", MAX_SEARCH_RADIUS)) {
				// still below max radius, keep looking
				searchItaration++;
				lookForTrashCans();
			} else {
				// reset
				searchItaration = 0;

				if (!isCached) { Toast.makeText(this, R.string.err_no_trashcans, Toast.LENGTH_LONG).show(); }
			}
		} else if (!isCached) {
			Util.insertTrashcanResult(appDatabase, elements);
			List<String> filter = Util.createFilterFromPreferences(sharedPreferences);
			Log.i("TabActivity", filter.toString());
			elements = Util.filterResponse(elements, filter);
		}
		updateClosestTrashcan(elements);
	}

	public void updateClosestTrashcan(Collection<? extends LatLon> elements) {
		//		Set<LatLon> joined = new HashSet<>();
		//		joined.addAll(elements);
		//		joined.addAll(nearbyTrashCans);
		//		elements = joined;

		if (elements.isEmpty()) {
			closestTrashCan = null;
			ViewModelProviders.of(this).get(PageViewModel.class).mClosestCan.setValue(null);
		} else {
			List<? extends LatLon> sortedElements = elementsSortedByDistanceFrom(elements, lastKnownLocation);
			// no need to convert to points again
			nearbyTrashCans.clear();
			nearbyTrashCans.addAll(sortedElements);

			//			int i = 0;
			//			for (OverpassResponse.Element element : elements) {
			//				Log.i("TrashApp", (i++) + " " + element.toLocation() + " => " + lastKnownLocation.distanceTo(element.toLocation()));
			//			}

			LatLon closest = sortedElements.get(0);
			ViewModelProviders.of(this).get(PageViewModel.class).mClosestCan.setValue(closest);
			closestTrashCan = closest;

			// reset
			searchItaration = 0;
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

	@Override
	public void launchBilling(SkuDetails skuDetails) {
		if (billingManager != null) {
			billingManager.initiatePurchaseFlow(skuDetails);
		}
	}

	@Override
	public boolean isPurchased(String sku) {
		return purchasedSkus.contains(sku);
	}

	@Override
	public void onBillingClientSetupFinished() {
		Log.i("TrashApp", "onBillingClientSetupFinished");

		Log.i("TrashApp", "Querying Sku Details...");
		billingManager.querySkuDetailsAsync(BillingClient.SkuType.INAPP, Arrays.asList(BillingConstants.IN_APP_SKUS), new SkuDetailsResponseListener() {
			@Override
			public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
				Log.i("TrashApp", "onSkuDetailsResponse");
				Log.i("TrashApp", "result: " + billingResult);
				Log.i("TrashApp", "list(" + (skuDetailsList != null ? skuDetailsList.size() : 0) + "): " + skuDetailsList);

				if (skuDetailsList != null && skuDetailsList.size() > 0) {
					for (SkuDetails details : skuDetailsList) {
						switch (details.getSku()) {
							case BillingConstants.SKU_PREMIUM:
								SKU_INFO_PREMIUM = new SkuInfo(details, TabActivity.this);
								break;
							case BillingConstants.SKU_THEMES:
								SKU_INFO_THEMES = new SkuInfo(details, TabActivity.this);
								break;
							case BillingConstants.SKU_REMOVE_ADS:
								SKU_INFO_REMOVE_ADS = new SkuInfo(details, TabActivity.this);
								break;
							default:
								Log.w("TabActivity", "Unhandled SkuDetails: " + details.getSku());
								break;
						}
					}

					billingManagerReady = true;
					for (PaymentReadyListener listener : paymentReadyListeners) {
						listener.ready();
					}
					paymentReadyListeners.clear();
				}
			}
		});
	}

	@Override
	public void waitForManager(PaymentReadyListener listener) {
		if (billingManagerReady) {
			listener.ready();
			return;
		}
		paymentReadyListeners.add(listener);
	}

	@Override
	public void onConsumeFinished(String token, BillingResult billingResult) {
		Log.i("TrashApp", "onConsumeFinished");
		Log.i("TrashApp", "token: " + token);
		Log.i("TrashApp", "result: " + billingResult);
	}

	@Override
	public void onPurchasesUpdated(List<Purchase> purchases) {
		Log.i("TrashApp", "onPurchasesUpdated");
		Log.i("TrashApp", "purchases(" + purchases.size() + "): " + purchases);

		for (Purchase purchase : purchases) {
			Log.i("TrashApp", purchase.getSku() + ": " + purchase.getPurchaseState());
			purchasedSkus.add(purchase.getSku());
		}
	}

	void exitApp() {
		Intent homeIntent = new Intent(Intent.ACTION_MAIN);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(homeIntent);
	}

}