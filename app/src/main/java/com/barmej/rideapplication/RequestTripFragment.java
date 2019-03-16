package com.barmej.rideapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.barmej.rideapplication.domain.model.FullStatus;
import com.barmej.rideapplication.domain.model.Rider;

public class RequestTripFragment extends Fragment {
    private static final String INITIAL_STATUS_EXTRA = "INITIAL_STATUS_EXTRA";

    private View pinView;
    private Button selectDestinationButton;
    private Button requestTripButton;
    private Button selectPickUpLocationButton;
    private View findingDriverLoadingRL;
    private RequestTripCommunicationInterface requestTripActionDelegates;


    public static RequestTripFragment getInstance(FullStatus status) {
        RequestTripFragment fragment = new RequestTripFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(INITIAL_STATUS_EXTRA,status);
        fragment.setArguments(arguments);
        return  fragment;    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.request_trip_fragment,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pinView = view.findViewById(R.id.image_view_location_pin);
        selectDestinationButton = view.findViewById(R.id.button_select_destination);
        requestTripButton = view.findViewById(R.id.button_request_trip);
        selectPickUpLocationButton = view.findViewById(R.id.button_select_pickup);
        findingDriverLoadingRL = view.findViewById(R.id.relative_layout_finding_driver);
        selectDestinationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDestination();
            }
        });
        requestTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestTrip();
            }
        });
        selectPickUpLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPickUpLocation();
            }
        });
        FullStatus status= (FullStatus) getArguments().getSerializable(INITIAL_STATUS_EXTRA);
        updateWithStatus(status);

    }

    private void selectPickUpLocation() {
        if(requestTripActionDelegates!=null&& requestTripActionDelegates.setPickUp()){

            hideAllViews();
            pinView.setVisibility(View.VISIBLE);
            selectDestinationButton.setVisibility(View.VISIBLE);
        }

    }

    private void selectDestination() {
        if(requestTripActionDelegates != null && requestTripActionDelegates.setDestination()){
            hideAllViews();
            pinView.setVisibility(View.VISIBLE);
            requestTripButton.setVisibility(View.VISIBLE);
        }


    }

    public void requestTrip(){
        if(requestTripActionDelegates != null){
            requestTripActionDelegates.requestTrip();
        }


    }

    private void hideAllViews(){
        findingDriverLoadingRL.setVisibility(View.GONE);
        pinView.setVisibility(View.GONE);
        requestTripButton.setVisibility(View.GONE);
        selectDestinationButton.setVisibility(View.GONE);
        selectPickUpLocationButton.setVisibility(View.GONE);
    }

    public void updateWithStatus(FullStatus status) {
        String riderStatus = status.getRider().getStatus();

        if(riderStatus.equals(Rider.Status.FREE.name())){
            showSelectPickUp();
        }else if(riderStatus.equals(Rider.Status.REQUESTING_TRIP.name())){
            showRequesting();
        }else if (riderStatus.equals(Rider.Status.REQUEST_FAILED.name())){
            showNoAvailableDriversMessage();
            showSelectPickUp();
        }
    }

    private void showNoAvailableDriversMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage( R.string.no_available_drivers);
        builder.setPositiveButton(R.string.ok,null);
        builder.show();
    }

    private void showRequesting() {
        hideAllViews();
        findingDriverLoadingRL.setVisibility(View.VISIBLE);
    }

    private void showSelectPickUp() {
        hideAllViews();
        selectPickUpLocationButton.setVisibility(View.VISIBLE);
        pinView.setVisibility(View.VISIBLE);
    }

    public void setActionDelegates(RequestTripCommunicationInterface requestTripActionDelegates) {
        this.requestTripActionDelegates = requestTripActionDelegates;
    }
}
