package com.barmej.rideapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.barmej.rideapplication.domain.model.FullStatus;

public class OnTripFragment extends Fragment {

    private static final String INITIAL_STATUS_EXTRA = "INITIAL_STATUS_EXTRA";

    public static OnTripFragment getInstance(FullStatus status){
        OnTripFragment fragment= new OnTripFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(INITIAL_STATUS_EXTRA,status);
        fragment.setArguments(arguments);
        return  fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.on_trip_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FullStatus status= (FullStatus) getArguments().getSerializable(INITIAL_STATUS_EXTRA);;
        updateWithStatus(status);
    }

    public void updateWithStatus(FullStatus status) {

    }
}
