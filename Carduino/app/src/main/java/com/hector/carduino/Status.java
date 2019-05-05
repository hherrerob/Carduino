package com.hector.carduino;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

/**
 * Contiene información sobre el estado del vehículo
 */
public class Status implements Serializable {
    private static final String FILENAME = "currentStatus.txt";
    private static final String REGEX = "[{][0-1][,][0-1][,][0-1][,][0-2][,][0-1][,][0-1][,][P,R,N,D][,][1-6][,][0-9]{1,2}[.]{0,1}[0-9]{0,2}[}]";
    private boolean _locked;
    private boolean _frontLightsOn;
    private boolean _posLightsOn;
    private int _turnLightsOn;
    private boolean _emLightOn;
    private boolean _emStopped;
    private char _marcha;
    private int _speed;
    private float _temp;
    private long lastStatus;
    private String last;
    private String lastValid;

    /**
     * Establece el Status a partir del mensaje recogido por Bluetooth
     * @param status Mensaje recibido del Bluetooth
     */
    public Status(String status) {
        lastValid = getLast(status);
        try {
            lastValid = lastValid.substring(1, lastValid.length() - 2);
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
            this.lastStatus = System.currentTimeMillis();
        } catch(IndexOutOfBoundsException e){
        } catch(NullPointerException ex){ }
    }

    public Status() {
        this.lastStatus = System.currentTimeMillis();
    }

    public Status(long lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getlast() {
        return last;
    }


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
     * @param str Booleano contenido en un String
     * @return Boolean: el string convertido a Boolean
     */
    public boolean parseToBool(String str) {
        if(str.equals("0"))
            return false;
        else return true;
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
}
