package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class Dialog extends AppCompatActivity {

    final int MENU_SEND_IMAGE = 0;
    final int MENU_SEND_LOCATION = 1;
    final int RESULT_LOAD_IMAGE = 2;

    private static final String MSG_TYPE_TEXT = "Text";
    private static final String MSG_TYPE_LOCATION = "Location";
    private static final String MSG_TYPE_PHOTO = "Photo";

    private static final String AROUND_ME_ID = "1";

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    private static final String BACK_ITEM = " ";
    private static final String FAVS_ADD_ITEM = "Добавить в избранное";
    private static final String FAVS_DEL_ITEM = "Убрать из избранного";
    private static final String EDIT_ITEM = " Редактировать";
    private static final String PEOPLE_ITEM = "People";
    private static final String INFO_ITEM = "Инфомация";
    private static final String COMPLAIN_ITEM = "Пожаловаться";
    private static final String DELETE_ITEM = "Удалить";

    private int page = 0;

    View actionBarDialog;
    com.stfalcon.chatkit.commons.ImageLoader imageLoader;
    com.nostra13.universalimageloader.core.ImageLoader normalImageLoader;
    TextView textViewRoomChatName;
    LayoutInflater inflater;
    AsyncHttpClient client = new AsyncHttpClient();
    WebSocket webSocket;
    WebSocketFactory webSocketFactory;
    User user;
    Room room;
    MessagesList messagesList;
    String room_name;
    ImageButton imageButtonAddMultimedia;
    ImageButton imageButtonOpenMenu;
    EditText editTextMessage;
    PopupWindow pw;
    PopupWindow popupWindowInfo;
    Button buttonSend;
    Button buttonLoadMore;
    MessagesListAdapter<IMessage> adapter;
    ListViewContextMenuAdapter listViewContextMenuAdapter;
    ListViewMultimediaAdapter listViewMultimediaAdapter;
    DisplayImageOptions options = null;


    double latitude;
    double longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.dialog);

        webSocketFactory = new WebSocketFactory().setConnectionTimeout(5000);
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        messagesList.invalidate();
        messagesList.setItemViewCacheSize(10000);

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        normalImageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        imageLoader = new com.stfalcon.chatkit.commons.ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, String url) {

                normalImageLoader.loadImage(url, options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        Log.i("fg", "onLoadingStarted");
                        Log.i("fg", imageUri);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        Log.i("fg", "onLoadingFailed");
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        Log.i("fg", "onLoadingComplete");
                        imageView.setImageBitmap(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        Log.i("fg", "onLoadingCancelled");
                    }

                });
            }
        };

        user = getIntent().getParcelableExtra("user");
        room = getIntent().getParcelableExtra("room");
        Log.i("fg","Longitude: " + Double.toString(room.getLongitude()));
        room_name = getIntent().getStringExtra("room_name");
        latitude = getIntent().getDoubleExtra("latitude", 0.0);
        longitude = getIntent().getDoubleExtra("longitude", 0.0);

        adapter = new MessagesListAdapter<IMessage>(user.getUser_id(), imageLoader);
        messagesList.setAdapter(adapter);


        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarDialog = inflater.inflate(R.layout.dialog_action_bar, null);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonLoadMore = (Button) findViewById(R.id.buttonLoadMore);
        buttonLoadMore.setVisibility(View.GONE);
        textViewRoomChatName = (TextView) actionBarDialog.findViewById(R.id.roomChatName);
        textViewRoomChatName.setText(room_name);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        imageButtonAddMultimedia = (ImageButton) findViewById(R.id.imageButtonAddMultimedia);
        imageButtonOpenMenu = (ImageButton) actionBarDialog.findViewById(R.id.imageButtonOpenMenu);


        if (Objects.equals(room.getRoom_id(), AROUND_ME_ID) && !room.isAdmin()) {
            editTextMessage.setEnabled(false);
            imageButtonAddMultimedia.setEnabled(false);
            buttonSend.setEnabled(false);
        }

        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("Отправить изображение");
        arrayList.add("Отправить геолокацию");

        listViewMultimediaAdapter = new ListViewMultimediaAdapter(this, arrayList);


        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarDialog);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        try {
            webSocket = webSocketFactory.createSocket("http://aroundme.lwts.ru/chat?room_id=" + room.getRoom_id(), 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        webSocket.connectAsynchronously();
        webSocket.setPingInterval(60000);
        loadDialog(false);

        webSocket.addListener(new WebSocketListener() {
            @Override
            public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                Log.i("fg", "onStateChanged");
            }

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                Log.i("fg", "onConnected");

            }

            @Override
            public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
                Log.i("fg", "onConnectError");
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                Log.i("fg", "onDisconnected");

            }

            @Override
            public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onFrame");
                JSONObject data = new JSONObject(frame.getPayloadText());
                Log.i("fg", "data: " + data.toString());

                if (data.has("data")) {

                    switch (data.getString("type")) {
                        case "Text":
                            Log.i("fg", "type text");
                            MyMessage myMessage = new MyMessage(data.getString("user_id"), data.getString("data"),
                                    data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                            Log.i("fg", "before " + Integer.toString(adapter.getItemCount()));
                            adapter.addToStart(myMessage, true);
                            adapter.update(myMessage);

                            break;
                        case "Location":
                            Log.i("fg", "type location");
                            MyLocationMessage myLocationMessage = new MyLocationMessage(data.getString("user_id"), data.getString("data"),
                                    data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                            Log.i("fg", "before " + Integer.toString(adapter.getItemCount()));
                            adapter.addToStart(myLocationMessage, true);
                            adapter.update(myLocationMessage);

                            break;
                        case "Photo":
                            Log.i("fg", "was here images");
                            MyImageMessage myImageMessage = new MyImageMessage(data.getString("user_id"), data.getString("data"),
                                    data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                            Log.i("fg", "before " + Integer.toString(adapter.getItemCount()));
                            adapter.addToStart(myImageMessage, true);
                            adapter.update(myImageMessage);
                            break;
                    }
                }
                Log.i("fg", "after " + Integer.toString(adapter.getItemCount()));
                //  adapter.notifyDataSetChanged();
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());


            }

            @Override
            public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onContinuationFrame");

            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onTextFrame");
                //messagesList.smoothScrollToPosition(0);
                adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            }

            @Override
            public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onBinaryFrame");
            }

            @Override
            public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onCloseFrame");
            }

            @Override
            public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onPingFrame");
            }

            @Override
            public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onPongFrame");
                //     adapter.notifyDataSetChanged();
                //    adapter.notifyItemRangeChanged(0, adapter.getItemCount());


            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {
                Log.i("fg", "onTextMessage");
                messagesList.smoothScrollToPosition(0);
                Log.i("fg", "Text message: " + text);
            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                Log.i("fg", "onBinaryMessage");
            }

            @Override
            public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onSendingFrame");
                //  messagesList.smoothScrollToPosition(0);
                //adapter.notifyDataSetChanged();
            }

            @Override
            public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onFrameSent");
                Log.i("fg", frame.getPayloadText());
                Log.i("fg", Integer.toString(adapter.getItemCount()));
                //   messagesList.smoothScrollToPosition(0);
                // adapter.notifyItemRangeChanged(0, adapter.getItemCount());
            }

            @Override
            public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onFrameUnsent");

            }

            @Override
            public void onThreadCreated(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                Log.i("fg", "onThreadCreated");
            }

            @Override
            public void onThreadStarted(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                Log.i("fg", "");
            }

            @Override
            public void onThreadStopping(WebSocket websocket, ThreadType threadType, Thread thread) throws Exception {
                Log.i("fg", "onThreadStarted");
            }

            @Override
            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                Log.i("fg", "onError");
            }

            @Override
            public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onFrameError");
            }

            @Override
            public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
                Log.i("fg", "onMessageError");
            }

            @Override
            public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
                Log.i("fg", "onMessageDecompressionError");
            }

            @Override
            public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
                Log.i("fg", "onTextMessageError");
            }


            @Override
            public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onSendError");
            }

            @Override
            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                Log.i("fg", "onUnexpectedError");
            }

            @Override
            public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                Log.i("fg", "handleCallbackError");
            }

            @Override
            public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
                Log.i("fg", "onSendingHandshake");
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject message = new JSONObject();


                if (!webSocket.isOpen()) {
                    try {
                        webSocket = webSocket.recreate(5000).connectAsynchronously();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                Log.i("fg", Boolean.toString(webSocket.isOpen()));

                if (webSocket.isOpen() && !editTextMessage.getText().toString().isEmpty()) {


                    Log.i("fg", "connected");
                    try {
                        message.put("user_id", user.getUser_id());
                        message.put("room_id", room.getRoom_id());
                        message.put("data", editTextMessage.getText().toString());
                        message.put("token", user.getToken());
                        message.put("type", "Text");
                        webSocket.sendText(message.toString());
                        editTextMessage.getText().clear();

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                } else {
                    Toast.makeText(Dialog.this, "Не удалось отправить сообщение", Toast.LENGTH_SHORT).show();
                }

                messagesList.smoothScrollToPosition(0);
            }
        });

        buttonLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;
                loadDialog(true);
                buttonLoadMore.setVisibility(View.GONE);

            }
        });

        messagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
                Log.i("fg", Integer.toString(dy));
                //    Log.i("fg", "lastVisible" + Integer.toString(lastVisible));
                //    Log.i("fg", "totalItemCount" + Integer.toString(totalItemCount));
                //    Log.i("fg", "getItemCount" + Integer.toString(adapter.getItemCount()));
                boolean endHasBeenReached = lastVisible + 1 >= totalItemCount;
                if (totalItemCount > 0 && endHasBeenReached && dy < 0) {
                    // Log.i("fg", "worked!");
                    buttonLoadMore.setVisibility(View.VISIBLE);
                } else {
                    buttonLoadMore.setVisibility(View.GONE);
                }
            }
        });
    }


    public void loadDialog(final boolean isLoadMore) {
        Log.i("fg", "was in load");


        final List<IMessage> listLoadMore = new ArrayList<IMessage>();
        Log.i("fg", "room id  " + room.getRoom_id());

        Log.i("fg", Integer.toString(adapter.getItemCount()));
        String URL = "http://aroundme.lwts.ru/getMessages?";
        final RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room.getRoom_id());
        if (adapter.getItemCount() != 0) {
            params.put("offset", Integer.toString(adapter.getItemCount() - 1));
        } else {
            params.put("offset", Integer.toString(adapter.getItemCount()));
        }
        params.put("limit", "20");


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    Log.i("fg", "response mes: " + response.toString());
                    Log.i("fg", Integer.toString(page));
                    JSONArray responseArray = response.getJSONArray("messages");
                    for (int i = 0; i < responseArray.length(); i++) {

                        JSONObject data = responseArray.getJSONObject(i);
                        if (Objects.equals(data.getString("type"), MSG_TYPE_PHOTO)) {
                            if (data.has("data")) {
                                MyImageMessage myImageMessage = new MyImageMessage(data.getString("user_id"), data.getString("data"),
                                        data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                                if (isLoadMore) {
                                    listLoadMore.add(myImageMessage);
                                } else {
                                    adapter.addToStart(myImageMessage, true);
                                }

                            }

                        } else if (Objects.equals(data.getString("type"), MSG_TYPE_TEXT)) {
                            if (data.has("data")) {
                                MyMessage myMessage = new MyMessage(data.getString("user_id"), data.getString("data"),
                                        data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                                if (isLoadMore) {
                                    listLoadMore.add(myMessage);
                                } else {
                                    adapter.addToStart(myMessage, true);
                                }
                            }
                        } else if (Objects.equals(data.getString("type"), MSG_TYPE_LOCATION)) {

                            if (data.has("data")) {
                                MyLocationMessage myLocationMessage = new MyLocationMessage(data.getString("user_id"), data.getString("data"),
                                        data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                                if (isLoadMore) {
                                    listLoadMore.add(myLocationMessage);
                                } else {
                                    adapter.addToStart(myLocationMessage, true);
                                }
                            }

                        }
                    }
                    if (isLoadMore) {
                        adapter.addToEnd(listLoadMore, true);
                    }
                    adapter.notifyDataSetChanged();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (page != 0) {
                    page--;
                }

            }
        });
    }

    public void onImageButtonClickBackToRooms(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if (popupWindowInfo != null) {
            if (popupWindowInfo.isShowing()) {
                popupWindowInfo.dismiss();
                return;
            }
        }
        if (pw != null) {
            if (pw.isShowing()) {
                pw.dismiss();
                return;
            }
        }
        webSocket.disconnect();
        finish();


    }

    public void onClickImageButtonAddMultimedia(View view) {
        new AlertDialog.Builder(this).setAdapter(listViewMultimediaAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case MENU_SEND_IMAGE:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
                        break;
                    case MENU_SEND_LOCATION:

                        String URL = "https://maps.googleapis.com/maps/api/staticmap?";
                        RequestParams params = new RequestParams();
                        params.put("center", Double.toString(latitude) + "," + Double.toString(longitude));
                        params.put("zoom", "17");
                        params.put("size", "1000x800");
                        params.put("key", "AIzaSyDKfZkd7pQ1FXbSACL9jrmGw-tbkl34icE");

                        client.get(URL, params, new FileAsyncHttpResponseHandler(Dialog.this) {

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                                Log.i("fg", "STATUS CODE " + statusCode);
                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File file) {
                                Log.i("fg", "STATUS CODE " + statusCode);
                                JSONObject message = new JSONObject();

                                try {

                                    message.put("user_id", user.getUser_id());
                                    message.put("room_id", room.getRoom_id());
                                    message.put("data", Double.toString(latitude) + " " + Double.toString(longitude));
                                    message.put("token", user.getToken());
                                    message.put("type", "Location");
                                    webSocket.sendText(message.toString());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        messagesList.smoothScrollToPosition(0);
                        adapter.notifyItemRangeChanged(0, adapter.getItemCount());

                        break;
                }
            }
        }).create().show();
    }

    public void onClickImageButtonOpenMenu(View view) {

        try {
            LayoutInflater inflater = (LayoutInflater) Dialog.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.context_menu_dialog, null);
            final ListView listViewContextMenu = (ListView) layout.findViewById(R.id.listViewContextMenu);
            ArrayList<String> items = new ArrayList<>();

            items.add(BACK_ITEM);
            if (room.isInFavs()) {
                items.add(FAVS_DEL_ITEM);
            } else {
                items.add(FAVS_ADD_ITEM);
            }
            if (room.isAdmin()) {
                items.add(EDIT_ITEM);
            }

            items.add(PEOPLE_ITEM);
            items.add(INFO_ITEM);
            items.add(COMPLAIN_ITEM);

            if (room.isAdmin()) {
                items.add(DELETE_ITEM);
            }

            Map<String, Integer> imageResourses = new HashMap<String, Integer>();
            imageResourses.put(BACK_ITEM, R.drawable.close_menu);
            imageResourses.put(FAVS_ADD_ITEM, R.drawable.bnv_favs);
            imageResourses.put(FAVS_DEL_ITEM, R.drawable.bnv_favs);
            imageResourses.put(EDIT_ITEM, R.drawable.edit);
            imageResourses.put(PEOPLE_ITEM, R.drawable.people);
            imageResourses.put(INFO_ITEM, R.drawable.info);
            imageResourses.put(COMPLAIN_ITEM, R.drawable.complain);
            imageResourses.put(DELETE_ITEM, R.drawable.trash);


            listViewContextMenuAdapter = new ListViewContextMenuAdapter(layout.getContext(), items, imageResourses);
            listViewContextMenu.setAdapter(listViewContextMenuAdapter);
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            listViewContextMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (view.getTag().toString()) {
                        case BACK_ITEM:
                            pw.dismiss();
                            break;
                        case FAVS_ADD_ITEM:
                            addToFavs();
                            if (room.isInFavs()) {
                                room.setInFavs(false);
                            } else {
                                room.setInFavs(true);
                            }
                            break;
                        case FAVS_DEL_ITEM:
                            addToFavs();
                            break;
                        case EDIT_ITEM:
                            editRoom();
                            break;
                        case PEOPLE_ITEM:
                            showPeoples();
                            break;
                        case INFO_ITEM:
                            showInfo();
                            break;
                        case COMPLAIN_ITEM:
                            complain();
                            break;
                        case DELETE_ITEM:
                            deleteRoom();
                            break;
                    }

                    if (pw.isShowing()) {
                        pw.dismiss();
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToFavs() {

        String URL = "http://aroundme.lwts.ru/favs?";
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room.getRoom_id());

        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i("fg", "add to favs " + response.toString());
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(Dialog.this, "Ошибка", Toast.LENGTH_LONG).show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {
                        String roomStatus = response.getString("response");
                        if (Objects.equals(roomStatus, "added")) {
                            Toast.makeText(Dialog.this, "Комната успешно добавлена в избранное", Toast.LENGTH_LONG).show();
                            room.setInFavs(true);
                        } else if (Objects.equals(roomStatus, "deleted")) {
                            Toast.makeText(Dialog.this, "Комната успешно удалена из избранного", Toast.LENGTH_LONG).show();
                            room.setInFavs(false);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void editRoom() {

        Intent intent = new Intent(Dialog.this, CreateRoomActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("room_name", room.getTitle());
        intent.putExtra("radius", room.getRadius());
        intent.putExtra("isEdit", true);
        startActivity(intent);
    }

    public void showPeoples() {

        Intent intent = new Intent(Dialog.this, UserGrid.class);
        intent.putExtra("user", user);
        intent.putExtra("room", room);
        startActivity(intent);
    }

    public void showInfo() {

        LayoutInflater inflater = (LayoutInflater) Dialog.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.context_menu_dialog_info, null);
        popupWindowInfo = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
        popupWindowInfo.showAtLocation(layout, Gravity.CENTER, 0, 0);
        ImageButton imageButtonCloseInfo = (ImageButton) layout.findViewById(R.id.imageButtonCloseInfo);
        imageButtonCloseInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindowInfo.dismiss();
            }
        });
        TextView textViewInfoAddress = (TextView) layout.findViewById(R.id.textViewInfoAddress);
        TextView textViewInfoRoomName = (TextView) layout.findViewById(R.id.textViewInfoRoomName);
        textViewInfoRoomName.setText(room_name);

        final ProgressBar progressBarInfo = (ProgressBar) layout.findViewById(R.id.progressBarLoadInfoMap);
        if (progressBarInfo != null) {
            progressBarInfo.setIndeterminate(true);
            progressBarInfo.getIndeterminateDrawable().setColorFilter(0xFFA20022, PorterDuff.Mode.MULTIPLY);
        }
        final ImageView imageViewInfoMap = (ImageView) layout.findViewById(R.id.imageViewInfoMap);
        String URL = "https://maps.googleapis.com/maps/api/staticmap?";
        RequestParams params = new RequestParams();
        params.put("center", Double.toString(room.getLatitude()) + "," + Double.toString(room.getLongitude()));
        params.put("zoom", "17");
        params.put("size", "500x300");
        params.put("scale", "2");
        params.put("markers", "color:red|" + Double.toString(room.getLatitude()) + "," + Double.toString(room.getLongitude()));
        params.put("key", "AIzaSyDKfZkd7pQ1FXbSACL9jrmGw-tbkl34icE");

        URL += params.toString();

        normalImageLoader.loadImage(URL, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                Log.i("fg", "onLoadingStarted");
                progressBarInfo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.i("fg", "onLoadingFailed");
                progressBarInfo.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Log.i("fg", "onLoadingComplete");
                imageViewInfoMap.setImageBitmap(loadedImage);
                progressBarInfo.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                Log.i("fg", "onLoadingCancelled");
                progressBarInfo.setVisibility(View.INVISIBLE);
            }

        });


    }

    public void complain() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "support@lwts.ru", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Жалоба на комнату " + "\"" + room.getTitle() + "\"" +  " (id: " + room.getRoom_id() + ")");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));

    }

    public void deleteRoom() {

        String URL = "http://aroundme.lwts.ru/deleteRoom?";
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room.getRoom_id());

        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i("fg", "delete room" + response.toString());
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(Dialog.this, "Ошибка", Toast.LENGTH_LONG).show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {
                        String str = response.getString("response");
                        if (Objects.equals(str, "You have not admin's rights")) {
                            Toast.makeText(Dialog.this, "У вас нет прав для этого", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Dialog.this, "Комната успешно удалена", Toast.LENGTH_LONG).show();
                            webSocket.disconnect();
                            finish();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {

                final Uri imageUri = data.getData();

                Log.i("fg", "real path: " + getRealPathFromURI(imageUri));
                File file = new File(getRealPathFromURI(imageUri));

                String URL = "http://aroundme.lwts.ru/sendPhoto?";
                RequestParams requestParams = new RequestParams();
                requestParams.put("photo", file);
                requestParams.put("token", user.getToken());
                requestParams.put("room_id", room.getRoom_id());
                requestParams.put("user_id", user.getUser_id());

                client.post(URL, requestParams, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("fg", "send photo " + response.toString());
                        try {
                            String status = response.getString("status");
                            if (Objects.equals(status, STATUS_FAIL)) {

                            } else if (Objects.equals(status, STATUS_SUCCESS)) {
                                //  adapter.notifyItemRangeChanged(0, adapter.getItemCount());

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(Dialog.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);

            cursor.close();
        }
        return result;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}






