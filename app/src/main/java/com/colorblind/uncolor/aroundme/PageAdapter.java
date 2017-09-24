package com.colorblind.uncolor.aroundme;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by uncolor on 04.07.17.
 */

public class PageAdapter extends FragmentPagerAdapter {
    private RoomsFragment roomsFragment;
    private MapFragment mapFragment;
    private FavsFragment favsFragment;

    public PageAdapter(FragmentManager fm, RoomsFragment roomsFragment, MapFragment mapFragment, FavsFragment favsFragment) {
        super(fm);
        this.roomsFragment = roomsFragment;
        this.mapFragment = mapFragment;
        this.favsFragment = favsFragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0: return roomsFragment;
            case 1: return mapFragment;
            case 2: return favsFragment;

        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
