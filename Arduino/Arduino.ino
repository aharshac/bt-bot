/*
 * Maker: Harsha Alva
 * Chassis: The Node Bot 
 * Board: Arduino Mega 2560
 * Bluetooth Module: HC-05
 * Profile: Serial Port (SPP)
 */

const int pinMotorRightForward = 4;
const int pinMotorRightReverse = 5;
const int pinMotorLeftForward = 6;
const int pinMotorLeftReverse = 7;


void setup() {
  Serial.begin(9600);       // Log output
  Serial1.begin(9600);      // HC-05 Bluetooth SPP
  Serial1.setTimeout(100);  // Wait only for 100 ms for complete message to arrive
  
  pinMode(pinMotorRightForward, OUTPUT);
  pinMode(pinMotorRightReverse, OUTPUT);
  pinMode(pinMotorLeftForward, OUTPUT);
  pinMode(pinMotorLeftReverse, OUTPUT);

  Serial.println("Arduino Bluetooth Bot");
}

void loop() {
  int state = 'B';  // Brake by default

  String line = Serial1.readStringUntil('\n');   
  state = line.charAt(0); 
  
  if (line.length() > 0) {
    Serial.println(char(state));
  }
  
  switch (state) {
    case 'W':
      moveForward();
      break;
    case 'S':
      moveReverse();
      break;
    case 'A':
      moveLeft();
      break;
    case 'D':
      moveRight();
      break;
    default:
      stopMove();
      break;
  }
}

void moveForward() {
  digitalWrite(pinMotorRightForward, HIGH);
  digitalWrite(pinMotorLeftForward, HIGH);
}

void moveReverse() {
  digitalWrite(pinMotorRightReverse, HIGH);
  digitalWrite(pinMotorLeftReverse, HIGH);
}

void moveRight() {
  digitalWrite(pinMotorRightReverse, HIGH);
  digitalWrite(pinMotorLeftForward, HIGH);
}

void moveLeft() {
  digitalWrite(pinMotorRightForward, HIGH);
  digitalWrite(pinMotorLeftReverse, HIGH);
}

void stopMove() {
  digitalWrite(pinMotorRightForward, LOW);
  digitalWrite(pinMotorRightReverse, LOW);
  digitalWrite(pinMotorLeftForward, LOW);
  digitalWrite(pinMotorLeftReverse, LOW);
}

