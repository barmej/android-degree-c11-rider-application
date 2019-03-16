package com.barmej.rideapplication.domain;

import com.barmej.rideapplication.domain.model.FullStatus;

public interface StatusCallback {
    void onUpdate(FullStatus status);
}
