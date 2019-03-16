package com.barmej.rideapplication.domain;

import android.support.annotation.NonNull;
import com.barmej.rideapplication.CallBack;
import com.barmej.rideapplication.domain.model.Driver;
import com.barmej.rideapplication.domain.model.FullStatus;
import com.barmej.rideapplication.domain.model.Rider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class TripManager {

    private static final String USER_REF_PATH="users";
    private static final String TRIP_REF_PATH="trips";
    private static final String DRIVER_REF_PATH="drivers";

    private static TripManager INSTANCE ;
    private FirebaseDatabase database;
    private Rider rider;
    private DatabaseReference riderRef;
    private StatusCallback statusListener;


    private TripManager(){
        database = FirebaseDatabase.getInstance();

    }



    public static void init(){
        INSTANCE = new TripManager();
    }


    public static TripManager getInstance() {
        if(INSTANCE == null){
            throw new IllegalStateException("can't call getInstance before calling init");
        }
        return INSTANCE;
    }
    public void login(final CallBack callBack){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful() ) {
                    getOrCreateAndGetUserInfoRef(task.getResult().getUser().getUid(),callBack);

                }else{
                    callBack.onComplete(false);
                }
            }
        });
    }
    private void getOrCreateAndGetUserInfoRef(final String uId, final CallBack callBack) {
        riderRef= database.getReference(USER_REF_PATH).child(uId);
        riderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    rider = dataSnapshot.getValue( Rider.class );
                    callBack.onComplete(true);
                }else {
                    createRiderInfo(uId,callBack);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                rider = new Rider(uId);
                createRiderInfo(uId,callBack);
            }
        });
    }

    private void createRiderInfo(String uId, final CallBack callBack) {
        Rider temp = new Rider(uId);
        temp.setStatus(Rider.Status.FREE.name());
        
        riderRef.setValue(temp).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    callBack.onComplete(true);
                }else{
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
        if(riderStatus.equals(Rider.Status.FREE.name())){
            FullStatus status = new FullStatus();
            status.setRider(rider);
            notifyListener(status);
        }else if (riderStatus.equals(Rider.Status.ON_TRIP)){
            startMonitoringTrip();
        }
    }

    private void startMonitoringTrip() {

    }

    public void stopListeningToUpdates() {
        statusListener = null;

    }

    public void requestTrip() {
        FullStatus fullStatus = new FullStatus();
        rider.setStatus(Rider.Status.REQUESTING_TRIP.name());
        fullStatus.setRider(rider);
        notifyListener(fullStatus);
        database.getReference()
                .child(DRIVER_REF_PATH)
                .orderByChild("status")
                .limitToFirst(1)
                .equalTo(Driver.Status.AVAILABLE.name())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Driver driver = dataSnapshot.getValue(Driver.class);
               if(driver == null){
                   notifyNoDriverFoundAndFreeStatus();
               }
                System.out.println();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void notifyListener(FullStatus fullStatus) {
        if(statusListener != null){
            statusListener.onUpdate(fullStatus);
        }
    }
}
