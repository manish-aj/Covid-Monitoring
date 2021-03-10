package com.manish.covidMonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;
public class BreathMonitor extends AppCompatActivity {

    List<Double>  accelValuesX = new ArrayList<Double>();
    List<Double>  accelValuesY = new ArrayList<Double>();
    List<Double>  accelValuesZ = new ArrayList<Double>();

    SensorManager sensorManager;
    Sensor accelerometer;
    CountDownTimer timer;
    ProgressBar mProgressBar;
    int per = 30;
    SimpleMovingAverage sma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath_monitor);

        int i = 0;

        final Button button2 = findViewById(R.id.startResp);
        mProgressBar = findViewById(R.id.progressBarResp);
        mProgressBar.setProgress(i);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

//        textX = (TextView) findViewById(R.id.textX);
//        textY = (TextView) findViewById(R.id.textY);
//        textZ = (TextView) findViewById(R.id.textZ);
        timer = new CountDownTimer(45000, 1000) {
            int i = 0;

            public void onTick(long millisUntilFinished) {
                Log.v("Log_tag", "Tick of Progress" + i + millisUntilFinished);
                i++;
                mProgressBar.setProgress((int) i * 10 / (45000 / 1000));
//                mTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
//                mTimer.setText("done!");
                i++;
                sensorManager.unregisterListener(accelListener);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                }
                mProgressBar.setProgress(100);
//                int breathRate = PeakDetector.getPeaks(accelValuesZ, 10, 3.0, 0.3);
                int breathRate =  sma.getPeakCount();
                System.out.println(breathRate);
                Intent returnBreathRate = new Intent();
                returnBreathRate.putExtra("Breath_RATE", breathRate);
                setResult(Activity.RESULT_OK, returnBreathRate);
                finish();
            }
        };

        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                timer.start();
                sma = new SimpleMovingAverage(per);
                sensorManager.registerListener(accelListener, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
            }

            ;

        });
    }


    public void onResume() {
        super.onResume();
//        sensorManager.registerListener(accelListener, accelerometer,
//                SensorManager.SENSOR_DELAY_UI);
    }

    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(accelListener);
    }

    SensorEventListener accelListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) { }


        public void onSensorChanged(SensorEvent event) {
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];

            accelValuesX.add(x);
            accelValuesY.add(y);
            accelValuesZ.add(z);
            sma.addData((float) z);
            System.out.println(accelValuesZ.size());

        }
    };
}
