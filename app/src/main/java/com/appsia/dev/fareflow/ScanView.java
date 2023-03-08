package com.appsia.dev.fareflow;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Size;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ScanView extends AppCompatActivity {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture;
    private Camera cam;
    private boolean torchEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_view);

        final ImageView flashBtn = findViewById(R.id.flashBtn);
        final ImageView galleryBtn = findViewById(R.id.galleryBtn);
        final ImageView cameraChangeBtn = findViewById(R.id.cameraChangeBtn);
        previewView = findViewById(R.id.cameraPreview);

        // checking for camera permissions
        if (ContextCompat.checkSelfPermission(ScanView.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            ActivityCompat.requestPermissions(ScanView.this, new String[]{Manifest.permission.CAMERA}, 101);
        }



        flashBtn.setOnClickListener(view -> {
            if (torchEnabled) {
                cam.getCameraControl().enableTorch(false);
                torchEnabled = false;
                flashBtn.setImageResource(R.drawable.flash_on_icon);
            } else {
                cam.getCameraControl().enableTorch(true);
                torchEnabled = true;
                flashBtn.setImageResource(R.drawable.flash_off_icon);
            }

        });

        galleryBtn.setOnClickListener(view -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);

            // pass the constant to compare it
            // with the returned requestCode
            startActivityForResult(Intent.createChooser(i, "Select Picture"), 101);

        });

        cameraChangeBtn.setOnClickListener(view -> finish());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 101) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();

                if (null != selectedImageUri) {
                    InputImage image;
                    try {
                        image = InputImage.fromFilePath(ScanView.this, selectedImageUri);
                        processBarcodeImage(image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void init() {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance(ScanView.this);

        cameraProviderListenableFuture.addListener(() -> {

            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                bindImageAnalysis(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(ScanView.this));
    }



    private void bindImageAnalysis(ProcessCameraProvider processCameraProvider) {

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(ScanView.this), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {

                @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();

                if (mediaImage != null) {
                    InputImage image2 = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

                    BarcodeScanner scanner = BarcodeScanning.getClient();

                    Task<List<Barcode>> results = scanner.process(image2);
                    results.addOnSuccessListener(barcodes -> {

                        if (barcodes.size() > 0) {
                            Barcode barcode = barcodes.get(0);

                            // vibrate phone
                            vibratePhone();

                            final String getValue = barcode.getRawValue();

                            Intent intent = new Intent(ScanView.this, Inicio.class);
                            intent.putExtra("type", barcode.getValueType());

                            if (barcode.getValueType() == Barcode.TYPE_TEXT) {
                                intent.putExtra("text",getValue);
                            }
                            else if(barcode.getValueType()==Barcode.TYPE_URL){
                                intent.putExtra("url", getValue);
                            }//if url
                            startActivity(intent);
                            finish();

                        }
                        else{
                            image.close();
                            mediaImage.close();
                        }
                    });
                }
            }
        });

        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cam = processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    private boolean processBarcodeImage(InputImage image2) {

        final boolean[] barcodeFound = {false};

        BarcodeScanner scanner = BarcodeScanning.getClient();

        Task<List<Barcode>> results = scanner.process(image2);
        results.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
            @Override
            public void onSuccess(List<Barcode> barcodes) {

                if(barcodes.size() > 0){

                    Barcode barcode =barcodes.get(0);
                    
                    // vibrate phone
                    vibratePhone();

                    barcodeFound[0] = true;
                    final String getValue = barcode.getRawValue();

                   Intent intent = new Intent(ScanView.this, Inicio.class);
                   intent.putExtra("type", barcode.getValueType());

                    if (barcode.getValueType() == Barcode.TYPE_TEXT) {
                        intent.putExtra("text",getValue);
                    }
                    else if(barcode.getValueType()==Barcode.TYPE_URL){
                        intent.putExtra("url", getValue);
                    }//if url
                  startActivity(intent);
                  finish();
                }
            }
        });

        return barcodeFound[0];
    }

    private void vibratePhone() {

        // get the VIBRATOR_SERVICE system service
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            Toast.makeText(ScanView.this, "Permissions Denied", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        //pasamos a pantalla de registro o login por cerrar sesion
        startActivity(new Intent(getApplicationContext(),Inicio.class));
        //finalizamos esta clase (visual incluido)para que no se pueda regresar despues de cerrar sesion
        finish();
    }
}//class