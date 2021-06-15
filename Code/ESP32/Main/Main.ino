//intervalle beachten
//To disable pragma messages on compile
//include this Before including FastLED.h
#define FASTLED_INTERNAL
#include <FastLED.h>
#include <DHTesp.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <time.h>


DHTesp dht;
const int DHT11_PIN =  21;
const int MOIS_SENSOR_PIN = 32;
const int FAN_PIN = 17;
const int WATER_PUMP_PIN = 18;
const int LED_PIN = 33;
const int NUMBER_OF_LEDS = 20;



unsigned long intervalOfSendData = 600000;//10MIn
unsigned long pastTimeSend = 0;

unsigned long intervalOfGetData = 30000;//30sekunden
unsigned long pastTimeGet = 0;

unsigned long intervalOfGetTime = 10000;//10sekunden
unsigned long pastTimeOfTime =  0;

String GEWAECHSHAUS_ID = "GTD2-ERH6-E665";
const char * SSID_D = "Vodafone-523C";
const char * PASSWORD =  "XXX";
const String SERVER_URL = "https://vig-mini.ddns.net";


HTTPClient http;
JsonObject settingsObj;
int temp;
int mois;
int hum;

int tempLimit = 24;
boolean isTempControlOn = true;

String timestamp= "12:00:00";






CRGB leds[NUMBER_OF_LEDS];


//Deklarierung von Funktionen
void sendData();
void connectToWifi();
int getMoisture();
float getTemp();
float getHum();
void activateFan();
void tempControl();
void connectToWifi();
void turnLightsOn(String color);
void turnLightsOff();
void requestSettings();
String requestTimeStamp();

void setup() {
  connectToWifi();
  

  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUMBER_OF_LEDS);
  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(WATER_PUMP_PIN, OUTPUT);
  Serial.begin(9600);

}


void loop() {


  //==Business-logic===
  /*
  if (WiFi.status() == WL_CONNECTED) {


    unsigned long currentMillis = millis();
    if ((currentMillis - pastTimeSend ) > intervalOfSendData) //send data every 10minuts
    {
      pastTimeSend = currentMillis;
      sendData();
    }
    unsigned long currentMillis2 = millis();
    if ((currentMillis2 - pastTimeGet ) > intervalOfGetData) //requestData every 30 seconds
    {
      pastTimeGet = currentMillis2;
      settingsObj = requestSettings();

    }
    unsigned long currentMillis3 = millis();
    if ((currentMillis3 - pastTimeGet ) > intervalOfGetTime) //requestCurrentTimeEr every 10 seconds
    {
      pastTimeOfTime = currentMillis3;
      timestamp = requestTimeStamp();

    }
  } else {
    connectToWifi();

  }
  tempControl();
  
 */
  //==Buiness-Logic-End===


  //==Test-Logic==
  requestSettings();
  delay(5000);
  //==TEst-Logic-End==

  //This is the way and nothing else LUL
 /*
  String time1 = "10:48:33";
  String time2 = "10:48:32";
  if (time1 > time2) {
    Serial.println("Time1 is bigger");

  } else {
    Serial.println("Time2 is bigger");
  }
  delay(1000);
  */
}



String requestTimeStamp() {
  Serial.println("Request Time wird ausgeführt");
  String serverpath = SERVER_URL + "/time";
  http.begin(serverpath);
  int httpResponseCode = http.GET();
  const char* timestamp = NULL;
  if (httpResponseCode > 0) {

    String response = http.getString();
    timestamp = response.c_str();

  } else {


    Serial.printf("Error while request Time %s \n", http.errorToString(httpResponseCode).c_str()); // httpClient.errorToString(statusCode).c_str()
    Serial.println(httpResponseCode);

  }

  return String(timestamp);



}


