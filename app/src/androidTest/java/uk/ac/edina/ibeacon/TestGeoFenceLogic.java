package uk.ac.edina.ibeacon;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import uk.ac.edina.ibeacon.geofence.BeaconGeoFence;
import uk.ac.edina.ibeacon.geofence.actions.GeoFenceAction;


/**
 * Created by murrayking on 21/10/2014.
 */
public class TestGeoFenceLogic extends TestCase {
    BeaconGeoFence beaconGeoFence;
    boolean onEnterActionCalled;
    boolean onLeaveActionCalled;
    String beaconMinorId = "11097";
    double radius = 1.5;

    @Override
    @Before
    protected void setUp() throws Exception {

        onEnterActionCalled = false;
        onLeaveActionCalled = false;



        GeoFenceAction geoFenceAction = new GeoFenceAction() {
            @Override
            public void onEnter() {
                onEnterActionCalled = true;
            }

            @Override
            public void onLeave() {
                onLeaveActionCalled = true;
            }
        };
        beaconGeoFence = new BeaconGeoFence(radius, beaconMinorId,geoFenceAction);

    }
    @Test
    public void testBeaconInitialSate() {

        assertEquals("Initial state is outside geofence", beaconGeoFence.getOutsideGeoFence(), beaconGeoFence.getCurrentState());
    }


    @Test
    public void testInsideGeoFence(){
        assertFalse(onLeaveActionCalled);
        assertFalse(onEnterActionCalled);

        assertEquals("Initial state is outside geofence", beaconGeoFence.getOutsideGeoFence(), beaconGeoFence.getCurrentState());
        beaconGeoFence.evaluateGeofence(beaconMinorId, radius - 0.1);

        assertEquals("Should be inside", beaconGeoFence.getInsideGeoFence(), beaconGeoFence.getCurrentState());
        assertTrue("onEnterActionCalled", onEnterActionCalled);
    }

    @Test
    public void testInsideOutsideGeoFence(){
        assertFalse(onLeaveActionCalled);
        assertFalse(onEnterActionCalled);

        assertEquals("Initial state is outside geofence", beaconGeoFence.getOutsideGeoFence(), beaconGeoFence.getCurrentState());

        beaconGeoFence.evaluateGeofence(beaconMinorId, radius + 0.1);

        assertEquals("Should be outside", beaconGeoFence.getOutsideGeoFence(), beaconGeoFence.getCurrentState());
        assertFalse("onenter shouldn't be called yet", onEnterActionCalled);

        beaconGeoFence.evaluateGeofence(beaconMinorId, radius - 0.1);
        assertTrue("onEnterActionCalled", onEnterActionCalled);

        assertEquals("Should be inside again", beaconGeoFence.getInsideGeoFence(), beaconGeoFence.getCurrentState());
        beaconGeoFence.evaluateGeofence(beaconMinorId, radius + 0.1);
        assertTrue("onLeaveActionCalled", onLeaveActionCalled);

    }


}
