package com.example.michaelzhang.yum;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.ServerTokenSession;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.io.InputStream;
import java.util.Arrays;

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


        //UBER
        SessionConfiguration config = new SessionConfiguration.Builder()
                // mandatory
                .setClientId(Constants.UBER_CLIENT_ID)
                // required for enhanced button features
                .setServerToken(Constants.UBER_SERVER_TOKEN)
                // required for implicit grant authentication
                // required scope for Ride Request Widget features
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                // optional: set sandbox as operating environment
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();

        UberSdk.initialize(config);

        // get current loc for ride estimates
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //ask for permissions - they should be enabled already
            return;
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // get the context by invoking ``getApplicationContext()``, ``getContext()``, ``getBaseContext()`` or ``this`` when in the activity class
        RideRequestButton requestButton = new RideRequestButton(this);

        RideParameters rideParams = new RideParameters.Builder()
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
                .setDropoffLocation(
                        mRestaurant.getLatitude(), mRestaurant.getLongitude(), businessNameString, businessLocString)
                // Required for pickup estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of pickup location
                .setPickupLocation(location.getLatitude(), location.getLongitude(), "Current Location", "Current Location")
                .build();

        ServerTokenSession session = new ServerTokenSession(config);
        requestButton.setSession(session);

        // get your layout, for instance:
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.uberLayout);
        requestButton.setRideParameters(rideParams);
        requestButton.loadRideInformation();

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        layout.addView(requestButton, params);

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
