package com.dartmouth.cs.intersection;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class LikeOrNotActivity extends WearableActivity {

    private ArrayList<Integer> choosedFeatures = new ArrayList<>();

    private SharedPreferences preferences;

    private ImageButton likeBtn;
    private RadarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_or_not);

        preferences = getSharedPreferences("settings", 0);
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

        mChart = (RadarChart) findViewById(R.id.chart);

        mChart.setDescription("");

        MobileMsgService.sendMessage(Global.REQ_SCORE, "3");
        // set the marker to the chart
        setData();

        mChart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(9f);


        likeBtn = (ImageButton) findViewById(R.id.button_like);

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Data
                MobileMsgService.sendMessage(Global.GET_SCORE, "");

                SharedPreferences preferences = getSharedPreferences("settings", 0);

                int settingStep = preferences.getInt("SettingSteps", 0);
                if (settingStep < 4) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("SettingSteps", 4);
                    editor.commit();
                    startActivity(new Intent(LikeOrNotActivity.this, LauncherActivity.class));
                    finish();
                }

                if(settingStep == 4){
                    finish();
                }
            }
        });
    }

    private void setData() {

        float mult = 150;
        int cnt = 5;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < cnt; i++) {
            yVals1.add(new Entry((float) (Math.random() * mult) + mult / 2, i));
        }

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < cnt; i++)
            xVals.add(Global.ALL_FEATURES[choosedFeatures.get(i)]);

        RadarDataSet set1 = new RadarDataSet(yVals1, "Set 1");
        set1.setColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        set1.setFillColor(ColorTemplate.VORDIPLOM_COLORS[0]);
        set1.setDrawFilled(true);
        set1.setLineWidth(2f);


        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);

        RadarData data = new RadarData(xVals, sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);

        mChart.setData(data);
        mChart.getYAxis().setEnabled(!mChart.getYAxis().isEnabled());

        mChart.invalidate();
    }
}
