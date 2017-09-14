package com.colorblind.uncolor.aroundme;

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
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class Authorization extends AppCompatActivity {

    User user;
    String oneSignaluserId;
    EditText editTextLogin;
    EditText editTextPassword;
    ProgressBar progressBarAuth;
    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("fg", "onCreate Auth");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authorization);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        user = new User();


        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);

        String token = sharedPref.getString(getString(R.string.token), "");
        String user_id = sharedPref.getString(getString(R.string.user_id), "");
        String avatar_url = sharedPref.getString(getString(R.string.avatar_url), "");
        String type = sharedPref.getString(getString(R.string.type), "");
        String login = sharedPref.getString(getString(R.string.login), "");

        if(!token.isEmpty()){
            user.setData(user_id, token, avatar_url, type);
            Intent intent = new Intent(Authorization.this, MainActivity.class);
            intent.putExtra(getString(R.string.user), user);
            intent.putExtra(getString(R.string.login), login);
            startActivity(intent);
            finish();
        }

        editTextLogin = (EditText) findViewById(R.id.editTextLogin);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        progressBarAuth = (ProgressBar)findViewById(R.id.progressBarAuth);
        progressBarAuth.setVisibility(View.INVISIBLE);

    }

    public void onButtonLoginClick(View view) {

        if(editTextLogin.getText().toString().isEmpty() || editTextPassword.getText().toString().isEmpty()){
            Toast.makeText(this, getString(R.string.fields_should_not_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if(oneSignaluserId == null){
           getOneSignalValid();
        }

        progressBarAuth.setVisibility(View.VISIBLE);
        Log.i("fg", "one signal  id: " + oneSignaluserId);
        String URL = getString(R.string.domain) + getString(R.string.url_login);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.login), editTextLogin.getText().toString());
        params.put(getString(R.string.password), editTextPassword.getText().toString());
        params.put(getString(R.string.oneSignalUserId), oneSignaluserId);


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressBarAuth.setVisibility(View.INVISIBLE);
                try {

                        String status = response.getString(getString(R.string.status));
                    if (Objects.equals(status, getString(R.string.failed))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Authorization.this);
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.wrong_login_or_password));
                        builder.setCancelable(false);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else if (Objects.equals(status, getString(R.string.success))) {

                        progressBarAuth.setVisibility(View.INVISIBLE);
                        Log.i("fg", "user: " + response.toString());
                        JSONArray responseArray = response.getJSONArray(getString(R.string.response));
                        JSONObject data = responseArray.getJSONObject(0);
                        String token = data.getString(getString(R.string.token));
                        String type = data.getString(getString(R.string.type));
                        String avatar_url = data.getString(getString(R.string.avatar_url));
                        String user_id = data.getString(getString(R.string.user_id));
                        user.setData(user_id, token, avatar_url, type);

                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.oneSignalUserId), oneSignaluserId);
                        editor.putString(getString(R.string.token), token);
                        editor.putString(getString(R.string.type), type);
                        editor.putString(getString(R.string.avatar_url), avatar_url);
                        editor.putString(getString(R.string.user_id), user_id);
                        editor.putString(getString(R.string.login), editTextLogin.getText().toString());

                        editor.apply();

                        Intent intent = new Intent(Authorization.this, MainActivity.class);
                        intent.putExtra(getString(R.string.user), user);
                        intent.putExtra(getString(R.string.login), editTextLogin.getText().toString());
                        startActivity(intent);
                        finish();


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


        if(editTextLogin.getText().toString().isEmpty() || editTextPassword.getText().toString().isEmpty()){
            Toast.makeText(this, getString(R.string.fields_should_not_be_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if(oneSignaluserId == null){
            getOneSignalValid();
        }

        progressBarAuth.setVisibility(View.VISIBLE);
        String URL = getString(R.string.domain) + getString(R.string.url_register);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.login), editTextLogin.getText().toString());
        params.put(getString(R.string.password), editTextPassword.getText().toString());
        params.put(getString(R.string.oneSignalUserId), oneSignaluserId);


        client.post(URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressBarAuth.setAlpha(0.0f);
                try {
                    String status = response.getString(getString(R.string.status));
                    if (Objects.equals(status, getString(R.string.failed))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Authorization.this);
                        builder.setTitle(getString(R.string.error));
                        builder.setMessage(getString(R.string.login_alredy_taken));
                        builder.setCancelable(false);
                        builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else if (Objects.equals(status, getString(R.string.success))) {

                        progressBarAuth.setVisibility(View.INVISIBLE);
                        JSONArray responseArray = response.getJSONArray(getString(R.string.response));
                        JSONObject data = responseArray.getJSONObject(0);
                        String token = data.getString(getString(R.string.token));
                        String type = data.getString(getString(R.string.type));
                        String avatar_url = data.getString(getString(R.string.avatar_url));
                        String user_id = data.getString(getString(R.string.user_id));
                        user.setData(user_id, token, avatar_url, type);

                        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = sharedPref.edit();

                        editor.putString(getString(R.string.oneSignalUserId), oneSignaluserId);
                        editor.putString(getString(R.string.token), token);
                        editor.putString(getString(R.string.type), type);
                        editor.putString(getString(R.string.avatar_url), avatar_url);
                        editor.putString(getString(R.string.user_id), user_id);
                        editor.putString(getString(R.string.login), editTextLogin.getText().toString());

                        editor.apply();

                        Intent intent = new Intent(Authorization.this, MainActivity.class);
                        intent.putExtra(getString(R.string.user), user);
                        intent.putExtra(getString(R.string.login), editTextLogin.getText().toString());
                        startActivity(intent);
                        finish();

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


            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("fg", "onResume Auth");
        getOneSignalValid();
    }


    public void getOneSignalValid(){
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