package com.barmej.rideapplication.callbacks;

import com.barmej.rideapplication.domain.entitis.FullStatus;

public interface StatusCallback {
    void onUpdate(FullStatus status);
}
