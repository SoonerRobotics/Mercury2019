#include "RobotLib.h"
#include <Wire.h>

Motor motorA;
Motor motorB;

void setup() {
    Wire.begin(0x8);                // join i2c bus with address #8
    Wire.onReceive(receiveEvent); // register event

    pinMode(13, OUTPUT);

    motorA.begin(4,5,3);
    motorB.begin(7,8,6);
}

void loop() {
    delay(100);
}

void receiveEvent(int howMany) {

    digitalWrite(13, HIGH);

    int counter = 0;
    while(Wire.available()) {
        int number = Wire.read();
        //Serial.print("data received: ");
        //Serial.println(number);
        Serial.print(number);
        Serial.print(" ");

        if (counter == 0){
            motorA.output((number - 128)/128.0);
        }

        if (counter == 1){
            motorB.output((number - 128)/128.0);
        }

        counter++;
    }

    Serial.println();

    digitalWrite(13, LOW);
}

int lerp(int start, int end, float fraction) {
  return (int)(start + (end - start) * fraction); //linear interpolation
}