package com.example.videochat.Modals;

public class Contacts
{
    String user_name, bio, image, uid;

    public Contacts(String user_name, String bio, String image, String uid) {
        this.user_name = user_name;
        this.bio = bio;
        this.image = image;
        this.uid = uid;
    }

    public Contacts() {

    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_ame) {
        this.user_name = user_ame;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}