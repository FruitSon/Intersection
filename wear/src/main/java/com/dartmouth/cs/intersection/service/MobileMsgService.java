package com.dartmouth.cs.intersection.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.dartmouth.cs.intersection.FeaturesActivity;
import com.dartmouth.cs.intersection.Global;
import com.dartmouth.cs.intersection.R;
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
 * Created by _ReacTor on 16/2/28.
 */
public class MobileMsgService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "WEAR_LISTENER";

    private static GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "Created");

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().equalsIgnoreCase(Global.FB_CONNECTED)){
                    //startActivity(new Intent(getApplicationContext(), FeaturesActivity.class));
                    Intent intent = new Intent(Global.GAC_BROADCAST_FILTER);
                    intent.putExtra("step", 1);
                    sendBroadcast(intent);
                } else if (messageEvent.getPath().equalsIgnoreCase(Global.VIBRATE)) {



                    int notificationId = 001;
                    String EXTRA_EVENT_ID = "1";
                    int eventId = 2;
                    byte[] bb = messageEvent.getData();
                    String eventTitle = "";
                    try {
                        eventTitle =  new String(bb, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String eventLocation = "Hello";
                    // Build intent for notification content
                    Intent viewIntent = new Intent(getApplicationContext(), FeaturesActivity.class);
                    viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
                    PendingIntent viewPendingIntent =
                            PendingIntent.getActivity(getApplicationContext(), 0, viewIntent, 0);

                    NotificationCompat.Builder notificationBuilder =
                            new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_light_normal)
                                    .setContentTitle(eventTitle)
                                    .setContentText(eventLocation)
                                    .setContentIntent(viewPendingIntent)
                                    .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});;

                    // Get an instance of the NotificationManager service
                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(getApplicationContext());

                    // Build the notification and issues it with notification manager.
                    notificationManager.notify(notificationId, notificationBuilder.build());
                }else{
                    SharedPreferences preferences = getSharedPreferences("settings", 0);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("SettingSteps", 1);
                    editor.commit();

                    startActivity(new Intent(getApplicationContext(), FeaturesActivity.class));

                    byte[] bb = messageEvent.getData();
                    String user_id = null;
                    try {
                        user_id = new String(bb, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    MobileMsgService.sendMessage("/received", user_id);
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
