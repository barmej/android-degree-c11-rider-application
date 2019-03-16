package com.barmej.rideapplication;

import android.app.Application;
import com.barmej.rideapplication.domain.TripManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TripManager.init();
    }
}
