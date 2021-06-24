/*
   TODO
   for buisness logic, default  setting for XYcontrol to false

*/
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

//TODO rebuild themeVar to array because of intervals


int minSoilMois = 0;
boolean istWateringControlOn = false;
boolean isWaterTimeTableOn = false;
boolean alreadyWatered = false;
String waterringTimestamp = "";



int tempLimit = 24;
boolean isTempControlOn = false;

String timestamp = "12:00:00";

String fromTime = "";
String toTime = "";
boolean isLightTimeTableOn = false;
int ledStatus = 0;








CRGB leds[NUMBER_OF_LEDS];


//Deklarierung von Funktionen

String requestTimeStamp();
void requestSettings();
void sendData();
void connectToWifi();

int getMoisture();
float getTemp();
float getHum();

void activateFan();
void tempControl();
void turnLightsOn(String color);
void turnLightsOff(); 
void controlLights(String from, String till);
void watering();
void wateringControlByValue();
void wateringControlByTime(String from);

boolean checkForHTTPError(int errorCode, String errorMsg);
void printVars();

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
  temp = (int)getTemp();
  mois = (int)getMoisture();
  hum = (int)getHum();
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
      requestSettings();

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
  controlLights(fromTime, toTime);
  wateringControlByValue();
  wateringControlByTime(waterringTimestamp);


  //==Buiness-Logic-End===


  //==Test-Logic==
  /*
    requestSettings();
    printVars();
  */
  //test();



 // delay(5000);

  //==TEst-Logic-End==


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

/*
  void test() {
  HTTPClient http;

  // Send request
  http.begin(SERVER_URL + "/greenhouse/settings/temperature?product_key=" + GEWAECHSHAUS_ID);
  http.GET();

  // Parse response
  DynamicJsonDocument doc(2048);
  deserializeJson(doc, http.getString());

  delay(500);
  // Read values
  Serial.println(doc["MAX_TEMPERATURE"].as<int>());
  int t = doc["MAX_TEMPERATURE"].as<int>();
  Serial.println(t);
  serializeJsonPretty(doc, Serial);
  // Disconnect
  http.end();
  }

*/
void requestSettings() {

  Serial.println("requestSettings wird ausgeführt");
  DeserializationError err;
  String settings = "";
  int httpResponseCode = NULL;
  HTTPClient http;
  String pathTemp = SERVER_URL + "/greenhouse/settings/temperature?product_key=" + GEWAECHSHAUS_ID;
  String pathMoisVal = SERVER_URL + "/greenhouse/settings/soil-moisture/value?product_key=" + GEWAECHSHAUS_ID + "&interval=1";
  String pathSoilMoisTime = SERVER_URL + "/greenhouse/settings/soil-moisture/time?product_key=" + GEWAECHSHAUS_ID;
  String pathLight = SERVER_URL + "/greenhouse/settings/light?product_key=" + GEWAECHSHAUS_ID;
  DynamicJsonDocument doc(2048);

  http.begin(pathTemp);
  httpResponseCode = http.GET();

  if ( checkForHTTPError( httpResponseCode,  http.errorToString(httpResponseCode))) {
    DeserializationError err = deserializeJson(doc, http.getString());
    delay(500);
    if (err) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(err.c_str());
    }

    //serializeJsonPretty(doc, Serial);
    delay(500);
    tempLimit = doc["MAX_TEMPERATURE"].as<int>();

    if ((int)doc["TEMP_ON"] == 1) {
      isTempControlOn = true;

    } else {
      isTempControlOn = false;
    }
  }

  http.end();
  http.begin(pathMoisVal);
  httpResponseCode = http.GET();

  if ( checkForHTTPError( httpResponseCode,  http.errorToString(httpResponseCode))) {
    DeserializationError err = deserializeJson(doc, http.getString());
    if (err) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(err.c_str());
    }
    minSoilMois = doc["MIN_SOIL_MOISTURE"].as<int>();
    //serializeJsonPretty(doc, Serial);
    if (doc["SOIL_MOISTURE_ON"].as<int>() == 1) {
      istWateringControlOn = true;


    } else {
      istWateringControlOn = false;
    }
  }

  http.end();
  http.begin(pathSoilMoisTime);
  httpResponseCode = http.GET();

  if ( checkForHTTPError( httpResponseCode,  http.errorToString(httpResponseCode))) {
    DeserializationError err = deserializeJson(doc, http.getString());
    if (err) {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(err.c_str());
    }

    serializeJsonPretty(doc, Serial);
    waterringTimestamp = doc["FROM_TIME"].as<String>();

    if (doc["SOIL_MOISTURE_ON"].as<int>() == 1) {
      istWateringControlOn = true;

    } else {
      istWateringControlOn = false;
    }

    if (doc["TIMETABLE_ON"].as<int>() == 1) {
      isWaterTimeTableOn = true;

    } else {
      isWaterTimeTableOn = false;
    }
  }
  http.end();

  http.begin(pathLight);
  httpResponseCode = http.GET();

  if ( checkForHTTPError( httpResponseCode,  http.errorToString(httpResponseCode))) {
    DeserializationError err = deserializeJson(doc, http.getString());
    if (err) {
      Serial.print(("deserializeJson() failed: "));
      Serial.println(err.c_str());
    }
    //serializeJsonPretty(doc, Serial);

    fromTime = doc["FROM_TIME"].as<String>();
    toTime = doc["TO_TIME"].as<String>();

    if (doc["TIMETABLE_ON"].as<int>() == 1) {
      isLightTimeTableOn = true;

    } else {
      isLightTimeTableOn = false;
    }

  }


  http.end();
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
  doc["led_status"] = ledStatus;
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
    Serial.println("---Send Data succsess---");
    Serial.println(httpResponseCode);
    Serial.println(response);
    Serial.println("------------------");

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
    if (temp > tempLimit) {
      activateFan();

    } else {
      deactivateFan();
    }

  }
}

