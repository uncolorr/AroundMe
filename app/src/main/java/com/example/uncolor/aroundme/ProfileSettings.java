package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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

public class ProfileSettings extends AppCompatActivity {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";
    private final static String URI_POLITICS = "https://bigbadbird.ru/around-me/privacy_policy.php?lang=ru";
    private final static String URI_CONTACT_US = " ";
    private final static String URI_OUR_WEBSITE = "https://bigbadbird.ru/around-me";

    LayoutInflater inflater;
    ListView listViewAbout;
    ListViewAboutAdapter arrayAdapter;
    ArrayList<String> arrayListAbout;
    View actionBarProfile;
    EditText editTextNewPassword;
    EditText editTextRepeatPassword;
    ImageButton imageButtonChangePassword;
    TextView textViewLogin;
    User user;
    String login;
    AsyncHttpClient client = new AsyncHttpClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);

        user = getIntent().getParcelableExtra("user");
        login = getIntent().getStringExtra("login");
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarProfile = inflater.inflate(R.layout.profile_action_bar, null);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarProfile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        imageButtonChangePassword = (ImageButton)findViewById(R.id.imageButtonChangePassword);
        textViewLogin = (TextView)findViewById(R.id.textViewLogin);
        textViewLogin.setText(login);

        editTextNewPassword = (EditText)findViewById(R.id.editTextNewPassword);
        editTextNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((!Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString()))) {
                    imageButtonChangePassword.setImageResource(R.drawable.cancel);
                }
                else if(Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())){
                    imageButtonChangePassword.setImageResource(R.drawable.ok);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        editTextRepeatPassword = (EditText)findViewById(R.id.editTextRepeatPassword);
        editTextRepeatPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if((!Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString()))) {
                    imageButtonChangePassword.setImageResource(R.drawable.cancel);
                }
                else if(Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())){
                    imageButtonChangePassword.setImageResource(R.drawable.ok);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        arrayListAbout = new ArrayList<String>();
        arrayListAbout.add("Политика конфиденциальности");
        arrayListAbout.add("Связаться с нами");
        arrayListAbout.add("Наш вебсайт");
        listViewAbout = (ListView)findViewById(R.id.listViewAbout);
        arrayAdapter =  new ListViewAboutAdapter(this, arrayListAbout);
        listViewAbout.setAdapter(arrayAdapter);

        listViewAbout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri adress;
                Intent openLink = null;
                switch (position){
                    case 0:
                        adress = Uri.parse(URI_POLITICS);
                        openLink = new Intent(Intent.ACTION_VIEW, adress);
                        startActivity(Intent.createChooser(openLink, "Открыть с помощью"));
                        break;
                    case 1: {
                        Intent gmail = new Intent(Intent.ACTION_VIEW);
                        gmail.setClassName("com.google.android.gm","com.google.android.gm.ComposeActivityGmail");
                        gmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "jckdsilva@gmail.com" });
                        gmail.setData(Uri.parse("jckdsilva@gmail.com"));
                        gmail.putExtra(Intent.EXTRA_SUBJECT, "enter something");
                        gmail.setType("plain/text");
                        gmail.putExtra(Intent.EXTRA_TEXT, "hi android jack!");
                        startActivity(gmail);
                    }
                        break;
                    case 2:
                        adress = Uri.parse(URI_OUR_WEBSITE);
                        openLink = new Intent(Intent.ACTION_VIEW, adress);
                        startActivity(Intent.createChooser(openLink, "Открыть с помощью"));
                        break;
                }
            }
        });
    }

    public void onClickImageButtonChangePassword(View view) {

        if(Objects.equals(editTextRepeatPassword.getText().toString(), "") && Objects.equals(editTextNewPassword.getText().toString(), "")){
            Toast.makeText(ProfileSettings.this,"Поля не должны быть пустыми",Toast.LENGTH_LONG).show();
        }

       else if (Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())) {

            String URL = new String("http://aroundme.lwts.ru/changepassword?");
            RequestParams params = new RequestParams();
            params.put("token", user.getToken());
            params.put("user_id", user.getUser_id());
            params.put("new_password", editTextRepeatPassword.getText().toString());


            client.post(URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.i("fg", response.toString());
                    try {
                        String status = response.getString("status");
                        if (Objects.equals(status, STATUS_FAIL)) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);
                            builder.setTitle("Ошибка");
                            builder.setMessage("Не удалось поменять пароль");
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
                            Toast.makeText(ProfileSettings.this,"Пароль успешно изменен",Toast.LENGTH_LONG).show();
                            editTextNewPassword.getText().clear();
                            editTextRepeatPassword.getText().clear();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

                }
            });

        }else {
            Toast.makeText(ProfileSettings.this,"Пароли не совпадают",Toast.LENGTH_LONG).show();
        }

    }
}
