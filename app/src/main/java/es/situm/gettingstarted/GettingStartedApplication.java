package es.situm.gettingstarted;

import android.app.Application;
import android.util.Log;

import es.situm.gettingstarted.drawbuilding.RouteResponse;
import es.situm.sdk.SitumSdk;
import io.realm.Realm;

public class GettingStartedApplication extends Application {


    private RouteResponse routeResponse;

    private String scanDestination;


    @Override
    public void onCreate() {
        super.onCreate();

        SitumSdk.init(this);
        Realm.init(this);

    }


    public RouteResponse getRouteResponse() {
        return routeResponse;
    }

    public void setRouteResponse(RouteResponse routeResponse) {
        this.routeResponse = routeResponse;
    }

    public String getScanDestination() {
        return scanDestination;
    }

    public void setScanDestination(String scanDestination) {
        this.scanDestination = scanDestination;
    }
}
