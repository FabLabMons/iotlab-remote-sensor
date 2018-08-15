#include <YunClient.h>
#include <PubSubClient.h>

/* Sensor config */
const int sensorCalibrationTimeSecs = 20;
const int sensorGpio = 3;
const int delayBetweenLoopsMillis = 50;
const int delayBetweenChangesMillis = 1000;

/* MQTT config */
const char* server = "10.130.1.204";
const int port = 1883;
const char* topic = "teacher/remote-sensor/presence";

YunClient yunClient;
PubSubClient pubSubClient;

bool currentSensorValue = false;
bool lastSensorValue = false;

void setup() {
  setupSerial();
  connectToWiFi();
  setupPubSubClient();
  setupSensor();
}

void setupSerial() {
    Serial.begin(9600);
    delay(2000);
    Serial.println("Serial port setup");
}

void connectToWiFi() {
  Serial.println("Connecting to WiFi...");
  Bridge.begin();
  printConnectionInformation();
}

void printConnectionInformation() {  
  // Initialize a new process
  Process wifiCheck;

  // Run Command
  wifiCheck.runShellCommand("/usr/bin/pretty-wifi-info.lua");

  // Print Connection Information  
  while (wifiCheck.available() > 0) 
  {
    char c = wifiCheck.read();
    Serial.print(c);
  }

  Serial.println("-----------------------------------------------");
  Serial.println("");
}

void setupPubSubClient() {
  pubSubClient.setServer(server, port);
  pubSubClient.setCallback(logPayload);
  pubSubClient.setClient(yunClient);
}

void logPayload(char* topic, byte* payload, unsigned int length) {
  String payloadContent = String((char *) payload);
  Serial.println("Payload: " + payloadContent);
}

void setupSensor() {
  setupSensorGpio();
  calibrateSensor();
  Serial.println("Sensor ready");
}

void setupSensorGpio() {
  pinMode(sensorGpio, INPUT);
  digitalWrite(sensorGpio, LOW);
}

void calibrateSensor() {
  Serial.print("Calibrating sensor...");
  for (int i = 0; i < sensorCalibrationTimeSecs; i++) {
    Serial.print('.');
    delay(1000);
  }
  Serial.println(" done");
}

void loop() {
  publishActivityIfNeeded();
}

void publishActivityIfNeeded() {
  readSensorData();
  if (newActivityIsDetected()) {
    Serial.println("New activity detected");
    publishNewActivity();
    resetChangeDetection();
  }
  delay(delayBetweenLoopsMillis);
}

void readSensorData() {
  currentSensorValue = digitalRead(sensorGpio) == HIGH;
  digitalWrite(13, currentSensorValue ? HIGH : LOW);
}

bool newActivityIsDetected() {
  return currentSensorValue != lastSensorValue;
}

void resetChangeDetection() {
  lastSensorValue = currentSensorValue;
}

void publishNewActivity() {
  char* message = prepareMessageToSend();
  connectToMqttBroker();
  publishMessageIfBrokerIsAvailable(message);
  disconnectFromMqttBroker();
}

char* prepareMessageToSend() {
  if (thereIsMovement()) {
    return "movement detected";
  } else {
    return "still";
  }
}

bool thereIsMovement() {
  return currentSensorValue;
}

void connectToMqttBroker() {
  Serial.println("Connecting to MQTT broker");
  if (pubSubClient.connect( "arduinoIoTClient" )) {
    Serial.println("Connected to MQTT broker");
  } else {
    Serial.println("Connection to MQTT Broker Failed");
  }
}

void publishMessageIfBrokerIsAvailable(char* message) {
  if (pubSubClient.connected()) {
    publishMessage(message);
  }
}

void publishMessage(char* message) {
  Serial.print("Publishing message: ");
  Serial.println(message);
  int status = pubSubClient.publish(topic, message);
  Serial.print("Published message, status: ");
  Serial.println(status);
}

void disconnectFromMqttBroker() {
  pubSubClient.disconnect();
}

