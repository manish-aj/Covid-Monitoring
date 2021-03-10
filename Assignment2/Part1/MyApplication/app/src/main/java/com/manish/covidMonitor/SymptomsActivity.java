package com.manish.covidMonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import java.util.HashMap;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;

public class SymptomsActivity extends AppCompatActivity {

    DataBaseHelper dbObject;
    int latitude;
    int longitude;

    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms);

        RatingBar nausea = findViewById(R.id.nausea);
        RatingBar headache = findViewById(R.id.headache);
        RatingBar diarrhea = findViewById(R.id.diarrhea);
        RatingBar sore_throat = findViewById(R.id.sore_throat);
        RatingBar fever = findViewById(R.id.fever);
        RatingBar muscle_ache = findViewById(R.id.muscle_ache);
        RatingBar loss_of_smell_or_taste = findViewById(R.id.loss_of_smell_or_taste);
        RatingBar cough = findViewById(R.id.cough);
        RatingBar shortness_of_breath = findViewById(R.id.shortness_of_breath);
        RatingBar feeling_tired = findViewById(R.id.feeling_tired);

        final HashMap<String, Integer> symptoms = new HashMap<String, Integer>();

        int REQUEST_CODE_PERMISSIONS = 1001;
        final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            latitude = (int) (location.getLatitude()*1000000);
                            longitude = (int) (location.getLongitude()*1000000);
                        }
                    }
                });

        nausea.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("nausea", Math.round(rating));
            }
        });

        headache.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("headache", Math.round(rating));
            }
        });

        diarrhea.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("diarrhea", Math.round(rating));
            }
        });

        sore_throat.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("soar_throat", Math.round(rating));
            }
        });

        fever.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("fever", Math.round(rating));
            }
        });

        muscle_ache.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("muscle_ache", Math.round(rating));
            }
        });

        loss_of_smell_or_taste.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("loss_of_smell_or_taste", Math.round(rating));
            }
        });

        cough.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("cough", Math.round(rating));
            }
        });

        shortness_of_breath.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("shortness_of_breath", Math.round(rating));
            }
        });

        feeling_tired.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                symptoms.put("feeling_tired", Math.round(rating));
            }
        });


        final Button button1 = findViewById(R.id.SympSubmit);


        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
                String userName = sharedPref.getString("userName", "user");
                dbObject = new DataBaseHelper(getApplicationContext(), userName);
                System.out.println(symptoms);
                SharedPreferences.Editor editor = sharedPref.edit();
                int heartRate = sharedPref.getInt("heartRate", 70);
                int breathRate =sharedPref.getInt("breathRate", 25);
                editor.commit();
                dbObject.insertOrUpdateData(System.currentTimeMillis(), heartRate, breathRate, latitude, longitude, symptoms);
                dbObject.updateSymptoms(0,symptoms);
//                dbObject.updateSymptoms(System.currentTimeMillis(), symptoms);
                Intent intent = new Intent(SymptomsActivity.this,ThankYou.class);
                startActivity(intent);
            }
        });
    }
}