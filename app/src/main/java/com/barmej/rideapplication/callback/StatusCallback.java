package com.barmej.rideapplication.callback;

import com.barmej.rideapplication.domain.entity.FullStatus;

public interface StatusCallback {
    void onUpdate(FullStatus status);
}
