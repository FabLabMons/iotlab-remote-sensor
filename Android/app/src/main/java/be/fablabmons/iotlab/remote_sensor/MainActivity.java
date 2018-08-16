package be.fablabmons.iotlab.remote_sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements PresenceListener {

    private static final String TAG = "MainActivity";
    private static final String LAST_SENSOR_MESSAGE_KEY = "lastSensorMessage";
    private static final String LAST_ICON_RESOURCE_KEY = "lastIconResource";
    private static final String UNIQUE_ID_KEY = "uniqueId";
    private static final String DEFAULT_SENSOR_MESSAGE = "No Activity Detected";
    private static final int DEFAULT_ICON_RESOURCE = R.drawable.still;
    private static final String PREFERENCE_FILE_KEY = "be.fablabmons.iotlab.remote_sensor.PREFERENCE_FILE_KEY";
    private static final Locale BE_LOCALE = new Locale("fr", "BE");
    private static final TimeZone BRUSSELS_TZ = TimeZone.getTimeZone("Europe/Brussels");

    private String sensorMessage;
    private SharedPreferences sharedPref;
    private int iconResource;
    private UUID uniqueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSharedPreferences();
        restoreState();
        updateUi();
        tryToListenToMqttMessages();
    }

    private void getSharedPreferences() {
        sharedPref = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
    }

    private void restoreState() {
        restoreSensorMessage();
        restoreIconResource();
        restoreUniqueId();
    }

    private void restoreSensorMessage() {
        sensorMessage = sharedPref.getString(LAST_SENSOR_MESSAGE_KEY, DEFAULT_SENSOR_MESSAGE);
    }

    private void restoreIconResource() {
        iconResource = sharedPref.getInt(LAST_ICON_RESOURCE_KEY, DEFAULT_ICON_RESOURCE);
    }

    private void restoreUniqueId() {
        String uniqueIdString = sharedPref.getString(UNIQUE_ID_KEY, UUID.randomUUID().toString());
        uniqueId = UUID.fromString(uniqueIdString);
    }

    private void tryToListenToMqttMessages() {
        try {
            listenToMqttMessages();
        } catch(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void listenToMqttMessages() throws MqttException {
        new MqttSubscriber(this, uniqueId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovementDetected() {
        sensorMessage = addTimestamp("Movement detected");
        iconResource = R.drawable.presence_detected;
        updateUi();
        storeCurrentState();
    }

    @Override
    public void onStill() {
        sensorMessage = addTimestamp("Still");
        iconResource = R.drawable.still;
        updateUi();
        storeCurrentState();
    }

    private String addTimestamp(String sensorMessage) {
        Date now = new Date();
        String formattedTimestamp = formatDateTime(now);
        return sensorMessage + " @ " + formattedTimestamp;
    }

    private String formatDateTime(Date dateTime) {
        DateFormat df = DateFormat.getTimeInstance(DateFormat.MEDIUM, BE_LOCALE);
        df.setTimeZone(BRUSSELS_TZ);
        return df.format(dateTime);
    }

    private void updateUi() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateText();
                updateIcon();
            }
        });
    }

    private void updateText() {
        TextView updatedField = findViewById(R.id.updated_field);
        updatedField.setText(sensorMessage);
    }

    private void updateIcon() {
        ImageView detectionIconView = findViewById(R.id.detection_icon);
        detectionIconView.setImageResource(iconResource);
    }

    private void storeCurrentState() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LAST_SENSOR_MESSAGE_KEY, sensorMessage);
        editor.putInt(LAST_ICON_RESOURCE_KEY, iconResource);
        editor.putString(UNIQUE_ID_KEY, uniqueId.toString());
        editor.apply();
    }
}
