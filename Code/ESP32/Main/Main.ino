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

String GEWAECHSHAUS_ID = "GTD2-ERH6-E665";
const char * SSID_D = "XXX";
const char * PASSWORD =  "XXX";
const String SERVER_URL = "http://79.231.136.119:5000";


HTTPClient http;
int temp;
int mois;
int hum;
int tempLimit = 24;
boolean isTempControlOn = true;

time_t curTimestamp;
char* testTime = "16:29:00";


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
void requestTimeStamp();

void setup() {
  connectToWifi();

  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUMBER_OF_LEDS);
  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  pinMode(WATER_PUMP_PIN, OUTPUT);
  Serial.begin(9600);

}


void loop() {
  sendData();
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

    }


    } else {
    connectToWifi();

    }
     /*
         turnLightsOn("blue");
    delay(4000);
    turnLightsOff();
    delay(4000);
    turnLightsOn("purple");
    delay(4000);
    turnLightsOff();
    delay(4000);
    turnLightsOn("bluePurple");
    delay(4000);
    turnLightsOff();
    delay(4000);


    requestTimeStamp();
    delay(2000);

  */


}

void requestTimeStamp() {
  Serial.println("Request Time wird ausgeführt");
  String serverpath = SERVER_URL + "/time";
  http.begin(serverpath);
  int httpResponseCode = http.GET();

  if (httpResponseCode > 0) {

    String response = http.getString();
    const char* timestamp = response.c_str();

    struct tm *timeptr, tm1, tm2;

    if (strptime(timestamp, "%T", &tm2) == NULL) { //https://man7.org/linux/man-pages/man3/strptime.3.html
      Serial.printf("\nstrptime failed\n");
    }
    curTimestamp = mktime(&tm2);

  } else {


    Serial.printf("Error while request Time %s \n", http.errorToString(httpResponseCode).c_str()); // httpClient.errorToString(statusCode).c_str()
    Serial.println(httpResponseCode);

  }


}
int comparetime(time_t time1, time_t time2) {
  if (difftime(time1, time2) > 0.0) {
    return 1;
  } else {
    return -1;
  }

}
void requestSettings() {


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
