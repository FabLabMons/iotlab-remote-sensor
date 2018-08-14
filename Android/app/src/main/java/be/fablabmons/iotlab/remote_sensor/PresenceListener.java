package be.fablabmons.iotlab.remote_sensor;

import java.util.Date;

public interface PresenceListener {
    void onMovementDetected();
    void onStill();
}
