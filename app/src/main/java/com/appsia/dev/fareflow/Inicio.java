package com.appsia.dev.fareflow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.mlkit.vision.barcode.common.Barcode;

import org.json.JSONException;
import org.json.JSONObject;

public class Inicio extends AppCompatActivity {

    RequestQueue request;
    TextView valueUSD,valueMXN,valueNEAR,MyNear,STnear;
    ImageButton Qr,profile,back;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        data = getIntent().getStringExtra("text");


        //inicializacion de visuales
        valueNEAR=findViewById(R.id.txt_near);
        valueMXN=findViewById(R.id.txt_mxn);
        valueUSD=findViewById(R.id.txt_usd);
        MyNear=findViewById(R.id.txt_COnear);
        STnear=findViewById(R.id.txt_STnear);
        Qr=findViewById(R.id.img_qr_Btn);
        profile=findViewById(R.id.img_profile_btn);
        back=findViewById(R.id.btnback);

        //inicializo el request
        request= Volley.newRequestQueue(getApplicationContext());

        getPriceNear();
        permisos();
        resultadoQR();

        Qr.setOnClickListener(v -> {
            Intent intent = new Intent(Inicio.this, ScanView.class);
            startActivity(intent);
            finish();

        });
        profile.setOnClickListener(v -> {
            String url="https://wallet.near.org/recover-account";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);

        });
        back.setOnClickListener(v -> cerrar());

    }//onCreate
//--------------------------------------------------------------------------------------------------------

    public void resultadoQR(){

        final int getType = getIntent().getIntExtra("type", 0);

        if (getType == Barcode.TYPE_URL) {
            String datos = getIntent().getStringExtra("url");
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(datos));
            startActivity(i);
        }//if
        else if(getType!=0){
            Toast.makeText(getApplicationContext(),R.string.qr_error, Toast.LENGTH_LONG).show();
        }
        else{


        }//
    }//resultadoQR
//--------------------------------------------------------------------------------------------------------
    public void getPriceNear() {
        String url = "https://api.coingecko.com/api/v3/simple/price?ids=near&vs_currencies=mxn,usd,rub,eur";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    // Manejar la respuesta exitosa
                    try {
                        JSONObject nearObj = response.getJSONObject("near");
                    double  mxnPrice = nearObj.getDouble("mxn");
                    double  usdPrice = nearObj.getDouble("usd");
                        valueNEAR.setText("1");
                        valueMXN.setText(String.valueOf(mxnPrice));
                        valueUSD.setText(String.valueOf(usdPrice));

                        STnear.setText("23");
                        MyNear.setText("120");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Manejar el error
                    Log.e("Error", error.toString());
                });

// Agregar la solicitud a la cola de solicitudes Volley
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);

    }//GetPriceNear
    //solicitud de permisos
    private  void permisos(){

        int permisosCamara = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int permisosLectura = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permisosCamara == PackageManager.PERMISSION_GRANTED && permisosLectura == PackageManager.PERMISSION_GRANTED){

        }//if
        else{
            requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},200);
        }//else
    }//permisos

    private void cerrar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(R.string.salir);
        builder.setPositiveButton("SI", (dialogInterface, i) -> finish());

        builder.setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }
    @Override
    public void onBackPressed() {
       cerrar();
    }//onBackPressed
}//class
