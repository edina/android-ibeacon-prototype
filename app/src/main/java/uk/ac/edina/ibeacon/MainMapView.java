package uk.ac.edina.ibeacon;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.murray.testapp.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.ac.edina.ibeacon.geofence.BeaconGeoFence;
import uk.ac.edina.ibeacon.geofence.actions.GeoFenceAction;
import uk.ac.edina.ibeacon.geofence.actions.GeoFenceAlertDialogAction;
import uk.ac.edina.ibeacon.geofence.actions.GeoFenceAudioAction;
import uk.ac.edina.ibeacon.geofence.actions.GeoFenceHighLightRegionAction;
import uk.ac.edina.ibeacon.geofence.actions.GeoFenceWebActionImpl;

/**
 * Created by murray on 25/08/14.
 */
public class MainMapView extends Activity  implements BeaconConsumer {

    protected static final String TAG = "RangingActivity";
    public static final String BEACON_LAYOUT_FOR_ESTIMOTE = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private boolean matched = false;
    TextView distanceFromBeacon;

    KmlDocument kmlDocument;
    FixedMapView mapView;
    Utils utils = Utils.getInstance();
    List<BeaconGeoFence> beaconGeoFences = new ArrayList<BeaconGeoFence>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BEACON_LAYOUT_FOR_ESTIMOTE));
        beaconManager.bind(this);

        setupAndDisplayMap();

        addGeoFences();

    }

    private void addGeoFences() {
        /*
        GeoFenceAction highLightEdinaMeetingRoom = new GeoFenceHighLightRegionAction(MainMapView.this, mapView);

        GeoFenceAction alertDialogAction = new GeoFenceAlertDialogAction(MainMapView.this, "Enter Message", "Leave Message");
        String printerHelpUrl = "http://www8.hp.com/uk/en/home.html";
        GeoFenceAction showPrinterPage = new GeoFenceWebActionImpl(MainMapView.this, printerHelpUrl);
        String lightBlueIbeaconMinorId = "59317";

        BeaconGeoFence blueBeaconShowPrinterPage = new BeaconGeoFence(1,lightBlueIbeaconMinorId, alertDialogAction);
        beaconGeoFences.add(blueBeaconShowPrinterPage);
        */

        GeoFenceAction highlightEdinaMeetingRoom = new GeoFenceHighLightRegionAction(MainMapView.this, mapView);
        GeoFenceAction geoFenceAudioAction = new GeoFenceAudioAction(MainMapView.this, "chime.mp3");
        GeoFenceAction alertDialogWelcome = new GeoFenceAlertDialogAction(MainMapView.this, "Welcome to EDINA", "Don't forget to leave FOB at reception!");
        GeoFenceAction alertDialogPrinter = new GeoFenceAlertDialogAction(MainMapView.this, "Printer CSCH2a", "Bye bye Printer");
        String printerHelpUrl = "http://www.okidata.com/printers/color/c830";
        GeoFenceAction showPrinterPage = new GeoFenceWebActionImpl(MainMapView.this, printerHelpUrl);
        String lightBlueBeaconMinorId = "59317";
        String blueberryBeaconMinorId = "24489";
        String mintBeaconMinorId = "11097";

        BeaconGeoFence blueBeaconHighlightMeetingRoom = new BeaconGeoFence(1.0,lightBlueBeaconMinorId, geoFenceAudioAction);
        BeaconGeoFence blueberryBeaconPrinter = new BeaconGeoFence(1,blueberryBeaconMinorId, alertDialogPrinter);
        BeaconGeoFence mintBeaconAlert = new BeaconGeoFence(1,mintBeaconMinorId, alertDialogWelcome);
        beaconGeoFences.add(blueberryBeaconPrinter) ;
        beaconGeoFences.add(blueBeaconHighlightMeetingRoom) ;
        beaconGeoFences.add(mintBeaconAlert) ;


    }


    private void setupAndDisplayMap() {
        SingleRow routeRow = (SingleRow)getIntent().getSerializableExtra(MyActivity.ROUTE_CHOSEN_KEY);


        kmlDocument = new KmlDocument();


        File route = utils.copyFileFromAssets(routeRow.getRouteKmlFile(), this.getAssets(), this.getPackageName());

        boolean success = kmlDocument.parseKMLFile(route);

        // Tell the activity which XML layout is right
        setContentView(R.layout.map_view);
        /**
         * This whole thing revolves around instantiating a MainMapView class, way,
         * way below. And MainMapView requires a ResourceProxy. Who are we to deny
         * its needs? Let's create one!
         *
         * It would have been nice if this was taken care of in the MainMapView
         * constructor. Interestingly MainMapView *has* a constructor that creates a
         * new DefaultResourceProxyImpl but unfortunately that one doesn't allow
         * us to specify the parameters we *do* need to set ...
         */
        DefaultResourceProxyImpl resProxy;
        resProxy = new DefaultResourceProxyImpl(this.getApplicationContext());

        /**
         * A class that implements the ITileSource interface knows how to
         * convert an InputStream or a file path into a Drawable. It doesn't do
         * much more than that. The real 'sourcery' is performed by
         * MapTileFileArchiveProvider which will be introduced shortly.
         *
         * What we need is really a BitmapTileSourceBase instance, but this
         * class is defined as abstract. XYTileSource is not and comes closest
         * to what we want.
         *
         * Comment: I don't quite get why BitmapTileSource base is abstract; it
         * doesn't contain any abstract methods.
         */

        XYTileSource tSource;
        tSource = new XYTileSource("mbtiles",
                ResourceProxy.string.offline_mode,
                8, 22, 256, ".png", new String[]{"http://who.cares/"});


        IArchiveFile[] files = { MBTilesFileArchive.getDatabaseFileArchive(utils.getOfflineMap()) };
        SimpleRegisterReceiver sr = new SimpleRegisterReceiver(this);

        MapTileModuleProviderBase moduleProvider;
        moduleProvider = new MapTileFileArchiveProvider(sr, tSource, files);

        MapTileModuleProviderBase[] pBaseArray;
        pBaseArray = new MapTileModuleProviderBase[] { moduleProvider };

        MapTileProviderArray provider;
        provider = new MapTileProviderArray(tSource, null, pBaseArray);

        mapView = new FixedMapView(this, 256, resProxy, provider);
        mapView.setBuiltInZoomControls(true);
        distanceFromBeacon = new TextView(this);

        distanceFromBeacon.setText("Distance from Blue iBeacon!");


        final RelativeLayout relativeLayout = new RelativeLayout(this);

        final RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(

                RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);

        final RelativeLayout.LayoutParams textViewLayoutParams = new RelativeLayout.LayoutParams(

                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        final RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(

                RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        buttonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);


        relativeLayout.addView(mapView, mapViewLayoutParams);

        relativeLayout.addView(distanceFromBeacon,buttonLayoutParams);
        setContentView(relativeLayout);

        final BoundingBoxE6 bb =  kmlDocument.mKmlRoot.getBoundingBox();


        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(this);
        this.mapView.getOverlays().add(scaleBarOverlay);


        FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay( mapView, null, null, kmlDocument);

        mapView.getOverlays().add(kmlOverlay);

        mapView.setClickable(true);

        mapView.setMultiTouchControls(true);


        //controller.zoomToSpan(boundingBoxE6.getLatitudeSpanE6(), boundingBoxE6.getLongitudeSpanE6());

        // Set the MainMapView as the root View for this Activity; done!
        setContentView(relativeLayout);
        mapView.getController().setZoom(15); //set initial zoom-level, depends on your need


        final ViewTreeObserver vto = mapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                mapView.getController().animateTo(bb.getCenter());
            }
        });

        //controller.zoomToSpan(boundingBoxE6.getLatitudeSpanE6(),boundingBoxE6.getLongitudeSpanE6());
        // Enable the "Up" button for more navigation options
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }



    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    for( Beacon beacon: beacons) {

                        for (final BeaconGeoFence geoFence : beaconGeoFences) {
                            geoFence.evaluateGeofence(beacon);

                            Log.d(TAG, beacon.toString());


                        }
                    }

                    /**************************** debug to display *******************/

                    final StringBuilder debug = new StringBuilder();

                    List<Beacon> sortedBeaconsByDistance = new ArrayList<Beacon>(beacons);

                    Collections.sort(sortedBeaconsByDistance, new Comparator<Beacon>() {
                        @Override
                        public int compare(Beacon beacon, Beacon beacon2) {
                            double test = beacon.getDistance()  - beacon2.getDistance() ;
                            return (int) Math.round(test*100);
                        }
                    });


                    for(Beacon b : sortedBeaconsByDistance) {
                        debug.append(b.getId3()).append(": dis : ").append(b.getDistance()).append("\n");
                    }
                    MainMapView.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            distanceFromBeacon.setText(debug.toString());
                        }
                    });


                    /**************************** end debug to display *******************/
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("all beacons", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

}
