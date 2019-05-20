package org.inventivetalent.trashapp.ui.main;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import org.inventivetalent.trashapp.R;
import org.inventivetalent.trashapp.TabActivity;
import org.inventivetalent.trashapp.common.OsmAndHelper;
import org.inventivetalent.trashapp.common.OverpassResponse;
import org.inventivetalent.trashapp.common.Util;

import java.util.HashSet;
import java.util.Set;

import static org.inventivetalent.trashapp.common.Constants.OSM_REQUEST_CODE;

public class MapFragment extends Fragment implements OnMapReadyCallback {

	private MapView     mapView;
	private ImageButton editButton;

	private GoogleMap map;
	private Marker    lastMarker;

	private Set<Marker> canMarkers = new HashSet<>();

	private OsmAndHelper osmAndHelper;

	public MapFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}

		osmAndHelper = new OsmAndHelper(getActivity(), OSM_REQUEST_CODE, new OsmAndHelper.OnOsmandMissingListener() {
			@Override
			public void osmandMissing() {
				Toast.makeText(getActivity(), "Please download OsmAnd to edit Trashcan locations", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		mapView = view.findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);

		final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);

		editButton = view.findViewById(R.id.addButton);
		editButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Location location = viewModel.mLocation.getValue();
				if (location != null) {
					showLocationInOsm(location.getLatitude(), location.getLongitude());
				}
			}
		});

		viewModel.mLocation.observe(this, new Observer<Location>() {
			@Override
			public void onChanged(@Nullable Location location) {
				//				moveMap(location);
			}
		});
		viewModel.mClosestCan.observe(this, new Observer<OverpassResponse.Element>() {
			@Override
			public void onChanged(@Nullable OverpassResponse.Element element) {
				setMarkers(element);
			}
		});

		return view;
	}

	void moveMap(Location location) {
		if (map != null && location != null) {
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
		}
	}

	void setMarkers(OverpassResponse.Element closestElement) {
		if (map != null && closestElement != null) {
			for (Marker marker : canMarkers) {
				marker.remove();
			}
			map.clear();

			final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
			Location lastLocation = viewModel.mLocation.getValue();

			if (lastLocation != null) {
				// add self marker
				MarkerOptions markerOptions = new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromBitmap(Util.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_person_pin_circle_black_24dp)))
						.anchor(.5f, 1f)
						.alpha(0.9f)
						.position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
				Marker marker = map.addMarker(markerOptions);
				canMarkers.add(marker);

				PolylineOptions polylineOptions = new PolylineOptions()
						.add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
						.add(new LatLng(closestElement.lat, closestElement.lon));
				map.addPolyline(polylineOptions);
			}

			// add closest marker
			MarkerOptions markerOptions = new MarkerOptions()
					.icon(BitmapDescriptorFactory.fromResource(R.raw.trashcan32))
					.anchor(.5f, 1f)
					.alpha(0.9f)
					.position(new LatLng(closestElement.lat, closestElement.lon));
			Marker marker = map.addMarker(markerOptions);
			canMarkers.add(marker);

			// add other markers
			for (OverpassResponse.Element element : TabActivity.nearbyTrashCans) {
				if (element.id == closestElement.id) {
					continue;// don't add twice
				}

				markerOptions = new MarkerOptions()
						.icon(BitmapDescriptorFactory.fromResource(R.raw.trashcan32))
						.anchor(.5f, 1f)
						.alpha(0.5f)
						.position(new LatLng(element.lat, element.lon));
				marker = map.addMarker(markerOptions);
				canMarkers.add(marker);
			}
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		map = googleMap;
		if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			map.setMyLocationEnabled(true);
			UiSettings settings = map.getUiSettings();
		}

		//		map.getUiSettings().setMyLocationButtonEnabled(false);
		//		map.setMyLocationEnabled(true);

		PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
		OverpassResponse.Element closestTrashCan = viewModel.mClosestCan.getValue();
		Location lastKnownLocation = viewModel.mLocation.getValue();

		moveMap(lastKnownLocation);
		setMarkers(closestTrashCan);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	void showLocationInOsm(double lat, double lon) {
		if (osmAndHelper != null) {
			osmAndHelper.showLocation(lat, lon);
		}
	}

}
