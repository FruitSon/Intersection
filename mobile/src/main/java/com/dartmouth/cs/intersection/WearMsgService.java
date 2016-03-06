package com.dartmouth.cs.intersection;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

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
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(mGoogleApiClient).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), path, text.getBytes() ).await();
                }
            }
        }).start();
    }


    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {


//        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//        v.vibrate(1000);


        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("Wear received", "received");
                SharedPreferences sharedPreferences
                        = getSharedPreferences("UserInfo", MODE_WORLD_READABLE);
                String user_id = sharedPreferences.getString("user_id", "-1");
                String url = null;
                String res = new String(messageEvent.getData(), StandardCharsets.UTF_8);

                switch (messageEvent.getPath()) {
                    case "/pairopen":
                        url = "http://intersectionserver-1232.appspot.com/admin/set_vibrate/"
                                + user_id + "/1";
                        break;
                    case "/logged":
                        url = "http://intersectionserver-1232.appspot.com/admin/set_vibrate/"
                                + user_id + "/1";
                        break;
                    case "/feature":
                        url = "http://intersectionserver-1232.appspot.com/set_features/"
                                + user_id + "/"+ res;
                        break;
                    case "/willing":
                        url = "http://intersectionserver-1232.appspot.com/set_willing/"
                                + user_id + "/"+ res;
                        break;
                    case "/req_score":
                        url = "http://intersectionserver-1232.appspot.com/others_to_grade/"
                                + user_id + "/3";
                        break;
                    //// TODO: 接收打分结果
                    case "/get_score":
                        url = "http://intersectionserver-1232.appspot.com/others_to_grade/"
                                + user_id + "/"+res;

                        break;
                    //// TODO: 上传录音结果
                    case "/update_audio":
                        url = "";
                        break;
                    default:
                        break;
                }

                if(url!=null){

                        StringRequest req = new StringRequest(
                                Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if(messageEvent.getPath().equals("/req_score")){
                                            sendMessage("/info_score",response);
                                        }
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                            }
                        });
                        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
                }
            }
        }).start();
    }


    //send image
    public static void sendAssets(final String path, final Asset asset){
        new Thread(new Runnable() {
            @Override
            public void run() {
                PutDataRequest photorequest = PutDataRequest.create(path);
                photorequest.putAsset("image", asset);
                Wearable.DataApi.putDataItem(mGoogleApiClient, photorequest);
            }
        }).start();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED &&
                    event.getDataItem().getUri().getPath().equals("/image")) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset profileAsset = dataMapItem.getDataMap().getAsset("image");
                final Bitmap bitmap = loadBitmapFromAsset(profileAsset);

                int seconds = Calendar.getInstance().get(Calendar.SECOND);
                int pending ;
                if(seconds<30) pending = 30-seconds;
                else pending = 60 - seconds;
                new CountDownTimer(pending*1000, 1000) {
                    public void onFinish() {
                        createNotification(bitmap);
                    }

                    public void onTick(long millisUntilFinished) {
                    }
                }.start();

            }
        }
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        if(!mGoogleApiClient.isConnected()){
            return null;
        }
        // convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                mGoogleApiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }

    //Create a BigPictureStyle notification that displays the image.
    private Notification createNotification(Bitmap image) {
        NotificationCompat.BigPictureStyle bigPictureStyle
                = new NotificationCompat.BigPictureStyle()
                .bigPicture(image);
        return new NotificationCompat.Builder(this)
                .setContentTitle("Image Received")
                .setContentText("")
                .setSmallIcon(R.drawable.icon)
                .setStyle(bigPictureStyle)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        /*Global.GACConnected = true;

        Intent intent = new Intent(Global.GAC_BROADCAST_FILTER);
        sendBroadcast(intent);*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
