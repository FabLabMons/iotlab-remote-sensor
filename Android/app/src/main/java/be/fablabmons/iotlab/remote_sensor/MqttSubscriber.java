package be.fablabmons.iotlab.remote_sensor;

import android.support.annotation.NonNull;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

class MqttSubscriber {

    private static final String TAG = "MqttSubscriber";
    private static final String BROKER_CONNECT_STRING = "tcp://10.130.1.204:1883";
    private static final String TOPIC = "teacher/remote-sensor/presence";
    private static final String CLIENT_ID = "teacherMqttListener";

    private final PresenceListener presenceListener;

    MqttSubscriber(PresenceListener presenceListener) throws MqttException {
        this.presenceListener = presenceListener;

        MqttClient client = createClient();
        connectClient(client);
        subscribeToTopic(client);
    }

    @NonNull
    private MqttClient createClient() throws MqttException {
        Log.i(TAG, "Creating New Client");
        MqttClient client = new MqttClient(BROKER_CONNECT_STRING, CLIENT_ID, new MemoryPersistence());
        client.setCallback(new PresenceMessageCallback());
        return client;
    }

    private void connectClient(MqttClient client) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);
    }

    private void subscribeToTopic(MqttClient client) throws MqttException {
        Log.i(TAG, "Subscribing to Topic");
        client.subscribe(TOPIC, 0);
    }

    private class PresenceMessageCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable arg0) {
            // Do nothing
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken arg0) {
            // Do nothing
        }

        @Override
        public void messageArrived(String topic, final MqttMessage msg) throws Exception {
            Log.i(TAG, "New Message Arrived from Topic - " + topic);
            tryToParseMessage(msg);
        }
    }

    private void tryToParseMessage(MqttMessage msg) {
        try {
            parseMessage(msg);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void parseMessage(MqttMessage msg) {
        if (new String(msg.getPayload()).contains("still")) {
            presenceListener.onStill();
        } else {
            presenceListener.onMovementDetected();
        }
    }
}
