#include <TinyGPS++.h>
#include <TimedAction.h>
#include <AFMotor.h>
#include <Servo.h>

// GLOBALS
char _MARCHA = 'P';
boolean _LOCKED = true; // COCHE CANDADO: NO LEE COMANDOS
int _VELOCIDAD = 1; // VELOCIDADES
double _TEMPERATURE = 0; // SENSOR DE TEMPERATURA
boolean _VENT = false; // VENTILACION ACTIVADA MANUALMENTE
int _BATTERY_1 = 100; // PORCENTAJE DE BATERÍA DEL ARDUINO
int _BATTERY_2 = 100; // PORCENTAJE DE BATERÍA DE LOS MOTORES
double _LAT = 0.0; // LATITUD
double _LONG = 0.0; // LONGITUD
int _SATS = -1; // SATÉLITES CONECTADOS
double _GPS_SPEED = 0; // VELOCIDAD DEL SATÉLITE

char cmd = '.';
boolean _PARAMETRIZE = false; // EL SIGUIENTE COMANDO RECIBIDO ES UN PARÁMETRO
int LDRReading = 0; // SENSOR DE LUZ
long duration = 0; // ULTRASONIC
int distance = 0; // ULTRASONIC
int BAT_1_MAX = 40; // MÁXIMO INPUT DE LA BATERÍA EN REPOSO
int BAT_1_MIN = 20; // MÍNIMO INPUT DE LA BATERÍA EN REPOSO
int BAT_2_MAX = 35; // MÁXIMO INPUT DE LA BATERÍA EN REPOSO
int BAT_2_MIN = 20; // MÍNIMO INPUT DE LA BATERÍA EN REPOSO
int PBV_1 = 0; // MÁXIMO REGISTRADO
int PBV_2 = 0; // MÁXIMO REGISTRADO

// SETTINGS
boolean _autoLights = true; // ACTIVAR LUCES AUTOMATICAMENTE
int _lightsSensorPitch = 700; // A PARTIR DE
boolean _autoStop = true; // PARAR EL VEHÍCULO SI DETECTA OBSTACULO
int _stopSensorDistance = 10; // A PARTIR DE (ESCALABLE)
boolean _autoVent = false; // ACTIVA LA VENTILACIÓN AUTOMATICAMENTE
int _autoVentPitch = 24; // SI LA TEMPERATURA ESTA POR ENCIMA DE

// PINES
int const TLL = 44; // INTERMITENTE IZQUIERDO
int const TLR = 42; // INTERMITENTE DERECHO
int activeTurnLight = 0; // AUXILIAR DE INTERMITENTES
int const FLL = 52; // LUZ DELANTERA IZQUIERDA
int const FLR = 22; // LUZ DELANTERA DERECHA
int const BLL = 53; // LUZ DE FRENO/POSICION IZQUIERDA
int const BLR = 23; // LUZ DE FRENO/POSICION DERECHA
int const BUZZER = 34; // CLAXON
int const TRIGGER = 24; // SENSOR DE ULTRASONIDO
int const ECHO = 26; // SENSOR DE ULTRASONIDO
int const _TEMP = 3; // SENSOR DE TEMPERATURA
int const LDR = 4; // SENSOR DE LUZ
int const BAT1 = 9; // LECTOR DE BATERIA DEL ARDUINO
int const BAT2 = 8; // LECTOR DE BATERIA DE LOS MOTORES
int const GPS_TX = 16; // TRANSMISOR GPS
int const GPS_RX = 17; // RECEPTOR GPS
AF_DCMotor DirMotor(2); // MOTOR DE DIRECCIÓN
AF_DCMotor VentMotor(3); // MOTOR VENTILADOR
AF_DCMotor TracMotor(4); // MOTOR DE TRACCIÓN
TinyGPSPlus gps; // OBJETO GPS

