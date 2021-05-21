#include "DHTesp.h"
<<<<<<< Updated upstream

DHTesp dht;
int DHT11_PIN =  21;
int MOIS_SENSOR_PIN = 32;/
int FAN_PIN = 17;

void setup(){
  
dht.setup(DHT11_PIN, DHTesp::DHT11);
pinMode(FAN_PIN, OUTPUT); 
 Serial.begin(9600);
}

void loop(){
 //Serial.println(getTempAndHum(2000));
  //Serial.println(getMoisture(1000));
  digitalWrite(FAN_PIN, HIGH); 
  delay(10000);
  digitalWrite(FAN_PIN, LOW); 
  delay(10000);
 
}
/*
 * @delay als gerade Zahl angeben
 */

 int getMoisture(int del){
  
   int anRead= analogRead(MOIS_SENSOR_PIN);
   delay(del);
   int erg = map(anRead,4095,0,0,100);//4095=>0% trocken, 0=> 100% nass
   return erg;
 }
String getTempAndHum(int delayNum){
  delayNum = delayNum/2;
  
  TempAndHumidity lastValues = dht.getTempAndHumidity();
  delay(1000); //zweitePause weil ansonsen zu schnell
  String erg= "Temp: "+String(lastValues.temperature,0)+",Hum: "+String(lastValues.humidity,0);
  delay(1000);

  return erg;
  
  }
  
=======
#include "WiFi.h"

DHTesp dht;
const int DHT11_PIN =  21;
const int MOIS_SENSOR_PIN = 32;
const int FAN_PIN = 17;

const String ssid = "yourNetworkName";
const String password =  "yourNetworkPass";


int tempLimit = 15;
boolean isTempControlOn = true;


void setup() {

  dht.setup(DHT11_PIN, DHTesp::DHT11);
  pinMode(FAN_PIN, OUTPUT);
  Serial.begin(9600);
  //WiFi.begin(ssid, password);
}

void loop() {
  Serial.println(getTempAndHum());
  delay(1000);
  tempControl();

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

String getTempAndHum() {
  String erg = "Temp: " + String(getTemp(), 0) + ",Hum: " + String(getHum(), 0);

  return erg;

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

    }

  }
}
>>>>>>> Stashed changes
