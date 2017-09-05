package com.colorblind.uncolor.aroundme;

import com.stfalcon.chatkit.commons.models.IUser;

/**
 * Created by uncolor on 22.06.17.
 */

public class Author implements IUser {

    private String id;
    private String name;
    private String avatar;
    public  Author(String id, String name, String avatar){

        this.avatar = avatar;
        this.id = id;
        this.name = name;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }
}
