package org.inventivetalent.trashapp.osmadditions;

import android.content.Context;
import android.view.MotionEvent;

import org.osmdroid.views.MapView;

public class InteractiveCompassOverlay extends org.osmdroid.views.overlay.compass.CompassOverlay {

    public InteractiveCompassOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event, MapView mapView)
    {
        mapView.setMapOrientation(0);
        return super.onSingleTapConfirmed(event, mapView);
    }

}
