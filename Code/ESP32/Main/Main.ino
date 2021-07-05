/*
   TODO
   for buisness logic, default  setting for XYcontrol to false
   CHECK OB ES IN ORDNUNG IST INVERVALL 1 ZU NEHMEN
*/
//To disable pragma messages on compile
//include this Before including FastLED.h
#define FASTLED_INTERNAL
#include <FastLED.h>
#include <DHTesp.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>



#define OFF LOW
#define ON HIGH

DHTesp dht;
const int DHT11_PIN =  21;
const int MOIS_SENSOR_PIN = 32;
const int FAN_PIN = 23;
const int WATER_PUMP_PIN = 18;
const int LED_PIN = 33;
const int NUMBER_OF_LEDS = 20;



unsigned long intervalOfSendData = 60000;//1MIn 10 =600000
unsigned long pastTimeSend = 0;

unsigned long intervalOfGetData = 10000;//30sekunden
unsigned long pastTimeGet = 0;

unsigned long intervalOfGetTime = 10000;//10sekunden
unsigned long pastTimeOfTime =  0;

unsigned long intervalOfPump = 1800000  ;//30min
unsigned long pastTimeOfPump = 0;

String GEWAECHSHAUS_ID = "GTD2-ERH6-E665";
const char * SSID_D = "Vodafone-DAE5";
const char * PASSWORD =  "XXX";
const String SERVER_URL = "https://vig-mini.ddns.net";

HTTPClient http;
JsonObject settingsObj;
int temp;
int mois;
int hum;

int minSoilMois = 0;
boolean istWateringControlOn = false;
boolean isWaterTimeTableOn = false;
boolean alreadyWatered = false; //flag for watertimetable
String waterringTimestamp = "";

int tempLimit = 24;
boolean isTempControlOn = false;

String timestamp = "";

String fromTime = "";
String toTime = "";
boolean isLightTimeTableOn = false;
int ledStatus = 0;








CRGB leds[NUMBER_OF_LEDS];


//Deklarierung von Funktionen

void requestTimeStamp();
void requestSettings();
void sendData();
void connectToWifi();

int getMoisture();
float getTemp();
float getHum();

void deactivateFan();
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
  Serial.begin(9600);

  connectToWifi();
  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUMBER_OF_LEDS);
  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  delay(1000);
  //pinMode(MOIS_SENSOR_PIN, OUTPUT);
  //delay(1000);
  pinMode(WATER_PUMP_PIN, OUTPUT);
  /*
    delay(1000);
    digitalWrite(FAN_PIN,OFF);
    delay(1000);
    digitalWrite(WATER_PUMP_PIN, OFF);
  */

}


void loop() {
  //==Business-logic===

  temp = (int)getTemp();
  mois = (int)getMoisture();
  hum = (int)getHum();

  if (WiFi.status() == WL_CONNECTED) {
    requestTimeStamp();
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

  } else {
    connectToWifi();

  }

  printVars();
  tempControl();
  //turnLightsOn("blue");
  controlLights(fromTime, toTime);

  if (istWateringControlOn == true) {
    if (isWaterTimeTableOn == false) {
      alreadyWatered = false; //need to be tested

      unsigned long currentMillis3 = millis();
      if ((currentMillis3 - pastTimeOfPump ) > intervalOfPump)// Check if value of soil changed every 10min
      {
        pastTimeOfPump = currentMillis3;
        wateringControlByValue();
      }
    } else if (isWaterTimeTableOn == true) {

      wateringControlByTime(waterringTimestamp);

    }
  }

  //==Buiness-Logic-End===


  //==Test-Logic==
  //testComponents();
  //activateFan();

  //Serial.println("Fan ist aus");
  //deactivateFan();
  //delay(3000);
  //sendData();
  //delay(100);

  //==TEst-Logic-End==

}

