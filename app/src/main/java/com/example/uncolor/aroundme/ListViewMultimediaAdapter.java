package com.example.uncolor.aroundme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by uncolor on 22.07.17.
 */

public class ListViewMultimediaAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<String> items;

    public ListViewMultimediaAdapter(Context context, ArrayList<String> items){
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
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
            view = layoutInflater.inflate(R.layout.multimedia_menu_dialog_item, parent, false);

        }
        TextView textViewMultimediaMenuItem = (TextView)view.findViewById(R.id.textViewMultimediaMenuItem);
        textViewMultimediaMenuItem.setText(items.get(position));

        return view;
    }
}
