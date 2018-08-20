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
  setupSerial();
  connectToWiFi();
  setupPubSubClient();
  setupSensor();
  notifySetupComplete();
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

void setupPubSubClient() {
  pubSubClient.setServer(broker, port);
  pubSubClient.setCallback(logPayload);
  pubSubClient.setClient(bridgeClient);
}

void setupSensor() {
  setupSensorGpio();
  calibrateSensor();
  Serial.println("Sensor ready");
}

void notifySetupComplete() {
  pinMode(onboardLedGpio, OUTPUT);
  for (int i = 0; i < 3; i++) {
    blinkOnboardLed();
  }
}

void blinkOnboardLed() {
  digitalWrite(onboardLedGpio, HIGH);
  delay(200);
  digitalWrite(onboardLedGpio, LOW);
  delay(200);
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

void logPayload(char* topic, byte* payload, unsigned int length) {
  String payloadContent = String((char *) payload);
  Serial.println("Payload: " + payloadContent);
}

void loop() {
  pubSubClient.loop();
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
}

bool newActivityIsDetected() {
  return currentSensorValue != lastSensorValue;
}

void publishNewActivity() {
  char* message = prepareMessageToSend();
  connectToMqttBroker();
  publishMessageIfBrokerIsAvailable(message);
  disconnectFromMqttBroker();
}

void resetChangeDetection() {
  lastSensorValue = currentSensorValue;
}

char* prepareMessageToSend() {
  if (thereIsMovement()) {
    return "movement detected";
  } else {
    return "still";
  }
}

void connectToMqttBroker() {
  Serial.println("Connecting to MQTT broker");
  if (pubSubClient.connect(mqttClientId)) {
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

void disconnectFromMqttBroker() {
  pubSubClient.disconnect();
}

bool thereIsMovement() {
  return currentSensorValue;
}

void publishMessage(char* message) {
  Serial.print("Publishing message: ");
  Serial.println(message);
  int status = pubSubClient.publish(topic, message, true);
  Serial.print("Published message, status: ");
  Serial.println(status);
}


