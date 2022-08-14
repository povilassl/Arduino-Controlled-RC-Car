#include <Servo.h>
Servo frontServo;
Servo backServo;

int const frontPin = A0;
int steeringVal;
int angle;
String data;


int iter = 0;
int frontAngleTemp = 0;
int backAngleTemp = 0;

void setup() {
  frontServo.attach(9);
  backServo.attach(10);
  Serial.begin(9600);
  

}



void loop() {
  //steeringVal = analogRead(frontPin);
  //steeringAngle = map(steeringVal, 0, 1023, 0, 179);

//  if(iter == 0){
//    iter = 1;
//    frontAngleTemp = 160;
//    backAngleTemp = 90;
//  }else{
//    iter = 0;
//    frontAngleTemp = 0;
//    backAngleTemp = 80;
//  }



    while(Serial.available() > 0){
      data = Serial.read();
      Serial.print(data + "\n");
      
    }

  
//  frontServo.write(frontAngleTemp);
//  backServo.write(backAngleTemp);
  
//  Serial.println("Steering angle: " + String(steeringAngle));
  
  delay(1000);
}
