#define _GLIBCXX_USE_C99 1

#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>
#include <SPI.h>
#include <SD.h>
//#include <string.h>
#include "SSD1306Wire.h"
#include "serialBuffer.h"
#include <WiFi.h>

#define LOWFREQ 110
#define HIFREQ 460

//SSD1306Wire  display(0x3c, 5, 4);

//for adafruit PWM
//RANGE OF MG90S IS 100 TO 500
//RANGE OF JX60 IS 110 TO 460
//RANGE OF ASME IS 211 TO 394
//RANGE OF JMT500 IS 220 TO 419

const char* ssid     = "NETGEAR-Guest";
const char* password = "chilidog";

IPAddress local_IP(192, 168, 1, 30);
IPAddress gateway(192, 168, 31, 1);
IPAddress subnet(255, 255, 0, 0);

WiFiServer server(5000);
WiFiClient client;

uint16_t junkData[16] = {};
uint16_t servo_current[16];
uint16_t servo_dest[16];
uint16_t pot_array[16];
uint16_t travel_state[16];
int offsets[16];
int middle;

void wifi_buffer_read_task( void *pvParameters );
void serial_buffer_read_task(void *pvParameters);
void menu_task(void *pvParameters);
void check_usb_connection_task(void *pvParameters);

bool wifiBufferInit = true;
bool modelOn = false;
bool programModeOn = true;


TaskHandle_t h_wifi_buffer_read = NULL;
TaskHandle_t h_menu = NULL;
TaskHandle_t h_check_usb = NULL;
TaskHandle_t h_serial_buffer_read = NULL;
SemaphoreHandle_t sem1 = NULL;


File myFile;
File root;

serialBufferClass serialBuffer;
char * currentMessage = NULL;
String myString = "";

void travel_to(uint16_t current, uint16_t dest, int speed);

Adafruit_PWMServoDriver pwm = Adafruit_PWMServoDriver(0x40);

void setup()
{
  Serial.begin(115200);

  // Wire.begin(5, 4);
  Wire.begin(21, 22);
  /*
    display.init();

    display.flipScreenVertically();
    display.setFont(ArialMT_Plain_16);
    display.setTextAlignment(TEXT_ALIGN_LEFT);
    display.drawString(0, 10, "Waiting");
    display.display();
  */


  offsets[0] = 0;
  offsets[1] = 0;
  offsets[2] = 0;
  offsets[3] = 0;
  offsets[4] = 0;
  offsets[5] = 0;
  offsets[6] = -6;
  offsets[7] = 0;
  offsets[8] = 15;
  offsets[9] = 0;
  offsets[10] = 0;
  offsets[11] = 10;
  offsets[12] = 12;
  offsets[13] = -5;
  offsets[14] = 15;
  offsets[15] = 0;

  middle = LOWFREQ + ((HIFREQ - LOWFREQ) / 2);

  client.setNoDelay(1);

  //DONT FORGET ABOUT SD CARD INIT HANGUP!!!


  while (!SD.begin()) {
    Serial.println("initialization failed!");
    delay(1000);
  }




  if (!WiFi.config(local_IP, gateway, subnet)) {
    Serial.println("STA Failed to configure");
  }


  Serial.println();
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);
  WiFi.mode(WIFI_STA);

  while (WiFi.status() != WL_CONNECTED) {
    delay(200);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  server.begin();
  server.setNoDelay(1);


  pwm.begin();
  pwm.setPWMFreq(50);

  while (!client.connected())
  {
    client = server.available();
    delay(50);
  }
  Serial.println("Client connected!");
  for (int x = 0; x < 16; x++)
  {
    pwm.setPWM(x, 0, (middle + offsets[x]));
    servo_current[x] = (middle + offsets[x]);
  }

  if ((sem1 = xSemaphoreCreateMutex()) == NULL)
  {
    Serial.println("could not create semiphore");
  }

}


