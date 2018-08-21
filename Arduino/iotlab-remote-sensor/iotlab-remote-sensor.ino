#include <BridgeClient.h>
#include <PubSubClient.h>

/* Sensor config */
const int sensorCalibrationTimeSecs = 20;
const int sensorGpio = 3;
const int delayBetweenLoopsMillis = 500;
const int onboardLedGpio = 13;

/* MQTT config */
const char* broker = "10.10.32.169";
const int port = 1883;
const char* topic = "teacher/remote-sensor/presence";
const char* mqttClientId = "teacher-arduino";

BridgeClient bridgeClient;
PubSubClient pubSubClient;

bool currentSensorValue = false;
bool lastSensorValue = false;

void setup() {
  Serial.begin(9600);
  delay(2000);

  Serial.println("Setup START");
  
  Bridge.begin();

  pinMode(sensorGpio, INPUT);
  digitalWrite(sensorGpio, LOW);
  Serial.print("Calibrating sensor...");
  for (int i = 0; i < sensorCalibrationTimeSecs; i++) {
    Serial.print('.');
    delay(1000);
  }
  Serial.println();
  Serial.println("Sensor ready");

  pubSubClient.setServer(broker, port);
  pubSubClient.setCallback(callback);
  pubSubClient.setClient(bridgeClient);

  Serial.println("Setup END");
}

void callback(char* topic, byte* payload, unsigned int length) {
  char message[100];
  strncpy(message, payload, length);
  Serial.print("Payload: ");
  Serial.println(message);
}

void loop() {
  pubSubClient.loop();
  
  currentSensorValue = digitalRead(sensorGpio);
  
  if (currentSensorValue != lastSensorValue) {
    Serial.println("New activity detected");
    char* message;
    if (currentSensorValue) {
      message = "movement detected";
    } else {
      message = "still";
    }

    pubSubClient.connect(mqttClientId);
    
    Serial.print("Publishing message: ");
    Serial.println(message);
    pubSubClient.publish(topic, message, true);

    pubSubClient.disconnect();
    
    lastSensorValue = currentSensorValue;
  }
  
  delay(delayBetweenLoopsMillis);
}


