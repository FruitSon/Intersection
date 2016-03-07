package com.dartmouth.cs.intersection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;

import com.dartmouth.cs.intersection.service.MobileMsgService;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class LikeOrNotActivity extends WearableActivity {

    private ArrayList<Integer> choosedFeatures = new ArrayList<>();
    private int userCount = 0;
    private JSONArray scoreUsers;
    private ArrayList list2 = new ArrayList<>();

    private SharedPreferences preferences;

    private ImageButton likeBtn;
    private ImageButton dislikeBtn;
    private RadarChart mChart;

    private int settingStep;

    private ScoreReceiver scoreReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_or_not);

        preferences = getSharedPreferences("settings", 0);
        settingStep = preferences.getInt("SettingSteps", 0);
        String[] savedFeatureArr = preferences.getString("Features", "-1").split(",");
        for(int i = 0; i < savedFeatureArr.length; i++){
            choosedFeatures.add(Integer.parseInt(savedFeatureArr[i]));
        }

        while (choosedFeatures.size()<5) {
            int random = (int)(Math.random() * 6 + 1);
            if(choosedFeatures.indexOf(random) == -1){
                choosedFeatures.add(random);
            }
        }

        scoreReceiver = new ScoreReceiver();

        MobileMsgService.sendMessage(Global.REQ_SCORE, "3");

        mChart = (RadarChart) findViewById(R.id.chart);

        mChart.setDescription("");


        mChart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(9f);


        likeBtn = (ImageButton) findViewById(R.id.button_like);
        dislikeBtn = (ImageButton) findViewById(R.id.button_dislike);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData("1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        dislikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendData("0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(Global.REQ_SCORE_RECEIVED);
        registerReceiver(scoreReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(scoreReceiver);
    }

    private void sendData(String s) throws JSONException {
        userCount++;
        String temp = scoreUsers.getJSONObject(userCount).get("user_id").toString()+"/"+s.toString();
        System.out.println(temp);
        MobileMsgService.sendMessage(Global.GET_SCORE, temp);

        if(userCount%3==2){
            if(settingStep<4){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("SettingSteps", 4);
                editor.commit();
                startActivity(new Intent(LikeOrNotActivity.this, LauncherActivity.class));
                finish();
            }
            MobileMsgService.sendMessage(Global.REQ_SCORE, "3");
        }else{
            setData((JSONObject)scoreUsers.get(userCount%3));
        }

    }

    private void setData(JSONObject scoreData) throws JSONException {
        int cnt = 5;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        ArrayList list = new ArrayList<>();

        Object obj = scoreData.get("similarities");
        JSONArray jsonArray = (JSONArray)obj;

        if (jsonArray != null) {
            int len = jsonArray.length();
            for (int i=0;i<len;i++){
                list.add(jsonArray.get(i));
            }
        }
        for (int i = 0; i < cnt; i++) {
            yVals1.add(new Entry((int)list.get(choosedFeatures.get(i)), i));
        }

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < cnt; i++)
            xVals.add(Global.ALL_FEATURES[choosedFeatures.get(i)]);

        RadarDataSet set1 = new RadarDataSet(yVals1, "Set 1");
        set1.setColor(Color.rgb(0, 187, 184));
        set1.setFillColor(Color.rgb(0, 187, 184));
        set1.setDrawFilled(true);
        set1.setLineWidth(2f);


        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);

        RadarData data = new RadarData(xVals, sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);

        mChart.setData(data);
        mChart.getYAxis().setEnabled(false);

        mChart.invalidate();
    }

    public class ScoreReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            String scores = intent.getStringExtra("scores");
            try {
                JSONArray users = new JSONArray(scores);
                scoreUsers = users;
                setData((JSONObject)scoreUsers.get(userCount%3));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
