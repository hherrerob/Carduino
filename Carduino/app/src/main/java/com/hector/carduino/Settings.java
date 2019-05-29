package com.hector.carduino;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Gestiona las preferencias de la aplicación y del vehículo
 */
public class Settings implements Serializable {

    private boolean _useDefault, // Configuración por defecto
                    _autoLights, // Luces automáticas
                    _autoStop, // Parada automática
                    _autoVent, // Ventilación automática
                    _notifCarOn, // Notificar encendido del vehículo
                    _notifCarParked, // Notificar coche estacionado
                    _notifLowBattery, // Notificar batería baja
                    _phoneVibeOn, // Activar vibración
                    _accelerometerOn, // Acelerar inclinando el teléfono
                    _gyroscopeOn, // Girar moviendo el teléfono
                    _usePhoneLoc; // Usar localización del teléfono

    private int _autoLightsPitch, // Sensibilidad de las luces automáticas
                _autoStopDistance, // Distancia a la que se detecta un obstaculo
                _autoVentTemp; // Temperatura para activar la ventilación

    private float  _lastLat, // Última latitud conocida
                    _lastLong; // Última longitud conocida

    private String _carName; // Nombre del vehículo


    /**
     * Constructor sobrecargado
     * @param _useDefault
     * @param _autoLights
     * @param _autoStop
     * @param _autoVent
     * @param _notifCarOn
     * @param _notifCarParked
     * @param _notifLowBattery
     * @param _phoneVibeOn
     * @param _accelerometerOn
     * @param _gyroscopeOn
     * @param _autoLightsPitch
     * @param _autoStopDistance
     * @param _autoVentTemp
     * @param _carName
     * @param _lastLat
     * @param _lastLong
     */
    public Settings(boolean _useDefault, boolean _autoLights, boolean _autoStop, boolean _autoVent, boolean _notifCarOn, boolean _notifCarParked, boolean _notifLowBattery, boolean _phoneVibeOn, boolean _accelerometerOn, boolean _gyroscopeOn, boolean _usePhoneLoc, int _autoLightsPitch, int _autoStopDistance, int _autoVentTemp, String _carName, float _lastLat, float _lastLong) {
        this._useDefault = _useDefault;
        this._autoLights = _autoLights;
        this._autoStop = _autoStop;
        this._autoVent = _autoVent;
        this._notifCarOn = _notifCarOn;
        this._notifCarParked = _notifCarParked;
        this._notifLowBattery = _notifLowBattery;
        this._phoneVibeOn = _phoneVibeOn;
        this._accelerometerOn = _accelerometerOn;
        this._gyroscopeOn = _gyroscopeOn;
        this._usePhoneLoc = _usePhoneLoc;
        this._autoLightsPitch = _autoLightsPitch;
        this._autoStopDistance = _autoStopDistance;
        this._autoVentTemp = _autoVentTemp;
        this._carName = _carName;
        this._lastLat = _lastLat;
        this._lastLong = _lastLong;
    }

    /**
     * Constructor por defecto
     */
    public Settings() {
        this._useDefault = true;
        this._autoLights = true;
        this._autoStop = true;
        this._autoVent = true;
        this._notifCarOn = false;
        this._notifCarParked = false;
        this._notifLowBattery = false;
        this._phoneVibeOn = false;
        this._accelerometerOn = false;
        this._gyroscopeOn = false;
        this._usePhoneLoc = false;
        this._autoLightsPitch = 700;
        this._autoStopDistance = 10;
        this._autoVentTemp = 24;
        this._carName = "Unknown";
        this._lastLat = 0;
        this._lastLong = 0;
    }

