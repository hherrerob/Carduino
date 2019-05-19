package com.hector.carduino;

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

    private TextView tempTracker, tempDesired;
    private ImageButton increaseTemp, decreaseTemp;
    private QButton tempToggler;
    private FloatingActionButton backButton;
    private Status status;
    private Rotator rotator;
    private int currTemp, desiredTemp;

    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_climate);
        this.settings = new Settings();
        this.settings.getPrefs(this);
        set();
    }

    /**
     * Instancia la información necesaria para el correcto funcionamiento de la actividad
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
                if(rotator.on)
                    tempToggler.setText(R.string.climate_on);
                else tempToggler.setText(R.string.climate_off);
                //TODO: Enviar comando
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
                settings.set_autoVent(rotator.on);
                settings.set_autoVentTemp(desiredTemp);
                settings.savePrefs(ClimateActivity.this);
                ClimateActivity.this.finish();
            }
        });
    }

    /**
     * Actualiza el tracker de la temperatura actual de vehículo
     * @param temp temperatura actual
     */
    private void updateCurrTemp(int temp) {
        this.tempTracker.setText(this.tempTracker.getText().toString() + temp + "º");
    }

    /**
     * Establece el valor de la temperatura deseado,
     * cuando la temperatura actual este por encima de ese valor se activa la refrigeración
     * @param temp temperatura deseada
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
         * @param on Ventilación encendida o apagada
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


