package setup;

import java.io.Serializable;


public class RouteRecord implements Serializable {

    private int routeDistance;
    private int nextHop;

    public RouteRecord(int routeDistance, int nextHop) {
        this.routeDistance = routeDistance;
        this.nextHop = nextHop;
    }

    public String toString() {
        String string;
        string = routeDistance + "," + nextHop;
        return string;
    }

    public int getRouteDistance() {
        return routeDistance;
    }

    public void setRouteDistance(int routeDistance) {
        this.routeDistance = routeDistance;
    }

    public int getNextHop() {
        return nextHop;
    }

    public void setNextHop(int nextHop){
        this.nextHop = nextHop;
    }
}
