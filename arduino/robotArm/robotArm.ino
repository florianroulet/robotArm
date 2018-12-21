/*        
       DIY Arduino Robot Arm Smartphone Control  
        by Dejan, www.HowToMechatronics.com  
*/

#include <Servo.h>

Servo servo01;
Servo servo02;
Servo servo03;
Servo servo04;
Servo servo05;
Servo servo06;

int servo1Pos, servo2Pos, servo3Pos, servo4Pos, servo5Pos, servo6Pos, position; // current position
int speedDelay = 20;
int index = 0;
String dataIn = "";

void setup() {
  Serial.begin(9600);
  
  delay(20);
  // Robot arm initial position
  move(servo01, 7, 90);
  move(servo02, 6, 50);
  move(servo03, 5, 35);
  move(servo04, 4, 140);
  move(servo05, 3, 85);
  move(servo05, 2, 150);
  position = 0;
  Serial.write("ready");
}

void loop() {
  // Check for incoming data
  if (Serial.available() > 0) {
    dataIn = Serial.readString();  // Read the data as string
    Serial.write("dataIn : ");
    Serial.println(dataIn.substring(0,2));
    String dataInS = dataIn.substring(2, dataIn.length()); // Extract only the number. E.g. from "s1120" to "120"
    position = dataInS.toInt();  // Convert the string into integer
    Serial.println(position);

    // If "Waist" slider has changed value - Move Servo 1 to position
    if (dataIn.startsWith("s1")) {
      move(servo01, 7, position);
    }
    // Move Servo 2
    if (dataIn.startsWith("s2")) {
      move(servo02, 6, position);
    }
    // Move Servo 3
    if (dataIn.startsWith("s3")) {
      move(servo03, 5, position);
    }
    // Move Servo 4
    if (dataIn.startsWith("s4")) {
      move(servo04, 4, position);
    }
    // Move Servo 5
    if (dataIn.startsWith("s5")) {
      move(servo05, 3, position);
    }
    // Move Servo 6
    if (dataIn.startsWith("s6")) {
      move(servo06, 2, position);
    }
  }
}

void move(Servo servo, int port, int position) {
  servo.attach(port);
  servo.write(position);
  delay(1000);
  servo.detach();
}