    /**
     * Guarda la configuración en el archivo de preferencias
     * @param context Actividad desde la cual se llama al método
     */
    public void savePrefs(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("CarduinoPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor edt = sharedPreferences.edit();

        edt.putBoolean("_useDefault", _useDefault);
        edt.putBoolean("_autoLights", _autoLights);
        edt.putBoolean("_autoStop", _autoStop);
        edt.putBoolean("_autoVent", _autoVent);
        edt.putBoolean("_notifCarOn", _notifCarOn);
        edt.putBoolean("_notifCarParked", _notifCarParked);
        edt.putBoolean("_notifLowBattery", _notifLowBattery);
        edt.putBoolean("_phoneVibeOn", _phoneVibeOn);
        edt.putBoolean("_accelerometerOn", _accelerometerOn);
        edt.putBoolean("_gyroscopeOn", _gyroscopeOn);
        edt.putBoolean("_usePhoneLoc", _usePhoneLoc);
        edt.putInt("_autoLightsPitch", _autoLightsPitch);
        edt.putInt("_autoStopDistance", _autoStopDistance);
        edt.putInt("_autoVentTemp", _autoVentTemp);
        edt.putString("_carName", _carName);
        edt.putFloat("_lastLat", _lastLat);
        edt.putFloat("_lastLong", _lastLong);

        edt.commit();
    }

    /**
     * Obtiene la configuración almacenada en el fichero de preferencias (Si hay)
     * @param context Actividad desde donde se llama al método
     */
    public void getPrefs(Context context) {
        new Settings();
        SharedPreferences sharedPreferences = context.getSharedPreferences("CarduinoPrefs", Context.MODE_PRIVATE);

        this._useDefault = sharedPreferences.getBoolean("_useDefault", _useDefault);
        this._autoLights = sharedPreferences.getBoolean("_autoLights", _autoLights);
        this._autoStop = sharedPreferences.getBoolean("_autoStop", _autoStop);
        this._autoVent = sharedPreferences.getBoolean("_autoVent", _autoVent);
        this._notifCarOn = sharedPreferences.getBoolean("_notifCarOn", _notifCarOn);
        this._notifCarParked = sharedPreferences.getBoolean("_notifCarParked", _notifCarParked);
        this._notifLowBattery = sharedPreferences.getBoolean("_notifLowBattery", _notifLowBattery);
        this._phoneVibeOn = sharedPreferences.getBoolean("_phoneVibeOn", _phoneVibeOn);
        this._accelerometerOn = sharedPreferences.getBoolean("_accelerometerOn", _accelerometerOn);
        this._gyroscopeOn = sharedPreferences.getBoolean("_gyroscopeOn", _gyroscopeOn);
        this._usePhoneLoc = sharedPreferences.getBoolean("_usePhoneLoc", _usePhoneLoc);
        this._autoLightsPitch = sharedPreferences.getInt("_autoLightsPitch", _autoLightsPitch);
        this._autoStopDistance = sharedPreferences.getInt("_autoStopDistance", _autoStopDistance);
        this._autoVentTemp = sharedPreferences.getInt("_autoVentTemp", _autoVentTemp);
        this._carName = sharedPreferences.getString("_carName", _carName);
        this._lastLat = sharedPreferences.getFloat("_lastLat", _lastLat);
        this._lastLong = sharedPreferences.getFloat("_lastLong", _lastLong);
    }

    public boolean is_autoLights() {
        return _autoLights;
    }

    public void set_autoLights(boolean _autoLights) {
        this._autoLights = _autoLights;
    }

    public boolean is_autoStop() {
        return _autoStop;
    }

    public void set_autoStop(boolean _autoStop) {
        this._autoStop = _autoStop;
    }

    public boolean is_useDefault() {
        return _useDefault;
    }

    public void set_useDefault(boolean _useDefault) {
        this._useDefault = _useDefault;
    }

    public boolean is_autoVent() {
        return _autoVent;
    }

    public void set_autoVent(boolean _autoVent) {
        this._autoVent = _autoVent;
    }

    public boolean is_notifCarOn() {
        return _notifCarOn;
    }

    public void set_notifCarOn(boolean _notifCarOn) {
        this._notifCarOn = _notifCarOn;
    }

    public boolean is_notifCarParked() {
        return _notifCarParked;
    }

    public void set_notifCarParked(boolean _notifCarParked) {
        this._notifCarParked = _notifCarParked;
    }

    public boolean is_notifLowBattery() {
        return _notifLowBattery;
    }

    public void set_notifLowBattery(boolean _notifLowBattery) {
        this._notifLowBattery = _notifLowBattery;
    }

    public boolean is_phoneVibeOn() {
        return _phoneVibeOn;
    }

    public void set_phoneVibeOn(boolean _phoneVibeOn) {
        this._phoneVibeOn = _phoneVibeOn;
    }

    public boolean is_accelerometerOn() {
        return _accelerometerOn;
    }

    public void set_accelerometerOn(boolean _accelerometerOn) {
        this._accelerometerOn = _accelerometerOn;
    }

    public boolean is_gyroscopeOn() {
        return _gyroscopeOn;
    }

    public void set_gyroscopeOn(boolean _gyroscopeOn) {
        this._gyroscopeOn = _gyroscopeOn;
    }

    public int get_autoLightsPitch() {
        return _autoLightsPitch;
    }

    public void set_autoLightsPitch(int _autoLightsPitch) {
        this._autoLightsPitch = _autoLightsPitch;
    }

    public int get_autoStopDistance() {
        return _autoStopDistance;
    }

    public void set_autoStopDistance(int _autoStopDistance) {
        this._autoStopDistance = _autoStopDistance;
    }

    public int get_autoVentTemp() {
        return _autoVentTemp;
    }

    public void set_autoVentTemp(int _autoVentTemp) {
        this._autoVentTemp = _autoVentTemp;
    }

    public String get_carName() {
        return _carName;
    }

    public float get_lastLat() {
        return _lastLat;
    }

    public void set_lastLat(float _lastLat) {
        this._lastLat = _lastLat;
    }

    public float get_lastLong() {
        return _lastLong;
    }

    public void set_lastLong(float _lastLong) {
        this._lastLong = _lastLong;
    }

    public void set_carName(String _carName) {
        this._carName = _carName;
    }

    public boolean is_usePhoneLoc() {
        return _usePhoneLoc;
    }

    public void set_usePhoneLoc(boolean _usePhoneLoc) {
        this._usePhoneLoc = _usePhoneLoc;
    }
}
