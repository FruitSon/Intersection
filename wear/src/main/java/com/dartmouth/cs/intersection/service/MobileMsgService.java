package com.dartmouth.cs.intersection.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.dartmouth.cs.intersection.BindFBActivity;
import com.dartmouth.cs.intersection.FeaturesActivity;
import com.dartmouth.cs.intersection.Global;
import com.dartmouth.cs.intersection.MainFragment;
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
import java.nio.charset.StandardCharsets;

/**
 * Created by _ReacTor on 16/2/28.
 */
public class MobileMsgService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "WEAR_LISTENER";

    private static GoogleApiClient mGoogleApiClient;

    private static int matched_people = 0;

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
            String path = messageEvent.getPath();

            @Override
            public void run() {
                switch (messageEvent.getPath()){
                    case Global.RESET:
                        SharedPreferences sp = getSharedPreferences("settings", 0);
                        sp.edit().clear().commit();
                        startActivity(new Intent(getApplication(), BindFBActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        break;
                    case Global.FB_CONNECTED:
                        Intent intent = new Intent(Global.GAC_BROADCAST_FILTER);
                        sendBroadcast(intent);
                        break;

                    case Global.VIBRATE:

                        int notificationId = 001;
                        String EXTRA_EVENT_ID = "1";
                        int eventId = 2;
                        byte[] bb = messageEvent.getData();
                        String eventTitle = "";
                        try {
                            eventTitle = new String(bb, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        String eventLocation = "within 10 meters from you";
                        // Build intent for notification content
                        Intent viewIntent = new Intent(getApplicationContext(), MainFragment.class);
                        viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
                        PendingIntent viewPendingIntent =
                                PendingIntent.getActivity(getApplicationContext(), 0, viewIntent, 0);


                        //// TODO: 3/6/16 有图

                        Bitmap liluyang = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.liluyang);
                        NotificationCompat.BigPictureStyle bigStyle = new NotificationCompat.BigPictureStyle();

                        String name = new String(messageEvent.getData());
                        if(name.equals("Weichen Wang")) {
                            bigStyle.bigPicture(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.wangweichen));
                        }
                        else bigStyle.bigPicture(liluyang);

                        NotificationCompat.Builder notificationBuilder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.icon)
                                        .setContentTitle(eventTitle)
                                        .setContentText(eventLocation)
                                        .setStyle(bigStyle)
                                        .setContentIntent(viewPendingIntent)
                                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000});

                        // Get an instance of the NotificationManager service
                        NotificationManagerCompat notificationManager =
                                NotificationManagerCompat.from(getApplicationContext());
                        notificationManager.notify(notificationId, notificationBuilder.build());


                        break;

                    case Global.INFO_SCORE:
                        String res = new String(messageEvent.getData(), StandardCharsets.UTF_8);

                        Intent scoreIntent = new Intent(Global.REQ_SCORE_RECEIVED);
                        scoreIntent.putExtra("scores", res);
                        sendBroadcast(scoreIntent);
                        break;

                    default:
                        SharedPreferences preferences = getSharedPreferences("settings", 0);
                        SharedPreferences.Editor editor = preferences.edit();

                        editor.putInt("SettingSteps", 1);
                        editor.commit();

                        startActivity(new Intent(getApplicationContext(), FeaturesActivity.class));

                        byte[] b1 = messageEvent.getData();
                        String user_id = null;
                        try {
                            user_id = new String(b1, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        MobileMsgService.sendMessage("/received", user_id);
                        break;
                }

            }
        }).start();
    }


//    @Override
//    public void onDataChanged(DataEventBuffer dataEvents) {
//        super.onDataChanged(dataEvents);
//
//        for (DataEvent event : dataEvents) {
//            if (event.getType() == DataEvent.TYPE_CHANGED &&
//                    event.getDataItem().getUri().getPath().equals("/image")) {
//                PutDataRequest dataRequest = PutDataRequest.createFromDataItem(event.getDataItem());
//                Asset profileAsset = dataRequest.getAsset("image");
//
//                final Bitmap bitmap = loadBitmapFromAsset(profileAsset);
//
//                int seconds = Calendar.getInstance().get(Calendar.SECOND);
//                int pending ;
//                if(seconds<30) pending = 30-seconds;
//                else pending = 60 - seconds;
//                new CountDownTimer(pending*1000, 1000) {
//                    public void onFinish() {
//                        createNotification(bitmap);
//                    }
//
//                    public void onTick(long millisUntilFinished) {
//                    }
//                }.start();
//
//            }
//        }
//    }
//
//    public Bitmap loadBitmapFromAsset(Asset asset) {
//        if (asset == null) {
//            throw new IllegalArgumentException("Asset must be non-null");
//        }
//        if(!mGoogleApiClient.isConnected()){
//            return null;
//        }
//        // convert asset into a file descriptor and block until it's ready
//        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
//                mGoogleApiClient, asset).await().getInputStream();
//
//        if (assetInputStream == null) {
//            Log.w(TAG, "Requested an unknown Asset.");
//            return null;
//        }
//        // decode the stream into a bitmap
//        return BitmapFactory.decodeStream(assetInputStream);
//    }

    //Create a BigPictureStyle notification that displays the image.
//    private Notification createNotification(Bitmap image) {
//        NotificationCompat.BigPictureStyle bigPictureStyle
//                = new NotificationCompat.BigPictureStyle()
//                .bigPicture(image);
//        return new NotificationCompat.Builder(this)
//                .setContentTitle("Image Received")
//                .setContentText("")
//                .setSmallIcon(R.drawable.icon)
//                .setStyle(bigPictureStyle)
//                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
//                .build();
//    }


    @Override
    public void onConnected(Bundle bundle) {
        Global.GACConnected = true;

        /*Intent intent = new Intent(Global.GAC_BROADCAST_FILTER);
        sendBroadcast(intent);*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
