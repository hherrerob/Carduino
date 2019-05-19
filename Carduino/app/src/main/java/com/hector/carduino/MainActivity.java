package com.hector.carduino;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.zagum.switchicon.SwitchIconView;

import eo.view.batterymeter.BatteryMeterView;

/**
 * Contiene el acceso a todas las funcionalidades principales de la aplicación
 */
public class MainActivity extends AppCompatActivity {
    public static final int NAMECHANGE_RESULT = 9;

    private ImageButton refresh, settings;
    private LinearLayout climateOption, driveOption, controlsOption, locationOption, summonOption;
    private BatteryMeterView motorBatteryIndicator, arduinoBatteryIndicator;
    private RelativeLayout isConnected;
    private Boolean _isConnected;
    private SwitchIconView lock, vent;
    private TextView carName;

    private int[] antiSpam;
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
        BluetoothAdapter.getDefaultAdapter().enable();
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
        this.antiSpam = new int[] {0, 0};
        this._isConnected = false;

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
        this.vent.setIconEnabled(false);
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

        this.motorBatteryIndicator = findViewById(R.id.BATTERY_MOTOR);
        this.motorBatteryIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_battery_level_motor) + currentStatus.get_battery_2() + "%", Toast.LENGTH_LONG).show();
            }
        });

        this.arduinoBatteryIndicator = findViewById(R.id.BATTERY_ARDUINO);
        this.arduinoBatteryIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, getString(R.string.msg_battery_level_arduino) + currentStatus.get_battery_1() + "%", Toast.LENGTH_LONG).show();
            }
        });

        this.carName = findViewById(R.id.CARNAME);
        Settings _settings = new Settings();
        _settings.getPrefs(this);
        this.carName.setText(_settings.get_carName().toString());

        // Abre la actividad de conducir
        this.driveOption = findViewById(R.id.DRIVE);
        this.driveOption .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_isConnected) {
                    Intent intent = new Intent(MainActivity.this, DriveActivity.class);
                    intent.putExtra("status", currentStatus);
                    startActivity(intent);
                } else Toast.makeText(MainActivity.this, getString(R.string.error_no_bt_con), Toast.LENGTH_LONG).show();
            }
        });

        // Abre la actividad de invocar
        this.summonOption = findViewById(R.id.SUMMON);
        this.summonOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_isConnected) {
                    Intent intent = new Intent(MainActivity.this, SummonActivity.class);
                    startActivity(intent);
                } else Toast.makeText(MainActivity.this, getString(R.string.error_no_bt_con), Toast.LENGTH_LONG).show();
            }
        });

        // Abre la actividad de control de temperatura
        this.climateOption = findViewById(R.id.CLIMATE);
        this.climateOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_isConnected) {
                    Intent intent = new Intent(MainActivity.this, ClimateActivity.class);
                    intent.putExtra("status", currentStatus);
                    startActivity(intent);
                } else Toast.makeText(MainActivity.this, getString(R.string.error_no_bt_con), Toast.LENGTH_LONG).show();
            }
        });

        // Abre la actividad con la localización
        this.locationOption = findViewById(R.id.LOCATION);
        this.locationOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("isConnected", _isConnected);
                intent.putExtra("currentStatus", currentStatus);
                startActivity(intent);
            }
        });

        // Abre la actividad con los controles
        this.controlsOption = findViewById(R.id.CONTROLS);
        this.controlsOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_isConnected) {
                    Intent intent = new Intent(MainActivity.this, ControlsActivity.class);
                    startActivity(intent);
                } else Toast.makeText(MainActivity.this, getString(R.string.error_no_bt_con), Toast.LENGTH_LONG).show();
            }
        });

        // Abre los ajustes
        this.settings = findViewById(R.id.SETTINGS);
        this.settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, NAMECHANGE_RESULT);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            Settings _settings = new Settings();
            _settings.getPrefs(this);
            this.carName.setText(_settings.get_carName());
        }
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

    public void dashboardHandler(final Status currentStatus, boolean connected) {
        if(connected) {
            this.currentStatus = currentStatus;
            this.isConnected.setBackgroundResource(R.drawable.circle_green);
            this._isConnected = true;
            this.arduinoBatteryIndicator.setChargeLevel(currentStatus.get_battery_1());
            this.motorBatteryIndicator.setChargeLevel(currentStatus.get_battery_2());
        } else {
            this.isConnected.setBackgroundResource(R.drawable.circle_red);
            this._isConnected = false;

            this.arduinoBatteryIndicator.setChargeLevel(null);
            this.motorBatteryIndicator.setChargeLevel(null);
        }

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                check(lock, currentStatus.is_locked(), 0);
                check(vent, currentStatus.is_vent(), 1);
            }
        });

        //TODO: COMPLETAR
    }

    public void check(SwitchIconView switchIcon, boolean value, int index) {
        if(switchIcon.isIconEnabled() != value) {
            if(antiSpam[index] > 2) {
                switchIcon.setIconEnabled(value);
                antiSpam[index] = 0;
            } else antiSpam[index]++;
        } else antiSpam[index] = 0;
    }
}