void loop()
{



  xTaskCreatePinnedToCore(&wifi_buffer_read_task, "buffer_read", 2000, NULL, 1, &h_wifi_buffer_read, 0);
  xTaskCreatePinnedToCore(&check_usb_connection_task, "usb", 10000, NULL, 1, &h_check_usb, 1);

  // xTaskCreatePinnedToCore(&menu_task, "menu", 2500, NULL, 1, &h_menu, 1);
  // xTaskCreatePinnedToCore(&serial_buffer_read_task, "serialBuffer", 1000, NULL, 1, &h_serial_buffer_read, 0);


  for (;;)
  {
  }

}

void wifi_buffer_read_task(void *pvparams)
{
  TickType_t xLastWakeTime;
  xLastWakeTime = xTaskGetTickCount();
  for (;;)
  {
    //once a semaphore is taken it cannot be 'taken' again until released
    xSemaphoreTake( sem1, portMAX_DELAY);

    //Serial.println("waiting for data");
    if (client.available())
    {
      //Serial.println("client data available");
      client.read((uint8_t*)pot_array, 32);

      if (modelOn)
      {
        for (int x = 0; x < 16; x++)
        {
          //     Serial.print(pot_array[x]); Serial.print(", ");
          pwm.setPWM(x, 0, pot_array[x]);

        }
        //   Serial.println("");
      }
    }
    xSemaphoreGive(sem1);

    vTaskDelayUntil( &xLastWakeTime, pdMS_TO_TICKS( 100 ));

  }
}


//void serial_buffer_read_task(void *pvparams)
//{
//  for (;;)
//  {
//    char* cInput = serialBuffer.readUntilDelim();
//    currentMessage = (char*)realloc(currentMessage, strlen(cInput) + 1);
//    strcpy(currentMessage, cInput);
//    free(cInput);
//    vTaskDelay(pdMS_TO_TICKS(100));
//
//  }
//
//}


