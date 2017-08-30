package com.example.uncolor.aroundme;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by uncolor on 15.06.17.
 */

public class Room implements Parcelable {

    private String title;
    private String usersCount;
    private String room_id;
    private boolean inFavs;
    private boolean isAdmin;
    private double latitude;
    private double longitude;
    private int radius;
    private int meters;

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
        latitude = in.readDouble();
        longitude = in.readDouble();
        radius = in.readInt();
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

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getUsersCount() {
        return usersCount;
    }

    void setUsersCount(String usersCount) {
        this.usersCount = usersCount;
    }

    String getRoom_id() {
        return room_id;
    }

    void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    int getRadius() {
        return radius;
    }

    void setRadius(int radius) {
        this.radius = radius;
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
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(meters);

    }

    boolean isInFavs() {
        return inFavs;
    }

    void setInFavs(boolean inFavs) {
        this.inFavs = inFavs;
    }

    boolean isAdmin() {
        return isAdmin;
    }

    void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    void setMeters(int meters) {
        this.meters = meters;
    }

    int getMeters() {
        return meters;
    }

}

