package com.barmej.rideapplication;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.barmej.rideapplication.callback.RequestTripCommunicationInterface;
import com.barmej.rideapplication.callback.StatusCallback;
import com.barmej.rideapplication.domain.TripManager;
import com.barmej.rideapplication.domain.entity.FullStatus;
import com.barmej.rideapplication.domain.entity.Rider;
import com.barmej.rideapplication.domain.entity.Trip;
import com.barmej.rideapplication.fragment.MapsContainerFragment;
import com.barmej.rideapplication.fragment.OnTripFragment;
import com.barmej.rideapplication.fragment.RequestTripFragment;
import com.google.android.gms.maps.model.LatLng;


public class HomeActivity extends AppCompatActivity {


    private final static String REQUEST_TRIP_FRAGMENT_TAG = "REQUEST_TRIP_FRAGMENT_TAG";
    private final static String ON_TRIP_FRAGMENT_TAG = "ON_TRIP_FRAGMENT_TAG";

    private StatusCallback statusListener;
    private RequestTripCommunicationInterface requestTripActionDelegates;
    private MapsContainerFragment mapsFragment;
    private LatLng pickUpLatLng;
    private LatLng destinationLatLng;
    private LatLng driverLatLng;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initListenersAndDelegates();
        FragmentManager manager = getSupportFragmentManager();
        mapsFragment = (MapsContainerFragment) manager.findFragmentById(R.id.map_container_fragment);

    }

    private void initListenersAndDelegates() {
        statusListener = new StatusCallback() {
            @Override
            public void onUpdate(FullStatus status) {
                onUpdateStatus(status);
            }
        };
        requestTripActionDelegates = new RequestTripCommunicationInterface() {

            @Override
            public boolean setPickUp() {
                pickUpLatLng = mapsFragment.captureCenter();
                if (pickUpLatLng != null) {
                    mapsFragment.setPickUpMarker(pickUpLatLng);
                    return true;
                }
                return false;
            }

            @Override
            public boolean setDestination() {
                destinationLatLng = mapsFragment.captureCenter();
                if (destinationLatLng != null) {
                    mapsFragment.setDestinationMarker(destinationLatLng);
                    return true;
                }
                return false;
            }

            @Override
            public void requestTrip() {

                TripManager.getInstance().requestTrip(pickUpLatLng, destinationLatLng);
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        TripManager.getInstance().startListeningToUpdates(statusListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        TripManager.getInstance().stopListeningToUpdates();
    }


    private void onUpdateStatus(FullStatus status) {
        String riderStatus = status.getRider().getStatus();
        if (riderStatus.equals(Rider.Status.FREE.name())
                || riderStatus.equals(Rider.Status.REQUESTING_TRIP.name())
                || riderStatus.equals(Rider.Status.REQUEST_FAILED.name())) {
            updateWithRequestTripTopFragment(status);
            if (riderStatus.equals(Rider.Status.REQUEST_FAILED.name())) {
                reset();
            }
        } else if (riderStatus.equals(Rider.Status.ON_TRIP.name())) {
            updateWithTripTopFragment(status);
            updateMarkers(status.getTrip());
        } else if (riderStatus.equals(Rider.Status.ARRIVED.name())) {
            showArrivedDialog();
            reset();
        }
    }

    private void updateMarkers(Trip trip) {
        driverLatLng = new LatLng(trip.getCurrentLat(), trip.getCurrentLng());
        LatLng pickUpLatlng = new LatLng(trip.getPickUpLat(), trip.getPickUpLng());
        LatLng destinationLatlng = new LatLng(trip.getDestinationLat(), trip.getDestinationLng());
        mapsFragment.setDriverMarker(driverLatLng);
        mapsFragment.setPickUpMarker(pickUpLatlng);
        mapsFragment.setDestinationMarker(destinationLatlng);
    }

    private void reset() {
        destinationLatLng = null;
        pickUpLatLng = null;
        mapsFragment.reset();
    }


    private void updateWithRequestTripTopFragment(FullStatus status) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        RequestTripFragment requestTripFragment = (RequestTripFragment) fragmentManager.findFragmentByTag(REQUEST_TRIP_FRAGMENT_TAG);
        Fragment onTripFragment = fragmentManager.findFragmentByTag(ON_TRIP_FRAGMENT_TAG);
        if (onTripFragment != null) {
            transaction.hide(onTripFragment);
        }
        if (requestTripFragment == null) {
            requestTripFragment = RequestTripFragment.getInstance(status);
            requestTripFragment.setActionDelegates(requestTripActionDelegates);
            transaction.add(R.id.frame_layout_top_fragment_container, requestTripFragment, REQUEST_TRIP_FRAGMENT_TAG);
            transaction.commit();
        } else {
            transaction.show(requestTripFragment);
            transaction.commit();
            requestTripFragment.updateWithStatus(status);

        }
    }


    private void updateWithTripTopFragment(FullStatus status) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment requestTripFragment = fragmentManager.findFragmentByTag(REQUEST_TRIP_FRAGMENT_TAG);
        OnTripFragment onTripFragment = (OnTripFragment) fragmentManager.findFragmentByTag(ON_TRIP_FRAGMENT_TAG);
        if (requestTripFragment != null) {
            transaction.hide(requestTripFragment);
        }

        if (onTripFragment == null) {
            onTripFragment = OnTripFragment.getInstance(status);
            transaction.add(R.id.frame_layout_top_fragment_container, onTripFragment, ON_TRIP_FRAGMENT_TAG);
            transaction.commit();
        } else {
            transaction.show(onTripFragment);
            transaction.commit();
            onTripFragment.updateWithStatus(status);
        }

        mapsFragment.removeMapLocationLayout();

    }

    private void showArrivedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.you_have_arrived);
        builder.show();
    }

    public void goToDriveCurrentLocation(View view) {
        if (driverLatLng != null)
            mapsFragment.showDriverCurrentLocationOnMap(driverLatLng);
    }
}
