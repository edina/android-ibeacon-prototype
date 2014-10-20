package uk.ac.edina.ibeacon.geofence.states;

import android.util.Log;

import org.altbeacon.beacon.Beacon;

import uk.ac.edina.ibeacon.geofence.BeaconGeoFence;

/**
 * Created by murrayking on 20/10/2014.
 */
public class GeoFenceOutsideState implements BeaconGeoFenceState {
    private BeaconGeoFence beaconGeoFence;

    public GeoFenceOutsideState(BeaconGeoFence beaconGeoFence) {
        this.beaconGeoFence = beaconGeoFence;
    }

    @Override
    public void evaluateGeofence(Beacon beacon) {
        String id = beacon.getId3().toString();

        if( !beaconGeoFence.getMinorId().equals(id)){
            return;
        }
        double distance = beacon.getDistance();
        boolean enteringGeofence = distance < beaconGeoFence.getRadius();

        Log.d("BeaconGeoFenceState", "Distance Outside" + distance);
        if(enteringGeofence) {
            beaconGeoFence.setCurrentState(beaconGeoFence.getInsideGeoFence());
            beaconGeoFence.getGeoFenceAction().onEnter();
        }

    }
}