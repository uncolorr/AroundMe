package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.flurry.android.FlurryAgent;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    private static final String FLURRY_API_KEY = "BY7KTGZPH9TS8924KJTR";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    CustomViewPager viewPager;
    RoomsFragment roomsFragment = RoomsFragment.newInstance();
    MapFragment mapFragment = MapFragment.newInstance();
    FavsFragment favsFragment = FavsFragment.newInstance();
    PageAdapter pageAdapter;
    LocationManager locationManager;

    LayoutInflater inflater;
    View actionBarRooms;
    View actionBarMapChats;
    View actionBarFavs;

    ImageButton imageButtonCreateRoom;
    ImageButton imageButtonProfileSettings;
    User user;
    String login;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withLogLevel(Log.INFO)
                .build(this, "BY7KTGZPH9TS8924KJTR");

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


        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        user = getIntent().getParcelableExtra("user");
        login = getIntent().getStringExtra("login");

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarRooms = inflater.inflate(R.layout.rooms_action_bar, null);
        actionBarMapChats = inflater.inflate(R.layout.map_chats_action_bar, null);
        actionBarFavs = inflater.inflate(R.layout.favs_action_bar, null);
        imageButtonCreateRoom = (ImageButton) actionBarRooms.findViewById(R.id.imageButtonCreateRoom);
        imageButtonProfileSettings = (ImageButton) actionBarRooms.findViewById(R.id.imageButtonProfileSettings);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarRooms);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));

        roomsFragment.getArguments().putParcelable("user", user);
        favsFragment.getArguments().putParcelable("user", user);
        mapFragment.getArguments().putParcelable("user", user);


        pageAdapter = new PageAdapter(getSupportFragmentManager(), roomsFragment, mapFragment, favsFragment);
        viewPager = (CustomViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(15);
        viewPager.setAdapter(pageAdapter);
        viewPager.setPagingEnabled(false);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener);

        //checkLocationPermission();

        //Log.i("fg", Boolean.toString(checkLocationPermission()));
    }

    private BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_search:
                    getSupportActionBar().setCustomView(actionBarRooms);
                    viewPager.setCurrentItem(0);
                    return true;

                case R.id.navigation_map:
                    getSupportActionBar().setCustomView(actionBarMapChats);
                    viewPager.setCurrentItem(1);
                    return true;

                case R.id.navigation_favs:
                    getSupportActionBar().setCustomView(actionBarFavs);
                    viewPager.setCurrentItem(2);
                    return true;

            }
            return false;
        }

    };


    @Override
    public void onBackPressed() {
        FlurryAgent.logEvent("call onBackPressed!!!");
        Log.i("fg", "back");
    }

    public void onImageButtonClickCreateRoom(View view) {
        Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
        intent.putExtra("user", user);
        intent.putExtra("isEdit", false);
        startActivity(intent);
    }

    public void onImageButtonClickProfileSettings(View view) {
        FlurryAgent.logEvent("call profile settings!!!");
        Intent intent = new Intent(MainActivity.this, ProfileSettings.class);
        intent.putExtra("user", user);
        intent.putExtra("login", login);
        startActivity(intent);
    }

    public void onClickImageButtonUpdateMapAllChats(View view) {

        mapFragment.updateMap();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i("fg", "onResume main");
        SharedPreferences sharedPref = getSharedPreferences("com.example.aroundme.KEYS", Context.MODE_PRIVATE);
        user.setAvatar_url(sharedPref.getString(getString(R.string.avatar_url), ""));
        checkLocationEnabled();
    }

    private void checkLocationEnabled(){

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setMessage("Передача геоданных сейчас выключена. Хотите ли вы включить ее?")
                    .setPositiveButton("Подключить",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                }
                            });
            alertDialogBuilder.setNegativeButton("Отмена",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }
    @Override
    public void onStart(){
        super.onStart();
        FlurryAgent.onStartSession(this);

    }

    @Override
    public void onStop(){
        super.onStop();
        FlurryAgent.onEndSession(this);

    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("fg", "11111111");


            new AlertDialog.Builder(this)
                    .setTitle(R.string.user_id)
                    .setMessage(R.string.app_name)
                    .setPositiveButton(R.string.avatar_url, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .create()
                    .show();


            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission. ACCESS_FINE_LOCATION)) {

                Log.i("fg", "2222222");

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.user_id)
                        .setMessage(R.string.app_name)
                        .setPositiveButton(R.string.avatar_url, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                Log.i("fg", "3333333");
            }
            return false;
        } else {
            return true;
        }
    }

}
