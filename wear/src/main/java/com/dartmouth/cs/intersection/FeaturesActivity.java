package com.dartmouth.cs.intersection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class FeaturesActivity extends WearableActivity {
    private static int selectedFeatures = 0;

    private Button confirmBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        confirmBtn = (Button) findViewById(R.id.button_features_confirm);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("settings", 0);

                int settingStep = preferences.getInt("SettingSteps", 0);
                if(settingStep < 2) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("SettingSteps", 2);
                    editor.commit();
                    startActivity(new Intent(FeaturesActivity.this, PreferencesActivity.class));
                    finish();
                }

                if(settingStep == 4){
                    finish();
                }
            }
        });
    }

    public void OnCheckBoxClicked(View v){
        CheckBox checkBox = (CheckBox) v;
        if(!checkBox.isChecked()){
            selectedFeatures --;
            return;
        }

        if(selectedFeatures < 3) {
            selectedFeatures++;
        }else{
            Toast.makeText(this, "Limit Reached", Toast.LENGTH_LONG);
            checkBox.toggle();
        }
    }
}
