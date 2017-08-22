package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class CreateRoomActivity extends AppCompatActivity {

    View actionBarCreateRoom;
    LayoutInflater inflater;
    ImageButton imageButtonUpdate;
    ImageButton imageButtonCreateRoom;
    TextView textViewCreateRoomTitle;
    FragmentTransaction transaction = null;
    User user;
    boolean isEdit;
    String room_name;
    String room_id;
    int radius;
    CreateRoom createRoom = CreateRoom.newInstance();
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "BY7KTGZPH9TS8924KJTR");

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationEnabled();
        isEdit = getIntent().getBooleanExtra("isEdit", false);
        user = getIntent().getParcelableExtra("user");
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarCreateRoom = inflater.inflate(R.layout.create_room_action_bar, null);
        imageButtonUpdate = (ImageButton) actionBarCreateRoom.findViewById(R.id.imageButtonUpdate);
        imageButtonCreateRoom = (ImageButton) actionBarCreateRoom.findViewById(R.id.imageButtonCreateRoom);
        textViewCreateRoomTitle = (TextView)actionBarCreateRoom.findViewById(R.id.textViewCreateRoomTitle);
        if(isEdit){
            textViewCreateRoomTitle.setText("Редактировать");
        }
        else {
            textViewCreateRoomTitle.setText("Добавление беседы");
        }





        if (isEdit){

            Log.i("fg" ,"true");
            room_name = getIntent().getStringExtra("room_name");
            room_id = getIntent().getStringExtra("room_id");
            radius = getIntent().getIntExtra("radius", 1);
            createRoom.getArguments().putString("room_name", room_name);
            createRoom.getArguments().putInt("radius", radius);
            createRoom.getArguments().putBoolean("isEdit", isEdit);
        }


        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(actionBarCreateRoom);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#a20022")));

        createRoom.getArguments().putParcelable("user", user);
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.createRoomMainLayout, createRoom);
        transaction.commit();



    }


    @Override
    public void onBackPressed() {
        finish();
    }

    public void onClickImageButtonCreateRoom(View view) {

        if (createRoom.isGoodData() && !isEdit) {
            createRoom.createNewRoom();
        } else if (createRoom.isGoodData() && isEdit) {
            createRoom.editRoom();
            Toast.makeText(CreateRoomActivity.this, "Комната успешно отредактирована", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(CreateRoomActivity.this, "Не удалось создать комнату", Toast.LENGTH_LONG).show();
        }

    }

    public void onClickImageButtonUpdate(View view) {
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.remove(createRoom);
        transaction.replace(R.id.createRoomMainLayout, createRoom);
        transaction.commit();
        createRoom.resetCircle();
        checkLocationEnabled();
    }

    private void checkLocationEnabled() {

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder
                    .setMessage("Передача геоданных сейчас выключена. Хотите ли вы включить ее?")
                    .setPositiveButton("Включить",
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
