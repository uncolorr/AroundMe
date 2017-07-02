package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity   {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    LayoutInflater inflater;
    View actionBarRooms;
    ImageButton imageButtonCreateRoom;
    ImageButton imageButtonProfileSettings;
    User user;
    String login;
    ListView listView;
    ArrayList<Room> roomsList = new ArrayList<Room>();
    ListViewRoomsAdapter listViewRoomsAdapter;
    AsyncHttpClient client = new AsyncHttpClient();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        user = getIntent().getParcelableExtra("user");
        login = getIntent().getStringExtra("login");
        Log.i("fg", "login " + login);

        listView = (ListView) findViewById(R.id.listViewRooms);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarRooms = inflater.inflate(R.layout.rooms_action_bar, null);
        imageButtonCreateRoom = (ImageButton)actionBarRooms.findViewById(R.id.imageButtonCreateRoom);
        imageButtonProfileSettings = (ImageButton)actionBarRooms.findViewById(R.id.imageButtonProfileSettings);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarRooms);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        loadRooms();
        Log.i("fg", Integer.toString(roomsList.size()));
        listViewRoomsAdapter = new ListViewRoomsAdapter(this, roomsList);
        listView.setAdapter(listViewRoomsAdapter);
    }


    public void loadRooms() {

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

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, Dialog.class);
                intent.putExtra("user", user);
                intent.putExtra("room_id", roomsList.get(position).getRoom_id());
                intent.putExtra("room_name",roomsList.get(position).getTitle());
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed(){

    }

    public void onImageButtonClickCreateRoom(View view){
        Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    public void onImageButtonClickProfileSettings(View view){
        Intent intent = new Intent(MainActivity.this, ProfileSettings.class);
        intent.putExtra("user", user);
        intent.putExtra("login", login);
        startActivity(intent);
    }

}
