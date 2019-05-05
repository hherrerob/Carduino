package com.hector.carduino;

import android.content.res.Resources;
import android.os.AsyncTask;
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
    public TextView messageDisplay, speedDisplay;
    private RMSwitch leftBlinker, rightBlinker;
    private RangeSliderView gearShift, speedControl;

    private ConnectThread connectThread;
    public StatusTracker statusTracker;
    private MessageTracker messageTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);
        //TODO: recoger parametros de intent con la configuración
        set();

        this.connectThread = ConnectThread.connect();
        this.statusTracker = new StatusTracker(connectThread, DriveActivity.this);
        this.messageTracker = new MessageTracker();
    }

    /**
     * Instancia los componentes y recursos necesarios para la actividad
     */
    private void set() {
        /** Activa el freno */
        this.brake = findViewById(R.id.BRAKE);
        this.brake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.send(Command.BRAKE);
            }
        });

        /** Realiza un pequeño pitído */
        this.buzzer = findViewById(R.id.BUZZER);
        this.buzzer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.send(Command.HONK);
            }
        });

        /** Hace un flash con las luces frontales */
        this.flash = findViewById(R.id.FLASH);
        this.flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.send(Command.FLASH);
            }
        });

        /** Recarga la conexión con el vehículo */
        this.refresh = findViewById(R.id.REFRESH);
        this.refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.cancel();
                connectThread = ConnectThread.connect();
                statusTracker = new StatusTracker(connectThread, DriveActivity.this);
            }
        });

        /** Activa/Desactiva las luces de posición */
        this.positionLights = findViewById(R.id.POSITION_LIGHTS);
        this.positionLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.send(Command.POSITION_LIGHTS);
            }
        });

        /** Activa/Desactiva las luces frontales */
        this.headLights = findViewById(R.id.HEADLIGHTS);
        this.headLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.send(Command.HEADLIGHTS);
            }
        });

        /** Activa/Desactiva las luces de emergencia */
        this.emergencyLights = findViewById(R.id.EMERGENCY_LIGHT);
        this.emergencyLights.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectThread.send(Command.EMERGENCY_LIGHT);
            }
        });

        // Solo indicativos
        this.parkingSign = findViewById(R.id.PARKING);
        this.frostSign = findViewById(R.id.FROST);
        this.messageDisplay = findViewById(R.id.MESSAGE);
        this.speedDisplay = findViewById(R.id.SPEED);

        /** Activa/Desactiva el control de crucero */
        this.cruiseControl = findViewById(R.id.CRUISE_CONTROL);
        this.cruiseControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Enviar comando
                connectThread.sendParameter();
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

                if(leftBlinker.isChecked())
                    connectThread.send(Command.LEFT_BLINKER_ON);
                else connectThread.send(Command.LEFT_BLINKER_OFF);
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

                if(rightBlinker.isChecked())
                    connectThread.send(Command.RIGHT_BLINKER_ON);
                else connectThread.send(Command.RIGHT_BLINKER_OFF);
            }
        });

        /** Cambia de marcha */
        this.gearShift = findViewById(R.id.SHIFT);
        this.gearShift.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                connectThread.send(Command.GEAR_SHIFT[index]);
            }
        });

        /** Cambia de velocidad */
        this.speedControl = findViewById(R.id.ACCELERATOR);
        this.speedControl.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                connectThread.send(Command.SPEED[index]);
            }
        });
    }

    /**
     * Cierra la conexión al cerrar la actividad
     */
    @Override
    protected void onDestroy() {
        connectThread.cancel();
        super.onDestroy();
    }

    /**
     * Cierra la conexión al poner la actividad en segundo plano
     */
    @Override
    protected void onStop() {
        connectThread.cancel();
        super.onStop();
    }

    /**
     * Reactiva la conexión al reactivar la actividad
     */
    @Override
    protected void onResume() {
        connectThread = ConnectThread.connect();
        super.onResume();
    }

    /**
     * Realiza una serie de ajustes en función de el status del coche
     * @param status Estado actual del coche
     */
    public synchronized void dashBoardHandler(Status status) {
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

        switch (status.get_turnLightsOn()) {
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
        }
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

    /**
     * Gestiona elos mensajes recibidos por Bluetooth
     */
    private class MessageTracker extends AsyncTask<Void, Void, Void> {

        /**
         * Llama cada 200ms a la función de lectura del Bluetooth
         */
        @Override
        protected synchronized Void doInBackground(Void... params) {
            while(true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {};

                publishProgress();
            }
        }

        /**
         * Lee los datos del Bluetooth y se los pasa al handler para que realice las tareas necesarias
         * @param params
         */
        @Override
        protected synchronized void onProgressUpdate(Void... params) {
            try {
                if(statusTracker.get_status() != null && !statusTracker.get_status().getlast().equals(""))
                    dashBoardHandler(statusTracker.get_status());

            } catch (NullPointerException e) { };
        }
    }
}
