package org.inventivetalent.trashapp.ui.main;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import com.google.firebase.analytics.FirebaseAnalytics;
import org.inventivetalent.trashapp.R;
import org.inventivetalent.trashapp.TabActivity;
import org.inventivetalent.trashapp.common.*;
import org.inventivetalent.trashapp.common.db.Converters;
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
import java.util.List;
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

	private FirebaseAnalytics mFirebaseAnalytics;

	private MapView              mapView;
	private FloatingActionButton addButton;
	private FloatingActionButton myLocationButton;

	private IMapController mapController;

	//	private GoogleMap map;
	//	private Marker    lastMarker;

	private boolean zoomedToSelf = false;

	private Marker      selfMarker;
	private Marker      closestCanMarker;
	private Set<Marker> canMarkers = new HashSet<>();
	private Polyline    polyline;

	private RadiusMarkerClusterer markerClusterer;

	private OsmAndHelper osmAndHelper;

	private PaymentHandler paymentHandler;

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

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
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
				updateSelfMarker(location);
				mapView.invalidate();
			}
		});
		viewModel.mClosestCan.observe(this, new Observer<LatLon>() {
			@Override
			public void onChanged(@Nullable LatLon element) {
				setMarkers(element);
				mapView.invalidate();
			}
		});

		//		final AdView adView = view.findViewById(R.id.mapAdView);
		//		paymentHandler.waitForManager(new PaymentReadyListener() {
		//			@Override
		//			public void ready() {
		//				boolean hasPremium = paymentHandler.isPurchased(BillingConstants.SKU_PREMIUM);
		//				Log.i("MapFragment", "hasPremium: " + hasPremium);
		//
		//				if (hasPremium) {
		//					adView.setVisibility(View.GONE);
		//				}else{
		//					adView.setVisibility(View.VISIBLE);
		//					adView.loadAd(new AdRequest.Builder().build());
		//				}
		//			}
		//		});

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
		LatLon closest = viewModel.mClosestCan.getValue();
		if (location != null && closest != null) {
			//TODO
		}
	}

	void moveMap(Location location, double zoom) {
		if (mapController != null && location != null) {
			mapController.animateTo(new GeoPoint(location.getLatitude(), location.getLongitude()), zoom, 1000L);
			//			map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
		}
	}

	void updateSelfMarker(Location location) {
		if (location != null) {
			// add self marker
			if (selfMarker == null) {
				selfMarker = new org.osmdroid.views.overlay.Marker(mapView);
				Drawable drawable = getResources().getDrawable(R.drawable.ic_person_pin_circle_black_24dp);
				selfMarker.setIcon(drawable);
				selfMarker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
				selfMarker.setInfoWindow(null);
				mapView.getOverlays().add(selfMarker);
			}
			GeoPoint selfPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
			selfMarker.setPosition(selfPoint);
			if (!zoomedToSelf) {
				moveToSelfLocation();
				zoomedToSelf = true;
			}

			if (polyline == null) {
				polyline = new Polyline(mapView);
				polyline.setWidth(5f);
				polyline.setInfoWindow(null);
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
	}

	void setMarkers(LatLon closestElement) {
		if (mapController != null && closestElement != null) {
			//			for (Marker marker : canMarkers) {
			//				marker.remove();
			//			}
			//			map.clear();

			final PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
			Location lastLocation = viewModel.mLocation.getValue();

			updateSelfMarker(lastLocation);

			// add closest marker
			//			if (closestCanMarker == null) {
			//				closestCanMarker = new Marker(mapView);
			//				Drawable drawable = getResources().getDrawable(R.drawable.ic_marker_32dp);
			//				drawable.setColorFilter(Util.getAttrColor(getActivity(),R.attr.colorAccent), PorterDuff.Mode.SRC_IN);
			//				closestCanMarker.setIcon(drawable);
			//				closestCanMarker.setInfoWindow(null);
			//				closestCanMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
			//				mapView.getOverlays().add(closestCanMarker);
			//			}
			//			closestCanMarker.setPosition(new GeoPoint(closestElement.lat, closestElement.lon));

			//			MarkerOptions markerOptions = new MarkerOptions()
			//					.icon(BitmapDescriptorFactory.fromResource(R.raw.trashcan32))
			//					.anchor(.5f, 1f)
			//					.alpha(0.9f)
			//					.position(new LatLng(closestElement.lat, closestElement.lon));
			//			Marker marker = map.addMarker(markerOptions);
			//			canMarkers.add(marker);

			if (selfMarker != null /*&& closestCanMarker != null*/) {
				polyline.setPoints(Arrays.asList(selfMarker.getPosition(), /*closestCanMarker.getPosition()*/new GeoPoint(closestElement.getLat(), closestElement.getLon())));
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
			for (LatLon element : TabActivity.nearbyTrashCans) {
				//				if (element.id == closestElement.id) {
				//					continue;// don't add twice
				//				}

				Marker marker = new Marker(mapView);
				Drawable drawable = getResources().getDrawable(R.drawable.ic_marker_32dp);
				drawable.setColorFilter(Util.getAttrColor(getActivity(), R.attr.colorAccent), PorterDuff.Mode.SRC_IN);
				marker.setIcon(drawable);

				if (element instanceof TrashType) {
					List<String> readables = Util.typeKeysToReadables(getActivity(), ((TrashType) element).getTypes());
					if (((TrashType) element).getTypes().contains("general") || ((TrashType) element).getTypes().contains("bin")) {
						marker.setTitle(getString(R.string.trashcan));
						marker.setSubDescription(getString(R.string.trashcan_info));
					} else {
						marker.setTitle(getString(R.string.recycling));
						marker.setSubDescription(getString(R.string.recycling_info));
					}
					marker.setSnippet(Converters.fromList(readables));
				} else {
					marker.setInfoWindow(null);
				}

				marker.setAlpha(.9f);
				marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
				marker.setPosition(new GeoPoint(element.getLat(), element.getLon()));
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
		if (context instanceof PaymentHandler) {
			paymentHandler = (PaymentHandler) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement PaymentHandler");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();

		mFirebaseAnalytics.setCurrentScreen(getActivity(), "MapTab", null);
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
