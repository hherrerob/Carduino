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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.manojbhadane.QButton;

/**
 * Gestiona el control del clima del vehículo
 */
public class ClimateActivity extends AppCompatActivity {

    /** Display de la temperatura actual */
    private TextView tempTracker;
    /** Display de la temperatura deseada */
    private TextView tempDesired;
    /** Botón para aumentar la temperatura deseada */
    private ImageButton increaseTemp;
    /** Botón para disminuir la temperatura deseada */
    private ImageButton decreaseTemp;
    /** Toggler de activación/desactivación de la ventilación automática */
    private QButton tempToggler;
    /** Botón de salida */
    private FloatingActionButton backButton;
    /** Estado del vehículo */
    private Status status;
    /** Hilo del animador */
    private Rotator rotator;
    /** Temperatura actual */
    private int currTemp;
    /** Temperatura deseada */
    private int desiredTemp;

    /** Última configuración guardada*/
    private Settings settings;
    /** Hilo que va actualizando el status y efectúa llamadas al método actualizador */
    private FeedbackTracker feedbackTracker;
    /** Comunicación con el servicio */
    private Messenger messageSender;
    /** Establecimiento de conexión con el servicio */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = new Messenger(service);
            feedbackTracker = new FeedbackTracker(messageSender, ClimateActivity.this, FeedbackTracker.CLIMATE_ACTIVITY);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate);
        this.settings = new Settings();
        this.settings.getPrefs(this);
        set();
        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Instancia la información necesaria para el correcto funcionamiento de la actividad
     * Activa el hilo de rotación
     * Activa el botón de salida
     */
    private void set() {

        this.status = (Status) getIntent().getExtras().getSerializable("status");

        this.rotator = new Rotator(settings.is_autoVent());
        rotator.start();

        this.tempToggler = findViewById(R.id.TEMP_TOGGLER);
        this.tempToggler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rotator.on = !rotator.on;
                Message msg;

                if(rotator.on) {
                    tempToggler.setText(R.string.climate_on);
                    msg = Message.obtain(null, BluetoothService.SEND, Command.AUTO_VENT_ON);
                } else {
                    tempToggler.setText(R.string.climate_off);
                    msg = Message.obtain(null, BluetoothService.SEND, Command.AUTO_VENT_ON);
                }

                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }


                settings.set_autoVent(rotator.on);
                settings.set_autoVentTemp(desiredTemp);
                settings.savePrefs(ClimateActivity.this);

                if(rotator.on) {
                    msg = Message.obtain(null, BluetoothService.SEND_PARAM, settings);
                    try {
                        messageSender.send(msg);
                    } catch (RemoteException e) {
                    }
                }
            }
        });

        if(rotator.on)
            tempToggler.setText(R.string.climate_on);
        else tempToggler.setText(R.string.climate_off);

        this.currTemp = (int) status.get_temp();
        this.desiredTemp = settings.get_autoVentTemp();

        this.tempTracker = findViewById(R.id.TEMP_TRACKER);
        updateCurrTemp(this.currTemp);

        this.tempDesired = findViewById(R.id.TEMP);
        updateDesiredTemp(this.desiredTemp);

        this.increaseTemp = findViewById(R.id.INCREASE_TEMP);
        this.increaseTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desiredTemp++;
                updateDesiredTemp(desiredTemp);
            }
        });

        this.decreaseTemp = findViewById(R.id.DECREASE_TEMP);
        this.decreaseTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desiredTemp--;
                updateDesiredTemp(desiredTemp);
            }
        });


        this.backButton = findViewById(R.id.BACK_BUTTON);
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClimateActivity.this.finish();
            }
        });
    }

    @Override
    protected void onPause() {
        settings.set_autoVent(rotator.on);
        settings.set_autoVentTemp(desiredTemp);
        settings.savePrefs(ClimateActivity.this);

        Message msg = Message.obtain(null, BluetoothService.SEND_TEMP, settings);
        try {
            messageSender.send(msg);
        } catch (RemoteException e) { }

        super.onPause();
    }

    /**
     * Actualiza el tracker de la temperatura actual de vehículo
     * @param temp(int) temperatura actual
     */
    private void updateCurrTemp(int temp) {
        this.tempTracker.setText(this.tempTracker.getText().toString() + temp + "º");
    }

    /**
     * Establece el valor de la temperatura deseado,
     * cuando la temperatura actual este por encima de ese valor se activa la refrigeración
     * @param temp(int) temperatura deseada
     */
    private void updateDesiredTemp(int temp) {
        this.tempDesired.setText(temp + "º");
    }


    /**
     * Gestiona la animación del ventilador
     */
    class Rotator extends Thread {
        ImageView fan;
        int r;
        boolean on;

        /**
         * Establece el ventidalor en encendido o apagado
         * @param on(boolean) Ventilación encendida o apagada
         */
        public Rotator(boolean on){
            super();
            fan = findViewById(R.id.FAN_IMG);
            this.on = on;
        }

        /**
         * Hace rotar el ventilador si la ventilación esta activada
         */
        @Override
        public void run() {
            super.run();
            while(true) {
                if(on) {
                    if (r > 360)
                        r = 0;
                    else r++;

                    fan.setRotation(r);
                }

                try {
                    sleep(3);
                } catch (InterruptedException e) {}
            }
        }
    }
}


