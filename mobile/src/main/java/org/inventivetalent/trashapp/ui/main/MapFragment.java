package org.inventivetalent.trashapp.ui.main;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.inventivetalent.trashapp.R;
import org.inventivetalent.trashapp.TabActivity;
import org.inventivetalent.trashapp.common.OsmAndHelper;
import org.inventivetalent.trashapp.common.OverpassResponse;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.inventivetalent.trashapp.common.Constants.OSM_REQUEST_CODE;

public class MapFragment extends Fragment {

	public static final OnlineTileSourceBase WIKIMAPS = new XYTileSource("WikimediaMaps",
			0, 19, 256, ".png", new String[] {
			"https://maps.wikimedia.org/osm-intl/" }, "© OpenStreetMap contributors",
			new TileSourcePolicy(2,
					TileSourcePolicy.FLAG_NO_BULK
							| TileSourcePolicy.FLAG_NO_PREVENTIVE
							| TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
							| TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
			));

	private MapView              mapView;
	private FloatingActionButton addButton;
	private FloatingActionButton myLocationButton;

	private IMapController mapController;

	//	private GoogleMap map;
	//	private Marker    lastMarker;

	private boolean zoomedToSelf = false;

	private Marker      selfMarker;
	private Marker      clostestCanMarker;
	private Set<Marker> canMarkers = new HashSet<>();
	private Polyline    polyline;

	private RadiusMarkerClusterer markerClusterer;

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

		Configuration.getInstance().load(getActivity(), PreferenceManager.getDefaultSharedPreferences(getActivity()));

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_map, container, false);

		mapView = view.findViewById(R.id.map);
		mapView.setTileSource(WIKIMAPS);
		mapView.setMultiTouchControls(true);
		mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
		mapController = mapView.getController();
		mapController.setZoom(15f);


		//		mapView.onCreate(savedInstanceState);
		//		mapView.getMapAsync(this);

		final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);

		addButton = view.findViewById(R.id.addButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Location location = viewModel.mLocation.getValue();
				if (location != null) {
					showLocationInOsm(location.getLatitude(), location.getLongitude());
				}
			}
		});
		myLocationButton = view.findViewById(R.id.myLocationButton);
		myLocationButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				moveToSelfLocation();
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

	void moveToSelfLocation() {
		final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
		Location location = viewModel.mLocation.getValue();
		if (location != null) {
			moveMap(location, 19);
		}
	}

	void focusOnSelfAndClosest() {
		final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
		Location location = viewModel.mLocation.getValue();
		OverpassResponse.Element closest =viewModel.mClosestCan.getValue();
		if (location != null&&closest!=null) {
			//TODO
		}
	}

	void moveMap(Location location, double zoom) {
		if (mapController != null && location != null) {
			mapController.animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()), zoom, 1000L);
			//			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
		}
	}

	void setMarkers(OverpassResponse.Element closestElement) {
		if (mapController != null && closestElement != null) {
			//			for (Marker marker : canMarkers) {
			//				marker.remove();
			//			}
			//			map.clear();

			final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
			Location lastLocation = viewModel.mLocation.getValue();

			if (lastLocation != null) {
				// add self marker
				if (selfMarker == null) {
					selfMarker = new org.osmdroid.views.overlay.Marker(mapView);
					selfMarker.setIcon(getResources().getDrawable(R.drawable.ic_person_pin_circle_black_24dp));
					selfMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
					selfMarker.setInfoWindow(null);
					mapView.getOverlays().add(selfMarker);
				}
				GeoPoint selfPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
				selfMarker.setPosition(selfPoint);
				if (!zoomedToSelf) {
					moveToSelfLocation();
					zoomedToSelf = true;
				}

				if (polyline == null) {
					polyline = new Polyline(mapView);
					mapView.getOverlays().add(polyline);
				}

				//				MarkerOptions markerOptions = new MarkerOptions()
				//						.icon(BitmapDescriptorFactory.fromBitmap(Util.getBitmapFromVectorDrawable(getActivity(), R.drawable.ic_person_pin_circle_black_24dp)))
				//						.anchor(.5f, 1f)
				//						.alpha(0.9f)
				//						.position(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
				//				Marker marker = map.addMarker(markerOptions);
				//				canMarkers.add(marker);

				//				PolylineOptions polylineOptions = new PolylineOptions()
				//						.add(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()))
				//						.add(new LatLng(closestElement.lat, closestElement.lon));
				//				map.addPolyline(polylineOptions);

			}

			// add closest marker
			if (clostestCanMarker == null) {
				clostestCanMarker = new Marker(mapView);
				clostestCanMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker_32dp));
				clostestCanMarker.setInfoWindow(null);
				clostestCanMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
				mapView.getOverlays().add(clostestCanMarker);
			}
			clostestCanMarker.setPosition(new GeoPoint(closestElement.lat, closestElement.lon));

			//			MarkerOptions markerOptions = new MarkerOptions()
			//					.icon(BitmapDescriptorFactory.fromResource(R.raw.trashcan32))
			//					.anchor(.5f, 1f)
			//					.alpha(0.9f)
			//					.position(new LatLng(closestElement.lat, closestElement.lon));
			//			Marker marker = map.addMarker(markerOptions);
			//			canMarkers.add(marker);

			if (selfMarker != null && clostestCanMarker != null) {
				polyline.setPoints(Arrays.asList(selfMarker.getPosition(), clostestCanMarker.getPosition()));
			}


			// add other markers
//			for (Marker oldMarker : canMarkers) {
//				mapView.getOverlays().remove(oldMarker);
//			}
//			canMarkers.clear();
			if (markerClusterer == null) {
				markerClusterer = new RadiusMarkerClusterer(getActivity());
				mapView.getOverlays().add(markerClusterer);
			}
			markerClusterer.getItems().clear();
			for (OverpassResponse.Element element : TabActivity.nearbyTrashCans) {
				if (element.id == closestElement.id) {
					continue;// don't add twice
				}

				Marker marker = new Marker(mapView);
				marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_32dp));
				marker.setInfoWindow(null);
				marker.setAlpha(.8f);
				marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
				marker.setPosition(new GeoPoint(element.lat, element.lon));
//				canMarkers.add(marker);
//				mapView.getOverlays().add(marker);
				markerClusterer.add(marker);

				//				markerOptions = new MarkerOptions()
				//						.icon(BitmapDescriptorFactory.fromResource(R.raw.trashcan32))
				//						.anchor(.5f, 1f)
				//						.alpha(0.5f)
				//						.position(new LatLng(element.lat, element.lon));
				//				marker = map.addMarker(markerOptions);
				//				canMarkers.add(marker);
			}
			markerClusterer.invalidate();
		}
	}

	//	@Override
	//	public void onMapReady(GoogleMap googleMap) {
	//		map = googleMap;
	//		if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
	//			map.setMyLocationEnabled(true);
	//			UiSettings settings = map.getUiSettings();
	//		}
	//
	//		//		map.getUiSettings().setMyLocationButtonEnabled(false);
	//		//		map.setMyLocationEnabled(true);
	//
	//		PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
	//		OverpassResponse.Element closestTrashCan = viewModel.mClosestCan.getValue();
	//		Location lastKnownLocation = viewModel.mLocation.getValue();
	//
	//		moveMap(lastKnownLocation);
	//		setMarkers(closestTrashCan);
	//	}

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
		//		mapView.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		//		mapView.onLowMemory();
	}

	void showLocationInOsm(double lat, double lon) {
		if (osmAndHelper != null) {
			osmAndHelper.showLocation(lat, lon);
		}
	}

}
