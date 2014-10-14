package com.example.murray.testapp;

/**
 * Created by murray on 14/10/14.
 */
public class BeaconGeoFenceImpl implements  BeaconGeoFence {

    private double triggerRangeInMeters;
    private String minorId;

    public BeaconGeoFenceImpl(double triggerRangeInMeters, String minorId) {
        if(minorId == null || minorId.isEmpty()){
            throw new IllegalArgumentException("Beacon minorId required");
        }
        this.triggerRangeInMeters = triggerRangeInMeters;
        this.minorId = minorId;
    }


    @Override
    public boolean isGeofenceTriggered(double range, String minorId) {
        return range < triggerRangeInMeters && minorId.equals(minorId);
    }
}
