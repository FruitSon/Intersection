package com.dartmouth.cs.intersection;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dartmouth.cs.intersection.service.MobileMsgService;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);


        getActivity().startService(new Intent(getActivity(), MobileMsgService.class));
        MobileMsgService.sendMessage("/connected", "wear msg");

        return view;
    }
}
