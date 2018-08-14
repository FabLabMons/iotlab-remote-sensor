package be.fablabmons.iotlab.remote_sensor;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MqttListener {

    private static final String TAG = "MqttListener";
    private String mqttBroker = "tcp://10.130.1.204:1883";
    private String mqttTopic = "teacher/remote-sensor/presence";
    private String deviceId = "teacherMqttListener";

    // Variables to store reference to the user interface activity.
    private MainActivity activity = null;

    public MqttListener(MainActivity activity) {
        this.activity = activity;
    }

    public void connectToMQTT() throws MqttException {
        // Request clean session in the connection options.
        Log.i(TAG, "Setting Connection Options");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        // Attempt a connection to MQTT broker using the values of connection variables.
        Log.i(TAG, "Creating New Client");
        MqttClient client = new MqttClient(mqttBroker, deviceId, new MemoryPersistence());
        client.connect(options);

        // Set callback method name that will be invoked when a new message is posted to topic,
        // MqttEventCallback class is defined later in the code.
        Log.i(TAG, "Subscribing to Topic");
        client.setCallback(new MqttEventCallback());

        // Subscribe to topic "codifythings/intrusiondetection", whenever a new message is published to
        // this topic MqttEventCallback.messageArrived will be called.
        client.subscribe(mqttTopic, 0);
    }

    // Implementation of the MqttCallback.messageArrived method, which is invoked whenever a
    // new message is published.
    private class MqttEventCallback implements MqttCallback {
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

            try {
                // Append the payload message with "@ Current Time".
                DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, new Locale("fr", "BE"));
                String sensorMessage = new String(msg.getPayload()) + " @ " +
                        df.format(new Date());

                // Update the screen with newly received message.
                activity.updateView(sensorMessage);
            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            }
        }
    }
}