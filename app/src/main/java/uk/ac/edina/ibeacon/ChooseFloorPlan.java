package uk.ac.edina.ibeacon;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

public class ChooseFloorPlan extends Activity implements  AdapterView.OnItemClickListener {
    private static final String MAP_DB_NAME = "edina1.mbtiles";



    public static final String ROUTE_CHOSEN_KEY = "ROUTE_CHOSEN_KEY";

    private Utils utils = Utils.getInstance();

    ListView listView;
    FloorPlanChooserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // 1. Access the TextView defined in layout XML
        // and then set its text
        adapter = new FloorPlanChooserAdapter(this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        // copy large offline map in bg task
        new Thread(new Runnable() {
            public void run() {

                utils.copyOfflineMap(MAP_DB_NAME, ChooseFloorPlan.this.getAssets(), ChooseFloorPlan.this.getPackageName());

            }
        }).start();



    }

    private void goToMap(SingleRow row){
        // create an Intent to take you over to a new DetailActivity
        Intent detailIntent = new Intent(this, MainMapView.class);

        detailIntent.putExtra(ROUTE_CHOSEN_KEY, row);

        // TODO: add any other data you'd like as Extras

        // start the next Activity using your prepared Intent
        startActivity(detailIntent);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu.
        // Adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);


        return true;
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        SingleRow row =(SingleRow)adapter.getItem(i);
        goToMap(row);
    }

}

class SingleRow implements Serializable {

    private String title;
    private int imageId;
    private String description;
    private String routeKmlFile;

    public int getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRouteKmlFile() {
        return routeKmlFile;
    }



    SingleRow(int imageId, String title, String description , String routeKmlFile) {
        this.imageId = imageId;
        this.title = title;
        this.description = description;

        this.routeKmlFile = routeKmlFile;
    }
}

class FloorPlanChooserAdapter extends BaseAdapter{

    private final Context context;
    ArrayList<SingleRow> rows = new ArrayList<SingleRow>();

    FloorPlanChooserAdapter(Context context){
        this.context = context;
        Resources resources = context.getResources();
        String[] titles = resources.getStringArray(R.array.titles);
        String[] descriptions = resources.getStringArray(R.array.descriptions);
        String[] routes = resources.getStringArray(R.array.routes);
        int[] images = new int[]{R.drawable.icon1,R.drawable.icon2,R.drawable.icon3,R.drawable.icon4};
        String[] routeKmlFiles = resources.getStringArray(R.array.routes_kml_filenames);
        for(int i =0 ; i< titles.length;i++ ){
            rows.add(new SingleRow(images[i], titles[i], descriptions[i], routeKmlFiles[i]));
        }

    }
;
    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        return rows.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.row_layout, viewGroup, false);
        TextView title = (TextView) row.findViewById(R.id.textTitle);
        TextView description = (TextView) row.findViewById(R.id.textDescription);
        ImageView imageView = (ImageView)row.findViewById(R.id.img_thumbnail);
        SingleRow singleRow = rows.get(i);

        title.setText(singleRow.getTitle());
        description.setText(singleRow.getDescription());
        imageView.setImageResource(singleRow.getImageId());
        return row;
    }
}
