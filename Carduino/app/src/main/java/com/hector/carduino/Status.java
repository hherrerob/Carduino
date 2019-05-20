package com.hector.carduino;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Contiene información sobre el estado del vehículo
 */
public class Status implements Serializable {
    /** Fichero del que se lee el estado */
    private static final String FILENAME = "currentStatus.txt";
    /** Expresión con la que se valida la recepción de información */
    private static final String REGEX = "[{][\\s\\S]*[}]";
    /** Vehículo candado/descandado */
    private boolean _locked;
    /** Luces delanteras encendidas/Apagadas */
    private boolean _frontLightsOn;
    /** Luces de posición encendidas/Apagadas */
    private boolean _posLightsOn;
    /** Intermitente encendido */
    private int _turnLightsOn;
    /** Luces de emergencia encendidas */
    private boolean _emLightOn;
    /** Parada automática detectada */
    private boolean _emStopped;
    /** Marcha activada */
    private char _marcha;
    /** Velocidad activada */
    private int _speed;
    /** Lectura de la temperatura */
    private float _temp;
    /** Ventilación Activada/Apagada */
    private boolean _vent;
    /** Porcentaje Batería 1 */
    private int _battery_1;
    /** Porcentaje Batería 2 */
    private int _battery_2;
    /** Latitud */
    private double _lat;
    /** Longitud */
    private double _lon;
    /** Satélites en uso por el GPS */
    private int _sats;
    /** Velocidad en km/h */
    private double _kmph;
    /** Pico más alto de batería 1 */
    private int _pvb_1;
    /** Pico más alto de batería 2 */
    private int _pvb_2;
    /** Parada automática */
    private boolean _autoStop;
    /** Lectura del LDR */
    private int _ldrReading;
    /** Distancia libre de frente */
    private int _distance;

    /** Último estado recibido */
    private long lastStatus;
    /** Último dato recibido */
    private String last;
    /** Último dato válido recibido */
    private String lastValid;

    /**
     * Establece el Status a partir del mensaje recogido por Bluetooth
     * @param status(Status) Mensaje recibido del Bluetooth
     */
    public Status(String status) {
        lastValid = getLast(status);
        try {
            lastValid = lastValid.substring(1, lastValid.length() - 1);
            last = lastValid;
            String[] data = lastValid.split(",");
            this._locked = parseToBool(data[0]);
            this._frontLightsOn = parseToBool(data[1]);
            this._posLightsOn = parseToBool(data[2]);
            this._turnLightsOn = Integer.parseInt(data[3]);
            this._emLightOn = parseToBool(data[4]);
            this._emStopped = parseToBool(data[5]);
            this._marcha = data[6].charAt(0);
            this._speed = Integer.parseInt(data[7]);
            this._temp = Float.parseFloat(data[8]);
            this._vent = parseToBool(data[9]);
            this._battery_1 = Integer.parseInt(data[10]);
            this._battery_2 = Integer.parseInt(data[11]);
            this._lat = Double.parseDouble(data[12]);
            this._lon = Double.parseDouble(data[13]);
            this._sats = Integer.parseInt(data[14]);
            this._kmph = Double.parseDouble(data[15]);
            this._pvb_1 = Integer.parseInt(data[16]);
            this._pvb_2 = Integer.parseInt(data[17]);
            this._autoStop = parseToBool(data[18]);
            this._ldrReading = Integer.parseInt(data[19]);
            this._distance = Integer.parseInt(data[20]);
            this.lastStatus = System.currentTimeMillis();
        } catch(IndexOutOfBoundsException e){
        } catch(NullPointerException ex){ }
    }

    /**
     * Contructor que sólo guarda el momento de construcción
     */
    public Status() {
        this.lastStatus = System.currentTimeMillis();
    }

    public Status(long lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getlast() {
        return last;
    }

    /**
     * Obtiene el último String parseable válido
     * @param status(String) String a parsear
     * @return El último String parseable válido
     */
    public String getLast(String status) {
        if(status != null && status.contains(";")) {
            String[] gatheredStatus = status.split(";");
            String lastValid = null;
            for(String s: gatheredStatus) {
                if(s.matches(REGEX))
                    lastValid = s;
            }

            return lastValid;
        } else return null;
    }

    /**
     * Escribe el Status en un fichero de objeto
     * @param context(Context) Desde donde se intenta escribir el fichero
     */
    public void writeToFile(Context context) {
        try {
            if(lastValid != null) {
                new File(FILENAME).delete();
                FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream os = new ObjectOutputStream(fos);
                os.writeObject(this);
                os.close();
                fos.close();
            }
        }
        catch (IOException e) { }
    }

    /**
     * Lee un Status de un fichero
     * @param context(Context) Desde donde se intenta leer el fichero
     * @return (Status) El último Status escrito en el fichero
     */
    public static Status readFromFile(Context context) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(FILENAME);
        ObjectInputStream is = new ObjectInputStream(fis);
        Status s = (Status) is.readObject();
        is.close();
        fis.close();
        return s;
    }

