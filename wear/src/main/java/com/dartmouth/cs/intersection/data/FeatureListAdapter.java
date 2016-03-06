package com.dartmouth.cs.intersection.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;


import com.dartmouth.cs.intersection.FeaturesActivity;
import com.dartmouth.cs.intersection.Global;
import com.dartmouth.cs.intersection.R;


/**
 * Created by _ReacTor on 16/3/1.
 */
public class FeatureListAdapter extends ArrayAdapter<String>{
    private Context context;
    public static final boolean[] isSetted = {false, false, false, false, false, false, false};

    public FeatureListAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {
        //final View v = LayoutInflater.from(context).inflate(R.layout.list_checkbox, parent);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.list_checkbox, null);
        final CheckBox mCheckBox = (CheckBox) v.findViewById(R.id.checkbox);
        mCheckBox.setText(Global.ALL_FEATURES[position]);

        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                if (!checkBox.isChecked()) {
                    FeaturesActivity.selectedFeatures--;
                    isSetted[position] = false;
                    return;
                }

                if (FeaturesActivity.selectedFeatures < 3) {
                    FeaturesActivity.selectedFeatures++;
                    isSetted[position] = true;
                } else {
                    Toast.makeText(getContext(), "Limit Reached", Toast.LENGTH_LONG).show();

                    checkBox.toggle();
                }
            }
        });

        mCheckBox.setChecked(isSetted[position]);
        return v;
    }

    @Override
    public int getCount() {
        return Global.ALL_FEATURES.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
