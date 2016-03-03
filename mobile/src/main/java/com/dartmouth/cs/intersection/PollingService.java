package com.dartmouth.cs.intersection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class PollingService extends Service {

    private final Context mContext;

    public PollingService(Context context) {
        this.mContext = context;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        RequestQueue rq = Volley.newRequestQueue(getApplicationContext());

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");

        String Pollingurl = "http://intersectionserver-1232.appspot.com/is_matched/"+user_id;
        StringRequest matchreq = new StringRequest(Request.Method.GET, Pollingurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        //// TODO: 3/2/16  send message to watch, if...else...
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        rq.add(matchreq);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
