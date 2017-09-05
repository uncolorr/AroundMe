package com.colorblind.uncolor.aroundme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by uncolor on 25.06.17.
 */

public class ListViewAboutAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<String> arrayListAbout;

    public ListViewAboutAdapter(Context context, ArrayList<String> arrayListAbout){
        layoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.arrayListAbout = arrayListAbout;
    }

    @Override
    public int getCount() {
        return arrayListAbout.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null){
            view = layoutInflater.inflate(R.layout.list_item_about, parent, false);
        }

        TextView textViewAboutItem = (TextView)view.findViewById(R.id.textViewAboutItem);
        textViewAboutItem.setText(arrayListAbout.get(position));
        return view;
    }
}
