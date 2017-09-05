package com.colorblind.uncolor.aroundme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by uncolor on 09.07.17.
 */

public class ListViewContextMenuAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private ArrayList<Integer> items;
    private Map<Integer, Integer> imageResources;
    private Context context;


    ListViewContextMenuAdapter(Context context, ArrayList<Integer> items, Map<Integer, Integer> imageResources){
        layoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.imageResources = imageResources;
        this.context = context;

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
            view = layoutInflater.inflate(R.layout.context_menu_dialog_item, parent, false);
        }

        view.setTag(context.getString(items.get(position)));
        TextView textViewContextMenuItem = (TextView)view.findViewById(R.id.textViewContextMenuItem);
        textViewContextMenuItem.setText(items.get(position));
        ImageView imageViewContextMenuItem = (ImageView)view.findViewById(R.id.imageViewContextMenuItem);
        imageViewContextMenuItem.setImageResource(imageResources.get(items.get(position)));
        return view;
    }
}
