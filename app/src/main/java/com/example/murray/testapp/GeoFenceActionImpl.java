package com.example.murray.testapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by murray on 15/10/14.
 */
public class GeoFenceActionImpl implements GeoFenceAction {
    private Activity activity;

    public GeoFenceActionImpl(Activity activity){
        this.activity = activity;
    }
    @Override
    public void onEnter() {
        String url = "http://www8.hp.com/uk/en/home.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        activity.startActivity(i);
    }

    @Override
    public void onLeave() {

    }
}
