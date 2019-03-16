package com.barmej.rideapplication;

import com.google.android.gms.maps.model.LatLng;

public interface RequestTripCommunicationInterface {
    /**
     * @return true if the location has been set correctly , false other wise
     * */
     boolean setPickUp();
    /**
     * @return true if the location has been set correctly , false other wise
     * */
     boolean setDestination();
     void requestTrip();
}
