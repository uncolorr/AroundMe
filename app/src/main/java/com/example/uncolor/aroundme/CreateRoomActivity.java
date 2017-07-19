package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class CreateRoomActivity extends AppCompatActivity {

    View actionBarCreateRoom;
    LayoutInflater inflater;
    ImageButton imageButtonUpdate;
    ImageButton imageButtonCreateRoom;
    FragmentTransaction transaction = null;
    User user;
    CreateRoom createRoom = CreateRoom.newInstance();
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        checkLocationEnabled();

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarCreateRoom = inflater.inflate(R.layout.create_room_action_bar, null);
        imageButtonUpdate = (ImageButton)actionBarCreateRoom.findViewById(R.id.imageButtonUpdate);
        imageButtonCreateRoom = (ImageButton)actionBarCreateRoom.findViewById(R.id.imageButtonCreateRoom);
        user = getIntent().getParcelableExtra("user");

        Log.i("fg", user.getUser_id());

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
    public void onBackPressed(){
        finish();
    }

    public void onClickImageButtonCreateRoom(View view)  {

        if (createRoom.isGoodData()) {
             Room room = createRoom.createNewRoom();
        }
        else {
            Toast.makeText(CreateRoomActivity.this, "Не удалось создать комнату", Toast.LENGTH_LONG).show();
        }

    }
    public void onClickImageButtonUpdate(View view){
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.remove(createRoom);
        transaction.replace(R.id.createRoomMainLayout, createRoom);
        transaction.commit();
        createRoom.resetCircle();
        checkLocationEnabled();
    }

    private void checkLocationEnabled(){

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

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