int comparetime(time_t time1, time_t time2) {
  if (difftime(time1, time2) > 0.0) {
    return 1;
  } else {
    return -1;
  }

}
//intervalgeschichte implementieren
void requestSettings() {
 
  
  Serial.println("requestSettings wird ausgeführt");

  String settings = "";
  int httpResponseCode = NULL;

  String pathTemp = SERVER_URL + "/greenhouse/settings/temperature?product_key=" + GEWAECHSHAUS_ID;
  String pathMoisVal = SERVER_URL + "/greenhouse/settings/soil-moisture/value?product_key=" + GEWAECHSHAUS_ID+ "&interval=1";
  String pathSoilMoisTime = SERVER_URL + "/greenhouse/settings/soil-moisture/time?product_key=" + GEWAECHSHAUS_ID;
  String pathLight = SERVER_URL + "/greenhouse/settings/light?product_key=" + GEWAECHSHAUS_ID;

  StaticJsonDocument<200> doc;
  JsonObject obj;

  http.begin(pathTemp);
  httpResponseCode = http.GET();

  if (httpResponseCode > 0 ) {
   deserializeJson(doc, http.getString());
   obj = doc.as<JsonObject>();
  Serial.println("Temp Settings in json format");
  serializeJsonPretty(doc, Serial);
  int tempLimit = int(doc("MAX_TEMPERATURE"));
  if(int(doc("TEMP_ON") == 1)){
    
  }
  
  } else {
    Serial.printf("Error while sending HTTP GET: %s \n", http.errorToString(httpResponseCode).c_str()); // httpClient.errorToString(statusCode).c_str()
    Serial.println(httpResponseCode);
  }

}
/*
   send Data in Json Format
*/
void sendData() {
  Serial.println("send Data wird ausgeführt");
  String serverpath = SERVER_URL + "/newMeasurements";

  http.begin(serverpath);

  StaticJsonDocument<200> doc;
  doc["product_key"] = GEWAECHSHAUS_ID;
  doc["led_status"] = true;
  doc["temperature"] = String(getTemp());
  doc["humidity"]   = getHum();
  doc["soil_moisture"] = String(getMoisture());

  http.addHeader("Content-Type", "application/json");
  String requestBody;
  Serial.println("Data");
  serializeJson(doc, requestBody);
  //serializeJsonPretty(doc, Serial);

  int httpResponseCode = http.POST(requestBody);

  if (httpResponseCode > 0) {

    String response = http.getString();

    Serial.println(httpResponseCode);
    Serial.println(response);

  } else {

    Serial.printf("Error while sending HTTP POST: %s \n", http.errorToString(httpResponseCode).c_str()); // httpClient.errorToString(statusCode).c_str()
    Serial.println(httpResponseCode);

  }

}

void connectToWifi() {
  WiFi.mode(WIFI_STA);
  WiFi.begin(SSID_D, PASSWORD);
  unsigned long timer = millis();

  while (WiFi.status() != WL_CONNECTED && ((millis() - timer) < 5000)) {
    delay(1000);
    Serial.println("Connecting to WiFi...");

  }

  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("Could not connect ");
  } else {
    Serial.println("Connected to Wifi");
  }
}

int getMoisture() {
  int anRead = analogRead(MOIS_SENSOR_PIN);
  delay(1000);
  int erg = map(anRead, 4095, 0, 0, 100); //4095=>0% trocken, 0=> 100% nass
  return erg;
}

float getTemp() {
  delay(1000);
  TempAndHumidity lastValues = dht.getTempAndHumidity();
  delay(1000);
  return lastValues.temperature;
}

float getHum() {
  delay(1000);
  TempAndHumidity lastValues = dht.getTempAndHumidity();
  delay(1000);
  return lastValues.humidity;
}

void activateFan() {
  digitalWrite(FAN_PIN, HIGH);
}
void deactivateFan() {
  digitalWrite(FAN_PIN, LOW);

}

void tempControl() {
  if (isTempControlOn == NULL || isTempControlOn == false) {
    deactivateFan();
  } else {
    if (getTemp() > tempLimit) {
      activateFan();

    } else {
      deactivateFan();
    }

  }
}
void turnLightsOn(String color) {
  int ledDelay = 20;
  for (int i = 0; i < NUMBER_OF_LEDS; i++) {
    if (color.equals("blue")) {
      leds[i] = CRGB(0, 0, 255);
      FastLED.show();
      delay(ledDelay);

    } else if (color.equals("purple")) {
      leds[i] = CRGB(127, 0, 255);
      FastLED.show();
      delay(ledDelay);

    } else if (color.equals("bluePurple")) {
      leds[i] = CRGB(0, 0, 255);
      delay(20);
      leds[i + 1] = CRGB(127, 0, 255);
      delay(20);
      i++;
      FastLED.show();


    }

  }
}
void turnLightsOff() {
  int ledDelay = 20;
  for (int i = NUMBER_OF_LEDS - 1; i >= 0; i--) {
    leds[i] = CRGB::Black;
    FastLED.show();
    delay(ledDelay);


  }
}
void watering() {
  digitalWrite(WATER_PUMP_PIN, HIGH);  // Schaltet ein
  delay(5000);
  digitalWrite(WATER_PUMP_PIN, LOW);   // Schaltet aus
  delay(5000);
}
