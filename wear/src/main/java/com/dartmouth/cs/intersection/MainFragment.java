package com.dartmouth.cs.intersection;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.dartmouth.cs.intersection.service.MobileMsgService;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        Switch sw = (Switch) view.findViewById(R.id.mode_switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Global.GACConnected) {
                        MobileMsgService.sendMessage("/pairopen", "on");
                    }
                }
            }
        });


        return view;
    }
}
