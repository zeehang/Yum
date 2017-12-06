package com.example.michaelzhang.yum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.michaelzhang.yum.Adapter.CardAdapter;
import com.example.michaelzhang.yum.Model.Model;
import com.huxq17.swipecardsview.SwipeCardsView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class YelpActivity extends AppCompatActivity {

    ArrayList<Restaurant> restaurants = new ArrayList<>();
    SwipeCardsView swipeCardsView;
    List<Model> modelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp);

        swipeCardsView = (SwipeCardsView)findViewById(R.id.swipeCardsView);
        swipeCardsView.retainLastCard(false);
        swipeCardsView.enableSwipe(true);

        Intent intent = getIntent();
        String location = intent.getStringExtra("location");
        getRestaurants(location);
    }

    private void getRestaurants(String location) {
        final YelpService yelpService = new YelpService();
        yelpService.findRestaurants(location, new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();

                    //Log.v("YelpActivity", jsonData);

                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONArray jsonResults = jsonObject.getJSONArray("businesses");
                    restaurants.addAll(Restaurant.fromJsonArray(jsonResults));
                    runOnUiThread(new Runnable(){
                        @Override
                        public void run() {
                            getData();
                        }
                    });
                    //Log.v("Restarants", restaurants.get(0).getAddress());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void getData() {

            //modelList.add(new Model("Spiderman", "http://i.annihil.us/u/prod/marvel/i/mg/2/00/53710b14a320b.png"));
            //modelList.add(new Model("Irom-Man", "https://lumiere-a.akamaihd.net/v1/images/usa_avengers_chi_ironman_n_cf2a66b6.png?region=0%2C0%2C300%2C300"));

        for(int i=0; i<restaurants.size(); i++)
        {
            modelList.add(new Model(restaurants.get(i).getTitle(),restaurants.get(i).getImageURL()));
        }

            CardAdapter cardAdapter = new CardAdapter(modelList,this);
            swipeCardsView.setAdapter(cardAdapter);
    }
}
