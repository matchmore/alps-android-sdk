package io.matchmore.sdk.examplejava;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.matchmore.config.SdkConfigTest;
import io.matchmore.sdk.Matchmore;
import io.matchmore.sdk.MatchmoreSDK;
import io.matchmore.sdk.api.models.Publication;
import io.matchmore.sdk.api.models.Subscription;
import kotlin.Unit;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request Location permission
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        // Configuration of api key/world id
        if (!Matchmore.isConfigured()) {
            Matchmore.config(this, SdkConfigTest.API_KEY, true);
        }

        // Getting instance. It's static variable. It's possible to have only one instance of matchmore.
        MatchmoreSDK matchmore = Matchmore.getInstance();

        // Creating main device.
        matchmore.startUsingMainDevice(device -> {
            Publication publication = new Publication("Test Topic", 20d, 100000d);
            matchmore.createPublicationForMainDevice(publication, createdPublication -> {
                Log.d("JavaExample", createdPublication.getId());
                return Unit.INSTANCE; // `return Unit.INSTANCE;` is important (b/c kotlin vs java lambdas differ in implementation)
            }, e -> {
                Log.d("JavaExample", e.getMessage());
                return Unit.INSTANCE;
            });

            Subscription subscription = new Subscription("Test Topic", 20d, 100000d, "");
            matchmore.createSubscriptionForMainDevice(subscription, createdSubscription -> {
                Log.d("JavaExample", createdSubscription.getId());
                return Unit.INSTANCE;
            }, e -> {
                Log.d("JavaExample", e.getMessage());
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        }, e -> {
            Log.d("JavaExample", e.getMessage());
            return Unit.INSTANCE;
        });

        // Start getting matches
        matchmore.getMatchMonitor().addOnMatchListener((matches, device) -> {
            Log.d("JavaExample", device.getId());
            return Unit.INSTANCE;
        });
        matchmore.getMatchMonitor().startPollingMatches();

        // Start updating location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            matchmore.startUpdatingLocation();
        }
    }
}
