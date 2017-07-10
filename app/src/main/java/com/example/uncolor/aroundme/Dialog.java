package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class Dialog extends AppCompatActivity {

    final int MENU_ADD_IMAGE = 1;
    final int MENU_ADD_LOCATION = 2;
    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";
    View actionBarDialog;
    ImageLoader imageLoader;
    TextView textViewRoomChatName;
    LayoutInflater inflater;
    AsyncHttpClient client = new AsyncHttpClient();
    WebSocket webSocket;
    WebSocketFactory webSocketFactory;
    User user;
    String room_id;
    MessagesList messagesList;
    String room_name;
    ImageButton imageButtonAddMultimedia;
    ImageButton imageButtonOpenMenu;
    PopupWindow pw;
    MessagesListAdapter<MyMessage> adapter;
    ListViewContextMenuAdapter listViewContextMenuAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
        messagesList = (MessagesList) findViewById(R.id.messagesList);

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url) {

            }
        };

        user = getIntent().getParcelableExtra("user");
        room_id = getIntent().getStringExtra("room_id");
        room_name = getIntent().getStringExtra("room_name");

        adapter = new MessagesListAdapter<MyMessage>(user.getUser_id(), imageLoader);
        messagesList.setAdapter(adapter);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarDialog = inflater.inflate(R.layout.dialog_action_bar, null);
        textViewRoomChatName = (TextView) actionBarDialog.findViewById(R.id.roomChatName);
        textViewRoomChatName.setText(room_name);

        imageButtonAddMultimedia = (ImageButton) findViewById(R.id.imageButtonAddMultimedia);
        imageButtonOpenMenu = (ImageButton) actionBarDialog.findViewById(R.id.imageButtonOpenMenu);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarDialog);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));


        loadDialog();
    }

    public void loadDialog() {
        String URL = new String("http://aroundme.lwts.ru/getMessages?");
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room_id);
        params.put("offset", "0");
        params.put("limit", "100");


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    JSONArray responseArray = response.getJSONArray("messages");
                    for (int i = 0; i < responseArray.length(); i++) {

                        JSONObject data = responseArray.getJSONObject(i);
                        MyMessage myMessage = new MyMessage(data.getString("user_id"), data.getString("data"),
                                data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                        adapter.addToStart(myMessage, true);

                    }

                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }
        });
    }

    public void onImageButtonClickBackToRooms(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (pw == null) {
            finish();
        } else if (pw.isShowing()) {
            pw.dismiss();
        } else {
            finish();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.imageButtonAddMultimedia:
                menu.add(0, MENU_ADD_IMAGE, 0, "Отправить фотографию");
                menu.add(0, MENU_ADD_LOCATION, 0, "Отправить геопозицию");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ADD_IMAGE:

                break;
            case MENU_ADD_LOCATION:

                break;
        }
        return super.onContextItemSelected(item);

    }

    public void onClickImageButtonAddMultimedia(View view) {
        PopupMenu popupMenu = new PopupMenu(Dialog.this, imageButtonAddMultimedia, Gravity.CENTER);
        popupMenu.inflate(R.menu.menu_multimedia);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                return true;
            }
        });
        popupMenu.show();

    }

    public void onClickImageButtonOpenMenu(View view) {
        try {

            LayoutInflater inflater = (LayoutInflater) Dialog.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.context_menu_dialog, null);
            ListView listViewContextMenu = (ListView) layout.findViewById(R.id.listViewContextMenu);
            listViewContextMenu.getAdapter();
            listViewContextMenuAdapter = new ListViewContextMenuAdapter(layout.getContext());
            listViewContextMenu.setAdapter(listViewContextMenuAdapter);
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);


        } catch (Exception e) {
            e.printStackTrace();
            Log.i("fg", "bDGFDGDGDGDGD DBDG DGDBD");
        }

    }


}




