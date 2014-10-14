package com.example.murray.testapp;

/**
 * Created by murray on 14/10/14.
 */
public interface BeaconGeoFence {
    boolean isGeofenceTriggered(double range, String minorId);
}
