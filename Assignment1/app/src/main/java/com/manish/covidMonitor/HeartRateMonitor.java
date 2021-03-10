package com.manish.covidMonitor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.impl.ImageAnalysisConfig;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HeartRateMonitor extends AppCompatActivity {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    ProgressBar mProgressBar;
    PreviewView mPreviewView;
    CountDownTimer timer;
    ImageView captureImage;
    List<Double> redValues = new ArrayList<Double>();
    Boolean start = true;
    ImageAnalysis imageAnalysis;
    ProcessCameraProvider cameraProvider;
    CameraSelector cameraSelector;
    int per = 10;
    SimpleMovingAverage sma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate_monitor);

        int i = 0;
        final Button button1 = findViewById(R.id.startHRM);
        mPreviewView = findViewById(R.id.previewView);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setProgress(i);

        timer = new CountDownTimer(45000, 1000) {
            int i = 0;

            public void onTick(long millisUntilFinished) {
//                Log.v("Log_tag", "Tick of Progress" + i + millisUntilFinished);
                i++;
                mProgressBar.setProgress((int) i * 10 / (45));
//                mTimer.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
//                mTimer.setText("done!");
                i++;
                start = false;
                mProgressBar.setProgress(100);
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(1000);
                }
//                int heartRate = PeakDetector.getPeaks(redValues, 20, 5.0, 0.5);

                int heartRate = sma.getPeakCount();

                System.out.println(heartRate);
                Intent returnHeartRate = new Intent();
                returnHeartRate.putExtra("Heart_RATE", heartRate);
                setResult(Activity.RESULT_OK, returnHeartRate);
                finish();
            }
        };

        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                start = true;
                timer.start();
                sma = new SimpleMovingAverage(per);
                imageAnalysis = startImageAnalysis();
                cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.bindToLifecycle((LifecycleOwner) HeartRateMonitor.this, cameraSelector, imageAnalysis);



            };

        });
//        captureImage = findViewById(R.id.captureImg);

        if (allPermissionsGranted()) {
            startCamera();
            //start camera if permission has been granted by use
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }

    public ImageAnalysis startImageAnalysis(){
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();



        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new ImageAnalysis.Analyzer() {

            @Override
            public void analyze(@NonNull ImageProxy image) {
                if(start == true) {
                    System.out.print("log");
                    Bitmap bitmap = mPreviewView.getBitmap();
                    if(bitmap == null){
                        return;
                    }
                    System.out.print("log");
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int lengthOfPixels = width * height;
                    int[] pixels = new int[lengthOfPixels];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    int sum = 0;
                    int R;
                    for (int i = 0; i < lengthOfPixels; i++) {
                        R = (pixels[i] >> 16) & 0xff;
                        sum += R;
                    }
                    double avgRed = sum / lengthOfPixels;

                    System.out.println(avgRed);
                    sma.addData((float) avgRed);
//                    redValues.add(avgRed);
//                    System.out.println(redValues.size());
                    image.close();
                }
            }

        });
        return imageAnalysis;
    }

    public void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();



        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
        CameraControl cameraControl = camera.getCameraControl();
        cameraControl.enableTorch(true);
//        captureImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
//                File file = new File(HeartRateMonitor.this.getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");
//
//                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
//                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                        new Handler(Looper.getMainLooper()).post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(HeartRateMonitor.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException error) {
//                        error.printStackTrace();
//                    }
//                });
//            }
//        });
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
}