// SWITCHES/TRIGGERS
boolean turnLightsOn = false; // ON/OFF INTERMITENTES
int turnLightOn = 0; // 0 NINGUNO, 1 IZQUIERDO, 2 DERECHO
boolean frontLightsOn = false; // ON/OFF LUZ FRONTAL
boolean posLightsOn = false; // ON/OFF LUCES DE POSICION
boolean emLightsOn = false; // ON/OFF LUZ DE EMERGENCIA
boolean emStopped = false; // PARADA DE EMERGENCIA
//TOGGLERS
boolean turnLightsToggler = false; // INTERMITENTES
boolean emLightsToggler = false; // LUZ DE EMERGENCIA
boolean autoStop = false; // PARADA AUTOMÁTICA
boolean ventOffAuto = false; // APAGA AUTOMÁTICAMENTE LA VENTILACIÓN

// THREADS
TimedAction turnLightsThread = TimedAction(300, turnLightsBlinker); // INTERMITENTES
TimedAction emLightThread = TimedAction(300, emLightsBlinker); // LUCES DE EMERGENCIA
TimedAction tempThread = TimedAction(2000, getTemp); // COMPROBAR TEMPERATURA
TimedAction autoStopThread = TimedAction(100, crashDetect); // DETECCIÓN DE CHOQUE
TimedAction autoLightsThread = TimedAction(500, autoLights); // LUCES AUTOMATICAS
TimedAction dataTrackThread = TimedAction(230, sendData); // ENVIAR DATOS
TimedAction autoVentThread = TimedAction(1000, autoVent); // ENVIAR DATOS
TimedAction findBatteryThread = TimedAction(60000, findBattery); // COMPROBAR ESTADO DE BATERÍAS
TimedAction peakBatteryThread = TimedAction(1000, peakBattery); // COMPROBAR ESTADO DE BATERÍAS

// ARRAY DE COMANDOS
int command[] = {
//                1                   2           3                 4                  5                6           7            8
    (int) motorSpeedHandler, (int) setLock, (int) blinker, (int) posLightsToggle, (int) brake, (int) setAutoVent, (int) NONE, (int) gearShift, // 1
    (int) motorSpeedHandler, (int) setLock, (int) blinker, (int) posLightsToggle, (int) buzzer, (int) setAutoVent, (int) NONE, (int) gearShift, // 2
    (int) motorSpeedHandler, (int) setLights, (int) blinker, (int) frontLightsToggle, (int) turn, (int) setVent, (int) NONE, (int) gearShift, // 3
    (int) motorSpeedHandler, (int) setLights, (int) blinker, (int) frontLightsToggle, (int) turn, (int) setVent, (int) NONE, (int) gearShift, // 4
    (int) motorSpeedHandler, (int) setAutoStop, (int) emLight, (int) activateParametrize, (int) turn, (int) NONE, (int) NONE, (int) NONE, // 5
    (int) motorSpeedHandler, (int) setAutoStop, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE, // 6
    (int) frontLightFlash, (int) defaultConfig, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE, // 7
    (int) peakBattery, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE, (int) NONE  // 8
//        1           2           3           4           5           6           7           8
};

char charCommand[] = {
//   1    2    3    4    5    6    7    8
    'A', 'B', 'C', 'f', 'E', 'F', 'G', 'P', // 1
    'I', 'J', 'K', 'L', 'M', 'X', 'O', 'R', // 2
    'Q', 'H', 'S', 'T', 'U', 'V', 'W', 'N', // 3
    'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'D', // 4
    'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 5
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 6
    'w', 'x', 'y', 'z', '¿', '?', '¡', '!', // 7
    '<', '>', '-', '=', '$', '%', '&', '#'  // 8
};

// LLAMAR A FUNCION A PARTIR DEL PUNTERO
struct _Object {
    void (* functionPointer)();
    int value;
};

void passFunction(void (* functionPointer)()) {
    functionPointer();
}

void setup() {
  Serial.begin(9600); // ARDUINO
  Serial1.begin(9600); // BLUETOOTH
  Serial2.begin(9600); // GPS
  
  // DEFINICIONES
  pinMode(TLL, OUTPUT);
  pinMode(TLR, OUTPUT);
  pinMode(FLL, OUTPUT);
  pinMode(FLR, OUTPUT);
  pinMode(BLL, OUTPUT);
  pinMode(BLR, OUTPUT);
  pinMode(TRIGGER, OUTPUT);
  pinMode(ECHO, INPUT);
  defaultConfig();
  
  // ENCENDER MOTORES
  DirMotor.run(RELEASE);
  TracMotor.run(RELEASE);
  VentMotor.run(FORWARD);

  // COCHE LISTO
  frontLightFlash();
  delay(100);
  frontLightFlash();
  
  peakBattery();
  findBattery();
}

