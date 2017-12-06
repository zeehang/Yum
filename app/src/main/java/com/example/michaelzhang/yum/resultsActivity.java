package com.example.michaelzhang.yum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;

public class resultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView businessName = findViewById(R.id.businessName);

        ImageView businessImage = findViewById(R.id.businessImage);
        ImageView ratings = findViewById(R.id.ratings);

        TextView businessLocation = findViewById(R.id.businessAddr);

        String businessNameString = "In-N-Out";
        String businessLocString = "1234 Glitrock Avenue";

        // show The Image in a ImageView
        new DownloadImageTask(businessImage)
                .execute("https://cdn.vox-cdn.com/thumbor/97Qk1jAVFMSmi5PMZOVPjlwj8_Y=/112x0:1932x1365/1200x800/filters:focal(112x0:1932x1365)/cdn.vox-cdn.com/uploads/chorus_image/image/47639083/7980042713_9e110b767e_k.0.0.jpg");


        ratings.setImageResource(R.drawable.stars_small_1);
        businessName.setText(businessNameString);
        businessLocation.setText(businessLocString);
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
