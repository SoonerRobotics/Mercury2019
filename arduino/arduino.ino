#include "Motor.h" //from RobotLib
#include <Adafruit_TiCoServo.h>
#include <ArduinoJson.h>
#include <FastLED.h>

//Generic Configs
#define TIMEOUT_TIME 2000 //How long to wait between packets to call a timeout

//Motor Configs
#define MOTOR_1_PWM 3
#define MOTOR_1_D1 4
#define MOTOR_1_D2 5
#define MOTOR_2_PWM 6
#define MOTOR_2_D1 7
#define MOTOR_2_D2 8

//Servo Configs
#define LAUNCHER_PIN 9
#define ARM_PIN 10
 
//Lights Configs
#define LIGHT_DATA_PIN 12
#define NUM_LEDS 32

//Motors
Motor motorA;
Motor motorB;

//Servos
//Note: We are using the Adafruit_TiCoServo instead of the standard Arduino
//Servo because the FastLED library makes them not work.
Adafruit_TiCoServo launcher;
Adafruit_TiCoServo arm;

//Color array for each LED on the LED strip.
CRGB leds[NUM_LEDS];

//Predefining colors so that colors can be sent over the network in fewer
//bytes. This isn't actually tue and I never should have made this lol.
CRGB colorScheme[] = {
  CHSV(0,0,100), //white
  CHSV(0,255,100), //red
  CHSV(0,0,255) //mega bright white
};

//For loss detection
long lastSignal = 0; //Last time we received a packet from the Pi
int lastStatus = 3; //The last mode the Pi said we are in
bool hasSeenPi = false; //Have we received at least one Pi packet?

//Runs once at power on.
void setup() {
  //Initalize Serial
  Serial.begin(115200);
  
  //Initalize Motors
  motorA.begin(MOTOR_1_D1,MOTOR_1_D2,MOTOR_1_PWM);
  motorB.begin(MOTOR_2_D1,MOTOR_2_D2,MOTOR_2_PWM);
  
  //Initalize Servos
  launcher.attach(LAUNCHER_PIN);
  arm.attach(ARM_PIN);

  //Initalize Lights
  FastLED.addLeds<WS2812B, LIGHT_DATA_PIN, GRB>(leds, NUM_LEDS);
  FastLED.clear();
 
  lastSignal = millis();
}

//Runs as often as possible.
void loop() {
  //Serial.available() returns the number of bytes that are in the Serial
  //buffer. This statement is basically "Is there serial data availble?"
  if (Serial.available() > 0) {
    lastSignal = millis(); //We read new data, update lastSignal for timeout

    //Our messages always end with a '\n', so we read bytes until we hit a '\n'
    String comms = Serial.readStringUntil('\n');
    
    //We have read the whole message. Use ArduinoJson to get the data out.
    StaticJsonBuffer<512> jsonBuffer;
    JsonObject& obj = jsonBuffer.parse(comms);
    
    //Is the data correct JSON format? AKA, did we receive the whole packet?
    if (obj.success()) {

      //Update the new state the Pi is in.
      lastStatus = obj["status"];

      //State 0 is normal functionality.
      if (lastStatus == 0) {
        hasSeenPi = true; //Update that we have seen the Pi in case it's false.

        MotorInstruction(obj["motor1"], obj["motor2"]);
        LauncherInstruction(obj["launcher"]);
        ArmInstruction(obj["arm"]);
        LightsInstruction(obj["lights"]);
      }

      //State 1 is the Pi has lost connection to the server
      if (lastStatus == 1) {
        WereFucked();
        MotorInstruction(0,0);
      }

      //State 2 is the Pi is waiting for initial server connection
      if (lastStatus == 2) {
        AwaitServer();
        MotorInstruction(0,0);
      }
    }

    //Write some data to tell the Pi we have read the message. This prevents
    //the pi from sending more data than we can handle here. May not
    //be necessary.
    Serial.write(1);

  } else { //This is when there is no available Serial data.
    
    //If we are in lost connection mode, or if we haven't received a packet in
    //TIMEOUT_TIME milliseconds, then flash red/white lights.
    if (lastStatus == 1 || (hasSeenPi && millis() - lastSignal > TIMEOUT_TIME)) {
      WereFucked();
      MotorInstruction(0,0);
    }

    //Keep updating the lights when in state 2
    if (lastStatus == 2) {
      AwaitServer();
      MotorInstruction(0,0);
    }

    //Keep updating the lights when in state 3
    //State 3 is when we are waiting for the first Pi packet.
    if (lastStatus == 3) {
      AwaitPi();
      MotorInstruction(0,0);
    }
  }
}

//Update the Motor without left and right speeds
//Speeds are between -255 and 255 inclusive.
void MotorInstruction(int motor1, int motor2) {
  //Convert [-255,255] to [-1.0,1.0]
  motorA.output(motor1/255.0f);
  motorB.output(motor2/255.0f);
}

//Update the Launcher
void LauncherInstruction(int data) {
  launcher.write(data);
}

//Update the Arm
void ArmInstruction(int data) {
  arm.write(data);
}

//Update the Lights with data from the Pi
void LightsInstruction(JsonArray& data) {

  //Loop through the LEDs
  for (unsigned int i=0; i<data.size(); i++) {
    int datai = data[i];
    leds[i] = colorScheme[datai]; //Here we use the color scheme
  }

  FastLED.show();
}

//
// The next 3 functions just do things to make the lights do cool things.
//

void AwaitServer() {
  for (int i=0; i<NUM_LEDS; i++) {
    leds[i] = CHSV(0,0,0);
  }

  //Select one LED and make it green. This math moves it right one LED every 
  //100 milliseconds
  leds[millis() % 3200 / 100] = CRGB( 0, 150, 0);

  FastLED.show();
}

void AwaitPi() {
  for (int i=0; i<NUM_LEDS; i++) {
    leds[i] = CHSV(0,0,0);
  }

  //Select one LED and make it blue. This math moves it right one LED every 
  //100 milliseconds
  leds[millis() % 3200 / 100] = CRGB( 0, 0, 150);

  FastLED.show();
}

void WereFucked() {
  //True for the second half of every second, false otherwise.
  if (millis() % 1000 > 500) {
    for (int i=0; i<NUM_LEDS; i++) {
      leds[i] = colorScheme[0]; //Make all LEDs white
    }
  } else {
    for (int i=0; i<NUM_LEDS; i++) {
      leds[i] = colorScheme[1]; //Make all LEDs red
    }
  }

  FastLED.show();
}