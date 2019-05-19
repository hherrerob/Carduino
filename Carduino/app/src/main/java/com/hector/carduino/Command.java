package com.hector.carduino;

/**
 * Contiene todos los comandos enviables al arduino y
 * métodos de gestión de parámetros
 */
public class Command {
    /** Activa el intermitente izquierdo */
    public static final char LEFT_BLINKER_ON = 'C';
    public static final char RIGHT_BLINKER_ON = 'S';
    public static final char RIGHT_BLINKER_OFF = 'K';
    public static final char LEFT_BLINKER_OFF = 'A';
    public static final char HEADLIGHTS = 'T';
    public static final char POSITION_LIGHTS = 'f';
    public static final char TURN_LEFT = 'U';
    public static final char TURN_RIGHT = 'c';
    public static final char HONK = 'M';
    public static final char FLASH = 'w';
    public static final char BRAKE = 'E';
    public static final char EMERGENCY_LIGHT = 'i';
    public static final char[] GEAR_SHIFT = {'P', 'R', 'N', 'D'};
    public static final char[] SPEED = {'o', 'g', 'I', 'Q', 'Y', 'A'};
    public static final char ACTIVATE_PARAMETRIZE = 'j';
    public static final char LOCK = 'J';
    public static final char UNLOCK = 'B';
    public static final char AUTO_LIGHTS_ON = 'H';
    public static final char AUTO_LIGHTS_OFF = 'Z';
    public static final char AUTO_STOP_ON = 'h';
    public static final char AUTO_STOP_OFF = 'p';
    public static final char AUTO_VENT_ON = 'F';
    public static final char AUTO_VENT_OFF = 'X';
    public static final char VENT_ON = 'V';
    public static final char VENT_OFF = 'd';
    public static final char USE_DEFAULT_CONFIG = 'x';


    /**
     * Constructor por defecto
     */
    public Command() {}

    /**
     * Le da el formato necesario a un comando con parametros para ser parseado sin problemas por el arduino
     * @param param Valor del parámetro (0-999)
     * @param command Comando a enviar
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
