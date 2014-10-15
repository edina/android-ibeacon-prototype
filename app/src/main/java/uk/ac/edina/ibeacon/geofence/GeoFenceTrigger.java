package uk.ac.edina.ibeacon.geofence;

/**
 * Created by murray on 15/10/14.
 */
public class GeoFenceTrigger {
    private boolean triggered;

    private String status;

    public GeoFenceTrigger(boolean triggered, String status) {
        this.triggered = triggered;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isTriggered() {
        return triggered;
    }
}
