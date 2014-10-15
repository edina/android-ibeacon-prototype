package com.example.murray.testapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.List;

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

        SingleRow routeRow = (SingleRow)getIntent().getSerializableExtra(MyActivity.ROUTE_CHOSEN_KEY);


        kmlDocument = new KmlDocument();

        addGeoFences();

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

    private void addGeoFences() {
        String lightBlueIbeaconMinorId = "59317";
        BeaconGeoFence beacon = new BeaconGeoFenceImpl(3,lightBlueIbeaconMinorId);
        beaconGeoFences.add(beacon);
    }
    /*
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {  Log.e(TAG, e.toString());   }
    }*/

    public void highlightArea(){

        File route = utils.copyFileFromAssets("meetingroom.kml", this.getAssets(), this.getPackageName());
        boolean success = kmlDocument.parseKMLFile(route);



        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                final FolderOverlay kmlOverlay = (FolderOverlay) kmlDocument.mKmlRoot.buildOverlay(mapView, null, null, kmlDocument);
                mapView.getOverlays().add(kmlOverlay);
                final BoundingBoxE6 bb = kmlDocument.mKmlRoot.getBoundingBox();
                mapView.getController().setZoom(22);
                mapView.getController().animateTo(bb.getCenter());


            }
        });

    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    final Beacon beacon = beacons.iterator().next();

                    Log.i(TAG, "The first beacon I see is about "+beacon.getDistance()+" meters away.");
                    //light light blue beacon

                    String lightBlueIbeacon = "8392";


                    if(lightBlueIbeacon.equals(beacon.getId3().toString())){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                distanceFromBeacon.setText("Blue beacon " + beacon.getDistance() + " meters away.");
                            }
                        });
                    }


                    for(final BeaconGeoFence geoFence : beaconGeoFences){
                        boolean triggered = geoFence.isGeofenceTriggered(beacon);
                        if(triggered){

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    AlertDialog.Builder builder1 = new AlertDialog.Builder(MainMapView.this);
                                    builder1.setMessage("Found " + geoFence.toString());
                                    builder1.setCancelable(true);
                                    builder1.setPositiveButton("Ok",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                }
                                            });


                                    AlertDialog alert11 = builder1.create();
                                    alert11.show();



                                }
                            });
                        }
                    }

                    if(!matched && lightBlueIbeacon.equals(beacon.getId3().toString()) ){

                        matched = true;
                        //matched beacon
                        Log.i(TAG, "Matched" );

                        highlightArea();
                        //zoom to kitchen

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainMapView.this);
                                builder1.setMessage("The light blue beacon I see is about " + beacon.getDistance() + " meters away.");
                                builder1.setCancelable(true);
                                builder1.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });


                                AlertDialog alert11 = builder1.create();
                                alert11.show();



                            }
                        });
                        /*

                        */

                    }
                    Log.i(TAG, beacon.toString() );
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
