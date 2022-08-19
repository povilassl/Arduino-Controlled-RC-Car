#include <Servo.h>
#include <SoftwareSerial.h>

#define frontMotorPin 9
#define backMotorPin 10

#define goF 80
#define goB 100

#define turnL 60
#define turnR 120
#define turnS 90

#define stopFront 100
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

void forward(){
  goingForward = true;
  backMotor.write(goF);
}

void backward(){
  backMotor.write(stopBack);  

  //if were going forward, rc stops, if not, the motor need a small pause, otherwise it won't work
  if(goingForward){
    goingForward = false;
  }else{
    delay(100);
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
  
//      Serial.print(command, HEX);
//      Serial.write("\n");
  
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
        default:
        Serial.write("error\n");
          break;
      }
    }
  }
}