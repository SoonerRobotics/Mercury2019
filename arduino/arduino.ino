#include "Motor.h" //from RobotLib
#include <ArduinoJson.h>

enum Event {
  Ping = 0,
  Motors = 1,
  Launcher = 2,
  Arm = 3,
  Scoop = 4,
  Lights = 5
};

//Motors, Servos, and Lights
Motor motorA;
Motor motorB;

void setup() {
  //Initalize Serial
  Serial.begin(115200);
  
  //Initalize Motors
  motorA.begin(4,5,3);
  motorB.begin(7,8,6);
  
  //Initalize Servos

  //Initalize Lights

}

void loop() {
  //wait for data
  if (Serial.available() > 0) {
    String comms = Serial.readStringUntil('\n');
    
    StaticJsonBuffer<256> jsonBuffer;
    JsonObject& obj = jsonBuffer.parse(comms);
    
    if (obj.success()) {
      int id = obj["id"];
      JsonArray& data = obj["data"];

      switch (id) {
        case Motors : MotorInstruction(data); break;
        case Launcher : LauncherInstruction(data); break;
        case Arm : ArmInstruction(data); break;
        case Scoop : ScoopInstruction(data); break;
        case Lights : LightsInstruction(data); break;
        //This should never have values other than id. If it does, we're donezo.
      }
    }
    
    Serial.write(1);
  }
}

void MotorInstruction(JsonArray& data) {
  int left = data[0]; //-255 to 255
  int right = data[1]; //-255 to 255
  
  motorA.output(left/255.0f);
  motorB.output(right/255.0f);
}

void LauncherInstruction(JsonArray& data) {
    
}

void ArmInstruction(JsonArray& data) {
    
}

void ScoopInstruction(JsonArray& data) {
    
}

void LightsInstruction(JsonArray& data) {
    
}
