package com.dartmouth.cs.intersection;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.wearable.Asset;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;


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
                        URL photo_url = null;
                        try {
                            JSONObject result = new JSONObject(response);
                            is_matched = result.get("is_matched").toString();
                            if(is_matched == "true") {
                                name = result.get("name").toString();
                                photo_url = new URL(result.get("photo_url").toString());
                                System.out.println("photourl" + photo_url);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //send msg to watch if is matched
                        if(is_matched == "true") {

                            //// TODO: 3/4/16
                            //make the watch vibrate and show name of the matched person
                            WearMsgService.sendMessage("/vibrate", name);

                            // send other information of matched people
                            WearMsgService.sendMessage("/vibrate", name);

                            // send photo
                            if(photo_url!= null){
                                try {
                                    Bitmap photo = BitmapFactory.decodeStream(photo_url.openConnection().getInputStream());
                                    Asset asset = createAssetFromBitmap(photo);
                                    WearMsgService.sendAssets("/image",asset);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }


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

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }
}