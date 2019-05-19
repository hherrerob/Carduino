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
     * @param device
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
     * @param cmd Comando a enviar
     */
    public void send(char cmd) {
        try {
            mmSocket.getOutputStream().write(cmd);
        } catch (IOException e) {
        } catch (NullPointerException e) { }
    }

    /**
     * Envia una serie de comandos de forma continuada
     * @param cmd Array de comandos A enviar
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

    public void setConfig(Settings settings) throws InterruptedException {
        if(settings.is_useDefault())
            send(Command.USE_DEFAULT_CONFIG);
        else {
            toParam(settings.is_autoLights(), Command.AUTO_LIGHTS_ON, Command.AUTO_LIGHTS_OFF, settings.get_autoLightsPitch());
            toParam(settings.is_autoStop(), Command.AUTO_STOP_ON, Command.AUTO_STOP_OFF, settings.get_autoStopDistance());
            toParam(settings.is_autoVent(), Command.AUTO_VENT_ON, Command.AUTO_VENT_OFF, settings.get_autoVentTemp());
        }
    }

    public void toParam(boolean b, char cmdTrue, char cmdFalse, int value) throws InterruptedException {
        if(b) {
            send(cmdTrue);
            Thread.sleep(100);
            sendParameter(value, cmdTrue);
        } else {
            send(cmdFalse);
        }
        Thread.sleep(300);
    }

    /**
     * Envia un comando para iniciar la parametrización y
     * acto seguido enía el comando con el parámetro
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