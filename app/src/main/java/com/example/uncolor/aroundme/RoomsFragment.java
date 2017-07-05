package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;



public class RoomsFragment extends Fragment {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    ListView listViewRooms;
    User user;
    AsyncHttpClient client = new AsyncHttpClient();
    ArrayList<Room> roomsList = new ArrayList<Room>();
    ListViewRoomsAdapter listViewRoomsAdapter;


    public RoomsFragment() {

    }




    public static RoomsFragment newInstance() {
        RoomsFragment fragment = new RoomsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = getArguments().getParcelable("user");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        listViewRooms = (ListView)view.findViewById(R.id.listViewRooms);
        loadRooms();
        listViewRoomsAdapter = new ListViewRoomsAdapter(getActivity(),roomsList);
        listViewRooms.setAdapter(listViewRoomsAdapter);



        return view;
    }


    private void loadRooms() {

        String URL = new String("http://aroundme.lwts.ru/allrooms?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("latitude", "55");
        params.put("longitude", "55");
        params.put("offset", "0");
        params.put("limit", "100");



        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Ошибка");
                        builder.setMessage("Не удалось загрузить список комнат");
                        builder.setCancelable(false);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {

                        JSONArray responseArray = response.getJSONArray("response");
                        for (int i = 0; i < responseArray.length(); i++) {

                            Room room = new Room();
                            JSONObject data = responseArray.getJSONObject(i);
                            if(data.has("title")) {
                                room.setTitle(data.getString("title"));
                            }
                            room.setUsersCount(data.getString("usersCount"));
                            room.setRoom_id(data.getString("room_id"));
                            roomsList.add(room);


                        }

                        listViewRoomsAdapter.notifyDataSetChanged();


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }
        });


        listViewRooms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), Dialog.class);
                intent.putExtra("user", user);
                intent.putExtra("room_id", roomsList.get(position).getRoom_id());
                intent.putExtra("room_name",roomsList.get(position).getTitle());
                startActivity(intent);
            }
        });

    }
}
