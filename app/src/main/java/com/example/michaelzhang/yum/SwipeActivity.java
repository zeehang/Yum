/*
package com.example.michaelzhang.yum;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.michaelzhang.yum.Adapter.CardAdapter;
import com.example.michaelzhang.yum.Model.Model;
import com.huxq17.swipecardsview.SwipeCardsView;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by Allan on 11/26/2017.
 *//*


public class SwipeActivity extends AppCompatActivity {

    private SwipeCardsView swipeCardsView;
    private List<Model> modelList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_activity);

        swipeCardsView = (SwipeCardsView)findViewById(R.id.swipeCardsView);
        swipeCardsView.retainLastCard(false);
        swipeCardsView.enableSwipe(true);
        getData();
    }

    private void getData() {
        for(int i=0; i<1; i++)
        {
            modelList.add(new Model())
        }

        modelList.add(new Model("Spiderman", "http://i.annihil.us/u/prod/marvel/i/mg/2/00/53710b14a320b.png"));
        modelList.add(new Model("Irom-Man", "https://lumiere-a.akamaihd.net/v1/images/usa_avengers_chi_ironman_n_cf2a66b6.png?region=0%2C0%2C300%2C300"));

        CardAdapter cardAdapter = new CardAdapter(modelList,this);
        swipeCardsView.setAdapter(cardAdapter);
    }
}
*/