void turnLightsOn(String color) {
  ledStatus = 1;
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
  ledStatus = 0;
  int ledDelay = 20;
  for (int i = NUMBER_OF_LEDS - 1; i >= 0; i--) {
    leds[i] = CRGB::Black;
    FastLED.show();
    delay(ledDelay);


  }
}
void controlLights(String from, String till) {
  if (from == NULL || from == "" || till == NULL || till == ""  ) {
    Serial.println("[ERROR] Zeit für LED null oder nicht da");
  } else {
    if ((isLightTimeTableOn == true)) {
      if ( (from <= timestamp) &&  (timestamp >= till) ) {
        turnLightsOn("bluePurple");

      } else {
        turnLightsOff();
      }


    }
  }
}
void watering() {
  digitalWrite(WATER_PUMP_PIN, HIGH);  // Schaltet ein
  delay(5000);
  digitalWrite(WATER_PUMP_PIN, LOW);   // Schaltet aus
  delay(5000);
}
void wateringControlByValue() {

  if (istWateringControlOn == true && isWaterTimeTableOn == false) {
    if (minSoilMois != NULL) {
      if (minSoilMois >= mois) {
        watering();
      }
    }
  }
}


void wateringControlByTime(String from) {
  String wall = "23:50:00";
  String wall2 = "23:59:59";
  if (from == NULL || from == "") {
    Serial.println("[ERROR] Zeit für Pumpe null oder nicht da");
  } else {
    if ((istWateringControlOn == true) && (isWaterTimeTableOn == true)) {
      if ((!((wall < timestamp ) && (timestamp < wall2))) && timestamp >= from && alreadyWatered == false) {
        watering();
        alreadyWatered = true;
      }
      //potenieller Bug besser wäre eine art get Day

      if ((wall < timestamp ) && (timestamp < wall2)) {
        alreadyWatered = false;
      }

    }
  }

}

boolean checkForHTTPError(int errorCode, String errorMsg) {
  boolean everthingOK = true;
  if (errorCode <= 0) {
    Serial.printf("Error while sending/recive: %s \n", errorMsg.c_str()); // http.errorToString(statusCode).c_str()
    Serial.println(errorCode);
    everthingOK = false;
  }
  return everthingOK;
}

void printVars() {

  Serial.print("VAriabeln");
  Serial.println("Auzeichnung der Geräte: ");
  Serial.print("Temp: ");
  Serial.println(temp);
  Serial.print("mois: ");
  Serial.println(mois);
  Serial.print("mois: ");
  Serial.println(hum);
  Serial.println("===============\n");



  Serial.print("minSoilMois: ");
  Serial.println(minSoilMois);
  Serial.print("istWateringControlOn: ");
  Serial.println(istWateringControlOn);
  Serial.print("isWaterTimeTableOn: ");
  Serial.println(isWaterTimeTableOn);
  Serial.print("waterringTimestamp: ");
  Serial.println(waterringTimestamp);

  Serial.print("tempLimit: ");
  Serial.println(tempLimit);
  Serial.print(" isTempControlOn: ");
  Serial.println(isTempControlOn);
  Serial.print("fromTime: ");
  Serial.println(fromTime);
  Serial.print("toTime: ");
  Serial.println(toTime);
  Serial.print("isLightTimeTableOn: ");
  Serial.println(isLightTimeTableOn);

  Serial.print("timestamp: ");
  Serial.println(timestamp);
  Serial.println("===============\n");



}

void testComponents(){
activateFan();
delay(5000);
deactivateFan();
turnLightsOn("blue");
delay(5000);
turnLightsOff();
watering();
  Serial.println("Auzeichnung: ");
  Serial.print("Temp: ");
  Serial.println(getTemp());
  Serial.print("mois: ");
  Serial.println(getMoisture());
  Serial.print("hum: ");
  Serial.println(getHum());
  Serial.println("===============\n");


}
