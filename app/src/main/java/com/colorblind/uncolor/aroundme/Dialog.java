package com.colorblind.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
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
import android.text.method.ScrollingMovementMethod;
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

import com.flurry.android.FlurryAgent;
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

    private int firstVisible = 0;


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
      //  Log.i("fg", "onCreate");
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.dialog);

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, getString(R.string.flurry_agent_key));

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

        user = getIntent().getParcelableExtra(getString(R.string.user));
        room = getIntent().getParcelableExtra(getString(R.string.room));

      //  Log.i("fg", "Longitude: " + Double.toString(room.getLongitude()));
        room_name = getIntent().getStringExtra(getString(R.string.room_name));
        latitude = getIntent().getDoubleExtra(getString(R.string.latitude), 0.0);
        longitude = getIntent().getDoubleExtra(getString(R.string.longitude), 0.0);

        adapter = new MessagesListAdapter<IMessage>(user.getUser_id(), imageLoader);
        messagesList.setAdapter(adapter);


        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarDialog = inflater.inflate(R.layout.dialog_action_bar, null);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        buttonLoadMore = (Button) findViewById(R.id.buttonLoadMore);
        buttonLoadMore.setVisibility(View.GONE);
        textViewRoomChatName = (TextView) actionBarDialog.findViewById(R.id.roomChatName);
        textViewRoomChatName.setText(room.getTitle());
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        imageButtonAddMultimedia = (ImageButton) findViewById(R.id.imageButtonAddMultimedia);
        imageButtonOpenMenu = (ImageButton) actionBarDialog.findViewById(R.id.imageButtonOpenMenu);


        if (Objects.equals(room.getRoom_id(), AROUND_ME_ID) && !room.isAdmin()) {
            editTextMessage.setEnabled(false);
            imageButtonAddMultimedia.setEnabled(false);
            buttonSend.setEnabled(false);
        }

        final ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(getString(R.string.send_photo));
        arrayList.add(getString(R.string.send_location));

        listViewMultimediaAdapter = new ListViewMultimediaAdapter(this, arrayList);


        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarDialog);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        try {
            webSocket = webSocketFactory.createSocket(getString(R.string.domain) + getString(R.string.url_websocket) + room.getRoom_id(), 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        webSocket.connectAsynchronously();
        webSocket.setPingInterval(60000);
        loadDialog(true);

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

                final JSONObject data = new JSONObject(frame.getPayloadText());
                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(messagesList.getLayoutManager());
                final int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
              //  Log.i("fg", "layout manager: " + Integer.toString(firstVisibleItemPosition));

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (data.has(getString(R.string.data))) {
                            try {
                                boolean isMyMessage = Objects.equals(data.getString(getString(R.string.user_id)), user.getUser_id());
                                switch (data.getString(getString(R.string.type))) {
                                    case MSG_TYPE_TEXT:
                                       // Log.i("fg", "type text");
                                        MyMessage myMessage = new MyMessage(data.getString(getString(R.string.message_id)), data.getString(getString(R.string.data)),
                                                data.getString(getString(R.string.login)), data.getString(getString(R.string.unix_time)), data.getString(getString(R.string.user_id)), data.getString(getString(R.string.avatar)));
                                        adapter.addToStart(myMessage, isMyMessage);
                                        break;
                                    case MSG_TYPE_LOCATION:
                                    //    Log.i("fg", "type location");
                                        MyLocationMessage myLocationMessage = new MyLocationMessage(data.getString(getString(R.string.message_id)), data.getString(getString(R.string.data)),
                                            data.getString(getString(R.string.login)), data.getString(getString(R.string.unix_time)), data.getString(getString(R.string.user_id)), data.getString(getString(R.string.avatar)));
                                        adapter.addToStart(myLocationMessage, isMyMessage);
                                        break;
                                    case MSG_TYPE_PHOTO:
                                     //   Log.i("fg", "was here images");
                                        MyImageMessage myImageMessage = new MyImageMessage(data.getString(getString(R.string.message_id)), data.getString(getString(R.string.data)),
                                                data.getString(getString(R.string.login)), data.getString(getString(R.string.unix_time)), data.getString(getString(R.string.user_id)), data.getString(getString(R.string.avatar)));
                                        adapter.addToStart(myImageMessage, isMyMessage);
                                        break;
                                }

                                if (isMyMessage) {
                                    messagesList.smoothScrollToPosition(0);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (firstVisibleItemPosition == 0) {
                            messagesList.smoothScrollToPosition(0);
                        }


                    }
                };
                runOnUiThread(runnable);
            }

            @Override
            public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onContinuationFrame");

            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onTextFrame");
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
            }

            @Override
            public void onTextMessage(WebSocket websocket, String text) throws Exception {
                Log.i("fg", "onTextMessage");

                Log.i("fg", "after: " + Integer.toString(adapter.getItemCount()));
                JSONObject data = new JSONObject(text);

                if (data.has(getString(R.string.user_id))) {
                    if (Objects.equals(data.getString(getString(R.string.user_id)), user.getUser_id())) {
                        messagesList.smoothScrollToPosition(0);
                    }
                }


            }

            @Override
            public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                Log.i("fg", "onBinaryMessage");
            }

            @Override
            public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onSendingFrame");

            }

            @Override
            public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onFrameSent");
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

                if(editTextMessage.getText().toString().isEmpty()){
                    Toast.makeText(Dialog.this, getString(R.string.field_should_not_be_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                JSONObject message = new JSONObject();


                if (!webSocket.isOpen()) {

                    try {
                        webSocket = webSocket.recreate(5000).connectAsynchronously();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                Log.i("fg", Boolean.toString(webSocket.isOpen()));

                if (webSocket.isOpen()) {


                    Log.i("fg", "connected");
                    try {
                        message.put(getString(R.string.user_id), user.getUser_id());
                        message.put(getString(R.string.room_id), room.getRoom_id());
                        message.put(getString(R.string.data), editTextMessage.getText().toString());
                        message.put(getString(R.string.token), user.getToken());
                        message.put(getString(R.string.type), MSG_TYPE_TEXT);
                        webSocket.sendText(message.toString());
                        editTextMessage.getText().clear();

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                } else {
                    Toast.makeText(Dialog.this, getString(R.string.failed_to_send_message), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDialog(true);
                buttonLoadMore.setVisibility(View.GONE);

            }
        });

        messagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                LinearLayoutManager layoutManager = LinearLayoutManager.class.cast(recyclerView.getLayoutManager());
                int totalItemCount = layoutManager.getItemCount();
                firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();
              //  Log.i("fg", "first visible: " + Integer.toString(firstVisible));
                boolean endHasBeenReached = (lastVisible + 1) >= totalItemCount;
                if (totalItemCount > 0 && endHasBeenReached && dy < 0) {
                    buttonLoadMore.setVisibility(View.VISIBLE);
                } else {
                    buttonLoadMore.setVisibility(View.INVISIBLE);
                }
            }
        });
    }


    public void loadDialog(final boolean isLoadMore) {

        final List<IMessage> listLoadMore = new ArrayList<IMessage>();
       // Log.i("fg", "room id  " + room.getRoom_id());
        //Log.i("fg", Integer.toString(adapter.getItemCount()));


        String URL = getString(R.string.domain) + getString(R.string.url_get_messages);
        final RequestParams params = new RequestParams();
        params.put(getString(R.string.token), user.getToken());
        params.put(getString(R.string.user_id), user.getUser_id());
        params.put(getString(R.string.room_id), room.getRoom_id());
        params.put(getString(R.string.offset), Integer.toString(adapter.getMessagesCount()));
        params.put(getString(R.string.limit), "20");


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {

                    //Log.i("fg", "response mes: " + response.toString());
                    JSONArray responseArray = response.getJSONArray(getString(R.string.messages));
                    for (int i = 0; i < responseArray.length(); i++) {

                        JSONObject data = responseArray.getJSONObject(i);

                        if (Objects.equals(data.getString(getString(R.string.type)), MSG_TYPE_PHOTO)) {
                            if (data.has(getString(R.string.data))) {
                                MyImageMessage myImageMessage = new MyImageMessage(data.getString(getString(R.string.message_id)), data.getString(getString(R.string.data)),
                                        data.getString(getString(R.string.login)), data.getString(getString(R.string.unix_time)), data.getString(getString(R.string.user_id)), data.getString(getString(R.string.avatar)));
                                if (isLoadMore) {
                                    listLoadMore.add(myImageMessage);
                                } else {
                                    adapter.addToStart(myImageMessage, true);
                                }

                            }

                        } else if (Objects.equals(data.getString(getString(R.string.type)), MSG_TYPE_TEXT)) {
                            if (data.has(getString(R.string.data))) {
                                MyMessage myMessage = new MyMessage(data.getString(getString(R.string.message_id)), data.getString(getString(R.string.data)),
                                        data.getString(getString(R.string.login)), data.getString(getString(R.string.unix_time)), data.getString(getString(R.string.user_id)), data.getString(getString(R.string.avatar)));
                                if (isLoadMore) {
                                    listLoadMore.add(myMessage);
                                } else {
                                    adapter.addToStart(myMessage, true);
                                }
                            }
                        } else if (Objects.equals(data.getString(getString(R.string.type)), MSG_TYPE_LOCATION)) {
                            if (data.has(getString(R.string.data))) {
                                MyLocationMessage myLocationMessage = new MyLocationMessage(data.getString(getString(R.string.message_id)), data.getString(getString(R.string.data)),
                                        data.getString(getString(R.string.login)), data.getString(getString(R.string.unix_time)), data.getString(getString(R.string.user_id)), data.getString(getString(R.string.avatar)));
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

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public void onClickImageButtonAddMultimedia(View view) {

        new AlertDialog.Builder(this).setAdapter(listViewMultimediaAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case MENU_SEND_IMAGE:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
                        break;
                    case MENU_SEND_LOCATION:

                        String URL = getString(R.string.url_static_map);
                        RequestParams params = new RequestParams();
                        params.put("center", Double.toString(latitude) + "," + Double.toString(longitude));
                        params.put("zoom", "17");
                        params.put("size", "1000x800");
                        params.put("key", "AIzaSyDKfZkd7pQ1FXbSACL9jrmGw-tbkl34icE");

                        client.get(URL, params, new FileAsyncHttpResponseHandler(Dialog.this) {

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {

                            }

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, File file) {
                                JSONObject message = new JSONObject();
                                try {

                                    message.put(getString(R.string.user_id), user.getUser_id());
                                    message.put(getString(R.string.room_id), room.getRoom_id());
                                    message.put(getString(R.string.data), Double.toString(latitude) + " " + Double.toString(longitude));
                                    message.put(getString(R.string.token), user.getToken());
                                    message.put(getString(R.string.type), MSG_TYPE_LOCATION);
                                    webSocket.sendText(message.toString());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
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
            final ArrayList<Integer> items = new ArrayList<>();

            items.add(R.string.back);
            if (room.isInFavs()) {
                items.add(R.string.del_from_favs);
            } else {
                items.add(R.string.add_to_favs);
            }
            if (room.isAdmin()) {
                items.add(R.string.edit);
            }

            items.add(R.string.people);
            items.add(R.string.informaion);
            items.add(R.string.complain);

            if (room.isAdmin()) {
                items.add(R.string.delete);
            }

            Map<Integer, Integer> imageResourses = new HashMap<>();
            imageResourses.put(R.string.back, R.drawable.close_menu);
            imageResourses.put(R.string.add_to_favs, R.drawable.bnv_favs);
            imageResourses.put(R.string.del_from_favs, R.drawable.bnv_favs_fill);
            imageResourses.put(R.string.edit, R.drawable.edit);
            imageResourses.put(R.string.people, R.drawable.people);
            imageResourses.put(R.string.informaion, R.drawable.info);
            imageResourses.put(R.string.complain, R.drawable.complain);
            imageResourses.put(R.string.delete, R.drawable.trash);


            listViewContextMenuAdapter = new ListViewContextMenuAdapter(layout.getContext(), items, imageResourses);
            listViewContextMenu.setAdapter(listViewContextMenuAdapter);
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, false);
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            listViewContextMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                    if(Objects.equals(view.getTag().toString(), getString(R.string.back))){
                        pw.dismiss();
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.add_to_favs))){
                        addToFavs();
                        if (room.isInFavs()) {
                            room.setInFavs(false);
                        } else {
                            room.setInFavs(true);
                        }
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.del_from_favs))){
                        addToFavs();
                        if (room.isInFavs()) {
                            room.setInFavs(false);
                        } else {
                            room.setInFavs(true);
                        }
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.edit))){
                        editRoom();
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.people))){
                        showPeoples();
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.informaion))){
                        showInfo();
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.complain))){
                        complain();
                    }
                    else if(Objects.equals(view.getTag().toString(), getString(R.string.delete))){
                        deleteRoom();
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

        String URL = getString(R.string.domain) + getString(R.string.url_favs);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.token), user.getToken());
        params.put(getString(R.string.user_id), user.getUser_id());
        params.put(getString(R.string.room_id), room.getRoom_id());

        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //Log.i("fg", "add to favs " + response.toString());
                    String status = response.getString(getString(R.string.status));
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(Dialog.this, getString(R.string.error), Toast.LENGTH_LONG).show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {
                        String roomStatus = response.getString(getString(R.string.response));
                        if (Objects.equals(roomStatus, getString(R.string.added))) {
                            Toast.makeText(Dialog.this, getString(R.string.add_to_favs_msg), Toast.LENGTH_LONG).show();
                            room.setInFavs(true);
                        } else if (Objects.equals(roomStatus, getString(R.string.deleted))) {
                            Toast.makeText(Dialog.this, getString(R.string.del_from_favs_msg), Toast.LENGTH_LONG).show();
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
        intent.putExtra(getString(R.string.user), user);
        intent.putExtra(getString(R.string.room_name), room.getTitle());
        intent.putExtra(getString(R.string.radius), room.getRadius());
        intent.putExtra(getString(R.string.is_edit), true);
        intent.putExtra(getString(R.string.room_id), room.getRoom_id());
        startActivity(intent);
    }

    public void showPeoples() {

        Intent intent = new Intent(Dialog.this, UserGrid.class);
        intent.putExtra(getString(R.string.user), user);
        intent.putExtra(getString(R.string.room), room);
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
        final TextView textViewInfoAddress = (TextView) layout.findViewById(R.id.textViewInfoAddress);
        textViewInfoAddress.setMovementMethod(new ScrollingMovementMethod());

        TextView textViewInfoRoomName = (TextView) layout.findViewById(R.id.textViewInfoRoomName);
        textViewInfoRoomName.setText(room_name);

        final ProgressBar progressBarInfo = (ProgressBar) layout.findViewById(R.id.progressBarLoadInfoMap);
        if (progressBarInfo != null) {
            progressBarInfo.setIndeterminate(true);
            progressBarInfo.getIndeterminateDrawable().setColorFilter(0xFFA20022, PorterDuff.Mode.MULTIPLY);
        }
        final ImageView imageViewInfoMap = (ImageView) layout.findViewById(R.id.imageViewInfoMap);

        String URL_adress_map = "https://maps.googleapis.com/maps/api/geocode/json?";
        RequestParams paramsAdressMap = new RequestParams();
        paramsAdressMap.put("latlng", Double.toString(room.getLatitude()) + "," + Double.toString(room.getLongitude()));
        paramsAdressMap.put("language", "ru");
        paramsAdressMap.put("key", "AIzaSyBK20b4ODBLOvvkw75SiEtz1vWV31TjCuU");

        Log.i("fg", URL_adress_map + paramsAdressMap.toString());
        client.get(URL_adress_map, paramsAdressMap, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {


                Log.i("fg", "response mes: " + response.toString());
                try {
                    if (Objects.equals(response.getString("status"), "OK")) {
                        JSONArray adressComponents = response.getJSONArray("results");
                        JSONObject data = adressComponents.getJSONObject(0);
                        String formatted_address = data.getString("formatted_address");
                        textViewInfoAddress.setText(formatted_address);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });

        String URL_static_map = "https://maps.googleapis.com/maps/api/staticmap?";
        RequestParams paramsStaticMap = new RequestParams();
        paramsStaticMap.put("center", Double.toString(room.getLatitude()) + "," + Double.toString(room.getLongitude()));
        paramsStaticMap.put("zoom", "17");
        paramsStaticMap.put("size", "500x300");
        paramsStaticMap.put("scale", "2");
        paramsStaticMap.put("markers", "color:red|" + Double.toString(room.getLatitude()) + "," + Double.toString(room.getLongitude()));
        paramsStaticMap.put("key", "AIzaSyDKfZkd7pQ1FXbSACL9jrmGw-tbkl34icE");

        URL_static_map += paramsStaticMap.toString();

        normalImageLoader.loadImage(URL_static_map, options, new ImageLoadingListener() {
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
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Жалоба на комнату " + "\"" + room.getTitle() + "\"" + " (id: " + room.getRoom_id() + ")");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));

    }

    public void deleteRoom() {

        String URL = getString(R.string.domain) + getString(R.string.url_delete_room);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.token), user.getToken());
        params.put(getString(R.string.user_id), user.getUser_id());
        params.put(getString(R.string.room_id), room.getRoom_id());

        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                  //  Log.i("fg", "delete room" + response.toString());
                    String status = response.getString(getString(R.string.status));
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(Dialog.this, getString(R.string.error), Toast.LENGTH_LONG).show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {
                        String str = response.getString(getString(R.string.response));
                        if (Objects.equals(str, "You have not admin's rights")) {
                            Toast.makeText(Dialog.this, "У вас нет прав для этого", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(Dialog.this, getString(R.string.del_room_msg), Toast.LENGTH_LONG).show();
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

        if (data != null) {

            if (reqCode == RESULT_LOAD_IMAGE) {
                try {
                    final Uri imageUri = data.getData();

                   // Log.i("fg", "real path: " + getRealPathFromURI(imageUri));
                    File file = new File(getRealPathFromURI(imageUri));

                    String URL = getString(R.string.domain) + getString(R.string.url_send_photo);
                    RequestParams requestParams = new RequestParams();
                    requestParams.put(getString(R.string.photo), file);
                    requestParams.put(getString(R.string.token), user.getToken());
                    requestParams.put(getString(R.string.room_id), room.getRoom_id());
                    requestParams.put(getString(R.string.user_id), user.getUser_id());

                    client.post(URL, requestParams, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            //Log.i("fg", "send photo " + response.toString());
                            try {
                                String status = response.getString(getString(R.string.status));
                                if (Objects.equals(status, STATUS_FAIL)) {

                                } else if (Objects.equals(status, STATUS_SUCCESS)) {

                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    });


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(Dialog.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                }
            }
        }
    }



    /**
     *  Get path for load picture from gallery
     */
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






