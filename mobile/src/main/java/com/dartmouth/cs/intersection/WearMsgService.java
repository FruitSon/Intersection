package com.dartmouth.cs.intersection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.UnsupportedEncodingException;

/**
 * Created by _ReacTor on 16/3/2.
 */
public class WearMsgService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "WEAR_LISTENER";

    private final String TEST_CONNECT_PATH = "/connected";
    private static final String START_ACTIVITY = "/connected";

    private static GoogleApiClient mGoogleApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }
        Log.v(TAG, "Created");
    }


    @Override
    public void onDestroy() {

        Log.v(TAG, "Destroyed");

        if(null != mGoogleApiClient){
            if(mGoogleApiClient.isConnected()){
                mGoogleApiClient.disconnect();
                Log.v(TAG, "GoogleApiClient disconnected");
            }
        }

        super.onDestroy();
    }

    public static void sendMessage( final String path, final String text) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mGoogleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {


        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);


        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Wear received", "received");
                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_WORLD_READABLE);
                String user_id = sharedPreferences.getString("user_id", "-1");

                if(messageEvent.getPath().equals("/pairopen")){
                    //set vibrate

                        String vibrateurl =
                                "http://intersectionserver-1232.appspot.com/admin/set_vibrate/" + user_id + "/1";

                        StringRequest req = new StringRequest(
                                Request.Method.GET, vibrateurl,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                System.out.println(error);
                            }
                        });
                        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);

                }


                if (messageEvent.getPath().equalsIgnoreCase(TEST_CONNECT_PATH)) {

                    byte[] bb = messageEvent.getData();
                    try {
                        Log.d("Wear received", new String(bb, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                    /*String user_id = null;
                    try {
                        user_id = new String(bb, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    WearMsgService.sendMessage("/received", user_id);*/
                }
            }
        }).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Global.GACConnected = true;

        Intent intent = new Intent(Global.GAC_BROADCAST_FILTER);
        sendBroadcast(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
