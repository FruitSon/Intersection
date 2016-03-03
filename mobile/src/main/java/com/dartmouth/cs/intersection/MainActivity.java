package com.dartmouth.cs.intersection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private boolean userSkippedLogin = false;
    private AccessTokenTracker mAccessTokenTracker;

    private MainFragment mainPage;

    protected LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken;
    private String user_id = "";
    private final String WEAR_MESSAGE_PATH = "/connected";


    private Scheduler GPSscheduler;
    private Scheduler POLLINGscheduler;


    //Mobile - wear connection
    private GoogleApiClient mApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);


        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        List<String> permissionNeeds = Arrays.asList("user_about_me", "user_actions.books",
                "user_actions.music", "user_education_history", "user_games_activity",
                "user_hometown", "user_location", "user_tagged_places", "user_work_history",
                "user_photos", "public_profile");
        mLoginButton.setReadPermissions(permissionNeeds);

        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                //initialize userINFO
                final JSONObject userINFO = new JSONObject();

                try {
                    userINFO.put("FacebookID","");
                    userINFO.put("AccessToken","");
                    userINFO.put("Installed App",new JSONArray());
                    userINFO.put("photo URL","");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(userINFO);

                //get accessToken
                mAccessToken = AccessToken.getCurrentAccessToken();
                final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

//                //Save access Token and required information in .xml file
//                SharedPreferences mPreferences = getSharedPreferences("UserInfo", Context.MODE_WORLD_READABLE);
//                SharedPreferences.Editor mEditor = mPreferences.edit();
//
//                mEditor.putString("AccessToken", mAccessToken.getToken());
//                mEditor.putString("LastRefresh", mAccessToken.getLastRefresh().toString());
//                mEditor.commit();


                //get a list of installed apps
                // TODO: 2/28/16 get app's category,http://wheredatapp.com/
                final PackageManager pm = getPackageManager();

                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                final ArrayList<String> appList = new ArrayList<String>();
                JSONArray packagelist = new JSONArray();

                for (ApplicationInfo p : packages) {
                    if ((p.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                        //packageName
                        packagelist.put(p.packageName);
                        //ApplicationName
                        appList.add(pm.getApplicationLabel(p).toString());
                    }
                }

                JSONObject packs = new JSONObject();
                try {
                    packs.put("packages",packagelist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println(packs);

//                //send it to http://api.wheredatapp.com/data
                String key = "438e3a19ff29c8df9fa44e3381d29e347460ca66bba8c46846739925";
                String appurl = "http://api.wheredatapp.com/data?key="+key;
                JsonObjectRequest appreq = new JsonObjectRequest(Request.Method.POST, appurl, packs,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                System.out.println(response);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.e(error.toString());
                    }
                });

                queue.add(appreq);
//
//
//                // Instantiate the RequestQueue.
//                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//                String url = "http://intersectionserver-1232.appspot.com/register";
//
//                // Request a string response from the provided URL.
//                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//                                // Display the first 500 characters of the response string.
//                                System.out.println(response);
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                    }
//                });
//
//                // Add the request to the RequestQueue.
//                queue.add(stringRequest);

                try {
                    userINFO.put("FacebookID", mAccessToken.getUserId());
                    userINFO.put("AccessToken", mAccessToken.getToken());
                    userINFO.put("Installed App", appList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //get photo url
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/{user-id}/picture",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                if (response != null) {
                                    try {
                                        String imgUrl = "https://graph.facebook.com/"
                                                + mAccessToken.getUserId() + "/picture?type=large";
                                        userINFO.put("photo URL", imgUrl);
                                        System.out.println("url"+response.toString());

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                GraphRequest requestMe = GraphRequest.newMeRequest(
                                        mAccessToken,
                                        new GraphRequest.GraphJSONObjectCallback() {
                                            @Override
                                            public void onCompleted(
                                                    JSONObject object,
                                                    GraphResponse response) {

                                                try {
                                                    if( !object.has("gender")) object.put("gender", "") ;
                                                    if( !object.has("tagged_places")) object.put("tagged_places", new JSONArray()) ;
                                                    if( !object.has("music")) object.put("music", new JSONObject().put("data",new JSONArray())) ;
                                                    if( !object.has("books")) object.put("books", new JSONObject().put("data",new JSONArray())) ;
                                                    if( !object.has("hometown")) object.put("hometown", new JSONObject()) ;
                                                    if( !object.has("education")) object.put("education", new JSONArray()) ;
                                                    if( !object.has("work")) object.put("work", new JSONArray()) ;
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                System.out.println(object);
                                                System.out.println("Feedback from fb"+response.toString());


                                                try {
                                                    userINFO.put("FBinfo", object);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                System.out.println("FBinfo"+object.toString());
                                                System.out.println("userINFO"+userINFO.toString());

                                                String serverurl =
                                                        "http://intersectionserver-1232.appspot.com/register";
                                                JsonObjectRequest req = new JsonObjectRequest(
                                                        Request.Method.POST, serverurl, userINFO,
                                                        new Response.Listener<JSONObject>() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {
                                                                System.out.println(response.toString());
                                                                try {
                                                                    user_id = response.get("user_id").toString();
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                                //Save user_id in .xml file
                                                                SharedPreferences mPreferences
                                                                        = getSharedPreferences
                                                                        ("UserInfo", Context.MODE_WORLD_READABLE);
                                                                SharedPreferences.Editor mEditor
                                                                        = mPreferences.edit();

                                                                mEditor.putString("user_id", user_id);
                                                                mEditor.commit();

                                                                System.out.println(user_id);
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        System.out.println(error);
                                                    }
                                                });
                                                queue.add(req);


                                                //Send Message to wearable devices
                                                initGoogleApiClient();

                                                //start update GPS service to server
                                                GPSscheduler.setSchedule(getApplicationContext());

                                                //start querying server, every 10 s
                                                POLLINGscheduler.setSchedule(getApplicationContext(),10000);


                                            }
                                        });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "name,gender,location,hometown," +
                                        "education,work,tagged_places,music,books");
                                requestMe.setParameters(parameters);
                                requestMe.executeAsync();
                            }
                        }
                ).executeAsync();

            }

            @Override
            public void onCancel() {
                System.out.println("onCancel");

            }

            @Override
            public void onError(FacebookException error) {
                Log.v("LoginActivity", error.getCause().toString());
            }

        });

//        //read access token from storage
//        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
//        String recordedAccessToken = sharedPreferences.getString("AccessToken", "-1");

    }

    @Override
    protected void onResume(){
        super.onResume();

        //TODO: 2/29/16 check if the first time of login

//        //read access token from storage
//        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
//        String recordedAccessToken = sharedPreferences.getString("AccessToken", "-1");
//
//        System.out.println(recordedAccessToken);
//
//        //check whether it's the first time of login, if not, show main page
//        if(recordedAccessToken!="-1"){
//            showMoreInfo();
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        Wearable.MessageApi.addListener(mApiClient, this);
        sendMessage("/loginsuccess", user_id);
        System.out.println("connected" + user_id);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("12313123", i + "");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("test", "Failed to connect to Google API Client");
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
                }
            }
        });
    }
}

