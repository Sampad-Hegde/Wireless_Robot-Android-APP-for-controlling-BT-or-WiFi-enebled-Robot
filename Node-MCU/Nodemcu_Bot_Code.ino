#include <Servo.h>
#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <Ultrasonic.h>

const char *ssid = "Sampad's BOT";
const char *password = "";

int distance, leftIR, rightIR, centreIR, leftDist, rightDist;

ESP8266WebServer server(80);

const int leftForward = D1;
const int leftBackward = D2;
const int rightForward = D3;
const int rightBackward = D4;
const int pwmPin = 3;

const int echoPin = D6;
 const int trigPin = D7;
const int servoPin = D8;

Ultrasonic ultrasonic(trigPin, echoPin);

const int leftIRPin = 1;
const int rightIRPin = 10;
const int centreIRPin = 9;

int Mode = 0;

Servo myservo;

const char index_html[] PROGMEM = R"rawliteral(

<!DOCTYPE html>

<html>
    <meta name="viewport" content="width=device-width, initial-scale=1">


    <head>
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>

        <script type="text/javascript">
            $(document).ready(function()
            {

                $('.notSelectable').disableSelection();

            });

            $.fn.extend(
                {
                    disableSelection: function() 
                    {
                        this.each(function() 
                        {
                            this.onselectstart = function() 
                            {
                                return false;
                            };
                            this.unselectable = "on";
                            $(this).css('-moz-user-select', 'none');
                            $(this).css('-webkit-user-select', 'none');
                        });
                    }
                });

        </script>
    </head>

    <style>
        .slider 
        {
            -webkit-appearance: none;
            width: 140%;
            height: 15px;
            border-radius: 5px;
            background: #d3d3d3;
            outline: none;
            opacity: 0.7;
            -webkit-transition: .2s;
            transition: opacity .2s;
            transform: translate(-20%, -350%);

        }

        .slider:hover 
        {
        opacity: 1;
        }

        .slider::-webkit-slider-thumb 
        {
            -webkit-appearance: none;
            appearance: none;
            width: 25px;
            height: 25px;
            border-radius: 50%;
            background: #4CAF50;
            cursor: pointer;
        }

        .slider::-moz-range-thumb 
        {
            width: 25px;
            height: 25px;
            border-radius: 50%;
            background: #4CAF50;
            cursor: pointer;
        }
        #ForwardBtn 
        {
            background-color: #008CBA;
            border: none;
            color: white;
            border-radius: 12px;
            text-align: center;
            padding: 8px 20px;
            text-decoration: none;
            display: inline-block;
            font-size: 24px;
            transform: translate(20%, 40%);
            -webkit-touch-callout:none;
            -webkit-user-select:none;
            -khtml-user-select:none;
            -moz-user-select:none;
            -ms-user-select:none;
            user-select:none;
            
        }
        #BackwardBtn 
        {
            background-color: #008CBA;
            border: none;
            color: white;
            border-radius: 12px;
            text-align: center;
            padding: 8px 12px;
            text-decoration: none;
            display: inline-block;
            font-size: 24px;
            transform: translate(20%, -70%);
            -webkit-touch-callout:none;
            -webkit-user-select:none;
            -khtml-user-select:none;
            -moz-user-select:none;
            -ms-user-select:none;
            user-select:none;
            
        }
        #LeftBtn
        {
            background-color: #008CBA;
            border: none;
            color: white;
            border-radius: 12px;
            padding: 8px 25px;
            text-align: center;
            text-decoration: none;
            display: inline-block;
            font-size: 24px;
            transform: translate(-60%, -10%);
            -webkit-touch-callout:none;
            -webkit-user-select:none;
            -khtml-user-select:none;
            -moz-user-select:none;
            -ms-user-select:none;
            user-select:none;
            
                
        }
        #RightBtn
        {
            background-color: #008CBA;
            border: none;
            color: white;
            border-radius: 12px;
            text-align: center;
            padding: 8px 20px;
            text-decoration: none;
            display: inline-block;
            font-size: 24px;
            transform: translate(40%, -10%); 
            -webkit-touch-callout:none;
            -webkit-user-select:none;
            -khtml-user-select:none;
            -moz-user-select:none;
            -ms-user-select:none;
            user-select:none; 
                
        }

        .custom-select
        {
            transform: translate(15%, -60%);
        }
    </style>

    <title>WIFI Robot</title>

    <body style="margin: 0;position: absolute;top: 50%;left: 50%;transform: translate(-50%, -50%);" oncopy="return false" oncut="return false" onpaste="return false" >        
        
        <header>
           <button id="ForwardBtn" class="notSelectable">Forward</button>
        </header>

    </br>
    </br>
    </br>
    </br>
        <header>
            <button id="LeftBtn"class="notSelectable" >Left</button> 

            <button id="RightBtn"class="notSelectable">Right</button>
        </header>
    </br>
    </br>
    </br>
    </br>
       
        <header>
            <button id="BackwardBtn"class="notSelectable">   Backward    </button>
       
        </header>
    </br>
    </br>
    </br>
    </br>



        <div class="slidecontainer" >

            <input type="range" min="1" max="10" value="1" class="slider" id="myRange">
      
        </div>

        <div class="custom-select" style="align-self: center;" onchange="modechanged(event)">
            <select name="modes" id="modeselector">
                <option value="Buttons_Control">Buttons Control</option>
                <option value="Obstacle_Avoid">Obstacle Avoid</option>
                <option value="Line_Follower">Line Follower</option>
            </select>
        </div>
    
    </body>
    <script>
        var frbtn = document.getElementById("ForwardBtn");
        var slider = document.getElementById("myRange");
        var bkbtn = document.getElementById("BackwardBtn");
        var lfbtn = document.getElementById("LeftBtn");
        var rgbtn = document.getElementById("RightBtn");

        //var modeselc = document.getElementById("modeselector");

        //modeselc.addEventListener("onchange",modechanged);

        if (navigator.userAgent.match(/Mobile/i))
        {

            console.log("Mobile Device Found !!");

            frbtn.addEventListener("touchstart", Forwardfn);
            frbtn.addEventListener("touchend", StopFn);

            bkbtn.addEventListener("touchstart", Backwardfn);
            bkbtn.addEventListener("touchend", StopFn);
            
            lfbtn.addEventListener("touchstart", Leftfn);
            lfbtn.addEventListener("touchend", StopFn);
            
            rgbtn.addEventListener("touchstart", Rightfn);
            rgbtn.addEventListener("touchend", StopFn);

        }
        else
        {
            frbtn.addEventListener("mousedown", Forwardfn);
            frbtn.addEventListener("mouseup", StopFn);

            bkbtn.addEventListener("mousedown", Backwardfn);
            bkbtn.addEventListener("mouseup", StopFn);
            
            lfbtn.addEventListener("mousedown", Leftfn);
            lfbtn.addEventListener("mouseup", StopFn);
            
            rgbtn.addEventListener("mousedown", Rightfn);
            rgbtn.addEventListener("mouseup", StopFn);
        }        

        slider.oninput = async function() 
        {
            fetch("http://192.168.4.1/setspeed?speed="+(slider.value - 1));
        }

        async function Forwardfn()
        {
            console.log("Fowrard");
            fetch("http://192.168.4.1/forward");
        }

        async function Backwardfn()
        {
            console.log("Backward");
            fetch("http://192.168.4.1/backward");
        }

        async function Leftfn()
        {
            console.log("Left");
            fetch("http://192.168.4.1/left");
        }

        async function Rightfn()
        {
            console.log("right");
            fetch("http://192.168.4.1/right");
        }

        async function StopFn()
        {
            console.log("stop");
            fetch("http://192.168.4.1/stop");
        }

        async function modechanged(event)
        {
            console.log("Mode Changed");
            console.log(event.srcElement.options.selectedIndex);
            fetch("http://192.168.4.1/setmode?mode="+event.srcElement.options.selectedIndex);
        }
    </script>

