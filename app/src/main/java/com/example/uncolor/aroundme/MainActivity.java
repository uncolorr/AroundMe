package com.example.uncolor.aroundme;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

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

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        checkLocationEnabled();

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

    }

    public void onImageButtonClickCreateRoom(View view) {
        Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
    }

    public void onImageButtonClickProfileSettings(View view) {
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
}
