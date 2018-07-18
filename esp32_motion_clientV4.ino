
#include <WiFi.h>
#include <driver/adc.h>

//for ledcWrite
//RANGE OF JX 2060 IS 1800 TO 7800
//RANGE OF MG90S IS 1750 TO 7550

//for adafruit PWM
//RANGE OF MG90S IS 100 TO 500
//RANGE OF JX60 IS 110 TO 460

#define Selector_0 2
#define Selector_1 4
#define Selector_2 5
#define Selector_3 12


#define LOWFREQ 110
#define HIFREQ 460

//#define pot_limit 315 // for 10bit
#define pot_limit 1260 // for 12bit

int middle;
const char* ssid     = "NETGEAR-Guest";
const char* password = "chilidog";
IPAddress local_IP(192, 168, 1, 20);
IPAddress gateway(192, 168, 31, 1);
IPAddress subnet(255, 255, 0, 0);
IPAddress serverIP(192, 168, 1, 30);
WiFiClient client;

uint16_t pot_array[16];

void setup()
{
  pinMode(Selector_0, OUTPUT);
  pinMode(Selector_1, OUTPUT);
  pinMode(Selector_2, OUTPUT);
  pinMode(Selector_3, OUTPUT);


  Serial.begin(115200);
  client.setNoDelay(1);

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
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  Serial.println("Connecting to server");


  while (!client.connected())
  {
    client.connect(serverIP, 5000);
    //delay(5);
    Serial.print('.');
  }
  Serial.println("Client connected biatch!");


  middle = LOWFREQ + ((HIFREQ - LOWFREQ) / 2);

}

void loop()
{
  //analog 0 = pin 36, svc on wemos oled 32, dev board

  for (;;)
  {

    for (int channel = 0; channel < 16; channel++)
    {

      select_channel(channel);
      delay(2); //give time for multiplexer switching
      pot_array[channel] = analogRead(36);

      if (pot_array[channel] < pot_limit)
      {
        pot_array[channel] = pot_limit;
      }
      pot_array[channel] = map(pot_array[channel], pot_limit, 4095, LOWFREQ, HIFREQ);
      if ((channel == 3) | (channel == 4) | (channel == 11) | (channel == 12) | (channel == 0) | (channel == 1) | (channel == 2) | (channel == 8) | (channel == 9) | (channel == 10))
      {
        pot_array[channel] = invert_val(pot_array[channel]);
      }

      //Serial.print(pot_array[channel]); Serial.print(',');
    }
    //Serial.println();


    client.write_P((const char*)pot_array, 32);
    delay(200);

    if (!client.connected())
    {
      client.connect(serverIP, 5000);
      //Serial.println("reconnecting");
    }
  }
}

void select_channel(int channel_num)
{
  switch (channel_num) {

    case 0:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 0);

      break;

    case 1:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 0);
      break;


    case 2:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 0);
      break;


    case 3:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 0);
      break;


    case 4:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 0);
      break;


    case 5:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 0);
      break;


    case 6:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 0);
      break;


    case 7:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 0);
      break;

    case 8:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 1);
      break;

    case 9:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 1);
      break;


    case 10:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 1);
      break;


    case 11:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 0);
      digitalWrite(Selector_3, 1);
      break;


    case 12:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 1);
      break;


    case 13:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 0);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 1);
      break;


    case 14:
      digitalWrite(Selector_0, 0);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 1);
      break;


    case 15:
      digitalWrite(Selector_0, 1);
      digitalWrite(Selector_1, 1);
      digitalWrite(Selector_2, 1);
      digitalWrite(Selector_3, 1);
      break;
  }
}
int invert_val(int pot_val)
{
  if (pot_val <= middle)
  {
    pot_val = (middle - pot_val) + middle;

  }
  else
  {
    pot_val = middle - (pot_val - middle);
  }
  return pot_val;
}
