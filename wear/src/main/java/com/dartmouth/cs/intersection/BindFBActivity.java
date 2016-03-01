package com.dartmouth.cs.intersection;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class BindFBActivity extends WearableActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks{
    private final String WEAR_MESSAGE_PATH = "/connected";

    private GoogleApiClient mApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bindfb);

        initGoogleApiClient();

        /*SharedPreferences preferences = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("SettingSteps", 1);
        editor.commit();*/
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Wearable.MessageApi.addListener( mApiClient, this );
        sendMessage("/connected", "wear msg");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH)) {
                    byte[] bb = messageEvent.getData();
                    try {
                        Log.d("Wear received", new String(bb, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }else{
                    byte[] bb = messageEvent.getData();
                    try {
                        Log.d("UserID received", new String(bb, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void sendMessage( final String path, final String text ) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes() ).await();
                }

                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();
    }


}
