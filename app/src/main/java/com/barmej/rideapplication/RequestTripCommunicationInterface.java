package com.barmej.rideapplication;

public interface RequestTripCommunicationInterface {
    /**
     * @return true if the location has been set correctly , false other wise
     */
    boolean setPickUp();

    /**
     * @return true if the location has been set correctly , false other wise
     */
    boolean setDestination();

    void requestTrip();
}
