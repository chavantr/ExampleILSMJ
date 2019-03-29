package es.situm.gettingstarted.drawbuilding;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Criteria;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import es.situm.gettingstarted.GettingStartedApplication;
import es.situm.gettingstarted.R;
import es.situm.gettingstarted.drawbuilding.models.UserInfoHolder;
import es.situm.gettingstarted.drawbuilding.router.BackgroundDetectedActivitiesService;
import es.situm.gettingstarted.drawbuilding.router.RouterConstants;
import es.situm.gettingstarted.drawpois.GetPoisUseCase;
import es.situm.gettingstarted.realtime.RealTimeActivity;
import es.situm.gettingstarted.wifiindoorpositioning.ui.HomeActivity;
import es.situm.sdk.SitumSdk;
import es.situm.sdk.directions.DirectionsRequest;
import es.situm.sdk.error.Error;
import es.situm.sdk.location.LocationListener;
import es.situm.sdk.location.LocationManager;
import es.situm.sdk.location.LocationRequest;
import es.situm.sdk.location.LocationStatus;
import es.situm.sdk.model.cartography.Building;
import es.situm.sdk.model.cartography.Poi;
import es.situm.sdk.model.cartography.Point;
import es.situm.sdk.model.directions.Route;
import es.situm.sdk.model.location.Bounds;
import es.situm.sdk.model.location.Coordinate;
import es.situm.sdk.model.location.Location;
import es.situm.sdk.model.navigation.NavigationProgress;
import es.situm.sdk.navigation.NavigationListener;
import es.situm.sdk.navigation.NavigationRequest;
import es.situm.sdk.utils.Handler;

public class DrawBuildingActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, OnResultListener, OnGetCurrentLocationListener {

    private static final int SELECT_TEACHER_LOCATION = 764;
    private GoogleMap map;
    private ProgressBar progressBar;
    private GetBuildingImageUseCase getBuildingImageUseCase = new GetBuildingImageUseCase();
    private GetPoisUseCase getPoisUseCase = new GetPoisUseCase();
    private LocationManager locationManager;
    private final int ACCESS_FINE_LOCATION_REQUEST_CODE = 3096;
    private LocationListener locationListener;
    private Circle circle;
    private Circle circleTLocation;
    private Circle circleTCLocation;
    private Button findRoute;
    private int walking = -1;
    private static final int FIND_ROUTE = 779;
    private static final int TEACHER_LOGIN = 799;
    private Polyline polyLine;
    private ProgressDialogUtil progressDialogUtil;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_building);
        setup();
        progressDialogUtil = new ProgressDialogUtil(this);
        findRoute = (Button) findViewById(R.id.btnFindRoute);

        findRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrawBuildingActivity.this, FindRouteActivity.class);
                startActivityForResult(intent, FIND_ROUTE);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setup(0);

        int minTime = 60000;
        // The minimum distance (in meters) traveled until you will be notified
        float minDistance = 15;

        MyLocationListener myLocListener = new MyLocationListener();

        android.location.LocationManager locationManagerL = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(true);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(RouterConstants.BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    handleUserActivity(type, confidence);
                }
            }
        };

// Get the best provider from the criteria specified, and false to say it can turn the provider on if it isn't already
        String bestProvider = locationManagerL.getBestProvider(criteria, false);