    /**
     * Convierte un String a Boolean
     * @param str(String) Booleano contenido en un String
     * @return (boolean) el string convertido a boolean
     */
    public boolean parseToBool(String str) {
        if(str.equals("0"))
            return false;
        else return true;
    }

    /**
     * Parsea el dato del intermitente a un índice
     * @return (int) El índice de un array de labels
     */
    public int tlToStr() {
        if(is_emLightOn())
            return 3;
        else if(_turnLightsOn == 1)
            return 1;
        else if(_turnLightsOn == 2)
            return 2;
        else return 0;
    }

    public boolean is_locked() {
        return _locked;
    }

    public void set_locked(boolean _locked) {
        this._locked = _locked;
    }

    public boolean is_frontLightsOn() {
        return _frontLightsOn;
    }

    public void set_frontLightsOn(boolean _frontLightsOn) {
        this._frontLightsOn = _frontLightsOn;
    }

    public boolean is_posLightsOn() {
        return _posLightsOn;
    }

    public void set_posLightsOn(boolean _posLightsOn) {
        this._posLightsOn = _posLightsOn;
    }

    public int get_turnLightsOn() {
        return _turnLightsOn;
    }

    public void set_turnLightsOn(int _turnLightsOn) {
        this._turnLightsOn = _turnLightsOn;
    }

    public boolean is_emLightOn() {
        return _emLightOn;
    }

    public void set_emLightOn(boolean _emLightOn) {
        this._emLightOn = _emLightOn;
    }

    public boolean is_emStopped() {
        return _emStopped;
    }

    public void set_emStopped(boolean _emStopped) {
        this._emStopped = _emStopped;
    }

    public char get_marcha() {
        return _marcha;
    }

    public void set_marcha(char _marcha) {
        this._marcha = _marcha;
    }

    public int get_speed() {
        return _speed;
    }

    public void set_speed(int _speed) {
        this._speed = _speed;
    }

    public float get_temp() {
        return _temp;
    }

    public String getREGEX() {
        return REGEX;
    }

    public long getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(long lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public void set_temp(float _temp) {
        this._temp = _temp;
    }

    public boolean is_vent() {
        return _vent;
    }

    public void set_vent(boolean _vent) {
        this._vent = _vent;
    }

    public int get_battery_1() {
        return _battery_1;
    }

    public void set_battery_1(int _battery_1) {
        this._battery_1 = _battery_1;
    }

    public int get_battery_2() {
        return _battery_2;
    }

    public void set_battery_2(int _battery_2) {
        this._battery_2 = _battery_2;
    }

    public String getLastValid() {
        return lastValid;
    }

    public void setLastValid(String lastValid) {
        this.lastValid = lastValid;
    }

    public double get_lat() {
        return _lat;
    }

    public void set_lat(double _lat) {
        this._lat = _lat;
    }

    public double get_lon() {
        return _lon;
    }

    public void set_lon(double _lon) {
        this._lon = _lon;
    }

    public int get_sats() {
        return _sats;
    }

    public void set_sats(int _sats) {
        this._sats = _sats;
    }

    public double get_kmph() {
        return _kmph;
    }

    public void set_kmph(double _kmph) {
        this._kmph = _kmph;
    }

    public int get_pvb_1() {
        return _pvb_1;
    }

    public void set_pvb_1(int _pvb_1) {
        this._pvb_1 = _pvb_1;
    }

    public int get_pvb_2() {
        return _pvb_2;
    }

    public void set_pvb_2(int _pvb_2) {
        this._pvb_2 = _pvb_2;
    }

    public boolean is_autoStop() {
        return _autoStop;
    }

    public void set_autoStop(boolean _autoStop) {
        this._autoStop = _autoStop;
    }

    public int get_ldrReading() {
        return _ldrReading;
    }

    public void set_ldrReading(int _ldrReading) {
        this._ldrReading = _ldrReading;
    }

    public int get_distance() {
        return _distance;
    }

    public void set_distance(int _distance) {
        this._distance = _distance;
    }
}
