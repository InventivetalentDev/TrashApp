package org.inventivetalent.trashapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.inventivetalent.trashapp.common.OsmAndHelper;
import org.inventivetalent.trashapp.common.OsmBridgeClient;
import org.inventivetalent.trashapp.common.Util;
import org.inventivetalent.trashapp.common.db.Converters;
import org.inventivetalent.trashapp.ui.main.MapFragment;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

import static org.inventivetalent.trashapp.common.Constants.OSM_REQUEST_CODE;

public class AddActivity extends AppCompatActivity {

	private MapView              mapView;
	private FloatingActionButton addDoneButton;
	private EditText             commentEditText;
	private IMapController       mapController;

	private Button btnType;
	private Button btnSubType;

	private OsmAndHelper      osmAndHelper;
	private FirebaseAnalytics mFirebaseAnalytics;
	private SharedPreferences sharedPreferences;
	private boolean           debug;

	private boolean isAddConfirmed = false;

	private AlertDialog currentDialog;

	double lat;
	double lon;
	String amenity = "waste_basket";

	private int    selectedTypeIndex = 0;
	private String selectedTypeName  = "General";
	private String selectedTypeKey   = "general";

	private boolean[]    selectedSubtypeBools = null;
	private List<String> selectedSubtypeKeys  = new ArrayList<>();
	private List<String> selectedSubtypeNames = new ArrayList<>();

	OsmBridgeClient client;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		osmAndHelper = new OsmAndHelper(this, OSM_REQUEST_CODE, new OsmAndHelper.OnOsmandMissingListener() {
			@Override
			public void osmandMissing() {
				Toast.makeText(AddActivity.this, "Please download OsmAnd to edit Trashcan locations", Toast.LENGTH_LONG).show();
				Util.openPlayStoreForPackage(AddActivity.this, "net.osmand");
			}
		});
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		debug = Util.getBoolean(sharedPreferences, "enable_debug", false);

		mapView = findViewById(R.id.map);
		mapView.setTileSource(MapFragment.WIKIMAPS);
		mapView.setMultiTouchControls(true);
		mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
		mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
		RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(mapView);
		mRotationGestureOverlay.setEnabled(true);
		mapView.getOverlays().add(mRotationGestureOverlay);
		mapController = mapView.getController();
		mapController.setZoom(20f);

		client = new OsmBridgeClient(sharedPreferences);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				lat = extras.getDouble("lat");
				lon = extras.getDouble("lon");

