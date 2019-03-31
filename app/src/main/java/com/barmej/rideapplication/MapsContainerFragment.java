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
import com.google.android.gms.maps.model.*;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsContainerFragment extends Fragment implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    @Nullable
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

    public LatLng captureCenter(){
        if (mMap == null) return null;
        return mMap.getCameraPosition().target;
    }

    public void setPickUpMarker(LatLng target) {
        if (mMap == null) return ;
        if (pickUpMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.pickup);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(target);
            pickUpMarker = mMap.addMarker(options);
        } else {
            pickUpMarker.setPosition(target);
        }
    }

    public void setDestinationMarker(LatLng target) {
        if (mMap == null) return ;
        if (destinationMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.destination);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);            options.position(target);
            destinationMarker = mMap.addMarker(options);
        } else {
            destinationMarker.setPosition(target);
        }
    }

    public void setDriverMarker(LatLng driverLatlng) {
        if (mMap == null) return ;
        if (driverMarker == null) {
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.car);
            MarkerOptions options = new MarkerOptions();
            options.icon(descriptor);
            options.position(driverLatlng);
            driverMarker = mMap.addMarker(options);
        } else {
            driverMarker.setPosition(driverLatlng);
        }
    }

    public void reset() {
        if (mMap == null) return ;
        mMap.clear();
        pickUpMarker = null;
        destinationMarker = null;
        driverMarker = null;
    }


}
