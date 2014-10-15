package com.example.murray.testapp;

import org.altbeacon.beacon.Beacon;

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
    public boolean isGeofenceTriggered(Beacon beacon) {
        double distance = beacon.getDistance();
        
        //only using minor id for demo
        String id = beacon.getId3().toString();
        return distance < triggerRangeInMeters && this.minorId.equals(id);
    }
}
