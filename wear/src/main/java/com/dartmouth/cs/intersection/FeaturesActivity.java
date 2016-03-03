package com.dartmouth.cs.intersection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.dartmouth.cs.intersection.data.FeatureListAdapter;
import com.dartmouth.cs.intersection.service.MobileMsgService;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class FeaturesActivity extends WearableActivity {
    public static int selectedFeatures = 0;
    public static Set featureSet = new HashSet();

    private ListView mListView;
    private FeatureListAdapter listAdapter;
    private Button confirmBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);

        startService(new Intent(this, MobileMsgService.class));
        MobileMsgService.sendMessage("/connected", "wear msg");

        mListView = (ListView) findViewById(R.id.listview_features);
        listAdapter = new FeatureListAdapter(this, 0);

        mListView.setAdapter(listAdapter);

        confirmBtn = (Button) findViewById(R.id.button_features_confirm);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view;
                CheckBox chBox;


                for (int i = 0; i < mListView.getCount(); i++) {
                    view = mListView.getAdapter().getView(i, null, null);
                    chBox = (CheckBox) view.findViewById(R.id.checkbox);//your xml id value for checkBox.
                    if (chBox.isChecked()) {
                        featureSet.add(i);
                    }
                }

                Log.d("Saved Features",featureSet.toString());

                /*SharedPreferences preferences = getSharedPreferences("settings", 0);

                int settingStep = preferences.getInt("SettingSteps", 0);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putStr("Features", featureStr);

                if(settingStep < 2) {
                    editor.putInt("SettingSteps", 2);
                    editor.commit();
                    startActivity(new Intent(FeaturesActivity.this, PreferencesActivity.class));
                    finish();
                }

                if(settingStep == 4){
                    finish();
                }*/
            }
        });
    }

    /*public void OnCheckBoxClicked(View v){
        CheckBox checkBox = (CheckBox) v;
        if(!checkBox.isChecked()){
            selectedFeatures --;
            return;
        }

        if(selectedFeatures < 3) {
            selectedFeatures++;
        }else{
            Toast.makeText(this, "Limit Reached", Toast.LENGTH_LONG).show();
            checkBox.toggle();
        }
    }*/
}
