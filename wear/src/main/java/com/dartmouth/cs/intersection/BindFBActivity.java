package com.dartmouth.cs.intersection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class BindFBActivity extends WearableActivity /*implements GoogleApiClient.ConnectionCallbacks*/{
    private final String WEAR_MESSAGE_PATH = "/connected";

    private GoogleApiClient mApiClient;
    private Context mContext;
    private int first_time = 0;

    private GACConnectedReceiver gacConnReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bindfb);
        mContext = this;

        gacConnReceiver = new GACConnectedReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Global.GAC_BROADCAST_FILTER);
        registerReceiver(gacConnReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gacConnReceiver);
    }

    public class GACConnectedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if(first_time != 1) {
                first_time = 1;
                startActivity(new Intent(BindFBActivity.this, FeaturesActivity.class));
                finish();
            }
        }
    }
}
