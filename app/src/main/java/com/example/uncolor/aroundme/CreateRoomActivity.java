package com.example.uncolor.aroundme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

public class CreateRoomActivity extends AppCompatActivity {

    View actionBarCreateRoom;
    LayoutInflater inflater;
    ImageButton imageButtonCancel;
    ImageButton imageButtonCreateRoom;
    FragmentTransaction transaction = null;
    User user;
    CreateRoom createRoom = CreateRoom.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        actionBarCreateRoom = inflater.inflate(R.layout.create_room_action_bar, null);
        imageButtonCancel = (ImageButton)actionBarCreateRoom.findViewById(R.id.imageButtonCancel);
        imageButtonCreateRoom = (ImageButton)actionBarCreateRoom.findViewById(R.id.imageButtonCreateRoom);
        user = getIntent().getParcelableExtra("user");

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

    public void onClickImageButtonCancel(View view){
        onBackPressed();
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void onClickImageButtonCreateRoom(View view){

        Room room = createRoom.createNewRoom();
        if(room == null){
            Log.i("fg", "room null");
        }
        if(room != null) {
            Intent intent = new Intent(CreateRoomActivity.this, Dialog.class);
            intent.putExtra("user", user);
            intent.putExtra("room_id", room.getRoom_id());
            intent.putExtra("room_name", room.getTitle());
            startActivity(intent);
        }

    }
}