				mapController.setCenter(new GeoPoint(lat, lon));
			}
		}

		TextView osmAndTextView = findViewById(R.id.osmAndTextView);
		osmAndTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IGeoPoint center = mapView.getMapCenter();
				lat = center.getLatitude();
				lon = center.getLongitude();

				showLocationInOsm(lat, lon);
			}
		});

		commentEditText = findViewById(R.id.commentEditText);

		btnType = findViewById(R.id.btnType);
		btnType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showTypePicker();
			}
		});
		btnSubType = findViewById(R.id.btnSubType);
		btnSubType.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSubTypePicker();
			}
		});

		selectedSubtypeBools = new boolean[Util.getStringArrayResLength(this, R.array.trash_subtype_recycling_values)];

		addDoneButton = findViewById(R.id.addDoneButton);
		addDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("AddActivity", "addDoneButton onClick");

				IGeoPoint center = mapView.getMapCenter();
				lat = center.getLatitude();
				lon = center.getLongitude();

				currentDialog = new AlertDialog.Builder(AddActivity.this).setMessage(R.string.alert_adding_trashcan).show();// TODO: might wanna change the message shown here

				// Run the add-task
				new AddTask().execute(getPendingTrashcans());

				Bundle bundle = new Bundle();
				bundle.putString("lat", String.valueOf(lat));
				bundle.putString("lon", String.valueOf(lon));
				bundle.putString("amenity", amenity);
				mFirebaseAnalytics.logEvent("add_trashcan", bundle);
			}
		});
	}

	void showTypePicker() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.pick_type)
				.setItems(R.array.trash_type_entries, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which != selectedTypeIndex) {
							selectedSubtypeNames.clear();
							selectedSubtypeKeys.clear();

							btnSubType.setText(R.string.none);
							selectedSubtypeBools = new boolean[selectedSubtypeBools.length];
						}

						selectedTypeIndex = which;

						Log.i("AddActivity", "Selected type #" + which);

						selectedTypeKey = Util.getArrayResString(AddActivity.this, R.array.trash_type_values, which);
						selectedTypeName = Util.getArrayResString(AddActivity.this, R.array.trash_type_entries, which);

						Log.i("AddActivity", selectedTypeKey + " = " + selectedTypeName);

						btnType.setText(selectedTypeName);

						btnSubType.setEnabled(!"general".equals(selectedTypeKey));
					}
				}).show();
	}

	void showSubTypePicker() {
		final List<Integer> tempSelectedSubtypeIndexes = new ArrayList<>();
		final List<String> tempSelectedSubtypeKeys = new ArrayList<>(selectedSubtypeKeys);
		final List<String> tempSelectedSubtypeNames = new ArrayList<>(selectedSubtypeNames);

		@ArrayRes
		int valueSrc = -1;
		@ArrayRes
		int entrySrc = -1;

		if ("waste".equals(selectedTypeKey)) {
			valueSrc = R.array.trash_subtype_waste_values;
			entrySrc = R.array.trash_subtype_waste_entries;
		} else if ("recycling".equals(selectedTypeKey)) {
			valueSrc = R.array.trash_subtype_recycling_values;
			entrySrc = R.array.trash_subtype_recycling_entries;
		} else {
			Log.w("AddActivity", "SubTypePicker opened with 'general' or other main type! Panic!");
			return;
		}

		@ArrayRes
		final int finalValueSrc = valueSrc;
		@ArrayRes
		final int finalEntrySrc = entrySrc;

		new AlertDialog.Builder(this)
				.setTitle(R.string.pick_type)
				.setMultiChoiceItems(entrySrc, selectedSubtypeBools, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						selectedSubtypeBools[which] = isChecked;

						String typeKey = Util.getArrayResString(AddActivity.this, finalValueSrc, which);
						String typeName = Util.getArrayResString(AddActivity.this, finalEntrySrc, which);

						if (isChecked) {
							tempSelectedSubtypeIndexes.add(which);
							tempSelectedSubtypeKeys.add(typeKey);
							tempSelectedSubtypeNames.add(typeName);
						} else if (tempSelectedSubtypeIndexes.contains(which) || selectedSubtypeKeys.contains(typeKey)) {
							tempSelectedSubtypeIndexes.remove(Integer.valueOf(which));
							tempSelectedSubtypeKeys.remove(typeKey);
							tempSelectedSubtypeNames.remove(typeName);
						}
					}
				})
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						Log.i("AddActivity", tempSelectedSubtypeKeys.toString());

						selectedSubtypeKeys.clear();
						selectedSubtypeKeys.addAll(tempSelectedSubtypeKeys);

						selectedSubtypeNames.clear();
						selectedSubtypeNames.addAll(tempSelectedSubtypeNames);

						if (selectedSubtypeNames.isEmpty()) {
							btnSubType.setText(R.string.none);
						} else {
							btnSubType.setText(Converters.fromList(selectedSubtypeNames));
						}
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				})
				.show();
	}

	OsmBridgeClient.PendingTrashcan[] getPendingTrashcans() {
		// TODO: support multiple

		String amenity = "waste_basket";
		String waste = null;
		String recycling = null;
		if ("waste".equals(selectedTypeKey)) {
			waste = Converters.fromList(selectedSubtypeKeys);
		}
		if ("recycling".equals(selectedTypeKey)) {
			amenity = "recycling";
			recycling = Converters.fromList(selectedSubtypeKeys);
		}

		Log.i("AddActivity", "amenity: " + amenity);
		Log.i("AddActivity", "waste: " + waste);
		Log.i("AddActivity", "recycling: " + recycling);

		return new OsmBridgeClient.PendingTrashcan[] {
				new OsmBridgeClient.PendingTrashcan(this.lat, this.lon, amenity, waste, recycling)
		};
	}

	void showLocationInOsm(double lat, double lon) {
		if (osmAndHelper != null) {
			currentDialog = new AlertDialog.Builder(this).setMessage(R.string.alert_open_osmand).show();
			osmAndHelper.showLocation(lat, lon);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == OsmBridgeClient.AUTH_REQUEST_CODE) {
			Log.i("AddActivity", "Got result from WebView auth: " + (resultCode == RESULT_OK ? "OK" : "NOT OK"));

			if (!client.notifyAuthFinished(resultCode)) {
				closeCurrentDialog();
				currentDialog = new AlertDialog.Builder(this).setMessage(R.string.osm_auth_failed).show();
				client.invalidateSession(sharedPreferences);
				return;
			}
			client.storeSessionId(sharedPreferences);

			// Start another task, this time hopefully with correct authentication
			new AddTask().execute(getPendingTrashcans());

			mFirebaseAnalytics.setUserProperty("osm_user", client.getOsmUsername());
			mFirebaseAnalytics.logEvent("osm_auth_success", null);
		}
	}

	void showAddConfirmationDialog(String osmUsername) {
		closeCurrentDialog();
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.add_trashcan_confirmation, osmUsername))
				.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isAddConfirmed = true;
						new AddTask().execute(getPendingTrashcans());
					}
				})
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isAddConfirmed = false;
					}
				}).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		outState.putDouble("lat", lat);
		outState.putDouble("lon", lon);
		outState.putString("amenity", amenity);

		outState.putInt("selectedTypeIndex", selectedTypeIndex);
		outState.putString("selectedTypeKey", selectedTypeKey);
		outState.putBooleanArray("selectedSubtypeBools", selectedSubtypeBools);
		outState.putStringArrayList("selectedSubtypeKeys", (ArrayList<String>) selectedSubtypeKeys);

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		lat = savedInstanceState.getDouble("lat");
		lon = savedInstanceState.getDouble("lon");
		amenity = savedInstanceState.getString("amenity");

		selectedTypeIndex = savedInstanceState.getInt("selectedTypeIndex");
		selectedTypeKey = savedInstanceState.getString("selectedTypeKey");
		selectedSubtypeBools = savedInstanceState.getBooleanArray("selectedSubtypeBools");
		selectedSubtypeKeys.clear();
		ArrayList<String> tempSelectedSubtypeKeys = savedInstanceState.getStringArrayList("selectedSubtypeKeys");
		if (tempSelectedSubtypeKeys != null) { selectedSubtypeKeys.addAll(tempSelectedSubtypeKeys); }
	}

	void closeCurrentDialog() {
		if (currentDialog != null) {
			currentDialog.dismiss();
		}
	}

	enum AddState {
		AUTH_PENDING,
		AWAITING_CONFIRMATION,
		SUCCESS,
		FAIL;
	}

	private class AddTask extends AsyncTask<OsmBridgeClient.PendingTrashcan, Void, AddState> {

		AddTask() {
		}

		@Override
		protected AddState doInBackground(OsmBridgeClient.PendingTrashcan... pendingTrashcans) {

			// Check session id
			boolean sessionValid = client.isSessionIdValid();
			if (!sessionValid) {
				Log.w("AddTask", "Session is not valid - launching WebView to authenticate with OSM");
				client.launchAuthWebView(AddActivity.this);
				// Stop this task - wait for the auth to complete to start another one
				return AddState.AUTH_PENDING;
			}
			Log.i("AddTask", "Session is valid");

			if (!isAddConfirmed) {
				Log.i("AddTask", "Asking for add confirmation");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showAddConfirmationDialog(client.getOsmUsername());
					}
				});
				return AddState.AWAITING_CONFIRMATION;
			}
			Log.i("AddTask", "Add Confirmed");

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					closeCurrentDialog();
					currentDialog = new AlertDialog.Builder(AddActivity.this).setMessage(R.string.alert_adding_trashcan).show();
				}
			});

			if (client.addTrashcans(commentEditText.getText().toString(), pendingTrashcans)) {
				Log.i("AddTask", "added!");
				return AddState.SUCCESS;
			}
			Log.w("AddTask", "failed!");
			return AddState.FAIL;
		}

		@Override
		protected void onPostExecute(AddState state) {
			super.onPostExecute(state);

			Bundle bundle = new Bundle();
			bundle.putString("lat", String.valueOf(lat));
			bundle.putString("lon", String.valueOf(lon));
			bundle.putString("amenity", amenity);

			if (state == AddState.SUCCESS) {
				closeCurrentDialog();
				currentDialog = new AlertDialog.Builder(AddActivity.this).setMessage(R.string.trashcan_added).show();

				mFirebaseAnalytics.logEvent("add_trashcan_success", bundle);
			} else if (state == AddState.FAIL) {
				closeCurrentDialog();
				currentDialog = new AlertDialog.Builder(AddActivity.this).setMessage(R.string.trashcan_add_failed).show();

				mFirebaseAnalytics.logEvent("add_trashcan_fail", bundle);
			}
		}
	}
}
