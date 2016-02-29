package com.dartmouth.cs.intersection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

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
                    Log.d("Wear received", messageEvent.getData()+"");
                }
            }
        });
    }


}
