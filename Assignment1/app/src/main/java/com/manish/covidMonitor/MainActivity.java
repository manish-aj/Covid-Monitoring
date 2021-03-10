package com.manish.covidMonitor;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText username;
    DataBaseHelper dbObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = findViewById((R.id.username));
        final Button button1 = findViewById(R.id.userSubmit);


        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                String userName = username.getText().toString();
                dbObject = new DataBaseHelper(getApplicationContext(), userName);
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("userName", userName);
                editor.commit();
                Intent int1 =  new Intent(MainActivity.this, UserActivity.class);
                startActivity(int1);
            }
        });

    }
}