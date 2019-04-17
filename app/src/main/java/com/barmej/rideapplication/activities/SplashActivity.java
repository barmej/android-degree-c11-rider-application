package com.barmej.rideapplication.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.barmej.rideapplication.R;
import com.barmej.rideapplication.callbacks.CallBack;
import com.barmej.rideapplication.domain.TripManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TripManager.getInstance().login(new CallBack() {
            @Override
            public void onComplete(boolean isSuccessful) {
                if (isSuccessful) {
                    startActivity(HomeActivity.getStartIntent(SplashActivity.this));
                    finish();

                } else {
                    Toast.makeText(SplashActivity.this, R.string.error_loading_data, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
