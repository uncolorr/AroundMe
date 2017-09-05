package com.colorblind.uncolor.aroundme;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by uncolor on 14.06.17.
 */

public class User implements Parcelable{

    private String user_id;
    private String token;
    private String avatar_url;
    private String type;

    public User(){

    }

    public User(String user_id, String token, String avatar_url, String type){
        this.user_id = user_id;
        this.avatar_url = avatar_url;
        this.token = token;
        this.type = type;
    }

    protected User(Parcel in) {
        user_id = in.readString();
        token = in.readString();
        avatar_url = in.readString();
        type = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setData(String user_id, String token, String avatar_url, String type){
        this.user_id = user_id;
        this.avatar_url = avatar_url;
        this.token = token;
        this.type = type;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_id);
        dest.writeString(token);
        dest.writeString(avatar_url);
        dest.writeString(type);
    }
}
