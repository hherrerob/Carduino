package com.hector.carduino;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.github.zagum.switchicon.SwitchIconView;

/**
 * Contiene el acceso a todas las funcionalidades principales de la aplicación
 */
public class MainActivity extends AppCompatActivity {

    private ImageButton refresh, settings;
    private LinearLayout climateOption, driveOption, controlsOption, locationOption, summonOption;
    private RelativeLayout isConnected;
    private SwitchIconView lock, vent;
    private TextView carName;

    private Status currentStatus;
    private FeedbackTracker feedbackTracker;

    private Messenger messageSender;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = new Messenger(service);
            feedbackTracker = new FeedbackTracker(messageSender, MainActivity.this, FeedbackTracker.MAIN_ACTIVITY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        set();

        currentStatus = new Status();
        ConnectThread.detectBluetooth(this);
        startService(new Intent(this, BluetoothService.class));
        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Instancia las propiedades de la actividad
     */
    private void set() {
        this.lock = findViewById(R.id.LOCK);
        this.lock.setIconEnabled(true);
        this.lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock.setIconEnabled(!lock.isIconEnabled());
                Message msg;
                if(lock.isIconEnabled())
                    msg = Message.obtain(null, BluetoothService.SEND, Command.LOCK);
                else msg = Message.obtain(null, BluetoothService.SEND, Command.UNLOCK);

                try {
                    messageSender.send(msg);
                } catch (RemoteException e) {
                } catch (NullPointerException e) { }
            }
        });

        this.vent = findViewById(R.id.VENT);
        this.vent.setIconEnabled(true);
        this.vent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vent.setIconEnabled(!vent.isIconEnabled());
                Message msg;
                if(vent.isIconEnabled())
                    msg = Message.obtain(null, BluetoothService.SEND, Command.VENT_ON);
                else msg = Message.obtain(null, BluetoothService.SEND, Command.VENT_OFF);

                try {
                    messageSender.send(msg);
                } catch (RemoteException e) {
                } catch (NullPointerException e) { }
            }
        });

        this.carName = findViewById(R.id.CARNAME);
        Settings _settings = new Settings();
        _settings.getPrefs(this);
        this.carName.setText(_settings.get_carName().toString());

        // SEPARADOR
        RelativeLayout b = findViewById(R.id.SPLITTER);
        int width = 0;
        int hei = getResources().getDisplayMetrics().heightPixels/5 * 2;
        b.setLayoutParams(new LinearLayout.LayoutParams(width,hei));

        // Abre la actividad de conducir
        this.driveOption = findViewById(R.id.DRIVE);
        this.driveOption .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DriveActivity.class);
                startActivity(intent);
            }
        });

        // Abre la actividad de invocar
        this.summonOption = findViewById(R.id.SUMMON);
        this.summonOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SummonActivity.class);
                startActivity(intent);
            }
        });

        // Abre la actividad de control de temperatura
        this.climateOption = findViewById(R.id.CLIMATE);
        this.climateOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ClimateActivity.class);
                startActivity(intent);
            }
        });

        // Abre la actividad con la localización
        this.locationOption = findViewById(R.id.LOCATION);
        this.locationOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: LocationActivity
            }
        });

        // Abre la actividad con los controles
        this.controlsOption = findViewById(R.id.CONTROLS);
        this.controlsOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: ControlsActivity
            }
        });

        // Abre los ajustes
        this.settings = findViewById(R.id.SETTINGS);
        this.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // Refresca la conexión Bluetooth
        this.refresh = findViewById(R.id.REFRESH);
        this.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        // INFO SOBRE SI LA CONEXION BLUETOOTH ESTA ACTIVA
        this.isConnected = findViewById(R.id.CONNECTED);
    }

    /**
     * Construye de nuevo la comunicación Bluetooth
     */
    public void refresh() {
        Message msg = Message.obtain(null, BluetoothService.RESTART);
        try {
            messageSender.send(msg);
        } catch (RemoteException e) { }
    }

    public void dashboardHandler(Status currentStatus, boolean connected) {
        if(connected) {
            this.currentStatus = currentStatus;
            this.isConnected.setBackgroundResource(R.drawable.circle_green);
        } else {
            this.isConnected.setBackgroundResource(R.drawable.circle_red);
        }

        //TODO: COMPLETAR
    }
}
