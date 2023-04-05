package setup;

import java.io.Serializable;


public class RouteRecord implements Serializable {
    //TODO: This class may be implemented as a "helper class" for helping implement the Table class
    // This class is optional: if you do not need this class, you can keep it empty

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
}
