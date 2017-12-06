package com.example.michaelzhang.yum.Model;

/**
 * Created by Allan on 11/26/2017.
 */

public class Model {

    public String title,image;

    public Model(String title, String image) {
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
