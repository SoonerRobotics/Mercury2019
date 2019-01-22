#include "Motor.h" //from RobotLib

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
  // send data only when you receive data:
  if (Serial.available() > 0) {
  
    InstructionPacket packet;
    
    if (Serial.readBytes(packet.buffr, sizeof(packet.buffr)) == sizeof(packet.buffr)) { //read all the bytes that make up an instruction packet into the packet buffer
      Instruction instruction = packet.data;
      motorA.output(instruction.leftMotor);
      motorB.output(instruction.rightMotor);
    }
  }
}
