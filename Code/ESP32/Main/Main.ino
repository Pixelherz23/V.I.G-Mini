//To disable pragma messages on compile
//include this Before including FastLED.h
#define FASTLED_INTERNAL
#include <FastLED.h>
#include <DHTesp.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>


DHTesp dht;
const int DHT11_PIN =  21;
const int MOIS_SENSOR_PIN = 32;
const int FAN_PIN = 17;
const int LED_PIN = 33;
const int NUMBER_OF_LEDS = 20;


unsigned long intervalOfSendData = 600000;
unsigned long startTimeOfTimeout = 0;

String GEWAECHSHAUS_ID = "ABCDE";
const char * SSID_D = "Vodafone-523C";
const char * PASSWORD =  "eYkGXnyhzbP3ahs9";
const String SERVER_URL = "http://79.231.136.119:5000";


HTTPClient http;
int temp;
int mois;
int hum;
int tempLimit = 24;
boolean isTempControlOn = true;

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
void setup() {
  // connectToWifi();

  FastLED.addLeds<WS2812, LED_PIN, GRB>(leds, NUMBER_OF_LEDS);
  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  Serial.begin(9600);

}


void loop() {
  /*
    if (WiFi.status() == WL_CONNECTED) {

    unsigned long currentMillis = millis();
    if ((currentMillis - startTimeOfTimeout ) > intervalOfSendData) //send data every 10minuts
    {
      startTimeOfTimeout = currentMillis;

    }
    } else {
    connectToWifi();

    }
  */
  //Leds testen
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
}

void sendData() {
  Serial.println("send Data wird ausgeführt");
  String serverpath = SERVER_URL + "/sensor";

  http.begin(serverpath);

  StaticJsonDocument<200> doc;
  doc["gewaechshaus-id"] = GEWAECHSHAUS_ID;
  doc["moisture"] = getMoisture();
  doc["humidity"]   = getHum();
  doc["temperature"] = getTemp();

  http.addHeader("Content-Type", "application/json");
  String requestBody;
  Serial.println("Data");
  serializeJson(doc, requestBody);
  int httpResponseCode = http.POST(requestBody);

  if (httpResponseCode > 0) {

    String response = http.getString();

    Serial.println(httpResponseCode);
    Serial.println(response);

  } else {

    Serial.println("Error while sending HTTP POST:"); // httpClient.errorToString(statusCode).c_str()
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
