package com.hector.carduino;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.channguyen.rsv.RangeSliderView;
import com.rm.rmswitch.RMSwitch;

/**
 * Gestiona la conducción del vehículo
 */
public class DriveActivity extends AppCompatActivity {

    private FloatingActionButton brake, buzzer, flash, refresh;
    private ImageView positionLights, headLights, emergencyLights, parkingSign, frostSign, cruiseControl, steeringWheel;
    public TextView speedDisplay;
    private RMSwitch leftBlinker, rightBlinker;
    private RangeSliderView gearShift, speedControl;

    private Status currentStatus;
    private FeedbackTracker feedbackTracker;
    private boolean isConnected;

    private Messenger messageSender;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = new Messenger(service);
            feedbackTracker = new FeedbackTracker(messageSender, DriveActivity.this, FeedbackTracker.DRIVE_ACTIVITY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        //TODO: recoger parametros de intent con la configuración
        set();

        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Instancia los componentes y recursos necesarios para la actividad
     */
    private void set() {
        this.brake = findViewById(R.id.BRAKE);
        this.brake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.BRAKE);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.buzzer = findViewById(R.id.BUZZER);
        this.buzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.HONK);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.flash = findViewById(R.id.FLASH);
        this.flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.FLASH);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.refresh = findViewById(R.id.REFRESH);
        this.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.RESTART);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.positionLights = findViewById(R.id.POSITION_LIGHTS);
        this.positionLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.POSITION_LIGHTS);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.headLights = findViewById(R.id.HEADLIGHTS);
        this.headLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.HEADLIGHTS);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.emergencyLights = findViewById(R.id.EMERGENCY_LIGHT);
        this.emergencyLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.EMERGENCY_LIGHT);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        // Solo indicativos
        this.parkingSign = findViewById(R.id.PARKING);
        this.frostSign = findViewById(R.id.FROST);
        this.speedDisplay = findViewById(R.id.SPEED);

        this.cruiseControl = findViewById(R.id.CRUISE_CONTROL);
        this.cruiseControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Enviar comando
                //connectThread.sendParameter();
            }
        });

        //TODO: rotate
        this.steeringWheel = findViewById(R.id.STEERING_WHEEL);

        /** Activa/Desactiva el intermitente izquierdo y desactiva el derecho(si está activado) */
        this.leftBlinker = findViewById(R.id.LEFT_BLINKER);
        this.leftBlinker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftBlinker.setChecked(!leftBlinker.isChecked());
                if(rightBlinker.isChecked())
                    rightBlinker.setChecked(false);

                Message msg;
                if(leftBlinker.isChecked())
                    msg = Message.obtain(null, BluetoothService.SEND, Command.LEFT_BLINKER_ON);
                else msg = Message.obtain(null, BluetoothService.SEND, Command.LEFT_BLINKER_OFF);

                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        /** Activa/Desactiva el intermitente derecho y desactiva el izquierdo(si está activado) */
        this.rightBlinker = findViewById(R.id.RIGHT_BLINKER);
        this.rightBlinker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightBlinker.setChecked(!rightBlinker.isChecked());
                if(leftBlinker.isChecked())
                    leftBlinker.setChecked(false);

                Message msg;
                if(leftBlinker.isChecked())
                    msg = Message.obtain(null, BluetoothService.SEND, Command.RIGHT_BLINKER_ON);
                else msg = Message.obtain(null, BluetoothService.SEND, Command.RIGHT_BLINKER_OFF);

                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        /** Cambia de marcha */
        this.gearShift = findViewById(R.id.SHIFT);
        this.gearShift.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.GEAR_SHIFT[index]);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        /** Cambia de velocidad */
        this.speedControl = findViewById(R.id.ACCELERATOR);
        this.speedControl.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.SPEED[index]);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });
    }


    /**
     * Realiza una serie de ajustes en función de el status del coche
     * @param status Estado actual del coche
     */
    public void dashBoardHandler(Status status, boolean connected) {
        this.currentStatus = status;
        this.isConnected = connected;

        if(status.is_emLightOn())
            emergencyLights.setImageResource(R.mipmap.ic_eml_on_foreground);
        else emergencyLights.setImageResource(R.mipmap.ic_eml_off_foreground);

        if(status.is_emStopped() || status.get_marcha() == 'P')
            parkingSign.setImageResource(R.mipmap.ic_pb_on_foreground);
        else parkingSign.setImageResource(R.mipmap.ic_pb_off_foreground);

        if(status.is_frontLightsOn())
            headLights.setImageResource(R.mipmap.ic_hl_on_foreground);
        else headLights.setImageResource(R.mipmap.ic_hl_off_foreground);

        if(status.is_posLightsOn())
            positionLights.setImageResource(R.mipmap.ic_lb_on_foreground);
        else positionLights.setImageResource(R.mipmap.ic_lb_off_foreground);

        /*switch (status.get_turnLightsOn()) {
            case 0:
                check(leftBlinker, false);
                check(rightBlinker, false);
                break;
            case 1:
                check(leftBlinker, true);
                check(rightBlinker, false);
                break;
            case 2:
                check(leftBlinker, false);
                check(rightBlinker, true);
                break;
            default: break;
        }*/
    }

    /**
     * Método antiSpam: Para evitar el spam de intermitentes reseteandose cada 500ms,
     * al llamar a este método solo se cambian si el estado que deberían tener es distinto
     * a su estado actual
     * @param rmSwitch Switch contenedor del valor
     * @param value Valor requerido
     */
    public void check(RMSwitch rmSwitch, boolean value) {
        if(rmSwitch.isChecked() != value) {
            rmSwitch.setChecked(value);
        }
    }
}
