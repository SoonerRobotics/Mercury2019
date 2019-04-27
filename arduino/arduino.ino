#include "Motor.h" //from RobotLib
#include <Adafruit_TiCoServo.h>
#include <ArduinoJson.h>
#include <FastLED.h>

//Generic Configs
#define TIMEOUT_TIME 2000

//Motor Configs
#define MOTOR_1_PWM 3
#define MOTOR_1_D1 4
#define MOTOR_1_D2 5
#define MOTOR_2_PWM 6
#define MOTOR_2_D1 7
#define MOTOR_2_D2 8

//Servo Configs
#define LAUNCHER_PIN 9
#define LAUNCHER_ARM_POS 140
#define ARM_PIN 10
 
//Lights Configs
#define LIGHT_DATA_PIN 12
#define NUM_LEDS 32

//Motors
Motor motorA;
Motor motorB;

//Servos
Adafruit_TiCoServo launcher;
Adafruit_TiCoServo arm;

//Lights
CRGB leds[NUM_LEDS];

CRGB colorScheme[] = {
  CHSV(0,0,100), //white
  CHSV(0,255,100), //red
  CHSV(0,0,255) //mega bright white
};

//For loss detection
long lastSignal = 0;
int lastStatus = 3;
bool hasSeenPi = false;

void setup() {
  //Initalize Serial
  Serial.begin(115200);
  
  //Initalize Motors
  motorA.begin(MOTOR_1_D1,MOTOR_1_D2,MOTOR_1_PWM);
  motorB.begin(MOTOR_2_D1,MOTOR_2_D2,MOTOR_2_PWM);
  
  //Initalize Servos
  launcher.write(LAUNCHER_ARM_POS); //set to armed position, is this legal?
  launcher.attach(LAUNCHER_PIN);

  arm.attach(ARM_PIN);

  //Initalize Lights
  FastLED.addLeds<WS2812B, LIGHT_DATA_PIN, GRB>(leds, NUM_LEDS);
  FastLED.clear();

  lastSignal = millis();
}


void loop() {
  //wait for data
  if (Serial.available() > 0) {
    lastSignal = millis();
    String comms = Serial.readStringUntil('\n');
    
    StaticJsonBuffer<512> jsonBuffer;
    JsonObject& obj = jsonBuffer.parse(comms);
    
    if (obj.success()) {
      lastStatus = obj["status"];
      if (lastStatus == 0) {
        hasSeenPi = true;
        MotorInstruction(obj["motor1"], obj["motor2"]);
        LauncherInstruction(obj["launcher"]);
        ArmInstruction(obj["arm"]);
        LightsInstruction(obj["lights"]);
      }
      if (lastStatus == 1) {
        WereFucked();
        MotorInstruction(0,0);
      }
      if (lastStatus == 2) {
        AwaitServer();
        MotorInstruction(0,0);
      }
    }
    Serial.write(1);
  } else {
    if (lastStatus == 1 || (hasSeenPi && millis() - lastSignal > TIMEOUT_TIME)) {
      WereFucked();
      MotorInstruction(0,0);
    }
    if (lastStatus == 2) {
      AwaitServer();
      MotorInstruction(0,0);
    }
    if (lastStatus == 3) {
      AwaitPi();
      MotorInstruction(0,0);
    }
  }
}

void MotorInstruction(int motor1, int motor2) {
  //data is between -255 to 255 int for speed, convert to -1 to 1 double
  motorA.output(motor1/255.0f);
  motorB.output(motor2/255.0f);
}

void LauncherInstruction(int data) {
  launcher.write(data);
}

void ArmInstruction(int data) {
  arm.write(data);
}

void LightsInstruction(JsonArray& data) {
  for (unsigned int i=0; i<data.size(); i++) {
    int datai = data[i];
    leds[i] = colorScheme[datai];
  }
  FastLED.show();
}

void AwaitServer() {
  for (int i=0; i<NUM_LEDS; i++) {
    leds[i] = CHSV(0,0,0);
  }

  leds[millis() % 3200 / 100] = CRGB( 0, 150, 0);

  FastLED.show();
}

void AwaitPi() {
  for (int i=0; i<NUM_LEDS; i++) {
    leds[i] = CHSV(0,0,0);
  }

  leds[millis() % 3200 / 100] = CRGB( 0, 0, 150);

  FastLED.show();
}

void WereFucked() {
  if (millis() % 1000 > 500) {
    for (int i=0; i<NUM_LEDS; i++) {
      leds[i] = colorScheme[0];
    }
  } else {
    for (int i=0; i<NUM_LEDS; i++) {
      leds[i] = colorScheme[1];
    }
  }
  FastLED.show();
}