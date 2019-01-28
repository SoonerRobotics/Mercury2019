#include "Motor.h" //from RobotLib
#include <ArduinoJson.h>

Motor motorA;
Motor motorB;

struct Instruction  {
  float leftMotor;
  float rightMotor;
};

union InstructionPacket  {
  Instruction data;
  byte buffr[sizeof(Instruction)];
};

void setup() {
  Serial.begin(9600);
  
  motorA.begin(4,5,3);
  motorB.begin(7,8,6);
}

void loop() {
  //wait for data
  if (Serial.available() > 0) {
    String comms = Serial.readStringUntil('\n');
    
    StaticJsonBuffer<256> jsonBuffer;
    JsonObject& obj = jsonBuffer.parse(comms);
    
    if (obj.success()) {
      String event = obj["event"];
      
      if (event.equals("move")) {
        JsonArray& data = obj["data"];

        int left = data[0];
        int right = data[1];
        
        motorA.output(left/255.0f);
        motorB.output(right/255.0f);
      }
    }
    
    Serial.write(1);
  }
}
