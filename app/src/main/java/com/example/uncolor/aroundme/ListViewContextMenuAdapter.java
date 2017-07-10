package com.example.uncolor.aroundme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Created by uncolor on 09.07.17.
 */

public class ListViewContextMenuAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;

    public ListViewContextMenuAdapter(Context context){
        layoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return 5;
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
            view = layoutInflater.inflate(R.layout.context_menu_dialog_item, parent, false);
        }
        return view;
    }
}
