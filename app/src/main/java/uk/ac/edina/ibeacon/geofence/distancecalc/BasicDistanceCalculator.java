package uk.ac.edina.ibeacon.geofence.distancecalc;

import uk.ac.edina.ibeacon.geofence.IBeacon;

/**
 * Created by murrayking on 22/10/2014.
 */
public class BasicDistanceCalculator implements DistanceCalculator {


    /**
     * http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing/20434019#20434019
     * The iBeacon output power is measured (calibrated) at a distance of 1 meter. Let's suppose that this is -59 dBm (just an example). The iBeacon will include this number as part of its LE advertisment.

     The listening device (iPhone, etc), will measure the RSSI of the device. Let's suppose, for example, that this is, say, -72 dBm.

     Since these numbers are in dBm, the ratio of the power is actually the difference in dB. So:

     ratio_dB = txCalibratedPower - RSSI
     To convert that into a linear ratio, we use the standard formula for dB:

     ratio_linear = 10 ^ (ratio_dB / 10)
     If we assume conservation of energy, then the signal strength must fall off as 1/r^2. So:

     power = power_at_1_meter / r^2. Solving for r, we get:

     r = sqrt(ratio_linear)

     * @return
     */
    @Override
    public double getDistance(IBeacon beacon) {

        double txCalibratedPower = beacon.getTxPower();
        double rssi = beacon.getRssi();

        double ratioDb = txCalibratedPower - rssi;
        double ratio_linear = Math.pow(10, ratioDb / 10);

        double r = Math.sqrt(ratio_linear);
        return r;
    }

}
