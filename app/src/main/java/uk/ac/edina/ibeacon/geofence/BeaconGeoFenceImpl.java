package uk.ac.edina.ibeacon.geofence;

import org.altbeacon.beacon.Beacon;

/**
 * Created by murray on 14/10/14.
 */
public class BeaconGeoFenceImpl implements  BeaconGeoFence {

    private final GeoFenceAction geoFenceAction;
    private double radius;
    private String minorId;
    private boolean enteredGeoFence = false;
    private final GeoFenceTrigger GEOFENCE_NOT_TRIGGERED = new GeoFenceTrigger(false, "NOT TRIGGERED");
    private final GeoFenceTrigger GEOFENCE_ENTERED = new GeoFenceTrigger(true, "GEOFENCE_ENTERED");
    private final GeoFenceTrigger GEOFENCE_LEAVING = new GeoFenceTrigger(true, "GEOFENCE_LEAVING");



    public BeaconGeoFenceImpl(double radius, String minorId, GeoFenceAction geoFenceAction) {
        if(minorId == null || minorId.isEmpty()){
            throw new IllegalArgumentException("Beacon minorId required");
        }
        this.radius = radius;
        this.minorId = minorId;
        this.geoFenceAction = geoFenceAction;
    }


    @Override
    public GeoFenceTrigger isGeofenceTriggered(Beacon beacon) {
        String id = beacon.getId3().toString();
        if( !this.minorId.equals(id)){
            //different ibeacon;
            return GEOFENCE_NOT_TRIGGERED;
        }
        double distance = beacon.getDistance();

        if(!enteredGeoFence){
            //check if entering geofence
            boolean enteringGeofence = distance < radius;
            if(enteringGeofence) {
                enteredGeoFence = true;
                geoFenceAction.onEnter();
                return GEOFENCE_ENTERED;
            }
        }

        if(enteredGeoFence){
            //check if leaving
            boolean leavingGeofence = distance > radius;
            if(leavingGeofence) {
                enteredGeoFence = false;
                geoFenceAction.onLeave();
                return GEOFENCE_LEAVING;
            }

        }

        return  GEOFENCE_NOT_TRIGGERED;
    }

    @Override
    public String toString() {
        return "BeaconGeoFenceImpl{" +
                "radius=" + radius +
                ", minorId='" + minorId + '\'' +
                '}';
    }
}