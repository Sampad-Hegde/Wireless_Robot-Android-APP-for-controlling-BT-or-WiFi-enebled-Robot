#include <SoftwareSerial.h>
#include <Ultrasonic.h>
#include <Servo.h>

int distance, leftIR, rightIR, centreIR, leftDist, rightDist;

const int R_Front  = 7;
const int R_Back  = 3;
const int L_Front  = 5;
const int L_Back  = 4;

const int pwm_pin = 6;

const int servo_pin = 9;

const int trig_pin = A0;
const int echo_pin = A1;

const int bt_rx = 11;
const int bt_tx = 12;

const int leftIRPin = 8;
const int rightIRPin = 10;
const int centreIRPin = 13;

SoftwareSerial BTSerial(bt_tx, bt_rx);
Ultrasonic ultrasonic(trig_pin, echo_pin);
Servo myservo;

int mode = 1;
int command;


void lineFollower()
{
  leftIR = digitalRead(leftIRPin);
  rightIR = digitalRead(rightIRPin);
  centreIR = digitalRead(centreIRPin);
  
  if ((leftIR == 0)&&(centreIR == 1)&&(rightIR == 0))
  {
    Forward();
  }
  
  if ((leftIR == 1)&&(centreIR == 1)&&(rightIR == 0))
  {
    Left();
  }
  if ((leftIR == 1)&&(centreIR ==0)&&(rightIR == 0)) 
  {
    Left();
  }
  
  if ((leftIR == 0)&&(centreIR == 1)&&(rightIR == 1))
  {
    Right();
  }
  if ((leftIR == 0)&&(centreIR == 0)&&(rightIR == 1))
  {
    Right();
  }
  
  if ((leftIR == 1)&&(centreIR == 1)&&(rightIR == 1))
  {
    Stop();
  }
}

void obstacleAvoid()
{

    distance = ultrasonic.read();
    if(distance < 15)
    {
      Stop();
      delay(100);
      Backward();
      delay(500);
      Stop();
      
      myservo.write(30);
      rightDist = ultrasonic.read();
      delay(500);
      
      myservo.write(90);
      delay(500);
      
      myservo.write(145);
      leftDist = ultrasonic.read();
      
      delay(500);
      myservo.write(90);

      if(rightDist > leftDist)
      {
        Right();
        delay(1000);
      }
      else
      {
        Left();
        delay(1000);
      }
      
    }
    else
    {
      Forward();
    }
  
}

void setup() 
{
    pinMode(R_Front, OUTPUT);
    pinMode(R_Back, OUTPUT);
    pinMode(L_Front, OUTPUT);
    pinMode(L_Back, OUTPUT);
    
    pinMode(pwm_pin, OUTPUT);
    analogWrite(pwm_pin,100);

    pinMode(leftIRPin, INPUT);
    pinMode(rightIRPin, INPUT);
    pinMode(centreIRPin, INPUT);

    myservo.attach(servo_pin);
    
    BTSerial.begin(9600);
    
}

void Forward()
{
    digitalWrite(R_Front, HIGH);
    digitalWrite(R_Back, LOW);
    digitalWrite(L_Front, HIGH);
    digitalWrite(L_Back, LOW);
}

void Backward()
{
    digitalWrite(R_Front, LOW);
    digitalWrite(R_Back, HIGH);
    digitalWrite(L_Front, LOW);
    digitalWrite(L_Back, HIGH);
}

void Left()
{
    digitalWrite(R_Front, HIGH);
    digitalWrite(R_Back, LOW);
    digitalWrite(L_Front, LOW);
    digitalWrite(L_Back, HIGH);
}

void Right()
{
    digitalWrite(R_Front, LOW);
    digitalWrite(R_Back, HIGH);
    digitalWrite(L_Front, HIGH);
    digitalWrite(L_Back, LOW);
}

void Stop()
{
    digitalWrite(R_Front, LOW);
    digitalWrite(R_Back, LOW);
    digitalWrite(L_Front, LOW);
    digitalWrite(L_Back, LOW);
}

void SetSpeed(int Speed)
{
  analogWrite(pwm_pin,Speed);
}

void buttonAction(int command)
{
  switch (command) 
  {
    case 'S':
      Stop();
      break;
    
    case 'F':
      Forward();
      break;
    
    case 'B':
      Backward();
      break;
    
    case 'L':
      Left();
      break;
    
    case 'R':
      Right();
      break;
  }
}


void loop() 
{
    if (BTSerial.available() > 0) 
    {
      command = BTSerial.read();
      switch (command) 
      {
        case 'b':
          mode = 1;
          break;

        case 'o':
          mode = 2;
          break;

        case 'l':
          mode = 3;
          break;

        case '0':
          SetSpeed(0);
          break;
        case '1':
          SetSpeed(30);
          break;
        case '2':
          SetSpeed(60);
          break;
        case '3':
          SetSpeed(90);
          break;
        case '4':
          SetSpeed(120);
          break;
        case '5':
          SetSpeed(150);
          break;
        case '6':
          SetSpeed(180);
          break;
        case '7':
          SetSpeed(210);
          break;
        case '8':
          SetSpeed(240);
          break;
        case '9':
          SetSpeed(255);
          break;
      }
    }
      if (mode == 3)
      {
        lineFollower();
      }
      else if (mode == 2)
      {
        obstacleAvoid();
      }
      else
      {
         buttonAction(command);
      } 
}
