#include <DHTesp.h>
#include <WiFi.h>
#include <HTTPClient.h>

DHTesp dht;
const int DHT11_PIN =  21;
const int MOIS_SENSOR_PIN = 32;
const int FAN_PIN = 17;

const String SSID_D = "yourNetworkName";
const String PASSWORD =  "yourNetworkPass";
const String SERVER_URL = "";

HTTPClient http;

int temp;
int mois;
int hum;

int tempLimit = 24;
boolean isTempControlOn = true;


void setup() {

  
  //WiFi.mode(WIFI_STA);
  //WiFi.begin(SSID_D, PASSWORD);
/*
  while(WiFi.status()!= WL_CONNECTED){
    delay(1000);
    Serial.println("Connecting to WiFi...");
    
  }
  */
  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  Serial.begin(9600);
    
}


void loop() {

  /*
  if(WiFi.status()== WL_CONNECTED){
    
  }else{
     while(WiFi.status()!= WL_CONNECTED){
    delay(1000);
    Serial.println("Connecting to WiFi...");
    
  }
    */
  tempControl();
   mois = getMoisture();
   hum  = getTemp();
   temp = getHum();
   
   Serial.println("==========");
   Serial.println("Boden: " + String(mois));
   Serial.println("Luft: "+  String(hum));
   Serial.println("Temp: "+  String(temp));
   Serial.println("==========");

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
    if (getTemp()> tempLimit) {
      activateFan();

    }else{
      deactivateFan();
    }

  }
}
