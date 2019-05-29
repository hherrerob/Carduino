package com.hector.carduino;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.channguyen.rsv.RangeSliderView;
import com.rm.rmswitch.RMSwitch;

import org.json.JSONObject;

import io.feeeei.circleseekbar.CircleSeekBar;

/**
 * Gestiona la conducción del vehículo
 */
public class DriveActivity extends AppCompatActivity implements SensorEventListener {

    /** Boton de freno */
    private FloatingActionButton brake;
    /** Boton de pitído */
    private FloatingActionButton buzzer;
    /** Boton de ráfaga */
    private FloatingActionButton flash;

    /** Vista del candado */
    private ImageView lock;
    /** Vista del modo Sport */
    private ImageView sportMode;
    /** Vista de las luces de posición */
    private ImageView positionLights;
    /** Vista de las luces delanteras */
    private ImageView headLights;
    /** Vista de las luces de emergencia */
    private ImageView emergencyLights;
    /** Vista del freno de estacionamiento*/
    private ImageView parkingSign;
    /** Vista del estado de la carretera*/
    private ImageView frostSign;
    /** Vista del control de crucero */
    private ImageView cruiseControl;
    /** Vista del volante */
    private ImageView steeringWheel;
    /** Control del volante */
    private CircleSeekBar steeringSeekBar;
    /** Indicadores de marchas */
    private TextView[] gearLabels;

    /** Display de la velocidad */
    public TextView speedDisplay;
    /** Switch del intermitente izquierdo */
    private RMSwitch leftBlinker;
    /** Switch del intermitente derecho */
    private RMSwitch rightBlinker;
    /** AntiSpam intermitentes */
    private int[] antiSpam;

    /** Barra de aceleración */
    private RangeSliderView speedControl;
    /** Barra de marchas */
    private RangeSliderView gearShift;
    /** Estado actual de vehículo */
    private Status currentStatus;
    /** Hilo que va actualizando el status y efectúa llamadas al método actualizador */
    private FeedbackTracker feedbackTracker;
    /** Flag: conectado o no */
    private boolean isConnected;
    /** Flag: control de crucero encendido */
    private boolean _cruiseControl;
    /** Flag: modo Sport */
    private boolean _sportMode;
    /** Vibración */
    private Vibrator vibrator;
    /** Flag: vibrar o no */
    private boolean vibrate;
    /** Colisión detectada */
    private boolean collisionDetected;

    /** Flag: acelerómetro */
    private boolean isAccOn;
    /** Flag: giroscópio */
    private boolean isGyroOn;
    /** Controlador de los sensores */
    private SensorManager sensorManager;
    /** Sensor */
    private Sensor sensor;
    /** Último comando enviado por el Acelerómetro */
    private char lastCmdSpeed;
    /** Último comando enviado por el Giroscópio */
    private char lastCmdTurn;
    /** AntiSpam en millis */
    private long lastTurn;

    /** Flag: llamada a la API del tiempo hecha */
    private boolean weatherUpdated;
    /** Momento en el que se hizo la última llamada */
    private long lastWeatherUpdate;
    /** Descripción de la API */
    private String weatherDescription;

    /** Comunicación con el servicio */
    private Messenger messageSender;
    /** Establecimiento de conexión con el servicio */
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
        set();

