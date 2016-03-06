package com.dartmouth.cs.intersection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.dartmouth.cs.intersection.service.MobileMsgService;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class PreferencesActivity extends WearableActivity {
    private SharedPreferences preferences;

    private Button contiuneBtn;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        preferences = getSharedPreferences("settings", 0);
        int progress = preferences.getInt("Progress", 0);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(10);
        seekBar.setProgress(progress);

        contiuneBtn = (Button) findViewById(R.id.button_preferences_continue);
        
        contiuneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("settings", 0);

                int settingStep = preferences.getInt("SettingSteps", 0);

                //TODO: Preferences
                MobileMsgService.sendMessage(Global.WILLING,
                        seekBar.getProgress()+"");

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("Progress", seekBar.getProgress());
                if(settingStep < 3) {
                    editor.putInt("SettingSteps", 3);
                    startActivity(new Intent(PreferencesActivity.this, LikeOrNotActivity.class));
                    finish();
                }

                if(settingStep == 4){
                    finish();
                }
                editor.commit();
            }
        });
    }
}