// Request location updates
        //locationManagerL.requestLocationUpdates(bestProvider, minTime, minDistance, myLocListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManagerL.requestLocationUpdates(bestProvider, 0, 0, myLocListener);

    }


    private Location lastLocation;

    private void handleUserActivity(int type, int confidence) {

        Log.d("test", "test" + type);

        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                break;
            }
            case DetectedActivity.ON_FOOT: {
                break;
            }
            case DetectedActivity.RUNNING: {
                break;
            }
            case DetectedActivity.STILL: {
                break;
            }
            case DetectedActivity.TILTING: {
                break;
            }
            case DetectedActivity.WALKING: {
                calculateProbability();
                break;
            }
            case DetectedActivity.UNKNOWN: {
                break;
            }
        }
    }

    private void calculateProbability() {
        if (null != routeLatLnt && !routeLatLnt.isEmpty()) {
            if (walking > routeLatLnt.size()) {
                stopTracking();
            }
            do {
                if (null != circle) {
                    circle.remove();
                }
                walking = walking + 1;
                LatLng latLntN = new LatLng(routeLatLnt.get(walking).latitude, routeLatLnt.get(walking).longitude);
                circle = map.addCircle(new CircleOptions()
                        .center(latLntN)
                        .radius(1d)
                        .strokeWidth(1f)
                        .zIndex(1.0f)
                        .fillColor(Color.RED));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLntN, 30));
            } while (walking <= routeLatLnt.size());
        }
    }

    @Override
    public void onGetCurrentLocationSuccess(LatLng location) {
        progressDialogUtil.hide();
        circle = map.addCircle(new CircleOptions()
                .center(location)
                .radius(1d)
                .strokeWidth(1f)
                .zIndex(1.0f)
                .fillColor(Color.RED));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 30));
    }

    private class MyLocationListener implements android.location.LocationListener {

        @Override
        public void onLocationChanged(android.location.Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    }


    @Override
    protected void onDestroy() {
        getBuildingImageUseCase.cancel();
        super.onDestroy();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        checkPermissions();
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        map = googleMap;
        getBuildingImageUseCase.get(new GetBuildingImageUseCase.Callback() {
            @Override
            public void onSuccess(Building building, Bitmap bitmap) {
                progressBar.setVisibility(View.GONE);
                drawBuilding(building, bitmap);
            }

            @Override
            public void onError(Error error) {
                Toast.makeText(DrawBuildingActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setup() {
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    void drawBuilding(Building building, Bitmap bitmap) {
        Bounds drawBounds = building.getBounds();
        Coordinate coordinateNE = drawBounds.getNorthEast();
        Coordinate coordinateSW = drawBounds.getSouthWest();
        LatLngBounds latLngBounds = new LatLngBounds(
                new LatLng(coordinateSW.getLatitude(), coordinateSW.getLongitude()),
                new LatLng(coordinateNE.getLatitude(), coordinateNE.getLongitude()));

        map.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromBitmap(bitmap))
                .bearing((float) building.getRotation().degrees())
                .positionFromBounds(latLngBounds));

        initPoints();
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 15));
    }

    private void getPois(final GoogleMap googleMap) {
        getPoisUseCase.get(new GetPoisUseCase.Callback() {
            @Override
            public void onSuccess(Building building, Collection<Poi> pois) {

                if (pois.isEmpty()) {
                    Toast.makeText(DrawBuildingActivity.this, "There isnt any poi in the building: " + building.getName() + ". Go to the situm dashboard and create at least one poi before execute again this example", Toast.LENGTH_LONG).show();
                } else {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Poi poi : pois) {
                        LatLng latLng = new LatLng(poi.getCoordinate().getLatitude(),
                                poi.getCoordinate().getLongitude());
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(poi.getName()));
                        builder.include(latLng);
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DrawBuildingActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.setting) {
            Intent intent = new Intent(DrawBuildingActivity.this, HomeActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.teacher) {
            Intent intent = new Intent(DrawBuildingActivity.this, LoginActivity.class);
            startActivityForResult(intent, TEACHER_LOGIN);
            return true;
        } else if (id == R.id.view_teacher) {
            Intent intent = new Intent(DrawBuildingActivity.this, ShowTeacherActivity.class);
            startActivityForResult(intent, SELECT_TEACHER_LOCATION);
            return true;
        } else if (id == R.id.view_path) {
            Intent intent = new Intent(DrawBuildingActivity.this, RealTimeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setup(int i) {
        locationManager = SitumSdk.locationManager();
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(DrawBuildingActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DrawBuildingActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermissionsNeeded();
            } else {
                requestPermission();
            }
        } else {
            //startLocation();
        }
    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(DrawBuildingActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_FINE_LOCATION_REQUEST_CODE);
    }


    private void showPermissionsNeeded() {
        Snackbar.make(findViewById(android.R.id.content),
                "Needed location permission to enable service",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("Open", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPermission();
                    }
                }).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    startLocation();
                } else {
                    showPermissionsNeeded();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void startLocation() {
        if (locationManager.isRunning()) {
            return;
        }
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(@NonNull Location location) {
                progressBar.setVisibility(View.GONE);
                LatLng latLng1 = new LatLng(location.getCoordinate().getLatitude(),
                        location.getCoordinate().getLongitude());

                if (null != routeLatLnt && position > routeLatLnt.size() - 1) {
                    position = 0;
                }


                if (null == lastLocation) {
                    lastLocation = location;
                }


                if (counter == 23) {
                    if (null != circle) {
                        circle.remove();
                    }
                }


                counter = counter + 1;

                if (counter < 25) {


                    if (null != routeLatLnt && position <= routeLatLnt.size()) {


                        if (counter >= 24) {

                            position = position + 1;

                            if (position >= routeLatLnt.size()) {
                                position = 0;
                            }

                            if (null != routeLatLnt && !routeLatLnt.isEmpty()) {

                                latLng1 = new LatLng(routeLatLnt.get(position).latitude, routeLatLnt.get(position).longitude);
                            } else {
                                latLng1 = new LatLng(location.getCoordinate().getLatitude(),
                                        location.getCoordinate().getLongitude());
                            }

                            circle = map.addCircle(new CircleOptions()
                                    .center(latLng1)
                                    .radius(1d)
                                    .strokeWidth(1f)
                                    .zIndex(1.0f)
                                    .fillColor(Color.RED));


                            counter = 0;
                        }
                    }
                }


                lastLocation = location;

            }

            @Override
            public void onStatusChanged(@NonNull LocationStatus locationStatus) {

            }

            @Override
            public void onError(@NonNull Error error) {
                Toast.makeText(DrawBuildingActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        LocationRequest locationRequest = new LocationRequest.Builder()
                .useWifi(true)
                .useBle(true)
                .useForegroundService(true)
                .build();

        locationManager.requestLocationUpdates(locationRequest, locationListener);
    }

    private void startNav(Route route) {
        NavigationRequest navigationRequest = new NavigationRequest.Builder()
                .route(route)
                .distanceToGoalThreshold(3d)
                .outsideRouteThreshold(50d)
                .build();
        SitumSdk.navigationManager().requestNavigationUpdates(navigationRequest, new NavigationListener() {
            @Override
            public void onDestinationReached() {
                //Log.d(TAG, "onDestinationReached: ");
            }

            @Override
            public void onProgress(NavigationProgress navigationProgress) {
                //Log.d(TAG, "onProgress: ");
            }

            @Override
            public void onUserOutsideRoute() {
                // Log.d(TAG, "onUserOutsideRoute: ");
            }
        });
    }


    private void init() {
        progressDialogUtil.show();
        GetCurrentLocationAsync getCurrentLocationAsync = new GetCurrentLocationAsync();
        getCurrentLocationAsync.setOnGetCurrentLocationListener(this);
    }


    private void stopLocation() {
        if (!locationManager.isRunning()) {
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    private void drawRoute() {
        getPoisUseCase.get(new GetPoisUseCase.Callback() {
            @Override
            public void onSuccess(Building building, Collection<Poi> pois) {
                if (pois.size() < 2) {
                    Toast.makeText(DrawBuildingActivity.this,
                            "Its mandatory to have at least two pois in a building: " + building.getName() + " to start directions manager",
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    Iterator<Poi> iterator = pois.iterator();
                    final Point from = iterator.next().getPosition();
                    final Point to = iterator.next().getPosition();
                    DirectionsRequest directionsRequest = new DirectionsRequest.Builder()
                            .from(from, null)
                            .to(to)
                            .build();
                    SitumSdk.directionsManager().requestDirections(directionsRequest, new Handler<Route>() {
                        @Override
                        public void onSuccess(Route route) {
                            PolylineOptions polyLineOptions = new PolylineOptions().color(Color.RED).width(4f);
                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            List<Point> routePoints = route.getPoints();
                            for (Point point : routePoints) {
                                LatLng latLng = new LatLng(point.getCoordinate().getLatitude(), point.getCoordinate().getLongitude());
                                builder.include(latLng);
                                polyLineOptions.add(latLng);
                            }
                            builder.include(new LatLng(from.getCoordinate().getLatitude(), from.getCoordinate().getLongitude()));
                            builder.include(new LatLng(to.getCoordinate().getLatitude(), to.getCoordinate().getLongitude()));
                            map.addPolyline(polyLineOptions);

                            startNav(route);
                        }

                        @Override
                        public void onFailure(Error error) {
                            Toast.makeText(DrawBuildingActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DrawBuildingActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initPoints() {
        DrawPointsAsync drawPointsAsync = new DrawPointsAsync();
        drawPointsAsync.setOnResultListener(this, IndoorConstants.URL + IndoorConstants.GET_POINTS);
    }

    @Override
    public void onSuccess(String result) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONArray jsonArrayPoints = new JSONArray(result);
                if (null != jsonArrayPoints && jsonArrayPoints.length() > 0) {
                    for (int i = 0; i < jsonArrayPoints.length(); i++) {
                        JSONObject jPoint = jsonArrayPoints.getJSONObject(i);
                        LatLng latLng = new LatLng(Double.parseDouble(jPoint.getString("Latitude")), Double.parseDouble(jPoint.getString("Longitude")));
                        //map.addMarker(new MarkerOptions().position(latLng)
                        //        .title(jPoint.getString("Name")));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        walking = -1;
        if (resultCode == RESULT_OK) {
            if (requestCode == FIND_ROUTE) {
                counter = 0;
                position = 0;
                GettingStartedApplication gettingStartedApplication = (GettingStartedApplication) getApplicationContext();
                if (null != gettingStartedApplication && gettingStartedApplication.getRouteResponse() != null) {
                    List<LatLng> route = new ArrayList<>();
                    if (null != polyLine) {
                        polyLine.remove();
                    }
                    if (null != gettingStartedApplication.getRouteResponse().getRoute()) {
                        route.add(new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLongitude())));
                        LatLng latLng = new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLongitude()));
                        if (null != circle) {
                            circle.remove();
                        }
                        circle = map.addCircle(new CircleOptions()
                                .center(latLng)
                                .radius(1d)
                                .strokeWidth(1f)
                                .zIndex(1.0f)
                                .fillColor(Color.RED));
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 25));
                        if (null != gettingStartedApplication.getRouteResponse().getRouteTrans() && gettingStartedApplication.getRouteResponse().getRouteTrans().size() > 0) {
                            for (int i = 0; i < gettingStartedApplication.getRouteResponse().getRouteTrans().size(); i++) {
                                route.add(new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRouteTrans().get(i).getLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRouteTrans().get(i).getLongitude())));
                            }
                        }
                        route.add(new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getdLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getdLongitude())));
                        polyLine = map.addPolyline(new PolylineOptions().addAll(route).color(Color.BLUE).width(7));
                        routeLatLnt = route;
                        startTracking();
                    }
                }
            } else if (requestCode == TEACHER_LOGIN) {
                if (null != circleTLocation) {
                    circleTLocation.remove();
                }
                LatLng latLngT = new LatLng(Double.parseDouble(UserInfoHolder.getInstance().getTeacher().getLat()), Double.parseDouble(UserInfoHolder.getInstance().getTeacher().getLng()));
                circleTLocation = map.addCircle(new CircleOptions()
                        .center(latLngT)
                        .radius(0.5d)
                        .strokeWidth(0.5f)
                        .zIndex(1.0f)
                        .fillColor(Color.RED));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngT, 25));
            } else if (requestCode == SELECT_TEACHER_LOCATION) {
                if (null != circleTCLocation) {
                    circleTCLocation.remove();
                }
                LatLng latLngT = UserInfoHolder.getInstance().getTeacherLocation();
                circleTCLocation = map.addCircle(new CircleOptions()
                        .center(latLngT)
                        .radius(0.5d)
                        .strokeWidth(0.5f)
                        .zIndex(1.0f)
                        .fillColor(Color.RED));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLngT, 35));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(RouterConstants.BROADCAST_DETECTED_ACTIVITY));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void startTracking() {
        Intent intent = new Intent(DrawBuildingActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(DrawBuildingActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

    private int position;
    private int counter;
    private List<LatLng> routeLatLnt;
}