void loop() {
  if(Serial1.available()) {
    if(_PARAMETRIZE)
      setParameter(Serial1.readString());
    else cmd = Serial1.read();
    Serial.println(cmd);
    execute(cmd);
  }
 
  checkThreads();
}

// ENVIA EL ESTADO DEL VEHÍCULO
void sendData() {
  String data = String(_LOCKED) + "," + String(frontLightsOn) + ",";
  data += String(posLightsOn) + "," + String(turnLightOn) + ",";
  data += String(emLightsOn) + "," + String(emStopped) + ",";
  data += String(_MARCHA) + "," + String(_VELOCIDAD) + ",";
  data += String(_TEMPERATURE) + "," + String(_VENT) + ",";
  data += String(_BATTERY_1) + "," + String(_BATTERY_2) + ",";
  data += String(_LAT, 6) + "," + String(_LONG, 6) + ",";
  data += String(_SATS) + "," + String(_GPS_SPEED) + ",";
  data += String(PBV_1) + "," + String(PBV_2) + ",";
  data += String(autoStop) + "," + String(LDRReading) + ",";
  data += String(distance);
  
  data = "{" + data + "};";
  
  Serial1.println(data);
  //Serial.println(data);
}

// ESTABLECE EL VALOR DE EL RESPECTIVO PARÁMETRO
void setParameter(String param) {
  String setter = String(param[1]) + String(param[2]) + String(param[3]);
  switch(param[0]) {
    case 'H': _lightsSensorPitch = setter.toInt();
              _autoLights = true;
      break;
    case 'Z': _lightsSensorPitch = setter.toInt();
              _autoLights = false;
      break;
    case 'h': _stopSensorDistance = setter.toInt();
              _autoStop = true;
      break;
    case 'p': _stopSensorDistance = setter.toInt();
              _autoStop = false;
      break;
    case 'F': _autoVentPitch = setter.toInt();
              _autoVent = true;
      break;
    case 'X': _autoVentPitch = setter.toInt();
              _autoVent = false;
      break;
    default: NONE();
      break;
  }

  cmd = '.';
  _PARAMETRIZE = false;
  sendData();
  sendData();
}

// ACTIVA EL MODO DE PARAMETRIZAJE
void activateParametrize() {
  _PARAMETRIZE = true;
}

// BLOQUEA/DESBLOQUEA EL COCHE
void setLock() {
  if(cmd == 'B') {
    _LOCKED = false;
    frontLightFlash();
    delay(100);
    frontLightFlash();
  } else {
    _LOCKED = true;
    allOff();
    delay(100);
    longFrontLightFlash();
  }
}

void allOff() {
  // RESET DE LOS TRIGGERS
  turnLightsOn = false;
  frontLightsOn = false;
  posLightsOn = false;
  emLightsOn = false;
  emStopped = false;

  // APAGADO DE TODAS LAS LUCES
  digitalWrite(TLL, LOW);
  digitalWrite(TLR, LOW);
  digitalWrite(FLL, LOW);
  digitalWrite(FLR, LOW);
  digitalWrite(BLL, LOW);
  digitalWrite(BLR, LOW);
}

// CAMBIA LA CONFIGURACIÓN ACTUAL POR LOS VALORES POR DEFECTO
void defaultConfig() {
  _autoLights = true;
  _lightsSensorPitch = 700;
  _autoStop = true;
  _stopSensorDistance = 10;
  _autoVent = false;
  _autoVentPitch = 24;
}

// ACTIVA/DESACTIVA LAS LUCES AUTOMATICAS
void setLights() {
  if(cmd == 'H')
    _autoLights = true;
  else _autoLights = false;
}

// ACTIVA/DESACTIVA LA PARADA AUTOMATICA
void setAutoStop() {
  if(cmd == 'h')
    _autoStop = true;
  else _autoStop = false;
}

