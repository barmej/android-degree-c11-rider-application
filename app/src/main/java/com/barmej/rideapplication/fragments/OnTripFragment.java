package com.barmej.rideapplication.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.barmej.rideapplication.R;
import com.barmej.rideapplication.domain.entitis.FullStatus;
import com.barmej.rideapplication.domain.entitis.Trip;

public class OnTripFragment extends Fragment {

    private static final String INITIAL_STATUS_EXTRA = "INITIAL_STATUS_EXTRA";
    private TextView tripStatusTv;
    private TextView driverNameTv;
    private TextView plateNumberTv;

    public static OnTripFragment getInstance(FullStatus status) {
        OnTripFragment fragment = new OnTripFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(INITIAL_STATUS_EXTRA, status);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.on_trip_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tripStatusTv = view.findViewById(R.id.text_view_trip_status);
        driverNameTv = view.findViewById(R.id.text_view_driver_name);
        plateNumberTv = view.findViewById(R.id.text_view_plate_number);

        FullStatus status = (FullStatus) getArguments().getSerializable(INITIAL_STATUS_EXTRA);

        updateWithStatus(status);
    }

    public void updateWithStatus(FullStatus status) {
        driverNameTv.setText(status.getDriver().getName());
        plateNumberTv.setText(String.valueOf(status.getDriver().getPlateNumber()));
        String tripStatus = status.getTrip().getStatus();
        String tripStatusText = "";
        if (tripStatus.equals(Trip.Status.GOING_TO_PICKUP.name())) {
            tripStatusText = getString(R.string.driver_going_to_pickup);
        } else if (tripStatus.equals(Trip.Status.GOING_TO_DESTINATION.name())) {
            tripStatusText = getString(R.string.going_to_destination);
        } else if (tripStatus.equals(Trip.Status.ARRIVED.name())) {
            tripStatusText = getString(R.string.arrived);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.arrived)
                    .setPositiveButton(R.string.ok, null);
            builder.show();
        }
        tripStatusTv.setText(tripStatusText);


    }
}
