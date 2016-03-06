package com.dartmouth.cs.intersection;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

public class GPSService extends Service implements LocationListener{

    private Location finalLocation = null;
    private Location curLocation = null;
    private Location location_g = null;
    private Location location_n = null;

    private double latitude;
    private double longitude;
    private static final int INTERVAL = 1000;
    private static final float DISTANCE = 10f;

    private LocationManager lm = null;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;


    public GPSService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        curLocation = getLocation();

        System.out.println("location:"+curLocation.toString());

        //send to server
        longitude = curLocation.getLongitude();
        latitude = curLocation.getLatitude();

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");

        String GPSurl = "http://intersectionserver-1232.appspot.com/upload_gps/"+user_id+"/"+longitude+","+latitude;
        StringRequest gpsreq = new StringRequest(Request.Method.GET, GPSurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(gpsreq);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        curLocation = getLocation();
        System.out.println("location:"+curLocation.toString());

        //send to server
        longitude = curLocation.getLongitude();
        latitude = curLocation.getLatitude();

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");

        String GPSurl = "http://intersectionserver-1232.appspot.com/upload_gps/"+user_id+"/"+latitude+","+longitude;
        StringRequest gpsreq = new StringRequest(Request.Method.GET, GPSurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(gpsreq);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopGPS();
    }


    public Location getLocation(){
        try {
            lm = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if(!isNetworkEnabled && !isGPSEnabled){
                Log.e("error:","open gps or network");
            }else{
                if(isGPSEnabled){
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            INTERVAL, DISTANCE, this);
                    Log.d("GPS", "GPS");
                    location_g = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

                if(isNetworkEnabled){
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            INTERVAL, DISTANCE, this);
                    Log.d("Network", "Network");
                    location_n = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        if(location_g!=null && location_n==null) finalLocation = location_g;
        else if(location_n!=null && location_g==null) finalLocation = location_n;
        else if(location_n!=null && location_g!=null) finalLocation = location_n;

        return finalLocation;
    }


    public void stopGPS(){
        if(lm != null){
            lm.removeUpdates(GPSService.this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("onStatusChanged", provider + status);

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("provider enabled", provider);

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e("provider disabled", provider);

    }

}