void check_usb_connection_task(void *pvparams)
{

  for (;;)
  {
    char* cInput = serialBuffer.readUntilDelim();
    char currentMessage[strlen(cInput) + 1];
    //currentMessage = (char*)realloc(currentMessage, strlen(cInput) + 1);
    strcpy(currentMessage, cInput);
    //free(currentMessage);

    //convert currentMessage into a std string
    String msgString(currentMessage);


    if (msgString.equals("ISTHISESP32"))
    {
      Serial.print("YESESP32|");

    }
    else if (msgString.equals("SENDFILELIST"))
    {
      myString = "";
      File dir = SD.open("/");
      while (true)
      {
        File entry =  dir.openNextFile();
        if (! entry) {
          break;
        }
        String temp = entry.name();
        if (temp.indexOf("System") == -1)
        {
          myString = myString + temp + ",";
        }
      }
      Serial.print(myString + "|");
    }
    else if (msgString.startsWith("SETPOSE"))
    {
      xSemaphoreTake( sem1, portMAX_DELAY );

      myString = msgString;
      myString.remove(0, 7);

      char cString[(myString.length())];
      for (int x = 0; x < (myString.length()); x++)
      {
        cString[x] = myString[x];
      }
      char* temp[16];
      int count = 1;
      int mappedInt;
      temp[0] = strtok(cString, ",");

      mappedInt = map(std::stoi(temp[0], nullptr, 0), 0, 180, LOWFREQ, HIFREQ);
      servo_dest[0] = mappedInt + offsets[0];

      while (count < 16)
      {
        temp[count] = strtok(NULL, ",");
        mappedInt = map(std::stoi(temp[count], nullptr, 0), 0, 180, LOWFREQ, HIFREQ);
        servo_dest[count] = mappedInt + offsets[count];
        count++;
      }

      //Update robot servo motors
      travel_to(servo_current, servo_dest, 50, 0);
      memcpy(servo_current, servo_dest, 32);
      
      xSemaphoreGive(sem1);

    }

    else if (msgString.startsWith("SAVENEWFILE"))
    {
      myString = msgString;
      myString.remove(0, 11);
      myFile = SD.open(myString, FILE_WRITE);
      myFile.print("90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,90,0,0,");
      myFile.close();

    }

    else if (msgString.startsWith("REMOVEFILE"))
    {
      myString = msgString;
      myString.remove(0, 10);
      SD.remove(myString);
    }

    else if (msgString.startsWith("SAVEFILE"))
    {
      myString = msgString;
      myString.remove(0, 8);
      int pos = myString.indexOf("^");
      String fileName = myString.substring(0, pos);
      Serial.println(fileName);

      myString.remove(0, pos + 1);
      Serial.println(myString);
      myFile = SD.open(fileName, FILE_WRITE);

      for (int x = 0; x < myString.length(); x++)
      {
        myFile.print(myString[x]);
      }

      myFile.close();
    }

    else if (msgString.startsWith("F_"))
    {
      myString = msgString;
      myString.remove(0, 2);

      if (SD.exists(myString))
      {
        myFile = SD.open(myString);
      }
      else
      {
      }

      while (myFile.available())
      {
        Serial.print((char)myFile.read());
      }
      Serial.print('|');
      myFile.close();

    }
    else if (msgString.equals("MODELON"))
    {
      modelOn = true;
    }
    else if (msgString.equals("MODELOFF"))
    {

      modelOn = false;

    }
    else if (msgString.equals("LOCKPOS"))
    {
      myString = "";
      for (int x = 0 ; x < 15 ; x++)
      {
        myString = myString + map(pot_array[x], LOWFREQ, HIFREQ, 0, 180) + ",";
      }
      myString = myString + map(pot_array[15], LOWFREQ, HIFREQ, 0, 180);

      myString = myString + "|";

      Serial.print(myString);

    }

    else if (msgString.startsWith("PLAYMOTION"))
    {
      myString = msgString;
      myString.remove(0, 10);
      int myLength = myString.length();

      char *cString = new char[myLength];
      strcpy(cString, myString.c_str());

      char* temp[16];
      int lineNum = 0;
      int mappedInt;
      bool starting = true;
      int theHold;
      int theSpeed;
      char* stringTemp;

      while (true)
      {
        if (starting)
        {
          stringTemp = strtok(cString, ",");
          if (stringTemp == NULL)
          {
            break;
          }
          temp[0 + (18 * lineNum)] = stringTemp;
        }
        else
        {
          stringTemp = strtok(NULL, ",");
          if (stringTemp == NULL)
          {
            break;
          }
          temp[0 + (18 * lineNum)] = stringTemp;
        }
        mappedInt = map(std::stoi(temp[0 + (18 * lineNum)], nullptr, 0), 0, 180, LOWFREQ, HIFREQ);
        servo_dest[0] = mappedInt + offsets[0];


        for (int x = 1; x < 16 ; x++)
        {
          temp[x + (18 * lineNum)] = strtok(NULL, ",");
          mappedInt = map(std::stoi(temp[x + (18 * lineNum)], nullptr, 0), 0, 180, LOWFREQ, HIFREQ);
          servo_dest[x] = mappedInt + offsets[x];
        }

        theSpeed = std::stoi( strtok(NULL, ","), nullptr, 0);
        theHold = std::stoi( strtok(NULL, ","), nullptr, 0);

        //Travel from current to destination
        travel_to(servo_current, servo_dest, theSpeed, theHold);
        memcpy(servo_current, servo_dest, 32);

        starting = false;
        lineNum++;
      }

    }

  }
}


void travel_to(uint16_t current[], uint16_t dest[], int speed, int holdtime)
{
  //find the servo with the furthest travel distance
  int max_distance = 0;
  for (int x = 0; x < 16; x++)
  {
    if ( (current[x] - dest[x] ) > max_distance)
    {
      max_distance = current[x] - dest[x];
    }
    if ((dest[x] - current[x] ) > max_distance)
    {
      max_distance = dest[x] - current[x];
    }
  }

  memcpy(travel_state, current, 32);

  for (float y = 1; y <= max_distance; y += 10)
  {
    for (int x = 0; x < 16; x++)
    {
      if ((current[x] - dest[x]) > 0) //going down/counterclockwise
      {

        travel_state[x] = current[x] - (y / max_distance) * (current[x] - dest[x]);
      }
      else  //going up/clockwise
      {

        travel_state[x] = current[x] + (y / max_distance) * (dest[x] - current[x]);

      }
      pwm.setPWM(x, 0, travel_state[x]);
      // Serial.print(travel_state[x]); Serial.print(',');
    }
    // Serial.println();

    delay(speed);
  }
}

