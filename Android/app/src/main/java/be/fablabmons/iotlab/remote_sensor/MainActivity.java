package be.fablabmons.iotlab.remote_sensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateView("");

        try {
            MqttListener client = new MqttListener(this);
            client.connectToMQTT();
        } catch(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateView(String sensorMessage) {
        try {
            SharedPreferences sharedPref = getSharedPreferences(
                    "be.fablabmons.iotlab.remote_sensor.PREFERENCE_FILE_KEY",
                    Context.MODE_PRIVATE);

            if (sensorMessage == null || sensorMessage.equals("")) {
                sensorMessage = sharedPref.getString("lastSensorMessage",
                        "No Activity Detected");
            }

            final int imageResource;
            if (sensorMessage.contains("still")) {
                imageResource = R.drawable.still;
            } else {
                imageResource = R.drawable.presence_detected;
            }

            final String tempSensorMessage = sensorMessage;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView updatedField = findViewById(R.id.updated_field);
                    updatedField.setText(tempSensorMessage);
                    ImageView detectionIconView = findViewById(R.id.detection_icon);
                    detectionIconView.setImageResource(imageResource);
                }
            });

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("lastSensorMessage", sensorMessage);
            editor.commit();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

}
