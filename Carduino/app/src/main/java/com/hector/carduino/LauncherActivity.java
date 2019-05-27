package com.hector.carduino;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LauncherActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private CountDownTimer cdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void exit(long time, long interval) {
        this.cdt = new CountDownTimer(time, interval) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startActivity(new Intent(LauncherActivity.this, MainActivity.class));
                LauncherActivity.this.finish();
            }
        };

        this.cdt.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            exit(1000, 1000);
        } else exit(1, 1);
    }
}