// ACTIVA/DESACTIVA LA VENTILACION AUTOMÁTICA
void setAutoVent() {
  if(cmd == 'F') {
    _autoVent = true;
    ventOffAuto = true;
  } else {
    _autoVent = false;
    ventOffAuto = false;
  }
}

// ACTIVA/DESACTIVA LA VENTILACION
void setVent() {
  if(cmd == 'V') {
    _VENT = true;
    ventOffAuto = false;
  } else {
    _autoVent = false;
    ventOffAuto = true;
    _VENT = false;
  }

  vent();
}

// CAMBIO DE VELOCIDAD
void motorSpeedHandler() {
  emStopped = false;
  autoStop = false;

  if(cmd == 'A') {
    runMotor(255);
    _VELOCIDAD = 6;
  } else if(cmd == 'Y') {
    runMotor(200);
    _VELOCIDAD = 5;
  } else if(cmd == 'Q') {
    runMotor(170);
    _VELOCIDAD = 4;
  } else if(cmd == 'I') {
    runMotor(130);
    _VELOCIDAD = 3;
  } else if(cmd == 'g') {
    runMotor(100);
    _VELOCIDAD = 2;
  } else if(cmd == 'o') {
    runMotor(0);
    _VELOCIDAD = 1;
  }
}

// CAMBIO DE MARCHA
void gearShift() {
  emStopped = false;
  autoStop = false;
  _MARCHA = cmd;
  
  if(_MARCHA == 'P') {
    TracMotor.run(RELEASE);
  } else if(_MARCHA == 'R') {
    TracMotor.run(BACKWARD);
  } else if(_MARCHA == 'N') {
    TracMotor.run(RELEASE);
  } else if(_MARCHA == 'D') {
    TracMotor.run(FORWARD);
  }
}

// APAGA Y ENCIENDE EL INTERMITENTE ACTIVADO
void turnLightsBlinker() {
  turnLightsToggler = !turnLightsToggler;
  if(turnLightsToggler) {
    digitalWrite(activeTurnLight, HIGH);
  } else {
    digitalWrite(activeTurnLight, LOW);
  }
}

// APAGA Y ENCIENDE LAS LUCES DE EMERGENCIA
void emLightsBlinker() {
  emLightsToggler = !emLightsToggler;
  if(emLightsToggler) {
    digitalWrite(TLL, HIGH);
    digitalWrite(TLR, HIGH);
  } else {
    digitalWrite(TLL, LOW);
    digitalWrite(TLR, LOW);
  }
}

// ACTIVA/DESACTIVA LAS LUCES DELANTERAS
void frontLightsToggle() {
  frontLightsOn = !frontLightsOn;
  if(frontLightsOn) {
    digitalWrite(FLL, HIGH);
    digitalWrite(FLR, HIGH);
  } else {
    digitalWrite(FLL, LOW);
    digitalWrite(FLR, LOW);
  }
}

// ACTIVA/DESACTIVA LAS LUCES DE POSICION
void posLightsToggle() {
  posLightsOn = !posLightsOn;
  if(posLightsOn) {
    digitalWrite(BLL, HIGH);
    digitalWrite(BLR, HIGH);
  } else {
    digitalWrite(BLL, LOW);
    digitalWrite(BLR, LOW);
  }
}

// ACTIVA/DESACTIVA LOS INTERMITENTES
void blinker() {
   emLightsOn = false;
   // RESETEAR AMBAS LUCES
   digitalWrite(TLL, LOW);
   digitalWrite(TLR, LOW);
   
   switch (cmd) {
    case 'C':
      turnLightsOn = true;
      activeTurnLight = TLL;
      turnLightOn = 1;
      break;
    case 'K':
      turnLightsOn = false;
      turnLightOn = 0;
      break;
    case 'S':
      turnLightsOn = true;
      activeTurnLight = TLR;
      turnLightOn = 2;
      break;
    case 'a':
      turnLightsOn = false;
      turnLightOn = 0;
      break;
    default:
      // NADA
      break;
  }
}

// ACTIVA/DESACTIVA LA LUZ DE EMERGENCIA
void emLight() {
  turnLightsOn = false;
  turnLightOn = 0;
  emLightsOn = !emLightsOn;
  emLightsToggler = true; // VALOR INVERTIDO
  emLightsBlinker();
}

