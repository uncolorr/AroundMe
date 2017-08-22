package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class Authorization extends AppCompatActivity {


    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";

    User user;
    String oneSignaluserId;
    EditText editTextLogin;
    EditText editTextPassword;
    ProgressBar progressBarAuth;
    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(OSNotificationOpenResult result) {
                        JSONObject data = result.toJSONObject();
                        Log.i("fg","notification data: " + data.toString());
                    }
                })
                .init();

        Log.i("fg", "onCreate Auth");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorization);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        user = new User();


        SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        String token = sharedPref.getString(getString(R.string.token), "");
        String user_id = sharedPref.getString(getString(R.string.user_id), "");
        String avatar_url = sharedPref.getString(getString(R.string.avatar_url), "");
        String type = sharedPref.getString(getString(R.string.type), "");
        String login = sharedPref.getString(getString(R.string.login), "");

        if(!token.isEmpty()){
            user.setData(user_id, token, avatar_url, type);
            Intent intent = new Intent(Authorization.this, MainActivity.class);
            intent.putExtra("user", user);
            intent.putExtra("login", login);
            startActivity(intent);
        }

        editTextLogin = (EditText) findViewById(R.id.editTextLogin);
        editTextLogin.setText("colorblind6");
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPassword.setText("123456");
        progressBarAuth = (ProgressBar)findViewById(R.id.progressBarAuth);
        progressBarAuth.setVisibility(View.INVISIBLE);
        
    }

    public void onButtonLoginClick(View view) {

        if(oneSignaluserId == null){
            OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                @Override
                public void idsAvailable(String userId, String registrationId) {
                    Log.i("fg", "User:" + userId);
                    oneSignaluserId = userId;

                    if (registrationId != null)
                        Log.i("fg", "registrationId:" + registrationId);

                }
            });
        }

        progressBarAuth.setAlpha(1.0f);
        Log.i("fg", "one signal  id: " + oneSignaluserId);
        String URL = new String("http://aroundme.lwts.ru/login?");
        RequestParams params = new RequestParams();
        params.put("login", editTextLogin.getText().toString());
        params.put("password", editTextPassword.getText().toString());
        params.put("oneSignalUserId", oneSignaluserId);


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressBarAuth.setVisibility(View.INVISIBLE);
                try {

                        String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Authorization.this);
                        builder.setTitle("Ошибка");
                        builder.setMessage("Неверный логин или пароль");
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

                        progressBarAuth.setVisibility(View.INVISIBLE);
                        Log.i("fg", "user: " + response.toString());
                        JSONArray responseArray = response.getJSONArray("response");
                        JSONObject data = responseArray.getJSONObject(0);
                        String token = data.getString("token");
                        String type = data.getString("type");
                        String avatar_url = data.getString("avatar_url");
                        String user_id = data.getString("user_id");
                        user.setData(user_id, token, avatar_url, type);

                        SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.token), token);
                        editor.putString(getString(R.string.type), type);
                        editor.putString(getString(R.string.avatar_url), avatar_url);
                        editor.putString(getString(R.string.user_id), user_id);
                        editor.putString(getString(R.string.login), editTextLogin.getText().toString());

                        editor.apply();

                        Intent intent = new Intent(Authorization.this, MainActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("login", editTextLogin.getText().toString());
                        startActivity(intent);

                    }

                } catch (JSONException e) {
                    progressBarAuth.setVisibility(View.INVISIBLE);
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressBarAuth.setVisibility(View.INVISIBLE);
                Log.i("fg" , responseString);

            }


        });
    }
    public void onButtonRegisterClick(View view){

        if(oneSignaluserId == null){
            OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                @Override
                public void idsAvailable(String userId, String registrationId) {
                    Log.i("fg", "User:" + userId);
                    oneSignaluserId = userId;

                    if (registrationId != null)
                        Log.i("fg", "registrationId:" + registrationId);

                }
            });
        }

        progressBarAuth.setVisibility(View.VISIBLE);
        String URL = "http://aroundme.lwts.ru/register?";
        RequestParams params = new RequestParams();
        params.put("login", editTextLogin.getText().toString());
        params.put("password", editTextPassword.getText().toString());
        params.put("oneSignalUserId", oneSignaluserId);


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressBarAuth.setAlpha(0.0f);
                try {
                    String status = response.getString("status");
                    if (Objects.equals(status, STATUS_FAIL)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Authorization.this);
                        builder.setTitle("Ошибка");
                        builder.setMessage("Логин уже занят");
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

                        progressBarAuth.setVisibility(View.INVISIBLE);
                        JSONArray responseArray = response.getJSONArray("response");
                        JSONObject data = responseArray.getJSONObject(0);
                        String token = data.getString("token");
                        String type = data.getString("type");
                        String avatar_url = data.getString("avatar_url");
                        String user_id = data.getString("user_id");
                        user.setData(user_id, token, avatar_url, type);

                        SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString(getString(R.string.token), token);
                        editor.putString(getString(R.string.type), type);
                        editor.putString(getString(R.string.avatar_url), avatar_url);
                        editor.putString(getString(R.string.user_id), user_id);
                        editor.putString(getString(R.string.login), editTextLogin.getText().toString());

                        editor.apply();

                        Intent intent = new Intent(Authorization.this, MainActivity.class);
                        intent.putExtra("user", user);
                        intent.putExtra("login", editTextLogin.getText().toString());
                        startActivity(intent);

                    }

                } catch (JSONException e) {
                    progressBarAuth.setVisibility(View.INVISIBLE);
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                progressBarAuth.setVisibility(View.INVISIBLE);
                Log.i("fg" , responseString);

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("fg", "onResume Auth");
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.i("fg", "User:" + userId);
                oneSignaluserId = userId;

                if (registrationId != null)
                    Log.i("fg", "registrationId:" + registrationId);

            }
        });
    }

}