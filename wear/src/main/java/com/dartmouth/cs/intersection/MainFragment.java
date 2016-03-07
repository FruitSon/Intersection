package com.dartmouth.cs.intersection;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.dartmouth.cs.intersection.service.MobileMsgService;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        /*TextView num = (TextView) view.findViewById(R.id.number_of_person);
        SharedPreferences sp = getActivity().getSharedPreferences("settings", 0);
        num.setText(sp.getInt("matched", 1));*/
        final TextView t1 = (TextView) view.findViewById(R.id.text_template1);
        final TextView t2 = (TextView) view.findViewById(R.id.prs);

        Switch sw = (Switch) view.findViewById(R.id.mode_switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(Global.GACConnected) {
                        t1.setText("Intersection is now");
                        t2.setText("searching");
                        MobileMsgService.sendMessage("/pairopen", "on");

                    }
                }
                else {
                    if(Global.GACConnected) {
                        t1.setText("Please turn on pair modes");
                        t2.setText("to find you friends");
                        MobileMsgService.sendMessage("/pairopen", "off");
                    }
                }
            }
        });


        return view;
    }
}
