package com.example.murray.testapp;

import org.altbeacon.beacon.Beacon;

/**
 * Created by murray on 14/10/14.
 */
public interface BeaconGeoFence {
    GeoFenceTrigger isGeofenceTriggered(Beacon beacon);
}
