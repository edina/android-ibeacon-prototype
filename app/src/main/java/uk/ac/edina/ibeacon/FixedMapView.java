package uk.ac.edina.ibeacon;

import java.util.Timer;
import java.util.TimerTask;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.view.MotionEvent;



public class FixedMapView extends MapView {
    private static final int NONE = 0;
    private static final int ZOOM = 1;
    private static final int LIMIT_ZOOM_TIMER = 500;


    int mode = NONE;

    public FixedMapView(final Context context, final int tileSizePixels,
                        final ResourceProxy resourceProxy, final MapTileProviderBase aTileProvider) {
        super(context,tileSizePixels,resourceProxy, aTileProvider);

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        switch (ev.getAction()){
            case MotionEvent.ACTION_POINTER_2_DOWN :
                if(ev.getPointerCount() > 1){
                    // Catch pointer 2 down and check if pointer count > 1 to activate zoom
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE :
                if(mode == ZOOM && ev.getPointerCount() <= 1){
                    // if zoom activated and pointer counter < 1 means panning event captured at zoom end,
                    // don't handle it for LIMIT_ZOOM_TIMER to avoid strange map behavior
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_1_UP :
            case MotionEvent.ACTION_POINTER_2_UP :
                startEndZoomTimer();
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void startEndZoomTimer(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // disable zoom mode after some time to reanable panning
                mode = NONE;
            }
        }, LIMIT_ZOOM_TIMER);
    }

}