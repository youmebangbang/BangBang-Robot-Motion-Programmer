#include "Arduino.h"
#include "serialBuffer.h"

serialBufferClass::serialBufferClass()
{
  bufPos = 0;
  serialSize = 0;
}

char * serialBufferClass::readUntilDelim()
{
  while (true)
  {
    if (serialSize = (Serial.available() > 0))
    {

      for (int x = 0; x < serialSize; x++)
      {
        serialBuffer[bufPos + x] = (char)Serial.read();
        //Check if a deliminator is found in the serial buffer for end of message
        if (serialBuffer[bufPos + x] == '|')
        {
          char* returnString = (char*)malloc(bufPos+x+1); //add extra slot for null char
          strncpy(returnString, serialBuffer, (bufPos + x));
          returnString[bufPos+x] = '\0';
          bufPos = 0;
          return returnString;
        }
        else if ((serialBuffer[bufPos + x] == '\n') || (serialBuffer[bufPos + x] == '\r'))
        {
            bufPos--;
        }
    bufPos++;
      }
    }
  }
}