/*
   ESP32 has no internal clock. This function request a timestamp from server
   tested
*/
void requestTimeStamp() {
  Serial.println("Request Time wird ausgeführt");
  String serverpath = SERVER_URL + "/time";
  http.begin(serverpath);
  int httpResponseCode = http.GET();
  if (httpResponseCode > 0) {

    String response = http.getString();
    http.end();

    timestamp = response.c_str();
  } else {
    Serial.printf("Error while request Time %s \n", http.errorToString(httpResponseCode).c_str());
    Serial.println(httpResponseCode);

  }

}

/*
    requests the server for the user settings
    tested
*/
void requestSettings() {

  Serial.println("requestSettings wird ausgeführt");
  DeserializationError err;
  String settings = "";
  int httpResponseCode = NULL;
  HTTPClient http;
  //URLs to the different interfaces
  String pathTemp = SERVER_URL + "/greenhouse/settings/temperature?product_key=" + GEWAECHSHAUS_ID;
  String pathMoisVal = SERVER_URL + "/greenhouse/settings/soil-moisture/value?product_key=" + GEWAECHSHAUS_ID + "&interval=1";
  String pathSoilMoisTime = SERVER_URL + "/greenhouse/settings/soil-moisture/time?product_key=" + GEWAECHSHAUS_ID;
  String pathLight = SERVER_URL + "/greenhouse/settings/light?product_key=" + GEWAECHSHAUS_ID + "&interval=1";
  DynamicJsonDocument doc(2048);
  //connect to server
  http.begin(pathTemp);
  httpResponseCode = http.GET();

  //check if connection failed. If so, checkForHTTPError prints the error message
  if ( checkForHTTPError( httpResponseCode,  http.errorToString(httpResponseCode))) {
    //format retrivied message to json
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
  //disconnect
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

    //serializeJsonPretty(doc, Serial);
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
    serializeJsonPretty(doc, Serial);


    JsonArray arr = doc.as<JsonArray>();
    fromTime = arr[0]["FROM_TIME"].as<String>();
    toTime = arr[0]["TO_TIME"].as<String>();
    //fromTime = doc["FROM_TIME"].as<String>();
    //toTime = doc["TO_TIME"].as<String>();

    if (arr[0]["TIMETABLE_ON"].as<int>() == 1) {
      isLightTimeTableOn = true;

    } else {
      isLightTimeTableOn = false;
    }

  }


  http.end();
}

