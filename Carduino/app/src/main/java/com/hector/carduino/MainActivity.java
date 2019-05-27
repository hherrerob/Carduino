package com.hector.carduino;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
    /** ID del canal de notificaciones */
    public static final String CHANNEL_ID = "1";
    /** ID Callback de settingsActivity */
    public static final int NAMECHANGE_RESULT = 9;

    /** Botones de recargar y configurar */
    private ImageButton refresh, settings;
    /** Opciónes que abren actividades **/
    private LinearLayout climateOption, driveOption, controlsOption, locationOption, summonOption;
    /** Medidores de batería */
    private BatteryMeterView motorBatteryIndicator, arduinoBatteryIndicator;
    /** Vista representativa de la conexión */
    private RelativeLayout isConnected;
    /** Flag: conectado o no */
    private Boolean _isConnected;
    /** Botones activar ventilación, candar/descandar el vehículo */
    private SwitchIconView lock, vent;
    /** Muestra el nombre del coche */
    private TextView carName;

    /** Contadores para evitar spam(activaciones y desactivaciones muy seguidas) en los switches */
    private int[] antiSpam;
    /** Contiene el status actual del vehículo */
    private Status currentStatus;

    /** Hilo que va actualizando el status y efectúa llamadas al método actualizador */
    private FeedbackTracker feedbackTracker;
    /** Comunicación con el servicio */
    private Messenger messageSender;
    /** Establecimiento de conexión con el servicio */
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

        createNotificationChannel();
        notifyThis("Hola", "Hola");
    }

    public void notifyThis(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_logo_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int notificationId = 1;
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Instancia las propiedades de la actividad
     * Valida accesos a actividades
     */
    private void set() {
        this.antiSpam = new int[] {0, 0};
        this._isConnected = false;

        this.lock = findViewById(R.id.LOCK);
        this.lock.setIconEnabled(true);
        this.lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock.setIconEnabled(!lock.isIconEnabled()); //TODO: Cambiar (?)
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

    /** Callback al cerrar una actividad abierta por ésta */
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

    /**
     * Maneja la UI y variables de control según los datos obtenidos de la conexión
     * Actualiza el nivel de carga de las baterías
     * Pone la vista de la conexión en verde si hay conexión, en rojo si no
     * Actualiza la vista del candado y la ventilación
     * @param currentStatus(Status) último status válido obtenido
     * @param connected(Boolean) Conexión establecida o no
     */
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

    /**
     * Método para evitar spam
     * Comprueba que el valor esperado sea igual que el que tiene una variable, si no,
     * lo intenta cambiar.
     * Debe hacer dos intentos para cambiarlo antes de que sea posible cambiarlo
     * @param switchIcon(SwitchIconView) Vista del switch
     * @param value(Boolean) Valor esperado
     * @param index(int) Índice en el array de antiSpam
     */
    public void check(SwitchIconView switchIcon, boolean value, int index) {
        if(switchIcon.isIconEnabled() != value) {
            if(antiSpam[index] > 1) {
                switchIcon.setIconEnabled(value);
                antiSpam[index] = 0;
            } else antiSpam[index]++;
        } else antiSpam[index] = 0;
    }
}
