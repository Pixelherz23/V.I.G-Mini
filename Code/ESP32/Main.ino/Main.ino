#include "DHTesp.h"

DHTesp dht;
int DHT11_PIN =  21;
int MOIS_SENSOR_PIN = 32;

void setup() {

  dht.setup(DHT11_PIN, DHTesp::DHT11);
  Serial.begin(9600);
}

void loop() {
  //Serial.println(getTempAndHum(2000));
  Serial.println(getMoisture(1000));
  delay(1000);


}

int getMoisture(int del) {
  int anRead = analogRead(MOIS_SENSOR_PIN);
  delay(del);
  int erg = map(anRead, 4095, 0, 0, 100); //4095=>0% trocken, 0=> 100% nass
  return erg;
}
String getTempAndHum(int delayNum) {
  TempAndHumidity lastValues = dht.getTempAndHumidity();
  delay(delayNum); //zweitePause weil ansonsen zu schnell
  String erg = "Temp: " + String(lastValues.temperature, 0) + ",Hum: " + String(lastValues.humidity, 0);
  delay(delayNum);

  return erg;

}
