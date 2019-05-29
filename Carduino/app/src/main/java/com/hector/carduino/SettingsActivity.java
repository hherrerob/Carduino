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
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Gestiona la edición de la configuración
 */
public class SettingsActivity extends AppCompatActivity {

    /** Array contenedor de los ajustes activados y desactivados */
    private Switch[] switchSettings;
    /** Rango de valores para las luces automáticas */
    private SeekBar sbPitch;
    /** Rango de valores para la parada automática */
    private SeekBar sbDistance;
    /** Display del valor en el que se activan las luces automáticas */
    private TextView sbPitchTxt;
    /** Display del valor en el que se activa la parada automática */
    private TextView sbDistTxt;
    /** Editor del nombre del vehículo */
    private EditText carName;
    /** Botón de salida */
    private FloatingActionButton backButton;
    /** Botón de parametrización */
    private ImageView sendButton;

    /** Configuración del vehículo */
    private Settings settings;

    /** Comunicación con el servicio */
    private Messenger messageSender;
    /** Establecimiento de conexión con el servicio */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messageSender = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            messageSender = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.settings = new Settings();
        this.settings.getPrefs(this);
        set();

        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Instancia los recursos necesarios y establece los valores actuales a partir de una configuración ya guardada
     */
    private void set() {
        this.backButton = findViewById(R.id.BACK_BUTTON);
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                SettingsActivity.this.setResult(RESULT_OK, returnIntent);
                SettingsActivity.this.finish();
            }
        });

        this.sendButton = findViewById(R.id.SEND);
        this.sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gatherSettings();
                settings.savePrefs(SettingsActivity.this);

                Message msg = Message.obtain(null, BluetoothService.SEND_PARAM, settings);
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        this.carName = findViewById(R.id.CAR_NAME);
        this.carName.setText(settings.get_carName());

        this.switchSettings = new Switch[10];
        this.switchSettings[0] = findViewById(R.id.HP_LIGHTS);
        this.switchSettings[0].setChecked(settings.is_autoLights());
        this.switchSettings[1] = findViewById(R.id.CRASH_DETECT);
        this.switchSettings[1].setChecked(settings.is_autoStop());
        this.switchSettings[2] = findViewById(R.id.CAR_ON);
        this.switchSettings[2].setChecked(settings.is_notifCarOn());
        this.switchSettings[3] = findViewById(R.id.CAR_PARKED);
        this.switchSettings[3].setChecked(settings.is_notifCarParked());
        this.switchSettings[4] = findViewById(R.id.LOW_BATTERY);
        this.switchSettings[4].setChecked(settings.is_notifLowBattery());
        this.switchSettings[5] = findViewById(R.id.VIBRATION);
        this.switchSettings[5].setChecked(settings.is_phoneVibeOn());
        this.switchSettings[6] = findViewById(R.id.ACCELEROMETER);
        this.switchSettings[6].setChecked(settings.is_accelerometerOn());
        this.switchSettings[7] = findViewById(R.id.GYROSCOPE);
        this.switchSettings[7].setChecked(settings.is_gyroscopeOn());
        this.switchSettings[8] = findViewById(R.id.DEFAULT);
        this.switchSettings[8].setChecked(settings.is_useDefault());
        this.switchSettings[9] = findViewById(R.id.LOCATION);
        this.switchSettings[9].setChecked(settings.is_usePhoneLoc());

        this.sbPitch = findViewById(R.id.SB_PITCH);
        this.sbPitch.setProgress(settings.get_autoLightsPitch());
        this.sbPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sbPitchTxt.setText((progress +100) + "lux");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        this.sbDistance = findViewById(R.id.SB_DISTANCE);
        this.sbDistance.setProgress(settings.get_autoStopDistance() -10);
        this.sbDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sbDistTxt.setText((sbDistance.getProgress() +10) + "cm");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        this.sbPitchTxt = findViewById(R.id.HP_PITCH);
        this.sbPitchTxt.setText((sbPitch.getProgress() +100)+ "lux");
        this.sbDistTxt = findViewById(R.id.CD_DISTANCE);
        this.sbDistTxt.setText((sbDistance.getProgress() +10) + "cm");
    }

    /**
     * Guarda la configuración a partir de los valores recogidos
     */
    public void gatherSettings() {
        this.settings.set_autoLights(this.switchSettings[0].isChecked());
        this.settings.set_autoStop(this.switchSettings[1].isChecked());
        this.settings.set_notifCarOn(this.switchSettings[2].isChecked());
        this.settings.set_notifCarParked(this.switchSettings[3].isChecked());
        this.settings.set_notifLowBattery(this.switchSettings[4].isChecked());
        this.settings.set_phoneVibeOn(this.switchSettings[5].isChecked());
        this.settings.set_accelerometerOn(this.switchSettings[6].isChecked());
        this.settings.set_gyroscopeOn(this.switchSettings[7].isChecked());
        this.settings.set_useDefault(this.switchSettings[8].isChecked());
        this.settings.set_usePhoneLoc(this.switchSettings[9].isChecked());
        this.settings.set_autoLightsPitch(this.sbPitch.getProgress() +10);
        this.settings.set_autoStopDistance(this.sbDistance.getProgress() +100);
        this.settings.set_carName(this.carName.getText().toString());
    }

    @Override
    protected void onPause() {
        gatherSettings();
        settings.savePrefs(SettingsActivity.this);
        super.onPause();
    }
}
