package com.hector.carduino;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;

import java.util.concurrent.ConcurrentMap;

/**
 * Maneja el servicio que conecta el dispositivo con el módulo Bluetooth de Arduino
 */
public class BluetoothService extends Service {
    /** Caso del menú para comando único */
    public static final int SEND = 1;
    /** Caso del menú para enviar todos comandos parametrizados */
    public static final int SEND_PARAM = 2;
    /** Caso del menú para reiniciar la conexión */
    public static final int RESTART = 3;
    /** Caso del menú para enviar sólo la temperatura */
    public static final int SEND_TEMP= 4;

    /** Looper del servicio */
    private Looper serviceLooper;
    /** Manejador de órdenes al servicio */
    private ServiceHandler serviceHandler;

    /** Hilo contenedor de la conexión Bluetooth */
    public ConnectThread connectThread;
    /** Actualizador del Estado del vehículo */
    public StatusTracker statusTracker;

    /** Comunicación con el servicio */
    public Messenger messageSender;

    @Override
    public void onCreate() {
        this.connectThread = ConnectThread.connect();
        this.statusTracker = new StatusTracker(connectThread, this);

        HandlerThread thread = new HandlerThread("CarduinoService", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        messageSender = new Messenger(serviceHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messageSender.getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);

        return START_STICKY;
    }

    /**
     * Maneja las órdenes dadas al servicio
     */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND:
                    if(msg.obj != null)
                        try {
                            connectThread.send(((String) msg.obj).toCharArray());
                        } catch (ClassCastException e) {
                            connectThread.send((char) msg.obj);
                        }
                    break;
                case SEND_PARAM:
                        Settings settings = (Settings) msg.obj;
                        try {
                            connectThread.setConfig(settings);
                        } catch (InterruptedException e) { }
                    break;
                case SEND_TEMP:
                        Settings s = (Settings) msg.obj;

                        char cmd;
                        if(s.is_autoVent())
                            cmd = Command.AUTO_VENT_ON;
                        else cmd = Command.AUTO_VENT_OFF;

                        connectThread.sendParameter(s.get_autoVentTemp(), cmd);
                    break;
                case RESTART:
                    connectThread.cancel();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) { }
                    connectThread = ConnectThread.connect();
                    statusTracker = new StatusTracker(connectThread, BluetoothService.this);
                    break;
                default: break;
            }
        }
    }
}
