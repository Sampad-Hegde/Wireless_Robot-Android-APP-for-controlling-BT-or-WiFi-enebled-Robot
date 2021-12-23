# NodeMCU based robot control using Android APP or phone and web responsive webpage which also has modes likes Line following and Obstacle avoiding
Here, Node-MCU acts as Access point
so, connect to its Wi-Fi network and open the ```Wireless Robot``` App or type [http://192.168.4.1](http://192.168.4.1) you will reach the robot control page
### Note : if you want to make any changes in web page then get preview in index.html then copy whole code and paste it into the NodeMCTBot.ino file and then upload the code

## Library Dependencies
Included inside ```Libraries ``` folder

| Library |
| :--- |
| ESP8266WiFi |
| ESP8266WebServer |
| Ultrasonic |
| Servo |
## To build the Robot make these connections and upload the given code to Node-MCU
!!! Make sure that you disconnect the GPIO 3 and GPIO 1 before uploading the code, Because both are RX and TX pins for serial communication

| Node-MCU Pin No | Peripheral Pin No |
| :---:           |              :---: |
| D1 | Motor Driver Left Forward Pin (IN1) |
| D2 | Motor Driver Left Backward Pin (IN2) |
| D3 | Motor Driver Right Forward Pin (IN3) |
| D4 | Motor Driver Right Backward Pin (IN4) |
| GPIO 3 | Motor Driver PWM Forward Pin (IN1) |
| D7 | Ultrasonic Trigger Pin |
| D6 | Ultrasonic Echo Pin |
| D8 | Servo Pin |
| GPIO 1 | Left IR Proximity Sensor Input |
| GPIO 9 | Centre IR Proximity Sensor Input |
| GPIO 10 | Right IR Proximity Sensor Input |

## Now open your favorite browser and visit [http://192.168.4.1](http://192.168.4.1) or open the ```Wireless Robot``` APP for full control.

## Circuit Diagram

![Circuit Diagram](https://github.com/Sampad-Hegde/Wireless_Robot-Android-APP-for-controlling-BT-or-WiFi-enebled-Robot/blob/main/Node-MCU/Node_MCU_Robot_CKT.png)


### DONE !!!