        bindService(new Intent(this, BluetoothService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);

        CountDownTimer cdt = new CountDownTimer(1, 1) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                sportModeOn();
                sportModeOff();
            }
        };

        cdt.start();
    }

    /**
     * Decide si hacer o no la petición
     */
    public void getWeather() {
        try {
            if (!weatherUpdated || System.currentTimeMillis() - lastWeatherUpdate > 10000 ) {
                weatherUpdated = true;
                lastWeatherUpdate = System.currentTimeMillis();
                JSONObject json = RemoteFetch.getJSON(DriveActivity.this, currentStatus.get_lat(), currentStatus.get_lon());
                renderWeather(json);
            }
        } catch (NullPointerException e) {
            sportModeIcon(frostSign, R.mipmap.ic_lane_unk_foreground, R.mipmap.ic_lane_unk_foreground);
        }
    }

    /**
     * Procesa una acción a partir de los datos recibidos de la API
     * @param json(JSONObject) Datos recibidos de la API
     */
    public void renderWeather(JSONObject json) {
        try {
            int weatherCode =  json.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherDescription = json.getJSONArray("weather").getJSONObject(0).getString("description");

            int idOn, idOff;
            if(200 <= weatherCode && weatherCode <= 321) {
                idOff = R.mipmap.ic_lane_rain_foreground;
                idOn = R.mipmap.ic_lane_rain_sport_foreground;
            } else if(weatherCode == 800) {
                idOff = R.mipmap.ic_lane_normal_foreground;
                idOn = R.mipmap.ic_lane_normal_foreground;
            } else if(600 <= weatherCode && weatherCode <= 622) {
                idOff = R.mipmap.ic_lane_frost_foreground;
                idOn = R.mipmap.ic_lane_frost_sport_foreground;
            } else idOff = idOn = R.mipmap.ic_lane_unk_foreground;

            sportModeIcon(frostSign, idOn, idOff);
        }catch(Exception e){
            sportModeIcon(frostSign, R.mipmap.ic_lane_unk_foreground, R.mipmap.ic_lane_unk_foreground);
        }
    }


    /**
     * Instancia los componentes y recursos necesarios para la actividad
     * Establece los comando a enviar al efectuar los eventos
     */
    private void set() {
        this.weatherDescription = "Unknown";
        this.weatherUpdated = false;
        this.lastWeatherUpdate = System.currentTimeMillis();
        this.antiSpam = new int[] {0, 0};
        this.lastCmdSpeed = ' ';
        this.lastCmdTurn = ' ';
        this.lastTurn = System.currentTimeMillis();
        this._cruiseControl = false;
        this._sportMode = false;
        this.collisionDetected = false;
        this.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        this.gearLabels = new TextView[] {
                findViewById(R.id.P),
                findViewById(R.id.R),
                findViewById(R.id.N),
                findViewById(R.id.D)
        };

        Settings settings = new Settings();
        settings.getPrefs(DriveActivity.this);
        this.isGyroOn = settings.is_gyroscopeOn();
        this.isAccOn = settings.is_accelerometerOn();
        this.vibrate = settings.is_phoneVibeOn();

        this.sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

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

        this.sportMode = findViewById(R.id.SPORT_MODE);
        this.sportMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _sportMode = !_sportMode;

                if(_sportMode)
                    sportModeOn();
                else {
                    sportModeOff();
                }

            }
        });

        this.lock = findViewById(R.id.LOCK);
        this.lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg;

                if(currentStatus.is_locked())
                    msg = Message.obtain(null, BluetoothService.SEND, Command.UNLOCK);
                else msg = Message.obtain(null, BluetoothService.SEND, Command.LOCK);
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

        this.positionLights.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.AUTO_LIGHTS_OFF);
                Toast.makeText(DriveActivity.this, R.string.drive_autol_off, Toast.LENGTH_SHORT).show();
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
                return true;
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

        this.headLights.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.AUTO_LIGHTS_ON);
                Toast.makeText(DriveActivity.this, R.string.drive_autol_on, Toast.LENGTH_SHORT).show();
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
                return true;
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

        this.emergencyLights.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.AUTO_STOP_ON);
                Toast.makeText(DriveActivity.this, R.string.drive_autoc_on, Toast.LENGTH_SHORT).show();
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
                return true;
            }
        });

        this.parkingSign = findViewById(R.id.PARKING);
        this.parkingSign.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.AUTO_STOP_OFF);
                Toast.makeText(DriveActivity.this, R.string.drive_autoc_off, Toast.LENGTH_SHORT).show();
                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
                return true;
            }
        });

        this.frostSign = findViewById(R.id.FROST);
        this.frostSign.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(DriveActivity.this, weatherDescription, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        this.speedDisplay = findViewById(R.id.SPEED);
        this.speedDisplay.setText("0 km/h");

        this.cruiseControl = findViewById(R.id.CRUISE_CONTROL);
        this.cruiseControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _cruiseControl = !_cruiseControl;

                if(_cruiseControl) {
                    sportModeIcon(cruiseControl, R.mipmap.ic_sport_cc_foreground, R.mipmap.ic_cl_on_foreground);
                } else cruiseControl.setImageResource(R.mipmap.ic_cl_off_foreground);

                speedControl.setEnabled(!_cruiseControl);
                speedControl.invalidate();
            }
        });
        this.cruiseControl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isAccOn = !isAccOn;
                if(isAccOn)
                    Toast.makeText(DriveActivity.this, R.string.drive_acc_on, Toast.LENGTH_SHORT).show();
                else Toast.makeText(DriveActivity.this, R.string.drive_acc_off, Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        this.steeringWheel = findViewById(R.id.STEERING_WHEEL);
        this.steeringSeekBar = findViewById(R.id.STEERING_SB);
        this.steeringSeekBar.setOnSeekBarChangeListener(new CircleSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onChanged(CircleSeekBar circleSeekBar, final int i) {
                DriveActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        steeringWheel.setRotation(i);
                        turnHandler(i);
                    }
                });
            }
        });

        // Activa/Desactiva el intermitente izquierdo y desactiva el derecho(si está activado)
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

        // Activa/Desactiva el intermitente derecho y desactiva el izquierdo(si está activado)
        this.rightBlinker = findViewById(R.id.RIGHT_BLINKER);
        this.rightBlinker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rightBlinker.setChecked(!rightBlinker.isChecked());
                if(leftBlinker.isChecked())
                    leftBlinker.setChecked(false);

                Message msg;
                if(rightBlinker.isChecked())
                    msg = Message.obtain(null, BluetoothService.SEND, Command.RIGHT_BLINKER_ON);
                else msg = Message.obtain(null, BluetoothService.SEND, Command.RIGHT_BLINKER_OFF);

                try {
                    messageSender.send(msg);
                } catch (RemoteException e) { }
            }
        });

        // Cambia de marcha
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

        // Cambia de velocidad
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
     * Hace la UI más atractiva
     */
    public void sportModeOn() {
        try {
            dashBoardHandler(currentStatus, isConnected);
        } catch (NullPointerException e) { }

        steeringWheel.setImageResource(R.drawable.sw_r);
        sportMode.setImageResource(R.mipmap.ic_sport_on_foreground);
        gearShift.setFilledColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        speedControl.setFilledColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
        speedControl.setRangeCount(6);

        if(_cruiseControl) {
            sportModeIcon(cruiseControl, R.mipmap.ic_sport_cc_foreground, R.mipmap.ic_cl_on_foreground);
        } else cruiseControl.setImageResource(R.mipmap.ic_cl_off_foreground);

        DriveActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                labelsBypass(true);
            }
        });
    }

    /**
     * Pone la UI en su estado normal
     */
    public void sportModeOff() {
        try {
            dashBoardHandler(currentStatus, isConnected);
        } catch (NullPointerException e) { }
        steeringWheel.setImageResource(R.drawable.sw_n);
        sportMode.setImageResource(R.mipmap.ic_sport_foreground);
        gearShift.setFilledColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
        speedControl.setFilledColor(ResourcesCompat.getColor(getResources(), R.color.blue, null));
        speedControl.setRangeCount(5);

        if(_cruiseControl) {
            sportModeIcon(cruiseControl, R.mipmap.ic_sport_cc_foreground, R.mipmap.ic_cl_on_foreground);
        } else cruiseControl.setImageResource(R.mipmap.ic_cl_off_foreground);

        DriveActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                labelsBypass(false);
            }
        });
    }

    /**
     * Elige que recurso poner en el display
     * @param display(ImageView): Display
     * @param idOn(int): ID del recurso con modo Sport encendido
     * @param idOff(int): ID del recurso con modo Sport apagado
     */
    public void sportModeIcon(ImageView display, int idOn, int idOff) {
        if(_sportMode)
            display.setImageResource(idOn);
        else display.setImageResource(idOff);
    }

    /**
     * Cambia el estilo de los indicadores de marcha
     * @param flag(boolean): true si está en modo Sport, false si no
     */
    public void labelsBypass(boolean flag) {
        for(int i=0; i < gearLabels.length; i++) {
            if(flag)
                gearLabels[i].setTypeface(null, Typeface.BOLD_ITALIC);
            else gearLabels[i].setTypeface(null, Typeface.BOLD);
        }

        if(flag)
            speedDisplay.setTypeface(null, Typeface.BOLD_ITALIC);
        else speedDisplay.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Maneja el giro con el ángulo dado
     * @param angle(int) ángulo del volante
     */
    public void turnHandler(int angle) {
        Message msg;
        if(angle > 45 && angle <= 135)
            msg = Message.obtain(null, BluetoothService.SEND, Command.TURN_LEFT);
        else if(angle > 225 && angle < 315)
            msg = Message.obtain(null, BluetoothService.SEND, Command.TURN_RIGHT);
        else msg = Message.obtain(null, BluetoothService.SEND, Command.RELEASE_SW);

        try {
            messageSender.send(msg);
        } catch (RemoteException e) { }
    }


    /**
     * Realiza una serie de ajustes en función de el status del coche
     * @param status(Status): Estado actual del coche
     */
    public void dashBoardHandler(Status status, boolean connected) {
        this.currentStatus = status;
        this.isConnected = connected;

        getWeather();

        if(status.is_locked())
            sportModeIcon(lock, R.mipmap.ic_sport_lock_foreground, R.mipmap.ic_lock_on_db_foreground);
        else lock.setImageResource(R.mipmap.ic_lock_off_db_foreground);

        if(status.is_emLightOn())
            sportModeIcon(emergencyLights, R.mipmap.ic_eml_on_foreground, R.mipmap.ic_eml_on_foreground);
        else emergencyLights.setImageResource(R.mipmap.ic_eml_off_foreground);

        if(status.is_emStopped() || status.get_marcha() == 'P')
            sportModeIcon(parkingSign, R.mipmap.ic_sport_pb_foreground, R.mipmap.ic_pb_on_foreground);
        else parkingSign.setImageResource(R.mipmap.ic_pb_off_foreground);

        if(status.is_frontLightsOn())
            sportModeIcon(headLights, R.mipmap.ic_sport_hl_foreground, R.mipmap.ic_hl_on_foreground);
        else headLights.setImageResource(R.mipmap.ic_hl_off_foreground);

        if(status.is_posLightsOn())
            sportModeIcon(positionLights, R.mipmap.ic_sport_pl_foreground, R.mipmap.ic_lb_on_foreground);
        else positionLights.setImageResource(R.mipmap.ic_lb_off_foreground);

        if(status.is_emStopped() && vibrate && !collisionDetected) {
            vibrator.vibrate(300);
            collisionDetected = true;
        } else collisionDetected = false;

        DriveActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    /**
     * Actualiza la UI según los datos recibidos
     */
    public void update() {
        speedDisplay.setText(currentStatus.get_kmph() + " km/h");

        switch (currentStatus.get_turnLightsOn()) {
            case 0:
                check(leftBlinker, false, 0);
                check(rightBlinker, false, 1);
                break;
            case 1:
                check(leftBlinker, true, 0);
                check(rightBlinker, false, 1);
                break;
            case 2:
                check(leftBlinker, false, 0);
                check(rightBlinker, true, 1);
                break;
            default:
                break;
        }
    }

    /**
     * Método antiSpam: Para evitar el spam de intermitentes reseteandose cada 500ms,
     * al llamar a este método solo se cambian si el estado que deberían tener es distinto
     * a su estado actual
     * @param rmSwitch(RMSwitch) contenedor del valor
     * @param value(Boolean) Valor requerido
     */
    public void check(RMSwitch rmSwitch, boolean value, int index) {
        if(rmSwitch.isChecked() != value) {
            if (antiSpam[index] > 1) {
                rmSwitch.setChecked(value);
                antiSpam[index] = 0;
            } else antiSpam[index]++;
        } else antiSpam[index] = 0;
    }

    /**
     * Óbtiene la situación del teléfono y ejecuta una respuesta
     * @param sensorEvent(SensorEvent) Moviemiento del dispositivo
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        try {
            if (isAccOn && !_cruiseControl)
                handleAcc(speedFromPercentage(percentageFromAcc(sensorEvent.values[0])));

            if (System.currentTimeMillis() - lastTurn >= 100 && isGyroOn) {
                handleTurn(sensorEvent.values[1]);
                lastTurn = System.currentTimeMillis();
            }
        } catch (NullPointerException e) { }
    }

    /**
     * Ejecuta una acción dependiendo del ángulo del acelerómetro
     * @param val(float) Angulo de situación del teléfono
     */
    public void handleAcc(float val) {
        if(val < 0)
            val = 0;
        else if(val > 255)
            val = 255;

        int i;
        if(val == 255 && _sportMode)
            i = 5;
        else if(val >= 200)
            i = 4;
        else if(val >= 150)
            i = 3;
        else if(val >= 100)
            i = 2;
        else if(val >= 50)
            i = 1;
        else i = 0;


        if(Command.SPEED[i] != lastCmdSpeed) {
            try {
                speedControl.setInitialIndex(i);
                lastCmdSpeed = Command.SPEED[i];
                Message msg = Message.obtain(null, BluetoothService.SEND, Command.SPEED[i]);
                messageSender.send(msg);
            } catch (RemoteException e) { }
        }
    }

    /**
     * Ejecuta una acción dependiendo del ángulo del giroscópio
     * @param angle(float) Angulo de situación del teléfono
     */
    public void handleTurn(float angle) {
        char cmd = ' ';
        if(angle > 2)
            cmd = Command.TURN_LEFT;
        else if(angle < -2)
            cmd = Command.TURN_RIGHT;
        else cmd = Command.RELEASE_SW;

        if(angle > 4)
            angle = 4;
        else if(angle < -4)
            angle = -4;

        steeringWheel.setRotation(round(map(angle, -4, 4, 0, 360) + 180));

        if(cmd != lastCmdTurn) {
            try {
                lastCmdTurn = cmd;
                System.out.println("ACELEROMETRO: " + cmd);
                Message msg = Message.obtain(null, BluetoothService.SEND, cmd);
                messageSender.send(msg);
            } catch (RemoteException e) { }
        }
    }

    /**
     * Redondea un valor al 10
     * @param n(int) valor a redondear
     * @return (int) valor redondeado
     */
    public int round(int n) {
        n /= 10;
        n *= 10;
        return n;
    }

    /**
     * Mapea un valor entre un rango
     * @param x(float) valor a mapear
     * @param a(int) valor mínimo de su rango
     * @param b(int) valor máximo de su rango
     * @param c(int) valor mínimo del nuevo rango
     * @param d(int) valor máximo del nuevo rango
     * @return (int) valor mapeado
     */
    public int map(float x, int a, int b, int c, int d) {
        return (int) ((x-a)/(b-a) * (d-c) + c);
    }

    /**
     * Obtiene el porcentaje de aceleración
     * @param x(Float) Inclinación
     * @return (int) Porcentaje de aceleración
     */
    public int percentageFromAcc(float x) {
        int max = 9 -3;
        x -= 3;
        x = max -x;

        return (int) (100 * x) / 6;
    }

    /**
     * Obtiene el porcentaje de aceleración
     * @param x(int) Porcentaje de aceleración
     * @return (int) Velocidad entre 0-255
     */
    public int speedFromPercentage(int x) {
        if(x < 0)
            x *= -1;
        return (255 * x) / 100;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // NADA
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, sensor);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this, sensor);
    }
}