</html>

)rawliteral";


void Set_Speed(char value)
{

  
  switch(value)
  {
     case '0':analogWrite(pwmPin,0);
             break;
     case '1':analogWrite(pwmPin,50);
               break;
     case '2':analogWrite(pwmPin,75);
              break;
     case '3':analogWrite(pwmPin,100);
              break;
     case '4':analogWrite(pwmPin,125);
              break;
     case '5':analogWrite(pwmPin,150);
              break;
     case '6':analogWrite(pwmPin,175);
              break;
     case '7':analogWrite(pwmPin,200);
              break;
     case '8':analogWrite(pwmPin,225);
              break;
     case '9':analogWrite(pwmPin,255);
              break;
              
    }
}

void Stop()
{
  digitalWrite(leftForward, LOW);
  digitalWrite(leftBackward, LOW);
  digitalWrite(rightForward, LOW);
  digitalWrite(rightBackward, LOW);
}

void Forward()
{
  digitalWrite(leftForward, HIGH);
  digitalWrite(leftBackward, LOW);
  digitalWrite(rightForward, HIGH);
  digitalWrite(rightBackward, LOW);
}

void Backward()
{

  digitalWrite(leftForward, LOW);
  digitalWrite(leftBackward, HIGH);
  digitalWrite(rightForward, LOW);
  digitalWrite(rightBackward, HIGH);
}

