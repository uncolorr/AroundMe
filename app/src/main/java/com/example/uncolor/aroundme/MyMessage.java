package com.example.uncolor.aroundme;

import android.util.Log;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;
import java.util.StringTokenizer;

/**
 * Created by uncolor on 22.06.17.
 */

public class MyMessage implements IMessage {

    private String message_id;
    private String text;
    private String login;
    private String date;
    private long long_date;
    private String user_id;
    private String avatar;


    public MyMessage(String message_id, String text, String login, String date, String user_id, String avatar) {

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


}
