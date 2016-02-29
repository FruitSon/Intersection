package com.dartmouth.cs.intersection;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private boolean userSkippedLogin = false;
    private AccessTokenTracker mAccessTokenTracker;

    private MainFragment mainPage;

    protected LoginButton mLoginButton;
    private CallbackManager mCallbackManager;
    private AccessToken mAccessToken;
    private boolean isLogged = false;

    //Mobile - wear connection
    private GoogleApiClient mApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_main);


        mLoginButton = (LoginButton) findViewById(R.id.login_button);
        List<String> permissionNeeds = Arrays.asList("user_about_me", "user_actions.books","user_actions.music",
                "user_education_history", "user_games_activity", "user_hometown", "user_location", "user_tagged_places",
                "user_work_history","user_photos", "public_profile");
        mLoginButton.setReadPermissions(permissionNeeds);

        mLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                System.out.println("onSuccess");
                final JSONObject userINFO = new JSONObject();

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
                ArrayList<ApplicationInfo> userInstalled = new ArrayList<ApplicationInfo>();

                final ArrayList<String> appList = new ArrayList<String>();
                ArrayList<String> packagelist = new ArrayList<String>();

                for (ApplicationInfo p : packages) {
                    if((p.flags & ApplicationInfo.FLAG_SYSTEM) != 1)
                    {
                        userInstalled.add(p);
                        packagelist.add(p.packageName);
                        System.out.println(p.packageName);
                        appList.add(pm.getApplicationLabel(p).toString());
                    }
                }

//                //send it to http://api.wheredatapp.com/data
//                String appurl = "http://api.wheredatapp.com/data";
//                JsonObjectRequest req = new JsonObjectRequest(appurl, userINFO,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                System.out.println(response.toString());
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        VolleyLog.e("Error: ", error.getMessage());
//                    }
//                });
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
                    userINFO.put("FacebookID",mAccessToken.getUserId());
                    userINFO.put("AccessToken",mAccessToken.getToken());
                    userINFO.put("Installed App",appList);

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
                                        String imgUrl ="https://graph.facebook.com/"
                                                + mAccessToken.getUserId() + "/picture?type=large";
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
                                                    userINFO.put("FBinfo",object);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                System.out.println(userINFO.toString());

                                                String appurl = "http://intersectionserver-1232.appspot.com/upload_gps/ddddd";
                                                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,appurl, userINFO,
                                                        new Response.Listener<JSONObject>() {
                                                            @Override
                                                            public void onResponse(JSONObject response) {
                                                                System.out.println(response.toString());
                                                            }
                                                        }, new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                        System.out.println(error);
                                                    }
                                                });
                                                queue.add(req);
                                            }
                                        });
                                Bundle parameters = new Bundle();
                                parameters.putString("fields", "name,gender,location,hometown,education,work,tagged_places,music,books");
                                requestMe.setParameters(parameters);
                                requestMe.executeAsync();
                            }
                        }
                ).executeAsync();


//                //batched request of facebook
//                GraphRequestBatch batch = new GraphRequestBatch(
//
//                );
//                batch.addCallback(new GraphRequestBatch.Callback() {
//                    @Override
//                    public void onBatchCompleted(GraphRequestBatch graphRequests) {
//                        System.out.println(graphRequests.toString());
//                    }
//                });
//                batch.executeAsync();


//                JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,appurl, userINFO,
//                        new Response.Listener<JSONObject>() {
//                            @Override
//                            public void onResponse(JSONObject response) {
//                                System.out.println("sss"+response.toString());
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        System.out.println(error);
//                    }
////                });
//
//                queue.add(req);
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

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreInfo();
            }
        });

//        //read access token from storage
//        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
//        String recordedAccessToken = sharedPreferences.getString("AccessToken", "-1");
//
//        //check whether it's the first time of login, if not, show main page

        //Send Message to wearable devices
        initGoogleApiClient();
    }

    @Override
    protected void onResume(){
        super.onResume();

        //read access token from storage
        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo",MODE_PRIVATE);
        String recordedAccessToken = sharedPreferences.getString("AccessToken", "-1");

        System.out.println(recordedAccessToken);

        //check whether it's the first time of login, if not, show main page
        if(recordedAccessToken!="-1"){
            showMoreInfo();
        }

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

    public void showMoreInfo(){
        mainPage = new MainFragment();

        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
//
//        ft.add(R.id.main, mainPage);
//        fm.beginTransaction().show(mainPage).commit();
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
        sendMessage("/connected", "test msg");
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
}

