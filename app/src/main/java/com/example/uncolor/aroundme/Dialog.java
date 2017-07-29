package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neovisionaries.ws.client.ThreadType;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;
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

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    private static final String BACK_ITEM = " ";
    private static final String FAVS_ITEM = "Добавить в избранное";
    private static final String EDIT_ITEM = " Редактировать";
    private static final String PEOPLE_ITEM = "People";
    private static final String INFO_ITEM = "Инфомация";
    private static final String COMPLAIN_ITEM = "Пожаловаться";
    private static final String DELETE_ITEM = "Удалить";

    View actionBarDialog;
    com.stfalcon.chatkit.commons.ImageLoader imageLoader;
    com.nostra13.universalimageloader.core.ImageLoader normalImageLoader;
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
    EditText editTextMessage;
    PopupWindow pw;
    Button buttonSend;
    MessagesListAdapter<IMessage> adapter;
    ListViewContextMenuAdapter listViewContextMenuAdapter;
    ListViewMultimediaAdapter listViewMultimediaAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        webSocketFactory = new WebSocketFactory().setConnectionTimeout(5000);
        messagesList = (MessagesList) findViewById(R.id.messagesList);
        messagesList.invalidate();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        normalImageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();

        imageLoader = new com.stfalcon.chatkit.commons.ImageLoader() {
            @Override
            public void loadImage(final ImageView imageView, String url) {
                
                normalImageLoader.loadImage(url, new ImageLoadingListener() {
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

        user = getIntent().getParcelableExtra("user");
        room_id = getIntent().getStringExtra("room_id");
        room_name = getIntent().getStringExtra("room_name");
        adapter = new MessagesListAdapter<IMessage>(user.getUser_id(), imageLoader);
        messagesList.setAdapter(adapter);
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarDialog = inflater.inflate(R.layout.dialog_action_bar, null);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        textViewRoomChatName = (TextView) actionBarDialog.findViewById(R.id.roomChatName);
        textViewRoomChatName.setText(room_name);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        imageButtonAddMultimedia = (ImageButton) findViewById(R.id.imageButtonAddMultimedia);
        imageButtonOpenMenu = (ImageButton) actionBarDialog.findViewById(R.id.imageButtonOpenMenu);

        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("Отправить изображение");
        arrayList.add("Отправить геолокацию");

        listViewMultimediaAdapter = new ListViewMultimediaAdapter(this, arrayList);


        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarDialog);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        try {
            webSocket = webSocketFactory.createSocket("http://aroundme.lwts.ru/chat?room_id=" + room_id, 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }


        webSocket.connectAsynchronously();
        loadDialog();

        webSocket.addListener(new WebSocketListener() {
            @Override
            public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
                Log.i("fg", "onStateChanged");
                if (newState == WebSocketState.CLOSED) {

                    if (!webSocket.isOpen()) {
                        try {
                            Log.i("fg", "was here c:");
                            webSocket = webSocket.recreate(5000).connectAsynchronously();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                Log.i("fg", "onConnected");
                Log.i("fg", Integer.toString(adapter.getItemCount()));

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
                if (data.has("data")) {
                    MyMessage myMessage = new MyMessage(data.getString("user_id"), data.getString("data"),
                            data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                    Log.i("fg", "before " + Integer.toString(adapter.getItemCount()));
                    adapter.addToStart(myMessage, true);
                    adapter.update(myMessage);
                }
                Log.i("fg", "after " + Integer.toString(adapter.getItemCount()));

            }

            @Override
            public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onContinuationFrame");

            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                Log.i("fg", "onTextFrame");
                adapter.notifyDataSetChanged();
                Log.i("fg", Integer.toString(adapter.getItemCount()));
                messagesList.smoothScrollToPosition(0);
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
                Log.i("fg", Integer.toString(adapter.getItemCount()));
                adapter.notifyDataSetChanged();
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
                messagesList.smoothScrollToPosition(1);
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
                        message.put("room_id", room_id);
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
    }

    public void loadDialog() {

        Log.i("fg", "room id  " + room_id);
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

                    Log.i("fg", "messages: " + response.toString());

                    JSONArray responseArray = response.getJSONArray("messages");
                    for (int i = 0; i < responseArray.length(); i++) {

                        JSONObject data = responseArray.getJSONObject(i);
                        if (Objects.equals(data.getString("type"), MSG_TYPE_PHOTO)) {
                            if (data.has("data")) {
                                MyImageMessage myImageMessage = new MyImageMessage(data.getString("user_id"), data.getString("data"),
                                        data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                                adapter.addToStart(myImageMessage, true);
                            }

                        } else if (Objects.equals(data.getString("type"), MSG_TYPE_TEXT)) {
                            if (data.has("data")) {
                                MyMessage myMessage = new MyMessage(data.getString("user_id"), data.getString("data"),
                                        data.getString("login"), data.getString("unix_time"), data.getString("user_id"), data.getString("avatar"));
                                adapter.addToStart(myMessage, true);
                            }
                        }
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
            webSocket.disconnect();
            finish();

        } else if (pw.isShowing()) {
            pw.dismiss();
        } else {
            webSocket.disconnect();
            finish();
        }
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
            items.add(FAVS_ITEM);
            items.add(EDIT_ITEM);
            items.add(PEOPLE_ITEM);
            items.add(INFO_ITEM);
            items.add(COMPLAIN_ITEM);
            items.add(DELETE_ITEM);

            Map<String, Integer> imageResourses = new HashMap<String, Integer>();
            imageResourses.put(BACK_ITEM, R.drawable.close_menu);
            imageResourses.put(FAVS_ITEM, R.drawable.bnv_favs);
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
                        case FAVS_ITEM:
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
        params.put("room_id", room_id);

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
                        } else if (Objects.equals(roomStatus, "deleted")) {
                            Toast.makeText(Dialog.this, "Комната успешно удалена из избранного", Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void editRoom() {

    }

    public void showPeoples() {
        String URL = "http://aroundme.lwts.ru/usersList?";
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room_id);
        params.put("offset", "0");
        params.put("limit", "100");


        client.get(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.i("fg", "show peoples " + response.toString());
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        Toast.makeText(Dialog.this, "Ошибка", Toast.LENGTH_LONG).show();

                    } else if (Objects.equals(status, STATUS_SUCCESS)) {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void showInfo() {

    }

    public void complain() {

    }

    public void deleteRoom() {

        String URL = "http://aroundme.lwts.ru/deleteRoom?";
        RequestParams params = new RequestParams();
        params.put("token", user.getToken());
        params.put("user_id", user.getUser_id());
        params.put("room_id", room_id);

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

                // final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                // final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                File file = new File(getRealPathFromURI(imageUri));

                String URL = "http://aroundme.lwts.ru/sendPhoto?";
                RequestParams requestParams = new RequestParams();
                requestParams.put("photo", file);
                requestParams.put("token", user.getToken());
                requestParams.put("room_id", room_id);
                requestParams.put("user_id", user.getUser_id());

                client.post(URL, requestParams, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("fg", "send photo " + response.toString());
                        try {
                            String status = response.getString("status");
                            if (Objects.equals(status, STATUS_FAIL)) {


                            } else if (Objects.equals(status, STATUS_SUCCESS)) {
                              /*  imageViewAvatar.setImageBitmap(selectedImage);
                                JSONArray responseArray = response.getJSONArray("response");
                                JSONObject data = responseArray.getJSONObject(0);
                                user.setAvatar_url(data.getString("avatar_url"));
                                Toast.makeText(ProfileSettings.this, "Аватар успешно загружен", Toast.LENGTH_LONG).show();*/
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

        } else {
            Toast.makeText(Dialog.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
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


}




