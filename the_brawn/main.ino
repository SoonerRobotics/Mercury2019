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
        curSpeed = lerp(curSpeed, targetSpeed, curTicks/10.0); //Lerp over 50 milliseconds (depends on delay below)
        motorA.output(curSpeed/128.0);
        motorB.output(curSpeed/128.0);
    }

    delay(5);
}

void receiveEvent(int howMany) {
    while(Wire.available()) {
        number = Wire.read();
        //Serial.print("data received: ");
        //Serial.println(number);

        targetSpeed = number - 128;
    }
}

int lerp(int start, int end, float fraction) {
  return (int)(start + (end - start) * fraction); //linear interpolation
}