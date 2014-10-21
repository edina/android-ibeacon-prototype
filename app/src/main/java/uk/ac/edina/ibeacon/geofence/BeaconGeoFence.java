package uk.ac.edina.ibeacon.geofence;

import uk.ac.edina.ibeacon.geofence.actions.GeoFenceAction;
import uk.ac.edina.ibeacon.geofence.states.BeaconGeoFenceState;
import uk.ac.edina.ibeacon.geofence.states.GeoFenceInsideState;
import uk.ac.edina.ibeacon.geofence.states.GeoFenceOutsideState;

/**
 * Created by murray on 14/10/14.
 */
public class BeaconGeoFence  {

    private final GeoFenceAction geoFenceAction;
    private double radius;
    private String minorId;


    private BeaconGeoFenceState currentState;

    private BeaconGeoFenceState outsideGeoFence;
    private BeaconGeoFenceState insideGeoFence;


    public BeaconGeoFence(double radius, String minorId, GeoFenceAction geoFenceAction) {
        if(minorId == null || minorId.isEmpty() || radius < 0){
            throw new IllegalArgumentException("Beacon minorId required");
        }
        this.radius = radius;
        this.minorId = minorId;
        this.geoFenceAction = geoFenceAction;
        this.outsideGeoFence = new GeoFenceOutsideState(this);
        this.insideGeoFence = new GeoFenceInsideState(this);
        this.currentState = outsideGeoFence;
    }


    public void evaluateGeofence(String id, double distance) {

        currentState.evaluateGeofence(id, distance);
    }

    public void setCurrentState(BeaconGeoFenceState currentState) {
        this.currentState = currentState;
    }

    public BeaconGeoFenceState getCurrentState() {
        return currentState;
    }

    public double getRadius() {
        return radius;
    }

    public String getMinorId() {
        return minorId;
    }

    public GeoFenceAction getGeoFenceAction() {
        return geoFenceAction;
    }

    public BeaconGeoFenceState getOutsideGeoFence() {
        return outsideGeoFence;
    }

    public BeaconGeoFenceState getInsideGeoFence() {
        return insideGeoFence;
    }

    @Override
    public String toString() {
        return "BeaconGeoFence{" +
                "radius=" + radius +
                ", minorId='" + minorId + '\'' +
                '}';
    }
}
