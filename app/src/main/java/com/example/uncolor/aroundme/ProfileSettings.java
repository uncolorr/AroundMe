package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSettings extends AppCompatActivity {

    private static final String STATUS_FAIL = "failed";
    private static final String STATUS_SUCCESS = "success";
    private final static String URI_POLITICS = "http://aroundme.bigbadbird.ru/privacy_policy.php?lang=ru";
    private final static String URI_OUR_WEBSITE = "http://aroundme.bigbadbird.ru";
    private final static int RESULT_LOAD_IMAGE = 1;

    LayoutInflater inflater;
    ListView listViewAbout;
    ListViewAboutAdapter arrayAdapter;
    ArrayList<String> arrayListAbout;

    View actionBarProfile;
    EditText editTextNewPassword;
    EditText editTextRepeatPassword;
    ImageButton imageButtonChangePassword;
    TextView textViewLogin;
    CircleImageView imageViewAvatar;
    ImageLoader imageLoader;
    Switch switchShowNews;

    User user;
    String login;

    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "BY7KTGZPH9TS8924KJTR");

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();


        user = getIntent().getParcelableExtra("user");
        Log.i("fg", "user avatar: " + user.getAvatar_url());
        imageViewAvatar = (CircleImageView) findViewById(R.id.imageViewAvatar);
        imageLoader.loadImage(user.getAvatar_url(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageViewAvatar.setImageBitmap(loadedImage);
            }
        });

        login = getIntent().getStringExtra("login");
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarProfile = inflater.inflate(R.layout.profile_action_bar, null);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarProfile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        imageButtonChangePassword = (ImageButton) findViewById(R.id.imageButtonChangePassword);
        switchShowNews = (Switch) findViewById(R.id.switchShowNews);
        SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        switchShowNews.setChecked(sharedPref.getBoolean(getString(R.string.showNews), false));

        switchShowNews.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("showNews", isChecked);
                editor.apply();
            }
        });

        textViewLogin = (TextView) findViewById(R.id.textViewLogin);
        textViewLogin.setText(login);

        editTextNewPassword = (EditText) findViewById(R.id.editTextNewPassword);
        editTextNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((!Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString()))) {
                    imageButtonChangePassword.setImageResource(R.drawable.cancel);
                } else if (Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())) {
                    imageButtonChangePassword.setImageResource(R.drawable.ok);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        editTextRepeatPassword = (EditText) findViewById(R.id.editTextRepeatPassword);
        editTextRepeatPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((!Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString()))) {
                    imageButtonChangePassword.setImageResource(R.drawable.cancel);
                } else if (Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())) {
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
        listViewAbout = (ListView) findViewById(R.id.listViewAbout);
        arrayAdapter = new ListViewAboutAdapter(this, arrayListAbout);
        listViewAbout.setAdapter(arrayAdapter);

        listViewAbout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Uri adress;
                Intent openLink = null;
                switch (position) {
                    case 0:
                        adress = Uri.parse(URI_POLITICS);
                        openLink = new Intent(Intent.ACTION_VIEW, adress);
                        startActivity(Intent.createChooser(openLink, "Открыть с помощью"));
                        break;
                    case 1: {

                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", "support@lwts.ru", null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
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

        if (Objects.equals(editTextRepeatPassword.getText().toString(), "") && Objects.equals(editTextNewPassword.getText().toString(), "")) {
            Toast.makeText(ProfileSettings.this, "Поля не должны быть пустыми", Toast.LENGTH_LONG).show();
        } else if (Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())) {

            String URL = "http://aroundme.lwts.ru/changepassword?";
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
                            Toast.makeText(ProfileSettings.this, "Пароль успешно изменен", Toast.LENGTH_LONG).show();
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

        } else {
            Toast.makeText(ProfileSettings.this, "Пароли не совпадают", Toast.LENGTH_LONG).show();
        }

    }

    public void onClickImageViewAvatar(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {

                final Uri imageUri = data.getData();

                Log.i("fg", "real path: " + getRealPathFromURI(imageUri));


                Log.i("fg", "picture " + imageUri.getEncodedPath());
                Log.i("fg", "scheme " + imageUri.getScheme());
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                File file = new File(getRealPathFromURI(imageUri));

                String URL = "http://aroundme.lwts.ru/changeavatar?";
                RequestParams requestParams = new RequestParams();
                requestParams.put("photo", file);
                requestParams.put("token", user.getToken());
                requestParams.put("user_id", user.getUser_id());

                client.post(URL, requestParams, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.i("fg", "change avatar " + response.toString());
                        try {
                            String status = response.getString("status");
                            if (Objects.equals(status, STATUS_FAIL)) {


                            } else if (Objects.equals(status, STATUS_SUCCESS)) {
                                imageViewAvatar.setImageBitmap(selectedImage);
                                JSONArray responseArray = response.getJSONArray("response");
                                JSONObject data = responseArray.getJSONObject(0);
                                user.setAvatar_url(data.getString("avatar_url"));
                                SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.avatar_url), data.getString("avatar_url"));
                                editor.apply();

                                Toast.makeText(ProfileSettings.this, "Аватар успешно загружен", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                });


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(ProfileSettings.this, "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(ProfileSettings.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }


    public void onClickImageButtonProfileBack(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onClickImageButtonExit(View view) {
        SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.token), "");
        editor.putString(getString(R.string.type), "");
        editor.putString(getString(R.string.avatar_url), "");
        editor.putString(getString(R.string.user_id), "");
        editor.putString(getString(R.string.login), "");

        editor.apply();

        finishAffinity();
        Intent intent = new Intent(ProfileSettings.this, Authorization.class);
        startActivity(intent);
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
