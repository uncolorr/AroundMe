package com.example.uncolor.aroundme;

/**
 * Created by uncolor on 15.06.17.
 */

public class Room {

    private String title;
    private String usersCount;
    private String room_id;

    public Room(){

    }

    public Room(String title, String usersCount){
        this.title = title;
        this.usersCount = usersCount;
    }


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
}
