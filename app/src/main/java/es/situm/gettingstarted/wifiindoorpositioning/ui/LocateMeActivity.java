package es.situm.gettingstarted.wifiindoorpositioning.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import es.situm.gettingstarted.R;
import io.realm.Realm;
import es.situm.gettingstarted.wifiindoorpositioning.adapter.NearbyReadingsAdapter;
import es.situm.gettingstarted.wifiindoorpositioning.core.Algorithms;
import es.situm.gettingstarted.wifiindoorpositioning.core.WifiService;
import es.situm.gettingstarted.wifiindoorpositioning.model.IndoorProject;
import es.situm.gettingstarted.wifiindoorpositioning.model.LocDistance;
import es.situm.gettingstarted.wifiindoorpositioning.model.LocationWithNearbyPlaces;
import es.situm.gettingstarted.wifiindoorpositioning.model.WifiData;
import es.situm.gettingstarted.wifiindoorpositioning.utils.AppContants;
import es.situm.gettingstarted.wifiindoorpositioning.utils.Utils;


public class LocateMeActivity extends AppCompatActivity {

    private WifiData mWifiData;
    private Algorithms algorithms = new Algorithms();
    private String projectId, defaultAlgo;
    private IndoorProject project;
    private MainActivityReceiver mReceiver = new MainActivityReceiver();
    private Intent wifiServiceIntent;
    private TextView tvLocation, tvNearestLocation, tvDistance;
    private RecyclerView rvPoints;
    private LinearLayoutManager layoutManager;
    private NearbyReadingsAdapter readingsAdapter = new NearbyReadingsAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiData = null;

        // set receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(AppContants.INTENT_FILTER));

        // launch WiFi service
        wifiServiceIntent = new Intent(this, WifiService.class);
        startService(wifiServiceIntent);

        // recover retained object
        mWifiData = (WifiData) getLastNonConfigurationInstance();

        // set layout
        setContentView(R.layout.activity_locate_me);
        initUI();

        defaultAlgo = Utils.getDefaultAlgo(this);
        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(getApplicationContext(), "Project Not Found", Toast.LENGTH_LONG).show();
            this.finish();
        }
        Realm realm = Realm.getDefaultInstance();
        project = realm.where(IndoorProject.class).equalTo("id", projectId).findFirst();
        Log.v("LocateMeActivity", "onCreate");
    }

    private void initUI() {
        layoutManager = new LinearLayoutManager(this);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        tvNearestLocation = (TextView) findViewById(R.id.tv_nearest_location);
        tvDistance = (TextView) findViewById(R.id.tv_distance_origin);
        rvPoints = (RecyclerView) findViewById(R.id.rv_nearby_points);
        rvPoints.setLayoutManager(layoutManager);
        rvPoints.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvPoints.setAdapter(readingsAdapter);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mWifiData;
    }

    public class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v("LocateMeActivity", "MainActivityReceiver");
            mWifiData = (WifiData) intent.getParcelableExtra(AppContants.WIFI_DATA);

            if (mWifiData != null) {
                LocationWithNearbyPlaces loc = Algorithms.processingAlgorithms(mWifiData.getNetworks(), project, Integer.parseInt(defaultAlgo));
                Log.v("LocateMeActivity", "loc:" + loc);
                if (loc == null) {
                    tvLocation.setText("Location: NA\nNote:Please switch on your wifi and location services with permission provided to App");
                } else {
                    String locationValue = Utils.reduceDecimalPlaces(loc.getLocation());
                    tvLocation.setText("Location: " + locationValue);
                    String theDistancefromOrigin = Utils.getTheDistancefromOrigin(loc.getLocation());
                    tvDistance.setText("The distance from stage area is: " + theDistancefromOrigin + "m");
                    LocDistance theNearestPoint = Utils.getTheNearestPoint(loc);
                    if (theNearestPoint != null) {
                        tvNearestLocation.setText("You are near to: " + theNearestPoint.getName());
                    }
                    readingsAdapter.setReadings(loc.getPlaces());
                    readingsAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(wifiServiceIntent);
    }
}
