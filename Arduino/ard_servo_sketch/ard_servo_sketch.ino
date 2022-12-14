#include <Servo.h>
#include <SoftwareSerial.h>

#define frontMotorPin 9
#define backMotorPin 10

#define goF 70
#define goB 110

#define turnL 60
#define turnR 120
#define turnS 90

#define stopFront 90
#define stopBack 90
  
SoftwareSerial bluetoothSerial(0, 1); // RX, TX
Servo frontServo;
Servo backMotor;

char command;
char temp;
int check;
bool goingForward;

void setup() {
  frontServo.attach(frontMotorPin);
  backMotor.attach(backMotorPin);
  
  bluetoothSerial.begin(9600);
  Serial.begin(9600);

  goingForward = false;
}

void stopMotors(int choice){

  if(choice == 1){
    frontServo.write(stopFront);  
  }
  
    backMotor.write(stopBack);

}

void forward(){
  backMotor.write(goF);
}

void backward(){
  
  backMotor.write(stopBack);
  
  for(int i=0; i<10000; i++){
    for(int j=0;j<1000; j++){}
  }

  backMotor.write(goB);
}

void straight(){
  frontServo.write(turnS);
}

void left(){
  frontServo.write(turnL);
}

void right(){
  frontServo.write(turnR);
}


void loop(){
  while(1){
      
      if (bluetoothSerial.available() > 0) {
      command = bluetoothSerial.read();
  
      //every other value is 0, so if so, return
      if(command == 0) {
        break;
      }
      
      switch (command) {
        case 'F':
          forward();
          break;
        case 'B':
          backward();
          break;
        case 'L':
          left();
          break;
        case 'R':
          right();
          break;
        case 'S':
          straight();
          break;
        case 'C':
          stopMotors(1); // choice 1 - stop front too
          break;
        case 'X':
          stopMotors(2); // choice 2 - stop back only
          break;
        default:
        Serial.write("error\n");
          break;
      }
    }
  }
}
