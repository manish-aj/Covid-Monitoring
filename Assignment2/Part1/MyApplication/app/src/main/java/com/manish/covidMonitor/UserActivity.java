package com.manish.covidMonitor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UserActivity extends AppCompatActivity {
    int heartRate;
    int breathRate;
    long latitude;
    long longitude;
    TextView heartRateText;
    TextView breathRateText;
    DataBaseHelper dbObject;
    FusedLocationProviderClient fusedLocationClient;

    private static final String DB_PATH = "/data/data/com.manish.covidMonitor/databases/";
    private static final String PRIVATE_SERVER_UPLOAD_URL = "http://192.168.0.48:8080/uploadDBs.php";
    private static final String CHARSET = "UTF-8";

    private String url = "http://" + "10.0.2.2" + ":" + 5000 + "/";
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;

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

        final Button button5 = findViewById(R.id.UploadDB);



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
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("heartRate", heartRate);
                editor.putInt("breathRate", breathRate);
                editor.putLong("latitude", latitude);
                editor.putLong("longitude", longitude);
                editor.commit();
                dbObject = new DataBaseHelper(getApplicationContext(), userName);
                dbObject.insertOrUpdateData(System.currentTimeMillis(), heartRate, breathRate, latitude, longitude, null);
            }
        });

        button4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Intent int3= new Intent(UserActivity.this, SymptomsActivity.class);
                startActivity(int3);
            }
        });

        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
                String userName = sharedPref.getString("userName", "user");

                String pickDBPath = DB_PATH + userName;
                new Thread(() -> {
                    try  {
                        File uploadFile = new File(pickDBPath);

                        MultipartUploader multipart = new MultipartUploader(PRIVATE_SERVER_UPLOAD_URL, CHARSET);
                        multipart.addNewFile("fileUpload", uploadFile);

                        List<String> response = multipart.completeUpload();
                        for (String line : response) {
                            if (!line.equals("DB Uploaded Successfully to the Server")) {
                                throw new IOException(line);
                            } else {
                                runOnUiThread(() -> Toast.makeText(UserActivity.this, line, Toast.LENGTH_SHORT).show());
                            }
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(UserActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();

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

    private RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        mediaType = MediaType.parse("text/plain");
        requestBody = RequestBody.create(postBodyString, mediaType);
        return requestBody;
    }


    private void postRequest(String message, String URL) {
        RequestBody requestBody = buildRequestBody(message);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request
                .Builder()
                .post(requestBody)
                .url(URL)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(UserActivity.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        call.cancel();


                    }
                });

            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(UserActivity.this, response.body().string(), Toast.LENGTH_LONG).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }
        });
    }
}