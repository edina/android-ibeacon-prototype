package uk.ac.edina.ibeacon.geofence;

import org.altbeacon.beacon.Beacon;

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
        if(minorId == null || minorId.isEmpty()){
            throw new IllegalArgumentException("Beacon minorId required");
        }
        this.radius = radius;
        this.minorId = minorId;
        this.geoFenceAction = geoFenceAction;
        this.outsideGeoFence = new GeoFenceOutsideState(this);
        this.insideGeoFence = new GeoFenceInsideState(this);
        this.currentState = outsideGeoFence;
    }


    public void evaluateGeofence(Beacon beacon) {
        currentState.evaluateGeofence(beacon);
    }

    public void setCurrentState(BeaconGeoFenceState currentState) {
        this.currentState = currentState;
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
