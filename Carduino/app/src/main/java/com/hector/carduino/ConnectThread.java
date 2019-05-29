package com.hector.carduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Gestiona la conexión Bluetooth con el vehículo
 */
public class ConnectThread extends Thread {
    /** Adaptador de Bluetooth */
    public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    /** Conexión con el módulo HC-06 de Arduino */
    private final BluetoothSocket mmSocket;
    /** Dispositivo al que está conectado*/
    private final BluetoothDevice mmDevice;
    /** ID de conexion */
    private UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Construye el Socket y comienza la comunicación
     * @param device(BluetoothDevice) Dispositivo con el que realizar la conexión
     */
    public ConnectThread(BluetoothDevice device) {
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
            while(!mBluetoothAdapter.isEnabled())
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) { }
        }

        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }

        mmSocket = tmp;

        this.start();
    }

    /**
     * Intenta conectar con el dispositivo
     */
    public void run() {
        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }
    }

    /**
     * Envía un solo comando
     * @param cmd(char) Comando a enviar
     */
    public void send(char cmd) {
        try {
            mmSocket.getOutputStream().write(cmd);
        } catch (IOException e) {
        } catch (NullPointerException e) { }
    }

    /**
     * Envia una serie de comandos de forma continuada
     * @param cmd(char[]) Array de comandos A enviar
     */
    public void send(char[] cmd) {
        for(char c: cmd) {
            try {
                mmSocket.getOutputStream().write(c);
                Thread.sleep(50);
            } catch (IOException e) {
            } catch (InterruptedException e) {
            } catch (NullPointerException e) { }
        }
    }

    /**
     * Establece la configuración a usar y se la envía a Arduino
     * @param settings(Settings) última configuración establecida
     */
    public void setConfig(Settings settings) throws InterruptedException {
        if(settings.is_useDefault())
            send(Command.USE_DEFAULT_CONFIG);
        else {
            synchronized (this) {
                toParam(settings.is_autoLights(), Command.AUTO_LIGHTS_ON, Command.AUTO_LIGHTS_OFF, settings.get_autoLightsPitch());
                Thread.sleep(1500);
                toParam(settings.is_autoStop(), Command.AUTO_STOP_ON, Command.AUTO_STOP_OFF, settings.get_autoStopDistance());
                Thread.sleep(1500);
                toParam(settings.is_autoVent(), Command.AUTO_VENT_ON, Command.AUTO_VENT_OFF, settings.get_autoVentTemp());
            }
        }
    }

    /**
     * Manda parametrizar (o no) un comando
     * @param b(boolean) Si está o no activado
     * @param cmdTrue(char) Comando si está activado
     * @param cmdFalse(char) Comando si esta desactivado
     * @param value(int) Valor de la config
     */
    public void toParam(boolean b, char cmdTrue, char cmdFalse, int value) throws InterruptedException {
        if(b) {
            sendParameter(value, cmdTrue);
        } else {
            sendParameter(value, cmdFalse);
        }
    }

    /**
     * Envia un comando para iniciar la parametrización y
     * acto seguido envía el comando con el parámetro
     * @param n(int) Valor del parámetro
     * @param cmd(char) Comando a parametrizar
     */
    public void sendParameter(int n, char cmd) {
        try {
            mmSocket.getOutputStream().write(Command.ACTIVATE_PARAMETRIZE);
            mmSocket.getOutputStream().write(Command.formatParameter(n, cmd).getBytes());
        } catch (IOException e) { }
    }

    /**
     * Comprueba si se ha recibido algún mensaje de la conexión
     * @return String con el mensaje recibido, null si no lee nada
     * @throws IOException
     */
    public String readBT() throws IOException {
        byte[] status = new byte[1024];
        try {
            mmSocket.getInputStream().read(status);
            return new String(status);
        }catch (NullPointerException e) {
            return null;
        }
    }

    /** Cancela cualquier conexión en progreso y cierra el Socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    /**
     * Busca el dispositivo HC-06 de la lista de dispositivos conocidos e intenta establecer una conexión
     * @return El objeto ConnectThread con la conexión ya establecida
     */
    public static ConnectThread connect() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bluetoothDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice device = null;

        for(BluetoothDevice bd : bluetoothDevices) {
            if(bd.getName().contains("HC")){
                device = bd;
            }
        }

        bluetoothAdapter.cancelDiscovery();

        return new ConnectThread(device);
    }

    /**
     * Comprueba si el Bluetooth del dispositivo está encendido
     * @param context(Context) Desde la actividad donde se llama al método
     */
    public static void detectBluetooth(Context context) {
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, R.string.error_no_bt, Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                Toast.makeText(context, R.string.error_bt_turn_on, Toast.LENGTH_LONG).show();
            }
        }
    }
}