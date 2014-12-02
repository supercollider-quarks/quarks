/* 
 ---- SimpleMessageSystem Example 1 ----
 Control Arduino board functions with the following messages:
 
 r a -> read analog pins
 r d -> read digital pins
 w d [pin] [value] -> write digital pin
 w a [pin] [value] -> write analog pin
 
 
 Base: Thomas Ouellet Fredericks 
 Additions: Alexandre Quessy 
 
 */

#include <SimpleMessageSystem.h> // Include de SimpleMessageSystem library

void setup()
{
  
  // The following command initiates the serial port at 9600 baud. Please note this is VERY SLOW!!!!!! 
  // I suggest you use higher speeds in your own code. You can go up to 115200 with the USB version, that's 12x faster
  Serial.begin(9600); //Baud set at 9600 for compatibility, CHANGE!


}

void loop()
{

  if (messageBuild() > 0) { // Checks to see if the message is complete and erases any previous messages
    switch (messageGetChar()) { // Gets the first word as a character
    case 'r': // Read pins (analog or digital)
      readpins(); // Call the readpins function
      break; // Break from the switch
    case 'w': // Write pin
      writepin(); // Call the writepin function
    }

  }

}

void readpins(){ // Read pins (analog or digital)

  switch (messageGetChar()) { // Gets the next word as a character

    case 'd': // READ digital pins

    messageSendChar('d');  // Echo what is being read
    for (char i=2;i<14;i++) {
      messageSendInt(digitalRead(i)); // Read pins 2 to 13
    }
    messageEnd(); // Terminate the message being sent
    break; // Break from the switch

  case 'a': // READ analog pins

    messageSendChar('a');  // Echo what is being read
    for (char i=0;i<6;i++) {
      messageSendInt(analogRead(i)); // Read pins 0 to 5
    }
    messageEnd(); // Terminate the message being sent

  }

}

void writepin() { // Write pin

  int pin;
  int state;

  switch (messageGetChar()) { // Gets the next word as a character

    case 'a' : // WRITE an analog pin

    pin = messageGetInt(); // Gets the next word as an integer
    state = messageGetInt(); // Gets the next word as an integer
    pinMode(pin, OUTPUT); //Sets the state of the pin to an output
    analogWrite(pin, state); //Sets the PWM of the pin 
    break;  // Break from the switch


    // WRITE a digital pin
  case 'd' : 

    pin = messageGetInt();  // Gets the next word as an integer
    state = messageGetInt();  // Gets the next word as an integer
    pinMode(pin,OUTPUT);  //Sets the state of the pin to an output
    digitalWrite(pin,state);  //Sets the state of the pin HIGH (1) or LOW (0)
    

  }

}

