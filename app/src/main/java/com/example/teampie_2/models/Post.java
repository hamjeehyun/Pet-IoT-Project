package com.example.teampie_2.models;

import com.google.firebase.database.Exclude;

public class Post {
    public String author;
    public String title;
    public String description;
    public String imageUri;
    public String date;

    public Post() {
        // public no-arg constructor needed

    }

    public Post(String author, String title, String description, String imageUri, String date) {
        if(title.trim().equals("")) {
            title = "No Name";
        }

        this.author = author;
        this.title = title;
        this.description = description;
        this.imageUri = imageUri;
        this.date = date;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getDate () {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
