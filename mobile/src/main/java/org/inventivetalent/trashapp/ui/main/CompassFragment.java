package org.inventivetalent.trashapp.ui.main;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import org.inventivetalent.trashapp.R;
import org.inventivetalent.trashapp.SettingsActivity;
import org.inventivetalent.trashapp.TabActivity;
import org.inventivetalent.trashapp.common.OverpassResponse;
import org.inventivetalent.trashapp.common.TrashcanUpdater;

public class CompassFragment extends Fragment {

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

	public CompassFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
		}

		//		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		//		fab.setOnClickListener(new View.OnClickListener() {
		//			@Override
		//			public void onClick(View view) {
		//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
		//						.setAction("Action", null).show();
		//			}
		//		});

		Log.i("TrashApp", "Hello Info World 5!");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_compass, container, false);

		distanceTextView = view.findViewById(R.id.distanceTextView);
		statusTextView = view.findViewById(R.id.statusTextView);
		coordTextView = view.findViewById(R.id.coordTextView);
		pointerView = view.findViewById(R.id.pointer);
		searchProgress = view.findViewById(R.id.progressBar);
		rotationTextView = view.findViewById(R.id.rotationView);
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
		viewModel.mClosestCan.observe(this, new Observer<OverpassResponse.Element>() {
			@Override
			public void onChanged(@Nullable OverpassResponse.Element element) {
				updatePointer();
			}
		});

		return view;
	}

	void updatePointer() {
		PageViewModel viewModel = ViewModelProviders.of(getActivity()).get(PageViewModel.class);
		OverpassResponse.Element closestTrashCan = viewModel.mClosestCan.getValue();
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
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}
