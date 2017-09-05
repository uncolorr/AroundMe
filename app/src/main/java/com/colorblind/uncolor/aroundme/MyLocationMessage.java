package com.colorblind.uncolor.aroundme;

import com.loopj.android.http.RequestParams;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

/**
 * Created by uncolor on 02.08.17.
 */

public class MyLocationMessage implements IMessage, MessageContentType.Image {
    private String message_id;
    private String text;
    private String login;
    private String date;
    private long long_date;
    private String user_id;
    private String avatar;

    public MyLocationMessage(String message_id, String text, String login, String date, String user_id, String avatar) {

        this.message_id = message_id;
        this.date = date;
        this.login = login;
        this.text = text;
        long_date = Long.valueOf(date).longValue();
        this.user_id = user_id;
        this.avatar = avatar;

    }

    @Override
    public String getId() {
        return message_id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        Author author = new Author(user_id, login, avatar);
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return new Date(long_date);
    }


    @Override
    public String getImageUrl() {

        String string = text;
        String[] coordinates = string.split(" ");
        String latitude = coordinates[0];
        String longitude = coordinates[1];


        String URL = "https://maps.googleapis.com/maps/api/staticmap?";
        RequestParams params = new RequestParams();
        params.put("center",latitude + "," + longitude);
        params.put("zoom", "14");
        params.put("size", "1000x800");
        params.put("key", "AIzaSyDKfZkd7pQ1FXbSACL9jrmGw-tbkl34icE");
        URL += params.toString();
        return URL;
    }

}

