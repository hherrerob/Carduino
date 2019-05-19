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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ResourceBundle;

public class ControlsActivity extends AppCompatActivity {
    private FloatingActionButton backButton;
    private ImageButton sendCommand, listCommands;
    private TextView carTrackers[], batteryTrackers[], gpsTrackers[], sensorsTrackers[];
    private TextView lastUpdate;
    private EditText console;

    private String[] blLabels;
    public Status currentStatus;

    private FeedbackTracker feedbackTracker;
    private Messenger messageSender;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = new Messenger(service);
            feedbackTracker = new FeedbackTracker(messageSender, ControlsActivity.this, FeedbackTracker.CONTROLS_ACTIVITY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);

        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);

        set();
    }


    public void set() {
        this.blLabels = new String[] {
                getString(R.string.controls_blinkers_none),
                getString(R.string.controls_blinkers_left),
                getString(R.string.controls_blinkers_right),
                getString(R.string.controls_blinkers_hazzard)
        };

        this.backButton = findViewById(R.id.BACK_BUTTON);
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlsActivity.this.finish();
            }
        });

        this.sendCommand = findViewById(R.id.SEND);
        this.sendCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(console.getText().toString().length() > 0) {
                    Message msg = Message.obtain(null, BluetoothService.SEND, console.getText().toString().toCharArray()[0]);
                    console.setText("");

                    try {
                        messageSender.send(msg);
                    } catch (RemoteException e) {
                    } catch (NullPointerException e) { }
                }
            }
        });

        this.listCommands = findViewById(R.id.LIST);
        this.listCommands.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: LISTA DE COMANDOS
            }
        });

        this.console = findViewById(R.id.CONSOLE);
        this.lastUpdate = findViewById(R.id.LAST);

        this.carTrackers = new TextView[] {
            findViewById(R.id.LOCK),
            findViewById(R.id.SPEED),
            findViewById(R.id.GEAR),
            findViewById(R.id.HEADLIGHTS),
            findViewById(R.id.POSLIGHTS),
            findViewById(R.id.BLINKERS),
            findViewById(R.id.VENT),
            findViewById(R.id.AUTOSTOP)
        };

        this.batteryTrackers = new TextView[] {
            findViewById(R.id.ABL),
            findViewById(R.id.AHPR),
            findViewById(R.id.MBL),
            findViewById(R.id.MHPR)
        };

        this.gpsTrackers = new TextView[] {
            findViewById(R.id.SATS),
            findViewById(R.id.LAT),
            findViewById(R.id.LON),
            findViewById(R.id.GPS_SPEED)
        };

        this.sensorsTrackers = new TextView[] {
            findViewById(R.id.LIGHT),
            findViewById(R.id.TEMP),
            findViewById(R.id.DTF)
        };
    }

    public void dashBoardHandler(Status currentStatus) {
        this.currentStatus = currentStatus;
        ControlsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    public void update() {
        carTrackers[0].setText(getString(R.string.controls_car_locked) + valueToOnOff(currentStatus.is_locked()));
        carTrackers[1].setText(getString(R.string.controls_car_speed) + currentStatus.get_speed());
        carTrackers[2].setText(getString(R.string.controls_car_gear) + currentStatus.get_marcha());
        carTrackers[3].setText(getString(R.string.controls_car_hl) + valueToOnOff(currentStatus.is_frontLightsOn()));
        carTrackers[4].setText(getString(R.string.controls_car_pl) + valueToOnOff(currentStatus.is_posLightsOn()));
        carTrackers[5].setText(getString(R.string.controls_car_bl) + blLabels[currentStatus.tlToStr()]);
        carTrackers[6].setText(getString(R.string.controls_car_vent) + valueToOnOff(currentStatus.is_vent()));
        carTrackers[7].setText(getString(R.string.controls_car_autostop) + valueToOnOff(currentStatus.is_autoStop()));

        batteryTrackers[0].setText(getString(R.string.controls_battery_blp) + currentStatus.get_battery_1());
        batteryTrackers[1].setText(getString(R.string.controls_battery_hpr) + currentStatus.get_pvb_1());
        batteryTrackers[2].setText(getString(R.string.controls_battery_blp) + currentStatus.get_battery_2());
        batteryTrackers[3].setText(getString(R.string.controls_battery_hpr) + currentStatus.get_pvb_2());

        gpsTrackers[0].setText(getString(R.string.controls_gps_sats) + currentStatus.get_sats());
        gpsTrackers[1].setText(getString(R.string.controls_gps_lat) + currentStatus.get_lat());
        gpsTrackers[2].setText(getString(R.string.controls_gps_lon) + currentStatus.get_lon());
        gpsTrackers[3].setText(getString(R.string.controls_gps_kmph) + currentStatus.get_kmph() + "km/h");

        sensorsTrackers[0].setText(getString(R.string.controls_car_ll) + currentStatus.get_ldrReading());
        sensorsTrackers[1].setText(getString(R.string.controls_car_temp) + currentStatus.get_temp());
        sensorsTrackers[2].setText(getString(R.string.controls_car_dto) + currentStatus.get_distance());

        lastUpdate.setText(getString(R.string.controls_last_update) + (System.currentTimeMillis() - currentStatus.getLastStatus()) + "ms");
    }

    public String valueToOnOff(boolean value) {
        if(value)
            return "On";
        else return "Off";
    }

    public String valueToOnOff(Integer value) {
        if(value == 1)
            return "On";
        else return "Off";
    }
}
