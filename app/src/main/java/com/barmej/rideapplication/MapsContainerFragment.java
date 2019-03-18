package com.barmej.rideapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsContainerFragment extends Fragment implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap mMap;
    private Marker pickUpMarker;
    private Marker destinationMarker;
    private Marker driverMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermissionAndSetUpUserLocation();
    }

    private void checkLocationPermissionAndSetUpUserLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            setupUserLocation();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @SuppressLint("MissingPermission")
    private void setupUserLocation() {
        mMap.setMyLocationEnabled(true);
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f);
                    mMap.moveCamera(update);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (permissions.length == 1 & grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUserLocation();
            } else {
                Toast.makeText(getActivity(), R.string.location_permission_needed, Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public LatLng captureAndMarkCenterForPickUp() {
        if (mMap == null) return null;
        LatLng target = mMap.getCameraPosition().target;
        setPickUpMarker(target);
        return target;
    }

    public void setPickUpMarker(LatLng target) {
        if (pickUpMarker == null) {
            MarkerOptions options = new MarkerOptions();
            options.position(target);
            pickUpMarker = mMap.addMarker(options);
        } else {
            pickUpMarker.setPosition(target);
        }
    }

    public LatLng captureAndMarkCenterForDestination() {
        if (mMap == null) return null;
        LatLng target = mMap.getCameraPosition().target;
        setDestinationMarker(target);
        return target;
    }

    public void setDestinationMarker(LatLng target) {
        if (destinationMarker == null) {
            MarkerOptions options = new MarkerOptions();
            options.position(target);
            destinationMarker = mMap.addMarker(options);
        } else {
            destinationMarker.setPosition(target);
        }
    }

    public void setDriverMarker(LatLng driverLatlng) {

        if (driverMarker == null) {
            MarkerOptions options = new MarkerOptions();
            options.position(driverLatlng);
            driverMarker = mMap.addMarker(options);
        } else {
            driverMarker.setPosition(driverLatlng);
        }
    }

    public void reset() {
        mMap.clear();
        pickUpMarker = null;
        destinationMarker = null;
        driverMarker = null;
    }


}
