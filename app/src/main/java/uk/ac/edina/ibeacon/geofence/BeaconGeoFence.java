package uk.ac.edina.ibeacon.geofence;

import org.altbeacon.beacon.Beacon;

/**
 * Created by murray on 14/10/14.
 */
public interface BeaconGeoFence {
    GeoFenceTrigger isGeofenceTriggered(Beacon beacon);
}
