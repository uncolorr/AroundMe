package com.example.uncolor.aroundme;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by uncolor on 15.06.17.
 */

public class Room implements Parcelable{

    private String title;
    private String usersCount;
    private String room_id;
    private boolean inFavs;
    private boolean isAdmin;
    private float distance;

    public Room() {

    }

    public Room(String title, String usersCount) {
        this.title = title;
        this.usersCount = usersCount;
    }


    protected Room(Parcel in) {
        title = in.readString();
        usersCount = in.readString();
        room_id = in.readString();
        inFavs = in.readByte() != 0;
        isAdmin = in.readByte() != 0;
        distance = in.readFloat();
    }

    public static final Creator<Room> CREATOR = new Creator<Room>() {
        @Override
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        @Override
        public Room[] newArray(int size) {
            return new Room[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(String usersCount) {
        this.usersCount = usersCount;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(usersCount);
        dest.writeString(room_id);
        dest.writeByte((byte) (inFavs ? 1 : 0));
        dest.writeByte((byte) (isAdmin ? 1 : 0));
        dest.writeFloat(distance);
    }

    public boolean isInFavs() {
        return inFavs;
    }

    public void setInFavs(boolean inFavs) {
        this.inFavs = inFavs;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}