void autoLights() {
  LDRReading = analogRead(LDR);

  // VALORES INVERTIDOS (AL EJECUTAR LA FUNCION SE PONEN CORRECTAMENTE)
  if(LDRReading <= _lightsSensorPitch) {
    frontLightsOn = false;
    posLightsOn = false;
  } else {
    frontLightsOn = true;
    posLightsOn = true;
  }
  frontLightsToggle();
  posLightsToggle();
}

// FLASH DE LUCES FRONTALES
void frontLightFlash() {
  if(frontLightsOn) { // SI ESTAN YA ENCENDIDAS LAS APAGA
    digitalWrite(FLL, LOW);
    digitalWrite(FLR, LOW);
    delay(50);
  }

  // HACE EL FLASH
  digitalWrite(FLL, HIGH);
  digitalWrite(FLR, HIGH);
  delay(100);
  digitalWrite(FLL, LOW);
  digitalWrite(FLR, LOW);
  
  if(frontLightsOn) { // Y LAS VUELVE A ENCENDER (SI ESTABAN ENCENDIDAS)
    delay(50);
    digitalWrite(FLL, HIGH);
    digitalWrite(FLR, HIGH);
  }
}

// FLASH DE LUCES FRONTALES LARGO
void longFrontLightFlash() {
  digitalWrite(FLL, HIGH);
  digitalWrite(FLR, HIGH);
  delay(700);
  digitalWrite(FLL, LOW);
  digitalWrite(FLR, LOW);
}

// GIRA
void turn() {
  DirMotor.setSpeed(255);
  if(cmd == 'U')
    DirMotor.run(BACKWARD);
  else if(cmd == 'k')
    DirMotor.setSpeed(0);
  else DirMotor.run(FORWARD);
}

// DISMINUYE/AUMENTA LA _VELOCIDAD (RECUBRIMIENTO)
void runMotor(int speed) {
  TracMotor.setSpeed(speed); 
}

// ACTIVA EL CLAXON
void buzzer() {
  tone(BUZZER, 440);
  delay(100);
  noTone(BUZZER);
}

// DETECTOR DE CHOQUE
void crashDetect() {
  // LIMPIA EL TRIGGER
  digitalWrite(TRIGGER, LOW);
  delayMicroseconds(2);
  // ACTIVA EL TRIGGER 10ms
  digitalWrite(TRIGGER, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIGGER, LOW);
  // LEE EL ECHO, DEVUELVE EL TIEMPO DEL VIAJE DE LA ONDA DE SONIDO EN ms
  duration = pulseIn(ECHO, HIGH);
  // CALCULA LA DISTANCIA
  distance = duration*0.034/2;

  // PARADA AUTOMATICA
  if(distance <= (_VELOCIDAD * _stopSensorDistance) and !emStopped and _MARCHA == 'D') {
    emergencyStop();
  }
}

// ACTIVA LAS LUCES DE EMERGENCIA
void emergencyStop() {
  emLightsOn = false; // VALOR INVERTIDO
  emStopped = true;
  autoStop = true;
  emLight();
  posLightsOn = false; // VALOR INVERTIDO
  posLightsToggle();
  fullBrake();
}

// HACE UNA PARADA EN SECO
void fullBrake() {
  TracMotor.run(BACKWARD);
  TracMotor.setSpeed(255);
  delay(_VELOCIDAD * 100);
  TracMotor.run(FORWARD);
  TracMotor.setSpeed(255);
  delay(_VELOCIDAD * 10);
  TracMotor.setSpeed(0);
}

// INVIERTE EL SENTIDO DEL MOTOR LIGERAMENTE
void brake() {
    if(_MARCHA == 'D') {
      TracMotor.run(BACKWARD);
    } else TracMotor.run(FORWARD);
    
    delay(100);
    
    if(_MARCHA == 'D') {
      TracMotor.run(FORWARD);
    } else TracMotor.run(BACKWARD);
}

