package com.barmej.rideapplication;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import com.barmej.rideapplication.domain.StatusCallback;
import com.barmej.rideapplication.domain.TripManager;
import com.barmej.rideapplication.domain.model.FullStatus;
import com.barmej.rideapplication.domain.model.Rider;
import com.barmej.rideapplication.domain.model.Trip;
import com.google.android.gms.maps.model.LatLng;


public class HomeActivity extends AppCompatActivity {


    private StatusCallback statusListener;
    private RequestTripFragment requestTripFragment;
    private OnTripFragment onTripFragment;
    private RequestTripCommunicationInterface requestTripActionDelegates;
    private MapsContainerFragment mapsFragment;
    private LatLng pickUpLatLng;
    private LatLng destinationLatLng;

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
                pickUpLatLng = mapsFragment.captureAndMarkCenterForPickUp();
                return pickUpLatLng != null;
            }

            @Override
            public boolean setDestination() {
                destinationLatLng = mapsFragment.captureAndMarkCenterForDestination();
                return destinationLatLng != null;
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

    @Override
    protected void onStart() {
        super.onStart();
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
        LatLng driverLatlng = new LatLng(trip.getCurrentLat(), trip.getCurrentLng());
        LatLng pickUpLatlng = new LatLng(trip.getPickUpLat(),trip.getPickUpLng());
        LatLng destinationLatlng = new LatLng(trip.getDestinationLat(),trip.getDestinationLng());
        mapsFragment.setDriverMarker(driverLatlng);
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
        if (onTripFragment != null) {
            transaction.hide(onTripFragment);
        }
        if (requestTripFragment == null) {
            requestTripFragment = RequestTripFragment.getInstance(status);
            requestTripFragment.setActionDelegates(requestTripActionDelegates);
            transaction.add(R.id.frame_layout_top_fragment_container, requestTripFragment, null);
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
        if (requestTripFragment != null) {
            transaction.hide(requestTripFragment);
        }

        if (onTripFragment == null) {
            onTripFragment = OnTripFragment.getInstance(status);
            transaction.add(R.id.frame_layout_top_fragment_container, onTripFragment, null);
            transaction.commit();
        } else {
            transaction.show(onTripFragment);
            transaction.commit();
            onTripFragment.updateWithStatus(status);
        }

    }

    private void showArrivedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.you_have_arrived);
        builder.show();
    }
}
