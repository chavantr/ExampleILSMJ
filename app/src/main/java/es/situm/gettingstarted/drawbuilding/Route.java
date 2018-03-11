package es.situm.gettingstarted.drawbuilding;

import java.io.Serializable;

/**
 * Created by Tatyabhau on 3/11/2018.
 */

public class Route implements Serializable {

    private int id;
    private String latitude;
    private String longitude;
    private int routeId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }
}
