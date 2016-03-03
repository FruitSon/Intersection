package com.dartmouth.cs.intersection;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class AlarmReceiver extends BroadcastReceiver implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

//    private boolean gps_enabled = false;
//    private boolean network_enabled = false;
//    private Location gps_location = null, network_location = null, final_location = null;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;
    // Stores the current instantiation of the location client in this object
    private GoogleApiClient mGoogleApiClient;
    private NotificationManager mNotificationManager;
    // service started flag
    private boolean mIsStarted;

    @Override
    public void onReceive(Context context, Intent intent) {

        mIsStarted = false;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        uploadGPS(context);

    }

    private void uploadGPS(Context context) {
        getLocation();

        //upload GPS
    }

    private Location getLocation(){


        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e("onConnected", "onConnected");
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
