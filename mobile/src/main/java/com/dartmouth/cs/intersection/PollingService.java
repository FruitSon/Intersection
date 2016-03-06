package com.dartmouth.cs.intersection;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

    private String is_matched = "false";
    private String name = "";
    private URL photo_url = null;
    private Bitmap photo = null;
    private Bundle bundle = new Bundle();
    private static final int MSG_SUCCESS = 0;
    private static final int MSG_FAIL = 1;

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

    static private Handler mHandler = new Handler() {
        public void handleMessage (Message msg) {
            switch(msg.what) {
                case MSG_SUCCESS:
                    Bundle received = (Bundle) msg.obj;
                    Bitmap image = received.getParcelable("image");
                    String n = received.getString("name");
                    Asset asset = createAssetFromBitmap(image);
                    WearMsgService.sendMessage("/vibrate",n);
                    WearMsgService.sendAssets("/image", asset);

                    break;

                //没有图片- -
                case MSG_FAIL:
//                  WearMsgService.sendMessage("/vibrate", name);
                    break;
            }
        }
    };

    public void polling(String id){
        String Pollingurl = "http://intersectionserver-1232.appspot.com/is_matched/"+id;
        StringRequest matchreq = new StringRequest(Request.Method.GET, Pollingurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("server response"+response);
                        try {
                            JSONObject result = new JSONObject(response);
                            is_matched = result.get("is_matched").toString();
                            if(is_matched == "true") {
                                name = result.get("name").toString();
                                photo_url = new URL(result.get("photo_url").toString());

                                //send msg to watch if is matched
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(photo_url!= null){
                                            try {
                                                photo = BitmapFactory.decodeStream(photo_url.openConnection().getInputStream());
                                                bundle.putParcelable("image",photo);
                                                bundle.putString("name",name);
                                                mHandler.obtainMessage(MSG_SUCCESS,bundle).sendToTarget();
                                            } catch (IOException e) {
                                                mHandler.obtainMessage(MSG_FAIL).sendToTarget();
                                                e.printStackTrace();
                                            }

                                        }
                                    }
                                }).start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }



                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
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