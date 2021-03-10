package com.manish.covidMonitor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserActivity extends AppCompatActivity {
    int heartRate;
    int breathRate;
    TextView heartRateText;
    TextView breathRateText;
    DataBaseHelper dbObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        heartRateText = findViewById(R.id.HeartRateText);
        breathRateText = findViewById(R.id.BreathRateText);


        final Button button1 = findViewById(R.id.HRM);

        final Button button2 = findViewById(R.id.Resp);
        final Button button3 = findViewById(R.id.submit);

        final Button button4 = findViewById(R.id.Symp);


        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent int2 = new Intent(UserActivity.this, BreathMonitor.class);
                startActivityForResult(int2, 1);
            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent int1 = new Intent(UserActivity.this, HeartRateMonitor.class);
                startActivityForResult(int1, 2);

            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
                String userName = sharedPref.getString("userName", "user");
                dbObject = new DataBaseHelper(getApplicationContext(), userName);
                dbObject.insertOrUpdateData(System.currentTimeMillis(), heartRate, breathRate, null);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent int3= new Intent(UserActivity.this, SymptomsActivity.class);
                startActivity(int3);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2){
            if(resultCode == RESULT_OK) {
                heartRate = data.getIntExtra("Heart_RATE", 70);
                heartRate *= 4 / 3;
                heartRateText.setText(String.valueOf(heartRate) + " BPM");
            }
        }

        if(requestCode == 1){
            if(resultCode == RESULT_OK) {
                breathRate = data.getIntExtra("Breath_RATE", 20);
                breathRate *= 4 / 3;
                breathRateText.setText(String.valueOf(breathRate) + " BPM");
            }
        }


    }
}