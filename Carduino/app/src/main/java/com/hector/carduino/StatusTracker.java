package com.hector.carduino;

import android.content.Context;
import java.io.IOException;

/**
 * Gestiona la obtención de información a partir de la conexión
 */
public class StatusTracker extends Thread {
    /** Hilo contenedor de la conexión Bluetooth */
    public ConnectThread connectThread;
    /** Estado del vehículo */
    public static Status _status;
    /** Última vez que fue actualizado el estado */
    public long lastUpdate;
    /** Actividad desde donde fué creado */
    public Context context;

    /**
     * Establece la conexión y comienza a intentar leer información
     * @param connectThread(ConnectThread) Conexión con el dispositivo Bluetooth
     * @param context(Context) Actividad donde se creó el objeto
     */
    public StatusTracker(ConnectThread connectThread, Context context) {
        super();
        this.connectThread = connectThread;
        this.context = context;
        this.lastUpdate = 0;
        this.start();
    }

    /**
     * Intenta leer información del Bluetooth cada 500ms y guarda el último momento en el que fue posible una lectura
     */
    public void run() {
        while(true) {
            try {
                String reading = connectThread.readBT();
                System.out.println("BTREADING: " + reading);
                _status = new Status(reading);
                _status.writeToFile(context);
                lastUpdate = System.currentTimeMillis();
            } catch (IOException e) {
            } catch (NullPointerException e) { }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) { }
        }
    }

    public static Status get_status() {
        return _status;
    }
}
