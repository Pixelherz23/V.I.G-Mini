#include <DHTesp.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>


DHTesp dht;
const int DHT11_PIN =  21;
const int MOIS_SENSOR_PIN = 32;
const int FAN_PIN = 17;

unsigned long intervalOfSendData = 10000;
unsigned long startTimeOfTimeout = 0;

String GEWAECHSHAUS_ID = "ABCDE";
const char * SSID_D = "XXX-XXX";
const char * PASSWORD =  "glaubstDuDochSelberNicht :D";
const String SERVER_URL = "XXX";
//dd

HTTPClient http;

int temp;
int mois;
int hum;

int tempLimit = 24;
boolean isTempControlOn = true;



void sendData();
void connectToWifi();
int getMoisture();
float getTemp();
float getHum();
void activateFan();
void tempControl();
void connectToWifi();

void setup() {
  connectToWifi();

  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  Serial.begin(9600);

}


void loop() {
  if (WiFi.status() == WL_CONNECTED) {

    unsigned long currentMillis = millis();
    if ((currentMillis - startTimeOfTimeout ) > intervalOfSendData) //send data every 10minuts
    {
      startTimeOfTimeout = currentMillis;

    }
  } else {
    connectToWifi();

  }



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

  // String jsonData = "{\""+GEWÄCHSHAUS_ID\":\"tPmAT5Ab3j7F9\",\"sensor\":\"BME280\",\"value1\":\"24.25\",\"value2\":\"49.54\",\"value3\":\"1005.14\"}"
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

/*
   @delay als gerade Zahl angeben
*/
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
