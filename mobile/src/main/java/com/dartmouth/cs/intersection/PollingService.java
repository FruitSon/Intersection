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

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");
        polling(user_id);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_WORLD_READABLE);
        String user_id = sharedPreferences.getString("user_id", "-1");
        polling(user_id);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void polling(String id){
        String Pollingurl = "http://intersectionserver-1232.appspot.com/is_matched/"+id;
        StringRequest matchreq = new StringRequest(Request.Method.GET, Pollingurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("server response"+response);
                        String is_matched = "false";

                        String name = "-1";
                        try {
                            JSONObject result = new JSONObject(response);
                            is_matched = result.get("is_matched").toString();
                            name = result.get("name").toString();
                            System.out.println(is_matched+name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //send msg to watch if is matched
                        if(is_matched == "true") {

                            WearMsgService.sendMessage("/vibrate", name);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(matchreq);
    }
}