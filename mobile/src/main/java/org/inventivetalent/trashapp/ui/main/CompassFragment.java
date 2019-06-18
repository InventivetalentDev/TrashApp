package org.inventivetalent.trashapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.inventivetalent.trashapp.R;
import org.inventivetalent.trashapp.SettingsActivity;
import org.inventivetalent.trashapp.TabActivity;
import org.inventivetalent.trashapp.common.*;

public class CompassFragment extends Fragment {

	private FirebaseAnalytics mFirebaseAnalytics;

	private SharedPreferences sharedPreferences;
	private boolean           debug;

	private TextView distanceTextView;
	private TextView statusTextView;

	private TextView coordTextView;
	private TextView rangeTextView;
	private TextView rotationTextView;

	private ImageButton settingsButton;

	private ProgressBar searchProgress;

	private ImageView pointerView;
	private ImageView trashCanView;

	private float lastPointerRotation;

	private TrashcanUpdater trashcanUpdater;
	private PaymentHandler  paymentHandler;

	public CompassFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		debug = Util.getBoolean(sharedPreferences, "enable_debug", false);

		//		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		//		fab.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View view) {
		//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
		//						.setAction("Action", null).show();
		//			}
		//		});

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

		Log.i("TrashApp", "CompassFragment onCreate");
	}

	@Override
	public void onResume() {
		super.onResume();

		// reset center to self location
		TabActivity.searchCenter = TabActivity.lastKnownLocation;

		mFirebaseAnalytics.setCurrentScreen(getActivity(), "CompassTab", null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i("TrashApp", "CompassFragment onCreateView");

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_compass, container, false);

		// debugging stuff
		rotationTextView = view.findViewById(R.id.rotationView);
		rotationTextView.setVisibility(debug ? View.VISIBLE : View.INVISIBLE);
		coordTextView = view.findViewById(R.id.coordTextView);
		coordTextView.setVisibility(debug ? View.VISIBLE : View.INVISIBLE);

		distanceTextView = view.findViewById(R.id.distanceTextView);
		statusTextView = view.findViewById(R.id.statusTextView);
		pointerView = view.findViewById(R.id.pointer);
		searchProgress = view.findViewById(R.id.progressBar);
		settingsButton = view.findViewById(R.id.settingsButton);
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openSettings();
			}
		});
		trashCanView = view.findViewById(R.id.trashCanImageView);
		trashCanView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchProgress.setVisibility(View.VISIBLE);
				pointerView.setVisibility(View.INVISIBLE);
				statusTextView.setText(R.string.searching_cans);
				distanceTextView.setText(R.string.shrug);

				trashcanUpdater.lookForTrashCans();
			}
		});

		final AdView adView1 = view.findViewById(R.id.compassAdView);
		//			final AdView adView2 = view.findViewById(R.id.compassAdView2);
		paymentHandler.waitForManager(new PaymentReadyListener() {
			@Override
			public void ready() {
				boolean hasPremium = paymentHandler.isPurchased(BillingConstants.SKU_PREMIUM);
				boolean hasAdsRemoved = paymentHandler.isPurchased(BillingConstants.SKU_REMOVE_ADS);
				Log.i("SettingsActivity", "hasPremium (deprecated): " + hasPremium);
				Log.i("SettingsActivity", "hasAdsRemoved: " + hasAdsRemoved);

				if (hasPremium || hasAdsRemoved) {
					adView1.setVisibility(View.GONE);
					//						adView2.setVisibility(View.GONE);
				} else {
					adView1.setVisibility(View.VISIBLE);
					//						adView2.setVisibility(View.VISIBLE);
					adView1.loadAd(new AdRequest.Builder().build());
					//						adView2.loadAd(new AdRequest.Builder().build());
				}
			}
		});

		PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
		viewModel.mLocation.observe(this, new Observer<Location>() {
			@Override
			public void onChanged(@Nullable Location location) {
				coordTextView.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
				updatePointer();
			}
		});
		viewModel.mRotation.observe(this, new Observer<Float>() {
			@Override
			public void onChanged(@Nullable Float aFloat) {
				updatePointer();
			}
		});
		viewModel.mClosestCan.observe(this, new Observer<LatLon>() {
			@Override
			public void onChanged(@Nullable LatLon element) {
				updatePointer();
			}
		});

		return view;
	}

	void updatePointer() {
		PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
		LatLon closestTrashCan = viewModel.mClosestCan.getValue();
		Location lastKnownLocation = viewModel.mLocation.getValue();

		if (closestTrashCan == null || lastKnownLocation == null) {
			searchProgress.setVisibility(View.VISIBLE);
			pointerView.setVisibility(View.INVISIBLE);
			statusTextView.setText(R.string.searching_cans);
			distanceTextView.setText(R.string.shrug);
			return;
		}
		searchProgress.setVisibility(View.INVISIBLE);
		pointerView.setVisibility(View.VISIBLE);
		statusTextView.setText("");

		double distance = lastKnownLocation.distanceTo(closestTrashCan.toLocation());
		distanceTextView.setText(getResources().getString(R.string.distance_format, (int) Math.round(distance)));

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
		float azimuth = TabActivity.rotationBuffer.getAverageAzimuth();
		//		 azimuth = (float) Math.toDegrees(azimuth);
		if (TabActivity.geoField != null) {
			azimuth += TabActivity.geoField.getDeclination();
		}
		float angle = (float) (azimuth - bearing);
		if (angle < 0) { angle += 360f; }

		//double canAngle =Math.toDegrees(angleTo(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), closestTrashCan.lat, closestTrashCan.lon))%360;
		//
		//		double angle = (-nortRotation)%360;

		rotationTextView.setText("" + azimuth + " / " + (angle));

		float imageRotation = -angle;
		RotateAnimation animation = new RotateAnimation(lastPointerRotation, imageRotation, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setDuration(500);
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());

		pointerView.startAnimation(animation);
		//		pointerView.setRotation((float) nortRotation);

		lastPointerRotation = imageRotation;
	}

	void openSettings() {
		Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
		startActivity(settingsIntent);
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof TrashcanUpdater) {
			trashcanUpdater = (TrashcanUpdater) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement TrashcanUpdater");
		}
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

}
