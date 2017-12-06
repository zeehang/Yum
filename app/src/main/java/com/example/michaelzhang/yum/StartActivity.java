package com.example.michaelzhang.yum;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        Typeface titleFont = Typeface.createFromAsset(getAssets(), "fonts/Pacifico.ttf");
        tvTitle.setTypeface(titleFont);

        Button beginBtn = (Button) findViewById(R.id.btnBegin);
        beginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(StartActivity.this, SetupBluetoothActivity.class);
                startActivity(intent);
            }
        });
    }

}
