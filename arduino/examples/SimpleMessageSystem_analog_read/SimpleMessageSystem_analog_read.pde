/* Analog Read to SMS
 * ------------------ 
 *
 * sends the <value> of the first analog input via SimpleMessageSystem
 * and waits for <value> milliseconds.
 *
 * 2006-12-20 sk: Adapted from analog_read_led
 *                (see copyrihght notice below)
 *
 * Created 1 December 2005
 * copyleft 2005 DojoDave <http://www.0j0.org>
 * http://arduino.berlios.de
 *
 */

#include "SimpleMessageSystem.h"

int potPin = 0;    // select the input pin for the potentiometer
int ledPin = 13;   // select the pin for the LED
int val = 0;       // variable to store the value coming from the sensor

void setup() {
  pinMode(ledPin, OUTPUT);  // declare the ledPin as an OUTPUT
  Serial.begin(115200);
}

void loop() {
  val = analogRead(potPin);    // read the value from the sensor
  messageSendChar('v');        // send the value in a message
  messageSendInt(millis());    // along with a timestamp
  messageSendInt(val);
  messageEnd();
  delay(val);                  // stop the program for some time
}
