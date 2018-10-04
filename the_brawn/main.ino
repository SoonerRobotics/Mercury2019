#include "RobotLib.h"
#include <Wire.h>

Motor motorA;
Motor motorB;

int number = 0;

int curSpeed = 0;
int lastTargetSpeed = 0;
int targetSpeed = 0;

void setup() {
    Wire.begin(0x8);                // join i2c bus with address #8
    Wire.onReceive(receiveEvent); // register event

    motorA.begin(4,5,3);
    motorB.begin(7,8,6);
}

int curTicks = 0;
void loop() {
    if (targetSpeed != lastTargetSpeed) {
        lastTargetSpeed = targetSpeed;
        curTicks = 0;
    }
    if (curSpeed != targetSpeed) {
        curTicks++;
        curSpeed = lerp(curSpeed, targetSpeed, ((float)curTicks)/10); //Lerp over 100 milliseconds (depends on delay below)
        motorA.output(curSpeed);
        motorB.output(curSpeed);
    }

    delay(10);
}

void receiveEvent(int howMany) {
    while(Wire.available()) {
        number = Wire.read();
        //Serial.print("data received: ");
        //Serial.println(number);

        float motorOutput = (number - 128) / 128.0;
        targetSpeed = motorOutput;
    }
}

float lerp(float start, float end, float fraction) {
  return start + (end - start) * fraction; //linear interpolation
}