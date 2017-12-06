package com.example.michaelzhang.yum.Model;

/**
 * Created by Allan on 11/26/2017.
 */

public class Model {

    public String title;
    public String image;
    public String rating;
    public String address;
    public String url;

    public String getRating() {
        return rating;
    }

    public String getAddress() {
        return address;
    }

    public String getUrl() {
        return url;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Model(String title, String image, String rating, String address, String url) {
        this.title = title;
        this.image = image;
        this.rating = rating;
        this.address = address;
        this.url = url;
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
