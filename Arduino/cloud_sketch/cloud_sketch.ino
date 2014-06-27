/*
  Arduino Yun Bridge example

 This example for the Arduino Yun shows how to use the
 Bridge library to access the digital and analog pins
 on the board through REST calls. It demonstrates how
 you can create your own API when using REST style
 calls through the browser.

 Possible commands created in this shetch:

 * "/arduino/digital/13"     -> digitalRead(13)
 * "/arduino/digital/13/1"   -> digitalWrite(13, HIGH)
 * "/arduino/analog/2/123"   -> analogWrite(2, 123)
 * "/arduino/analog/2"       -> analogRead(2)
 * "/arduino/mode/13/input"  -> pinMode(13, INPUT)
 * "/arduino/mode/13/output" -> pinMode(13, OUTPUT)

 This example code is part of the public domain

 http://arduino.cc/en/Tutorial/Bridge

 */

//**************************************************************//
//  Project:  It's Raining Data
//  Name    : cloud_sketch                                
//  Author  : Alberto Rivero 
//  Date    : 26/06/2014    
//  Modified: 23 Mar 2010                                 
//  Version : 0.1                                             
//****************************************************************



#include <Bridge.h>
#include <YunServer.h>
#include <YunClient.h>

// Listen on default port 5555, the webserver on the Yun
// will forward there all the HTTP requests for us.
YunServer server;
 int ledPin = 13;
 
//Pin connected to ST_CP of 74HC595
int latchPin = 8;
//Pin connected to SH_CP of 74HC595
int clockPin = 12;
////Pin connected to DS of 74HC595
int dataPin = 11;

void setup() {
  // Set pins output
  pinMode(latchPin, OUTPUT);
  pinMode(clockPin, OUTPUT);
  pinMode(dataPin, OUTPUT);
  
  // Bridge startup
  pinMode(13, OUTPUT);
  digitalWrite(13, LOW);
  Bridge.begin();
  digitalWrite(13, HIGH);

  // Listen for incoming connection only from localhost
  // (no one from the external network could connect)
  server.listenOnLocalhost();
  server.begin();
}

void loop() {
  // Get clients coming from server
  YunClient client = server.accept();

  // There is a new client?
  if (client) {
    // Process request
    process(client);

    // Close connection and free resources.
    client.stop();
  }

  delay(50); // Poll every 50ms
}

void process(YunClient client) {
  // read the command
  String command = client.readStringUntil('/');

  // is "digital" command?
  if (command == "byte") {
    getdatabyte(client);
  }

}

void getdatabyte(YunClient client) {
   //int datalenght = 8;
   // String databyte = client.readString();
   
  // String strbyte1 = client.readStringUntil('/');
   int maxarray = 8;
   int databyte[maxarray];
   
   databyte[0] = client.parseInt();
   client.print(F("BYTE 1: "));
   client.println(databyte[0]);
   for (int i = 0; i < maxarray-1; i++) {
     String strbyte = client.readStringUntil('/');
     databyte[i+1] = client.parseInt();
     client.print(F("BYTE "));
     client.print(i+2);
     client.print(F(": "));
     client.println(databyte[i+1]);
   }
 
        
  // take the latchPin low so 
    // the LEDs don't change while you're sending in bits:
    digitalWrite(latchPin, LOW);
    // shift out the bits:
    for (int k = 0; k < maxarray; k++) {
      shiftOut(dataPin, clockPin, MSBFIRST, databyte[k]);
    }

    //take the latch pin high so the LEDs will light up:
    digitalWrite(latchPin, HIGH);
    client.println(F("DATA SENT"));
    // pause before next value:
    delay(600);
  
  
}

