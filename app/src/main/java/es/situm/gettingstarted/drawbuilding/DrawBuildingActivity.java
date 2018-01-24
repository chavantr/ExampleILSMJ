package es.situm.gettingstarted.drawbuilding;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collection;

import es.situm.gettingstarted.R;
import es.situm.gettingstarted.drawpois.GetPoisUseCase;
import es.situm.sdk.error.Error;
import es.situm.sdk.model.cartography.Building;
import es.situm.sdk.model.cartography.Poi;
import es.situm.sdk.model.location.Bounds;
import es.situm.sdk.model.location.Coordinate;

public class DrawBuildingActivity
        extends AppCompatActivity
        implements OnMapReadyCallback {


    private GoogleMap map;
    private ProgressBar progressBar;
    private GetBuildingImageUseCase getBuildingImageUseCase = new GetBuildingImageUseCase();
    private GetPoisUseCase getPoisUseCase = new GetPoisUseCase();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw_building);
        setup();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    protected void onDestroy() {
        getBuildingImageUseCase.cancel();
        super.onDestroy();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
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


        getPois(map);

        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 150));
    }

    private void getPois(final GoogleMap googleMap) {
        getPoisUseCase.get(new GetPoisUseCase.Callback() {
            @Override
            public void onSuccess(Building building, Collection<Poi> pois) {
                //hideProgress();
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
                    // googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                }
            }

            @Override
            public void onError(String error) {
                //hideProgress();
                Toast.makeText(DrawBuildingActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
