package com.example.uncolor.aroundme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;


public class UserGrid extends AppCompatActivity {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    GridView gridViewUsers;
    ArrayList<UserItem> userItems = new ArrayList<UserItem>();
    AsyncHttpClient client = new AsyncHttpClient();
    TextView textViewUsersCount;
    User user;
    Room room;
    LayoutInflater inflater;
    View actionBarUserGrid;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_grid);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarUserGrid = inflater.inflate(R.layout.user_grid_action_bar, null);
        textViewUsersCount = (TextView) actionBarUserGrid.findViewById(R.id.textViewUsersCount);
        //getSupportActionBar().setDisplayShowCustomEnabled(true);
      //  getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
    //    getSupportActionBar().setCustomView(actionBarUserGrid);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));

        user = getIntent().getParcelableExtra("user");
        room = getIntent().getParcelableExtra("room");
        final UsersListAdapter usersListAdapter = new UsersListAdapter(UserGrid.this, userItems);


        String URL = "http://aroundme.lwts.ru/usersList?";
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room.getRoom_id());
        params.put("offset", "0");
        params.put("limit", "200");

        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i("fg", "show peoples " + response.toString());
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(UserGrid.this, "Ошибка", Toast.LENGTH_LONG).show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {

                        if (response.has("response")) {
                            JSONArray responseArray = response.getJSONArray("response");
                            count = responseArray.length();
                            getSupportActionBar().setTitle("ПОЛЬЗОВАТЕЛЕЙ: " + Integer.toString(count));
                            for (int i = 0; i < responseArray.length(); i++) {

                                UserItem userItem = new UserItem();
                                JSONObject data = responseArray.getJSONObject(i);
                                if (data.has("login")) {
                                    userItem.setLogin(data.getString("login"));
                                }
                                if (data.has("avatar_url")) {
                                    userItem.setAvatar_url(data.getString("avatar_url"));
                                }
                                userItems.add(userItem);
                            }
                            usersListAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        gridViewUsers = (GridView)findViewById(R.id.gridViewUsers);
        gridViewUsers.setAdapter(usersListAdapter);
        gridViewUsers.setNumColumns(3);
        gridViewUsers.setVerticalSpacing(5);
        gridViewUsers.setHorizontalSpacing(5);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