void Right()
{
  digitalWrite(leftForward, HIGH);
  digitalWrite(leftBackward, LOW);
  digitalWrite(rightForward, LOW);
  digitalWrite(rightBackward, HIGH);

}

void Left()
{
  digitalWrite(leftForward, LOW);
  digitalWrite(leftBackward, HIGH);
  digitalWrite(rightForward, HIGH);
  digitalWrite(rightBackward, LOW);
}

void handleRoot() 
{
  server.send(200, "text/html", index_html);
}

void handleForward() 
{
  server.send(200, "text/html", "ok");
  if ( Mode == 0)
  {
    Forward();
  }
}

void handleBackward() 
{
  server.send(200, "text/html", "ok");
  if ( Mode == 0)
  {
    Backward();
  }
}

void handleLeft() 
{
  server.send(200, "text/html", "ok");
  if ( Mode == 0)
  {
    Left();
  }
  
}

void handleRight() 
{
  server.send(200, "text/html", "ok");
  if ( Mode == 0)
  {
    Right();
  }
}


void handleStop() 
{
  
  server.send(200, "text/html", "ok");
  if ( Mode == 0)
  {
    Stop();
  }
}

void handleSetSpeed() 
{
  server.send(200, "text/html", "ok");
  if(server.args()>0)
  {
    if(server.argName(0) == "speed")
    {
      Set_Speed((char)server.arg(0)[0]);
    }
  }
}

void handleSetMode() 
{
  server.send(200, "text/html", "ok"); 
  if(server.args()>0)
  {
    if(server.argName(0) == "mode")
    {
      Mode = (int)server.arg(0)[0] - 48;
    }
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
void setup() 
{
  delay(1000);
  
  WiFi.softAP(ssid, password);

  IPAddress myIP = WiFi.softAPIP();

  pinMode(leftForward, OUTPUT);
  pinMode(leftBackward, OUTPUT);
  pinMode(rightForward, OUTPUT);
  pinMode(rightBackward, OUTPUT);

  pinMode(leftIRPin, INPUT);
  pinMode(rightIRPin, INPUT);
  pinMode(centreIRPin, INPUT);

  pinMode(pwmPin, OUTPUT);
  
  server.on("/", handleRoot);
  server.on("/forward", handleForward);
  server.on("/backward", handleBackward);
  server.on("/left", handleLeft);
  server.on("/right", handleRight);
  server.on("/stop", handleStop);
  server.on("/setspeed",handleSetSpeed);
  server.on("/setmode",handleSetMode);

  server.begin();

  myservo.attach(servoPin);
  
}

void loop() 
{
  server.handleClient();
  if(Mode == 1)
  {
    obstacleAvoid();
  }
  else if(Mode == 2)
  {
    lineFollower();
  }
}
