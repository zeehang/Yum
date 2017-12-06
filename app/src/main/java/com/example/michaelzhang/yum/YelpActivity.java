package com.example.michaelzhang.yum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

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
    boolean isHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yelp);

        swipeCardsView = (SwipeCardsView)findViewById(R.id.swipeCardsView);
        swipeCardsView.retainLastCard(false);
        swipeCardsView.enableSwipe(true);

        swipeCardsView.setCardsSlideListener(new SwipeCardsView.CardsSlideListener() {
            @Override
            public void onShow(int index) {
                Log.i("yelpactivity","test showing index = "+index);
            }

            @Override
            public void onCardVanish(int index, SwipeCardsView.SlideType type) {
                String orientation = "";
                switch (type){
                    case LEFT:
                        restaurants.get(index).decrement();
                        break;
                    case RIGHT:
                        restaurants.get(index).increment();
                        break;
                }
                if(index == restaurants.size()-1) {
                    Intent intent = new Intent();
                    intent.putExtra("restaurants", restaurants);
                    if(isHost) {
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                    else{
                        int[] preferred = new int[restaurants.size()];
                        for(int i = 0; i < restaurants.size(); i++) {
                            preferred[i] = restaurants.get(i).getChosen();
                        }
                        intent.putExtra("preferred", preferred);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            }

            @Override
            public void onItemClick(View cardImageView, int index) {
                //toast("点击了 position="+index);
            }
        });

        Intent intent = getIntent();
        String location = intent.getStringExtra("location");
        if(intent.getStringExtra("id") == "host") {
            isHost = true;
        }
        else {
            isHost = false;
        }
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

        for(int i=0; i<restaurants.size(); i++)
        {
            modelList.add(new Model(restaurants.get(i).getTitle(),restaurants.get(i).getImageURL(), restaurants.get(i).getRating(), restaurants.get(i).getAddress(), restaurants.get(i).getUrl()));
        }

            CardAdapter cardAdapter = new CardAdapter(modelList,this);
            swipeCardsView.setAdapter(cardAdapter);
    }
}
