package com.dartmouth.cs.intersection;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity /*implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/{

    private MainFragment mainPage;

    protected LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken;
    private String user_id = "";
    private final String WEAR_MESSAGE_PATH = "/connected";
    private GACConnectedReceiver gacConnReceiver;

    private Scheduler GPSscheduler;
    private Scheduler POLLINGscheduler;

    //Mobile - wear connection
    private GoogleApiClient mApiClient;

    //



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //initGoogleApiClient();
        startService(new Intent(this, WearMsgService.class));
        gacConnReceiver = new GACConnectedReceiver();

        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);

        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        List<String> permissionNeeds = Arrays.asList("user_about_me", "user_actions.books",
                "user_actions.music", "user_education_history", "user_games_activity",
                "user_hometown", "user_location", "user_tagged_places", "user_work_history",
                "user_photos", "public_profile","user_likes");
        mLoginButton.setReadPermissions(permissionNeeds);

        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                //initialize userINFO
                final JSONObject userINFO = new JSONObject();

                try {
                    userINFO.put("FacebookID","");
                    userINFO.put("AccessToken","");
//                    userINFO.put("Installed App",new JSONArray());
                    userINFO.put("app",new JSONArray());
                    userINFO.put("photo URL","");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //get accessToken
                mAccessToken = AccessToken.getCurrentAccessToken();

                //get a list of installed apps
                final PackageManager pm = getPackageManager();
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                final ArrayList<String> appList = new ArrayList<>();
                JSONArray packagelist = new JSONArray();

                for (ApplicationInfo p : packages) {
                    if ((p.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                        //packageName
                        packagelist.put(p.packageName);
                        //ApplicationName
                        appList.add(pm.getApplicationLabel(p).toString());
                    }
                }

                final JSONObject packs = new JSONObject();
                try {
                    packs.put("packages",packagelist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


//                //send it to http://api.wheredatapp.com/data
//                String key = "438e3a19ff29c8df9fa44e3381d29e347460ca66bba8c46846739925";
//                String appurl = "http://api.wheredatapp.com/data?key="+key;
//                JsonObjectRequest appreq = new JsonObjectRequest(Request.Method.POST, appurl, packs,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                System.out.println(response);
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        VolleyLog.e(error.toString());
//                    }
//                });
//
//                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(appreq);

                try {
                    userINFO.put("FacebookID", mAccessToken.getUserId());
                    userINFO.put("AccessToken", mAccessToken.getToken());
//                    userINFO.put("Installed App", appList);

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
                                                + mAccessToken.getUserId() + "/picture?type=small";
                                        userINFO.put("photo URL", imgUrl);


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
                                                    if( !object.has("gender"))
                                                        object.put("gender", "") ;
                                                    if( !object.has("tagged_places"))
                                                        object.put("tagged_places", new JSONArray()) ;
                                                    if( !object.has("music"))
                                                        object.put("music", new JSONObject().put("data",new JSONArray())) ;
                                                    if( !object.has("books"))
                                                        object.put("books", new JSONObject().put("data",new JSONArray())) ;
                                                    if( !object.has("hometown"))
                                                        object.put("hometown", new JSONObject()) ;
                                                    if( !object.has("education"))
                                                        object.put("education", new JSONArray()) ;
                                                    if( !object.has("work"))
                                                        object.put("work", new JSONArray()) ;
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                try {
                                                    userINFO.put("FBinfo", object);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

//                                                //test
//                                                System.out.println("Feedback from fb"+response.toString());
//                                                System.out.println("FBinfo"+object.toString());


                                                //get app types
                                                String appurl = "http://api.wheredatapp.com/data?key="+Global.APP_QUERY_KEY;
                                                JsonObjectRequest appreq = new JsonObjectRequest(Request.Method.POST, appurl, packs,
                                                        new Response.Listener<JSONObject>() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {

//                                                                JSONArray jsonArray = null;
//                                                                try {
//                                                                    jsonArray = response.getJSONArray("apps");
//                                                                } catch (JSONException e) {
//                                                                    e.printStackTrace();
//                                                                }


                                                                try {
                                                                    userINFO.put("app",response);
                                                                } catch (JSONException e) {
                                                                    e.printStackTrace();
                                                                }

                                                                //test
//                                                                System.out.println("userINFO"+userINFO.toString());

                                                                //send all user info to server for register
//                                                                String serverurl =
//                                                                        "http://intersectionserver-1232.appspot.com/register";
                                                                String serverurl = "http://129.170.212.221:8080/register";
                                                                JsonObjectRequest req = new JsonObjectRequest(
                                                                        Request.Method.POST, serverurl, userINFO,
                                                                        new Response.Listener<JSONObject>() {
                                                                            @Override
                                                                            public void onResponse(JSONObject response) {
                                                                                System.out.println(response.toString());

                                                                                //record id generated by server
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

                                                                                //Send Message to wearable devices
                                                                                if(Global.GACConnected) {
                                                                                    WearMsgService.sendMessage("/fbconnected", "mobile msg");
                                                                                }

                                                                                //start update GPS service to server
                                                                                GPSscheduler.setSchedule(getApplicationContext());

                                                                                //start querying server, every 10 s
                                                                                POLLINGscheduler.setSchedule(getApplicationContext(),60000);

                                                                            }
                                                                        }, new Response.ErrorListener() {
                                                                    @Override
                                                                    public void onErrorResponse(VolleyError error) {
                                                                        error.printStackTrace();
                                                                    }
                                                                });
                                                                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        VolleyLog.e(error.toString());
                                                    }
                                                });

                                                VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(appreq);
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

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null) WearMsgService.sendMessage("/default","reset");
            }
        };

//        //read access token from storage
//        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
//        String recordedAccessToken = sharedPreferences.getString("AccessToken", "-1");

        IntentFilter intentFilter = new IntentFilter(Global.GAC_BROADCAST_FILTER);
        registerReceiver(gacConnReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gacConnReceiver);

        stopService(new Intent(this, GPSService.class));
        stopService(new Intent(this,PollingService.class));

        /*mApiClient.disconnect();*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public class GACConnectedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            //WearMsgService.sendMessage("/connected", "mobile msg");
        }
    }

}