/*
   send Data in Json Format to REST
*/
void sendData() {
  Serial.println("send Data wird ausgeführt");
  String serverpath = SERVER_URL + "/greenhouse/measurements/new";

  //save measurements  with keys
  StaticJsonDocument<200> doc;
  doc["product_key"] = GEWAECHSHAUS_ID;
  doc["led_status"] = ledStatus;
  doc["temperature"] = String(getTemp());
  doc["humidity"]   = getHum();
  doc["soil_moisture"] = String(getMoisture());

  //build connection
  http.begin(serverpath);
  http.addHeader("Content-Type", "application/json");
  String requestBody;
  Serial.println("Data");
  //convert doc to a Json Format
  serializeJson(doc, requestBody);
  //print it on console
  serializeJsonPretty(doc, Serial);
  //send it
  int httpResponseCode = http.POST(requestBody);
  //check for error
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
  http.end();

}
/*
   create a wifi connection
*/
void connectToWifi() {
  WiFi.mode(WIFI_STA);
  WiFi.begin(SSID_D, PASSWORD);
  unsigned long timer = millis();
  //try to connect to wifi till connection established or the the timeout hits
  while (WiFi.status() != WL_CONNECTED && ((millis() - timer) < 10000)) {
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
  int erg = map(anRead, 4095, 231, 0, 100); //4095=>0% trocken, 231=> 100% nass
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
  Serial.println("Fan ist an");
  digitalWrite(FAN_PIN, ON);

}
void deactivateFan() {
  Serial.println("Fan ist aus");
  digitalWrite(FAN_PIN, OFF);

}

//tested
void tempControl() {
  Serial.println("TempControll wird ausgeführt");
  if (isTempControlOn == NULL || isTempControlOn == false) {
    deactivateFan();
    Serial.println("Lüfter ist aus weil isTempControlOn null oder isTempControlOn false");
  } else {
    if (temp > tempLimit) {

      activateFan();
      Serial.println("Lüfter ist an");
    } else {
      deactivateFan();
      Serial.println("Lüfter ist aus");
    }

  }
}
//tested
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
//tested
void turnLightsOff() {
  ledStatus = 0;
  int ledDelay = 20;
  for (int i = NUMBER_OF_LEDS - 1; i >= 0; i--) {
    leds[i] = CRGB::Black;
    FastLED.show();
    delay(ledDelay);


  }
}

/*
   turn light on if timestamp is between the from-time and till-time
   tested
*/
void controlLights(String from, String till) {
  if (from == NULL || from == "" || till == NULL || till == ""  ) {
    Serial.println("[ERROR] Zeit für LED null oder nicht da");
  } else {
    if ((isLightTimeTableOn == true)) {
      if ( (from <= timestamp) &&  (timestamp <= till) ) {
        Serial.println("LEDs an");
        turnLightsOn("bluePurple");

      } else {
        turnLightsOff();
      }


    } else {
      turnLightsOff();
    }
  }
}
void watering() {
  Serial.println("Pumpe ist an");
  digitalWrite(WATER_PUMP_PIN, ON);
  delay(5000);
  Serial.println("Pumpe ist aus");
  digitalWrite(WATER_PUMP_PIN, OFF);

}
//tested
void wateringControlByValue() {
  Serial.println("Watering by Val wird ausgeführt");
  if (minSoilMois != NULL) {
    if (minSoilMois >= mois) {
      watering();
    }
  }

}
/*
   water once a day if timestamp is bigger or equals than from
   tested
*/
void wateringControlByTime(String from) {
  Serial.println("Watering by Time wird ausgeführt");
  String wall = "23:55:00";
  String wall2 = "23:59:59";
  if (from == NULL || from == "") {
    Serial.println("[ERROR] Zeit für Pumpe null oder nicht da");
  } else {
    if ((!((wall < timestamp ) && (timestamp < wall2))) && timestamp >= from && alreadyWatered == false) {
      watering();
      alreadyWatered = true;
    }
    //reset flag for the next day
    if ((wall < timestamp ) && (timestamp < wall2)) {
      Serial.println("reset");
      alreadyWatered = false;
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

  Serial.print("Variabeln");
  Serial.println("===============");
  Serial.print("Temp: ");
  Serial.println(temp);
  Serial.print("mois: ");
  Serial.println(mois);
  Serial.print("hum: ");
  Serial.println(hum);
  Serial.println("===============");



  Serial.print("minSoilMois: ");
  Serial.println(minSoilMois);
  Serial.print("istWateringControlOn: ");
  Serial.println(istWateringControlOn);
  Serial.print("isWaterTimeTableOn: ");
  Serial.println(isWaterTimeTableOn);
  Serial.print("waterringTimestamp: ");
  Serial.println(waterringTimestamp);
  Serial.println("===============\n");
  Serial.print("tempLimit: ");
  Serial.println(tempLimit);
  Serial.print(" isTempControlOn: ");
  Serial.println(isTempControlOn);
  Serial.println("===============\n");
  Serial.print("fromTime: ");
  Serial.println(fromTime);
  Serial.print("toTime: ");
  Serial.println(toTime);
  Serial.print("isLightTimeTableOn: ");
  Serial.println(isLightTimeTableOn);
  Serial.println("===============");
  Serial.print("timestamp: ");
  Serial.println(timestamp);
  Serial.println("===============");



}

void testComponents() {
  delay(5000);
  activateFan();
  delay(5000);
  Serial.println("Fan aus");
  deactivateFan();
  delay(1000);
  turnLightsOn("blue");
  delay(5000);
  turnLightsOff();
  watering();
  /*
    Serial.println("Auzeichnung: ");
    Serial.print("Temp: ");
    Serial.println(getTemp());
    Serial.print("mois: ");
    Serial.println(getMoisture());
    Serial.print("hum: ");
    Serial.println(getHum());
    Serial.println("===============\n");
  */

}