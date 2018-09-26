#include "RobotLib.h"

#define MOTOR_A_PIN_A 2
#define MOTOR_A_PIN_B 3

Motor motorA;

void setup() {
    motorA.begin(MOTOR_A_PIN_A, MOTOR_A_PIN_B);
}

void loop() {
    motorA.output(1);
    delay(500);
    motorA.output(0);
    delay(500);
}