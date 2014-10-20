package uk.ac.edina.ibeacon.geofence;

import org.altbeacon.beacon.Beacon;

/**
 * Created by murrayking on 20/10/2014.
 */
public interface BeaconGeoFenceState {

    void isGeofenceTriggered(Beacon beacon);
}