// COMPRUEBA LOS HILOS EN FUNCIONAMIENTO
void checkThreads() {
  if(emLightsOn and !_LOCKED)
    emLightThread.check();
  else if(turnLightsOn and !_LOCKED)
    turnLightsThread.check();
  
  tempThread.check();
  if(_autoStop and !_LOCKED)
    autoStopThread.check();
  if(_autoLights and !_LOCKED)
    autoLightsThread.check();

  dataTrackThread.check();
  autoVentThread.check();
  peakBatteryThread.check();
  findBatteryThread.check();
  gpsCheck();
}

// ALMACENA LOS VALORES MAS ALTOS RECIBIDOS POR LA PILA
void peakBattery() {
  int b1 = analogRead(BAT1);
  int b2 = analogRead(BAT2);
  
  if(b1 <= BAT_1_MAX and b1 > PBV_1)
    PBV_1 = b1;

   if(b1 <= BAT_2_MAX and b2 > PBV_2)
    PBV_2 = b2;
}

// OBTIENE LOS PORCENTAJES DE BATERÍA RESTANTES
void findBattery() {
  if(PBV_1 > 0) {
    _BATTERY_1 = scale(PBV_1, BAT_1_MIN, BAT_1_MAX);
    if(_BATTERY_1 > 100)
      _BATTERY_1 = 100;
    else if(_BATTERY_1 < 0)
      _BATTERY_1 = 0;

    PBV_1 = 0;
  }

  if(PBV_2 > 0) {
    _BATTERY_2 = scale(PBV_2, BAT_2_MIN, BAT_2_MAX);
    
    if(_BATTERY_2 > 100)
      _BATTERY_2 = 100;
    else if(_BATTERY_2 < 0)
      _BATTERY_2 = 0;

    PBV_2 = 0;
  }
}

// ESCALA UN VALOR ENTRE 0-100
int scale(double n, double minv, double maxv) {
  return (int) ((n-minv)/(maxv-minv) * (100));
}

// COMPRUEBA EL GPS
void gpsCheck() {
  if (Serial2.available()) {
   gps.encode(Serial2.read());
   if (gps.location.isUpdated()){
      _LAT = gps.location.lat();
      _LONG = gps.location.lng();
      _SATS = gps.satellites.value();
      _GPS_SPEED = gps.speed.kmph();
   }
 }
}

// HACE 3 FLASHES DE LAS LUCES DE FRENO
void brakeLights() {
  for(int i=0;i<3;i++){
    digitalWrite(BLL, HIGH);
    digitalWrite(BLR, HIGH);
    delay(25);
    digitalWrite(BLL, LOW);
    digitalWrite(BLR, LOW);
    delay(25);
  }

  if(posLightsOn) {
    digitalWrite(BLL, HIGH);
    digitalWrite(BLR, HIGH);
  }
}

// EJECUTA EL COMANDO SI ES POSIBLE
void execute(char cmd) {
  int index = findIndex(charCommand, cmd);
  if(index != -1) {
    if(cmd == 'B' or !_LOCKED)
      ((void (*)()) command[index])();
  }
}

// BUSCA EL INDICE DE UN ELEMENTO SI EXISTE
int findIndex(char charCommand[], char cmd) {
  for(int i=0;i<strlen(charCommand);i++){
    if(cmd == charCommand[i])
      return i;
  }
  return -1;
}

// ACTUALIZA LA TEMPERATURA DEL COCHE
void getTemp() {
  _TEMPERATURE = (5.0 * analogRead(_TEMP) * 100.0)/1023.0;
}

// ACTIVA/DESACTIVA LA VENTILACIÓN AUTOMATICAMNTE
void autoVent() {
  if((_TEMPERATURE > _autoVentPitch and _autoVent)) {
      _VENT = true;
      VentMotor.setSpeed(123);
  } else if(ventOffAuto) {
    VentMotor.setSpeed(0);
    _VENT = false;
  }
}

// ACTIVA/DESACTIVA LA VENTILACIÓN AUTOMATICAMNTE
void vent() {
  if(_VENT)
      VentMotor.setSpeed(123);
  else VentMotor.setSpeed(0);
}

// COMANDO SIN USAR
void NONE() {
  Serial.println("NOT IMPLEMENTED");
}
