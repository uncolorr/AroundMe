package com.example.uncolor.aroundme;

/**
 * Created by uncolor on 18.08.17.
 */

public class UserItem {
    private String login = "";
    private String avatar_url = "";

    public UserItem(){

    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }
}
