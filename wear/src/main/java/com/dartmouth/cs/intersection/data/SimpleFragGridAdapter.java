package com.dartmouth.cs.intersection.data;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.wearable.view.FragmentGridPagerAdapter;

import com.dartmouth.cs.intersection.MainFragment;
import com.dartmouth.cs.intersection.MenuFragment;

import java.util.ArrayList;

/**
 * Created by _ReacTor on 16/2/22.
 */
public class SimpleFragGridAdapter extends FragmentGridPagerAdapter {
    private ArrayList<Fragment> fragmentRow = new ArrayList<>();

    public SimpleFragGridAdapter(FragmentManager fm) {
        super(fm);

        MainFragment mainFragment = new MainFragment();
        MenuFragment menuFragment = new MenuFragment();

        fragmentRow.add(mainFragment);
        fragmentRow.add(menuFragment);
    }

    @Override
    public Fragment getFragment(int row, int column) {
        if (row == 0 && column == 0)
            return fragmentRow.get(0);
        else
            return fragmentRow.get(1);
    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int i) {
        return fragmentRow.size();
    }
}
