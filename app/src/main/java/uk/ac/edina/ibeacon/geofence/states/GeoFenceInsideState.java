package uk.ac.edina.ibeacon.geofence.states;

import android.util.Log;

import org.altbeacon.beacon.Beacon;

import uk.ac.edina.ibeacon.geofence.BeaconGeoFence;

/**
 * Created by murrayking on 20/10/2014.
 */
public class GeoFenceInsideState implements  BeaconGeoFenceState {

    private BeaconGeoFence beaconGeoFence;


    public GeoFenceInsideState(BeaconGeoFence beaconGeoFence) {
        this.beaconGeoFence = beaconGeoFence;
    }

    @Override
    public void evaluateGeofence(Beacon beacon) {

        String id = beacon.getId3().toString();
        if( !beaconGeoFence.getMinorId().equals(id)){
            return;
        }
        double distance = beacon.getDistance();
        Log.d("BeaconGeoFenceState", "Distance Inside " + distance);

        boolean leavingGeofence = distance > beaconGeoFence.getRadius();
        if(leavingGeofence) {
            beaconGeoFence.setCurrentState(beaconGeoFence.getOutsideGeoFence());
            beaconGeoFence.getGeoFenceAction().onLeave();
        }
    }
}