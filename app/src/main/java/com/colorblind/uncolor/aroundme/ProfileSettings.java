package com.colorblind.uncolor.aroundme;

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
                .build(this, getString(R.string.flurry_agent_key));

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        imageLoader = ImageLoader.getInstance();


        user = getIntent().getParcelableExtra(getString(R.string.user));
        Log.i("fg", "user avatar: " + user.getAvatar_url());
        imageViewAvatar = (CircleImageView) findViewById(R.id.imageViewAvatar);
        imageLoader.loadImage(user.getAvatar_url(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageViewAvatar.setImageBitmap(loadedImage);
            }
        });

        login = getIntent().getStringExtra(getString(R.string.login));
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarProfile = inflater.inflate(R.layout.profile_action_bar, null);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarProfile);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));
        imageButtonChangePassword = (ImageButton) findViewById(R.id.imageButtonChangePassword);
        switchShowNews = (Switch) findViewById(R.id.switchShowNews);
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);
        switchShowNews.setChecked(sharedPref.getBoolean(getString(R.string.showNews), false));

        switchShowNews.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);

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
        arrayListAbout.add(getString(R.string.сonfidentiality_policy));
        arrayListAbout.add(getString(R.string.connect_with_us));
        arrayListAbout.add(getString(R.string.our_website));
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
                        startActivity(Intent.createChooser(openLink, ""));
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
                        startActivity(Intent.createChooser(openLink, ""));
                        break;
                }
            }
        });
    }

    public void onClickImageButtonChangePassword(View view) {

        if (Objects.equals(editTextRepeatPassword.getText().toString(), "") && Objects.equals(editTextNewPassword.getText().toString(), "")) {
            Toast.makeText(ProfileSettings.this, getString(R.string.fields_should_not_be_empty), Toast.LENGTH_LONG).show();
        } else if (Objects.equals(editTextNewPassword.getText().toString(), editTextRepeatPassword.getText().toString())) {

            String URL = getString(R.string.domain) + getString(R.string.url_change_password);
            RequestParams params = new RequestParams();
            params.put(getString(R.string.token), user.getToken());
            params.put(getString(R.string.user_id), user.getUser_id());
            params.put(getString(R.string.new_pass), editTextRepeatPassword.getText().toString());


            client.post(URL, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
               //     Log.i("fg", response.toString());
                    try {
                        String status = response.getString(getString(R.string.status));
                        if (Objects.equals(status, STATUS_FAIL)) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);
                            builder.setTitle(getString(R.string.error));
                            builder.setMessage(getString(R.string.change_password_failed));
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
                            Toast.makeText(ProfileSettings.this, getString(R.string.password_successfully_changed), Toast.LENGTH_LONG).show();
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
            Toast.makeText(ProfileSettings.this, getString(R.string.passwords_do_not_match), Toast.LENGTH_LONG).show();
        }

    }

    /**
     *  onClick method for open image gallery for change avatar
     */
    public void onClickImageViewAvatar(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
    }


    /**
     *  callback method for channge avatar
     */
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {

                final Uri imageUri = data.getData();

                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                File file = new File(getRealPathFromURI(imageUri));

                String URL = getString(R.string.domain)+ getString(R.string.url_change_avatar);
                RequestParams requestParams = new RequestParams();
                requestParams.put(getString(R.string.photo), file);
                requestParams.put(getString(R.string.token), user.getToken());
                requestParams.put(getString(R.string.user_id), user.getUser_id());

                client.post(URL, requestParams, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        //Log.i("fg", "change avatar " + response.toString());
                        try {
                            String status = response.getString(getString(R.string.status));
                            if (Objects.equals(status, STATUS_FAIL)) {


                            } else if (Objects.equals(status, STATUS_SUCCESS)) {
                                imageViewAvatar.setImageBitmap(selectedImage);
                                JSONArray responseArray = response.getJSONArray(getString(R.string.response));
                                JSONObject data = responseArray.getJSONObject(0);
                                user.setAvatar_url(data.getString(getString(R.string.avatar_url)));
                                SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);

                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.avatar_url), data.getString(getString(R.string.avatar_url)));
                                editor.apply();

                                Toast.makeText(ProfileSettings.this, getString(R.string.avatar_successfully_uploaded), Toast.LENGTH_LONG).show();
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

        final SharedPreferences sharedPref = getSharedPreferences(getString(R.string.sharedPrefKeys), Context.MODE_PRIVATE);

        String URL = getString(R.string.domain) + getString(R.string.url_logout);
        RequestParams params = new RequestParams();
        params.put(getString(R.string.oneSignalUserId), sharedPref.getString(getString(R.string.oneSignalUserId), ""));
        params.put(getString(R.string.user_id), sharedPref.getString(getString(R.string.user_id), ""));
        params.put(getString(R.string.token), sharedPref.getString(getString(R.string.token), ""));


        client.post(URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            //    Log.i("fg", response.toString());
                try {
                    String resp = response.getString(getString(R.string.response));
                    if(Objects.equals(resp, "ok")){

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.oneSignalUserId), "");
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

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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

}
