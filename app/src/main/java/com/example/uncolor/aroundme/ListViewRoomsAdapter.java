package com.example.uncolor.aroundme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by uncolor on 15.06.17.
 */

public class ListViewRoomsAdapter extends BaseAdapter {


    private ArrayList<Room> roomsList = new ArrayList<Room>();
    private LayoutInflater layoutInflater;

    public ListViewRoomsAdapter(Context context, ArrayList<Room> roomsList) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.roomsList = roomsList;
    }

    @Override
    public int getCount() {
        return roomsList.size();
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
        if (view == null) {
            view = layoutInflater.inflate(R.layout.room_item, parent, false);
        }

        TextView textViewUsersCount = (TextView) view.findViewById(R.id.textViewUsersCount);
        textViewUsersCount.setText(roomsList.get(position).getUsersCount());
        TextView textViewDistance = (TextView) view.findViewById(R.id.textViewDistance);
        if (roomsList.get(position).getDistance() == 0) {
            textViewDistance.setVisibility(View.INVISIBLE);
        } else {
            textViewDistance.setText(getDistanceFormat(roomsList.get(position).getDistance()));
        }

        TextView textViewRoomsTitle = (TextView) view.findViewById(R.id.textViewRoomsTitle);
        textViewRoomsTitle.setText(roomsList.get(position).getTitle());
        return view;
    }

    private String getDistanceFormat(float distance) {

        String result = "";
        int intDistance = (int) distance;
        if (intDistance < 1000) {
            result = Integer.toString(intDistance) + "м";
            return result;
        }
        float floatDistance = (float) intDistance;
        floatDistance /= 1000;
        result = Float.toString(floatDistance) + "км";

        return result;
    }

}
