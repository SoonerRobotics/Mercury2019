#include "Motor.h" //from RobotLib
#include <Servo.h>
#include <ArduinoJson.h>

//Motors, Servos, and Lights
Motor motorA;
Motor motorB;

Servo launchPin;

void setup() {
  //Initalize Serial
  Serial.begin(115200);
  
  //Initalize Motors
  motorA.begin(4,5,3);
  motorB.begin(7,8,6);
  
  //Initalize Servos
  launchPin.attach(9);
  launchPin.write(140); //set to armed position

  //Initalize Lights

}

void loop() {
  //wait for data
  if (Serial.available() > 0) {
    String comms = Serial.readStringUntil('\n');
    
    StaticJsonBuffer<256> jsonBuffer;
    JsonObject& obj = jsonBuffer.parse(comms);
    
    if (obj.success()) {
      MotorInstruction(obj["motor1"], obj["motor2"]);
      LauncherInstruction(obj["launcher"]);
      ArmInstruction(obj["arm"]);
      ScoopInstruction(obj["scoop"]);
      LightsInstruction(obj["lights"]);
    }
    
    Serial.write(1);
  }
}

void MotorInstruction(int motor1, int motor2) {
  //data is between -255 to 255 int for speed, convert to -1 to 1 double
  
  motorA.output(motor1/255.0f);
  motorB.output(motor2/255.0f);
}

int timeLaunched = 0;
int timeClosed = 0;
int inArmState = true;
void LauncherInstruction(int launcher) {
  //There is no data, we just care about when the packet is sent. Data can be used to control
  //servo angles from client though, if needed.
  if (inArmState && launcher == 1 && millis() - timeClosed > 500) {
    launchPin.write(70);
    timeLaunched = millis();
    inArmState = false;
  }
  if (!inArmState && launcher == 0 && millis() - timeLaunched > 500) {
    launchPin.write(140);
    timeClosed = millis();
    inArmState = true;
  }
}

void ArmInstruction(int arm) {

}

void ScoopInstruction(int scoop) {
    
}

void LightsInstruction(JsonArray& data) {
    
}
