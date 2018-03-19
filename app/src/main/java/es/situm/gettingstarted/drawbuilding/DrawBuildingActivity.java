package es.situm.gettingstarted.drawbuilding;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import es.situm.gettingstarted.drawpois.GetPoisUseCase;
import es.situm.gettingstarted.wifiindoorpositioning.ui.HomeActivity;
import es.situm.gettingstarted.wifiindoorpositioning.ui.NewProjectActivity;
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
import es.situm.gettingstarted.wifiindoorpositioning.ui.ProjectDetailActivity;

public class DrawBuildingActivity
        extends AppCompatActivity
        implements OnMapReadyCallback, OnResultListener {


    private GoogleMap map;
    private ProgressBar progressBar;
    private GetBuildingImageUseCase getBuildingImageUseCase = new GetBuildingImageUseCase();
    private GetPoisUseCase getPoisUseCase = new GetPoisUseCase();
    private LocationManager locationManager;
    private final int ACCESS_FINE_LOCATION_REQUEST_CODE = 3096;
    private LocationListener locationListener;
    private Circle circle;
    private Button findRoute;
    private static final int FIND_ROUTE = 779;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_building);
        setup();
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
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        map = googleMap;


        //drawRoute();

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


        //getPois(map);

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
                //hideProgress();
                Toast.makeText(DrawBuildingActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
            startLocation();
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
                LatLng latLng = new LatLng(location.getCoordinate().getLatitude(),
                        location.getCoordinate().getLongitude());
                if (circle == null) {
                    circle = map.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(1d)
                            .strokeWidth(0f)
                            .fillColor(Color.BLUE));
                } else {
                    circle.setCenter(latLng);
                }
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
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
                            // hideProgress();
                            Toast.makeText(DrawBuildingActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onError(String error) {
                //hideProgress();
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
                        map.addMarker(new MarkerOptions().position(latLng)
                                .title(jPoint.getString("Name")));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == FIND_ROUTE) {
                GettingStartedApplication gettingStartedApplication = (GettingStartedApplication) getApplicationContext();
                if (null != gettingStartedApplication && gettingStartedApplication.getRouteResponse() != null) {
                    List<LatLng> route = new ArrayList<>();
                    if (null != gettingStartedApplication.getRouteResponse().getRoute()) {
                        route.add(new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLongitude())));
                        LatLng latLng = new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getsLongitude()));
                        if (circle == null) {
                            circle = map.addCircle(new CircleOptions()
                                    .center(latLng)
                                    .radius(1d)
                                    .strokeWidth(0f)
                                    .fillColor(Color.CYAN));
                        } else {
                            circle.setCenter(latLng);
                        }
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 25));
                        if (null != gettingStartedApplication.getRouteResponse().getRouteTrans() && gettingStartedApplication.getRouteResponse().getRouteTrans().size() > 0) {
                            for (int i = 0; i < gettingStartedApplication.getRouteResponse().getRouteTrans().size(); i++) {
                                route.add(new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRouteTrans().get(i).getLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRouteTrans().get(i).getLongitude())));
                            }
                        }
                        route.add(new LatLng(Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getdLatitude()), Double.parseDouble(gettingStartedApplication.getRouteResponse().getRoute().getdLongitude())));
                        map.addPolyline(new PolylineOptions().addAll(route).color(Color.BLUE).width(7));
                    }
                }
            }
        }
    }
}
