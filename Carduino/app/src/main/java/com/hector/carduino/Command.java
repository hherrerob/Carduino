package com.hector.carduino;

/**
 * Contiene todos los comandos enviables al arduino y
 * métodos de gestión de parámetros
 */
public class Command {
    /** Activa el intermitente izquierdo */
    public static final char LEFT_BLINKER_ON = 'C';
    /** Apaga el intermitente izquierdo */
    public static final char LEFT_BLINKER_OFF = 'A';
    /** Activa el intermitente derecho */
    public static final char RIGHT_BLINKER_ON = 'S';
    /** Apaga el intermitente derecho */
    public static final char RIGHT_BLINKER_OFF = 'K';
    /** Activa las luces frontales */
    public static final char HEADLIGHTS = 'T';
    /** Activa las luces de posición */
    public static final char POSITION_LIGHTS = 'f';
    /** Giro a la izquierda */
    public static final char TURN_LEFT = 'U';
    /** Giro a la derecha */
    public static final char TURN_RIGHT = 'c';
    /** Cláxon */
    public static final char HONK = 'M';
    /** Ráfaga */
    public static final char FLASH = 'w';
    /** Freno */
    public static final char BRAKE = 'E';
    /** Activa las luces de emergencia */
    public static final char EMERGENCY_LIGHT = 'i';
    /** Marchas */
    public static final char[] GEAR_SHIFT = {'P', 'R', 'N', 'D'};
    /** Velocidades */
    public static final char[] SPEED = {'o', 'g', 'I', 'Q', 'Y', 'A'};
    /** Activa el modo de parametrización */
    public static final char ACTIVATE_PARAMETRIZE = 'j';
    /** Canda el vehículo */
    public static final char LOCK = 'J';
    /** Descanda el vehículo */
    public static final char UNLOCK = 'B';
    /** Activa las luces automáticas */
    public static final char AUTO_LIGHTS_ON = 'H';
    /** Desactiva las luces atumáticas */
    public static final char AUTO_LIGHTS_OFF = 'Z';
    /** Activa la parada automática */
    public static final char AUTO_STOP_ON = 'h';
    /** Apaga la parada automáticao */
    public static final char AUTO_STOP_OFF = 'p';
    /** Activa la ventilación automática */
    public static final char AUTO_VENT_ON = 'F';
    /** Desactiva la ventilación automática */
    public static final char AUTO_VENT_OFF = 'X';
    /** Activa la ventilación */
    public static final char VENT_ON = 'V';
    /** Desactiva la ventilación */
    public static final char VENT_OFF = 'd';
    /** Configuración por defecto */
    public static final char USE_DEFAULT_CONFIG = 'x';
    /** Comprobar pico de batería */
    public static final char PEAK_BATTERY = '<';


    /**
     * Constructor por defecto
     */
    public Command() {}

    /**
     * Le da el formato necesario a un comando con parametros para ser parseado sin problemas por el arduino
     * @param param(int) Valor del parámetro (0-999)
     * @param command(char) Comando a enviar
     * @return String de longitud = 4 con sintaxis [COMANDO][VALOR][VALOR][VALOR]
     */
    public static String formatParameter(int param, char command) {
        String s = param + "";
        while(s.length() != 3) {
            s = "0" + s;
        }

        return (command + s);
    }
}
