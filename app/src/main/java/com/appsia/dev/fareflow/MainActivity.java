package com.appsia.dev.fareflow;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //hilo para pasar del splash al menu principal
        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(MainActivity.this,Inicio.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }//onCreate

    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }
}//class