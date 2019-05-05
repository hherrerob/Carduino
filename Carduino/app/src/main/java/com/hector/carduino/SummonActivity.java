package com.hector.carduino;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Permite conducir el dispositivo en (solo) dos direcciones y a la mínima velocidad posible
 */
public class SummonActivity extends AppCompatActivity {

    private Button moveForward, moveBackwards, stopMove;
    private FloatingActionButton backButton;

    private Messenger messageSender;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summon);
        set();

        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Instancia los recursos necesarios para el funcionamiento de la aplicación
     */
    private void set() {
        this.backButton = findViewById(R.id.BACK_BUTTON);
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStop();
                SummonActivity.this.finish();
            }
        });

        this.moveForward = findViewById(R.id.FORWARD);
        this.moveForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, new String(new char[] {Command.GEAR_SHIFT[3], Command.SPEED[2]}));
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.moveBackwards = findViewById(R.id.BACKWARD);
        this.moveBackwards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, new String(new char[]{Command.GEAR_SHIFT[1], Command.SPEED[2]}));
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.stopMove = findViewById(R.id.STOP);
        this.stopMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStop();
            }
        });

    }

    public void sendStop() {
        Message msg = Message.obtain(null, BluetoothService.SEND, new String(new char[]{Command.GEAR_SHIFT[0], Command.SPEED[0]}));
        try {
            messageSender.send(msg);
        } catch (RemoteException e) { }
    }

    @Override
    protected void onStop() {
        sendStop();
        super.onStop();
    }

    @Override
    protected void onPause() {
        sendStop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        sendStop();
        super.onDestroy();
    }
}
