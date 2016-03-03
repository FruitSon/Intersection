package com.dartmouth.cs.intersection;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class PollingService extends Service {


    public PollingService( ) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");

        String Pollingurl = "http://intersectionserver-1232.appspot.com/is_matched/"+user_id;
        StringRequest matchreq = new StringRequest(Request.Method.GET, Pollingurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            JSONObject result = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //// TODO: 3/2/16  send message to watch, if...else...

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(matchreq);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");

        String Pollingurl = "http://intersectionserver-1232.appspot.com/is_matched/"+user_id;
        StringRequest matchreq = new StringRequest(Request.Method.GET, Pollingurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try {
                            JSONObject result = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //// TODO: 3/2/16  send message to watch, if...else...

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(matchreq);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
