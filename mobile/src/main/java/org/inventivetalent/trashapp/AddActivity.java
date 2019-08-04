package org.inventivetalent.trashapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.inventivetalent.trashapp.common.OsmAndHelper;
import org.inventivetalent.trashapp.ui.main.MapFragment;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import static org.inventivetalent.trashapp.common.Constants.OSM_REQUEST_CODE;

public class AddActivity extends AppCompatActivity {

	private MapView              mapView;
	private FloatingActionButton addDoneButton;
	private IMapController       mapController;

	private OsmAndHelper osmAndHelper;

	double lat;
	double lon;

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
			}
		});

		mapView = findViewById(R.id.map);
		mapView.setTileSource(MapFragment.WIKIMAPS);
		mapView.setMultiTouchControls(true);
		mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
		mapController = mapView.getController();
		mapController.setZoom(20f);

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

		addDoneButton = findViewById(R.id.addDoneButton);
		addDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("AddActivity", "addDoneButton onClick");

				IGeoPoint center = mapView.getMapCenter();
				lat = center.getLatitude();
				lon = center.getLongitude();

				//TODO: store changes and eventually make a request to the API
			}
		});
	}

	void showLocationInOsm(double lat, double lon) {
		if (osmAndHelper != null) {
			osmAndHelper.showLocation(lat, lon);
		}
	}

}
