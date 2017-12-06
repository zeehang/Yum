package com.example.michaelzhang.yum;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by annikatsai on 12/5/17.
 */

public class Restaurant implements Serializable {

    public String getTitle() {
        return title;
    }

    public String getImageURL() {
        return imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getRating() {
        return rating;
    }

    public String getAddress() {
        return address;
    }

    public int getChosen() { return chosen; }

    public void decrement() { chosen --; }

    public void increment() { chosen ++; }

    String title;
    String imageUrl;
    String url;
    String rating;
    String address;
    int chosen;

    public Restaurant(JSONObject jsonObject) {
        try {
            this.title = jsonObject.getString("name");
            this.imageUrl = jsonObject.getString("image_url");
            this.url = jsonObject.getString("url");
            this.rating = jsonObject.getString("rating");
            this.chosen = 0;
            if (jsonObject.getJSONObject("location").getString("address1") != "") {
                this.address = jsonObject
                        .getJSONObject("location")
                        .getString("address1")
                        .concat(", ")
                        .concat(jsonObject.getJSONObject("location").getString("city"))
                        .concat(", ")
                        .concat(jsonObject.getJSONObject("location").getString("state"))
                        .concat(" ")
                        .concat(jsonObject.getJSONObject("location").getString("zip_code"));
            }
            else {
                this.address = "";
            }

        } catch (JSONException e) {}
    }

    public static ArrayList<Restaurant> fromJsonArray(JSONArray array) {
        ArrayList<Restaurant> results = new ArrayList<>();
        for (int x = 0; x < array.length(); x++) {
            try {
                results.add(new Restaurant(array.getJSONObject(x)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }
}
