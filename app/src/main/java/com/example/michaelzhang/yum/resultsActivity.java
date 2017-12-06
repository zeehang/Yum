package com.example.michaelzhang.yum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class resultsActivity extends AppCompatActivity {

    private Restaurant mRestaurant = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Intent intent = getIntent();
        mRestaurant = (Restaurant) intent.getSerializableExtra("restaurant");

        TextView businessName = findViewById(R.id.businessName);
        CardView wholeCard = findViewById(R.id.wholeCard);
        ImageView businessImage = findViewById(R.id.businessImage);
        ImageView ratings = findViewById(R.id.ratings);

        TextView businessLocation = findViewById(R.id.businessAddr);

        String businessNameString = mRestaurant.getTitle();
        String businessLocString = mRestaurant.getAddress();

        String rating = mRestaurant.getRating();

        String image = mRestaurant.getImageURL();
        double ratingDouble = Double.parseDouble(rating);

        // show The Image in a ImageView
        new DownloadImageTask(businessImage)
                .execute(image);


        wholeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(mRestaurant.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        if(ratingDouble >= 0 && ratingDouble < 0.5) {
            ratings.setImageResource(R.drawable.stars_small_0);
        } else if (ratingDouble >= 0.5 && ratingDouble <= 1.2) {
            ratings.setImageResource(R.drawable.stars_small_1);
        } else if (ratingDouble > 1.2 && ratingDouble <= 1.7) {
            ratings.setImageResource(R.drawable.stars_small_1_half);
        } else if (ratingDouble > 1.7 && ratingDouble <= 2.2) {
            ratings.setImageResource(R.drawable.stars_small_2);
        } else if (ratingDouble > 2.2 && ratingDouble <= 2.7) {
            ratings.setImageResource(R.drawable.stars_small_2_half);
        } else if (ratingDouble > 2.7 && ratingDouble <= 3.2) {
            ratings.setImageResource(R.drawable.stars_small_3);
        } else if (ratingDouble > 3.2 && ratingDouble <= 3.7) {
            ratings.setImageResource(R.drawable.stars_small_3_half);
        } else if (ratingDouble > 3.7 && ratingDouble <= 4.2) {
            ratings.setImageResource(R.drawable.stars_small_4);
        } else if (ratingDouble > 4.2 && ratingDouble <= 4.7) {
            ratings.setImageResource(R.drawable.stars_small_4_half);
        } else if (ratingDouble > 4.7 && ratingDouble <= 5) {
            ratings.setImageResource(R.drawable.stars_small_5);
        }

        businessName.setText(businessNameString);
        businessLocation.setText(businessLocString);


    }

    public void returnHome(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }



}
