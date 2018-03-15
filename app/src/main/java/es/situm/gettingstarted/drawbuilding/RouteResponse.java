package es.situm.gettingstarted.drawbuilding;

import java.util.List;

/**
 * Created by Tatyabhau on 3/15/2018.
 */

public class RouteResponse {

    private Route route;

    private List<RouteTran> routeTrans;


    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }


    public List<RouteTran> getRouteTrans() {
        return routeTrans;
    }

    public void setRouteTrans(List<RouteTran> routeTrans) {
        this.routeTrans = routeTrans;
    }

}
