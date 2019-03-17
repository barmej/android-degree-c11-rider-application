package com.barmej.rideapplication.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.barmej.rideapplication.CallBack;
import com.barmej.rideapplication.domain.model.Driver;
import com.barmej.rideapplication.domain.model.FullStatus;
import com.barmej.rideapplication.domain.model.Rider;
import com.barmej.rideapplication.domain.model.Trip;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.UUID;

public class TripManager {

    private static final String USER_REF_PATH = "users";
    private static final String TRIP_REF_PATH = "trips";
    private static final String DRIVER_REF_PATH = "drivers";

    private static TripManager INSTANCE;
    private FirebaseDatabase database;
    private Rider rider;
    private DatabaseReference riderRef;
    private StatusCallback statusListener;
    private Driver driver;
    private Trip trip;
    private ValueEventListener tripListener;


    private TripManager() {
        database = FirebaseDatabase.getInstance();

    }

    public static void init() {
        INSTANCE = new TripManager();
    }

    public static TripManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("can't call getInstance before calling init");
        }
        return INSTANCE;
    }

    public void login(final CallBack callBack) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getOrCreateAndGetUserInfoRef(task.getResult().getUser().getUid(), callBack);

                } else {
                    callBack.onComplete(false);
                }
            }
        });
    }

    private void getOrCreateAndGetUserInfoRef(final String uId, final CallBack callBack) {
        riderRef = database.getReference(USER_REF_PATH).child(uId);
        riderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    rider = dataSnapshot.getValue(Rider.class);
                    callBack.onComplete(true);
                } else {
                    createRiderInfo(uId, callBack);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                rider = new Rider(uId);
                createRiderInfo(uId, callBack);
            }
        });
    }

    private void createRiderInfo(String uId, final CallBack callBack) {
        Rider temp = new Rider(uId);
        temp.setStatus(Rider.Status.FREE.name());

        riderRef.setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callBack.onComplete(true);
                } else {
                    callBack.onComplete(false);
                }
            }
        });
    }

    public void startListeningToUpdates(StatusCallback statusListener) {
        this.statusListener = statusListener;
        startMonitoringState();
    }

    private void startMonitoringState() {
        String riderStatus = rider.getStatus();
        if (riderStatus.equals(Rider.Status.FREE.name())) {
            FullStatus status = new FullStatus();
            status.setRider(rider);
            notifyListener(status);
        } else if (riderStatus.equals(Rider.Status.ON_TRIP.name())) {
            startMonitoringTrip(rider.getAssignedTrip());
        }
    }

    private void startMonitoringTrip(String id) {
        ChildEventListener mListener = database.getReference(TRIP_REF_PATH).child(id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                System.out.println();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        tripListener = database.getReference(TRIP_REF_PATH).child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trip = dataSnapshot.getValue(Trip.class);
                if (driver == null) {
                    database.getReference(DRIVER_REF_PATH).child(trip.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            driver = dataSnapshot.getValue(Driver.class);
                            updateStatusWithTrip();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    updateStatusWithTrip();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateStatusWithTrip() {
        FullStatus fullStatus = new FullStatus();
        fullStatus.setDriver(driver);
        fullStatus.setRider(rider);
        fullStatus.setTrip(trip);
        if (trip.getStatus().equals(Trip.Status.ARRIVED.name())) {
            removeTripListener();
            rider.setStatus(Rider.Status.ARRIVED.name());
            notifyListener(fullStatus);
            rider.setStatus(Rider.Status.FREE.name());
            rider.setAssignedTrip(null);
            trip = null;
            driver = null;
            fullStatus.setTrip(null);
            fullStatus.setDriver(null);
            database.getReference(USER_REF_PATH).child(rider.getId()).setValue(rider);
            notifyListener(fullStatus);

        } else {
            notifyListener(fullStatus);
        }
    }

    public void requestTrip(final LatLng pickup, final LatLng destination) {
        FullStatus fullStatus = new FullStatus();
        rider.setStatus(Rider.Status.REQUESTING_TRIP.name());
        fullStatus.setRider(rider);
        notifyListener(fullStatus);
        database.getReference(DRIVER_REF_PATH)
                .orderByChild("status")
                .limitToFirst(1)
                .equalTo(Driver.Status.AVAILABLE.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot current : dataSnapshot.getChildren()) {
                            driver = current.getValue(Driver.class);
                        }
                        if (driver == null) {
                            notifyNoDriverFoundAndFreeStatus();
                        } else {
                            createAndSubscribeToTrip(pickup, destination);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void createAndSubscribeToTrip(LatLng pickup, LatLng destination) {
        final String id = UUID.randomUUID().toString();
        trip = new Trip();
        trip.setId(id);
        trip.setDriverId(driver.getId());
        trip.setRiderId(rider.getId());
        trip.setPickUpLat(pickup.latitude);
        trip.setPickUpLng(pickup.longitude);
        trip.setDestinationLat(destination.latitude);
        trip.setDestinationLng(destination.longitude);
        trip.setStatus(Trip.Status.GOING_TO_PICKUP.name());
        database.getReference(TRIP_REF_PATH).child(id).setValue(trip).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    driver.setAssignedTrip(id);
                    driver.setStatus(Driver.Status.ON_TRIP.name());
                    database.getReference(DRIVER_REF_PATH).child(driver.getId()).setValue(driver);
                    rider.setAssignedTrip(id);
                    rider.setStatus(Rider.Status.ON_TRIP.name());
                    database.getReference(USER_REF_PATH).child(rider.getId()).setValue(rider);
                    startMonitoringTrip(id);
                }
            }
        });


    }

    private void notifyNoDriverFoundAndFreeStatus() {
        rider.setStatus(Rider.Status.REQUEST_FAILED.name());
        FullStatus fullStatus = new FullStatus();
        fullStatus.setRider(rider);
        notifyListener(fullStatus);
        rider.setStatus(Rider.Status.FREE.name());
        notifyListener(fullStatus);
    }

    public void stopListeningToUpdates() {
        statusListener = null;
        removeTripListener();

    }

    private void removeTripListener() {
        if (tripListener != null && trip != null) {
            database.getReference(TRIP_REF_PATH).child(trip.getId()).removeEventListener(tripListener);
            tripListener = null;
        }
    }

    private void notifyListener(FullStatus fullStatus) {
        if (statusListener != null) {
            statusListener.onUpdate(fullStatus);
        }
    }
}
