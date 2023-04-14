package setup;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;


//This class represents the distance vector that a router initiates, optimizes, and exchanges with neighbors
public class Table implements Serializable {
    //Represents <Starting point router, Map<Destination router, cost to travel>
    private Map<Integer, RouteRecord> distanceVector;

    public void addEntry(int destination, RouteRecord routeRecord) {
        // add both parameters to HashMap
        distanceVector.put(destination, routeRecord);
    }

    public Table() {
        distanceVector = new HashMap<>();
    }

    public Table(Table table) {
        this.distanceVector = new HashMap<>(table.getDistanceVector());
    }

    public Map<Integer, RouteRecord> getDistanceVector() {
        return distanceVector;
    }

    @Override
    public String toString() {
//        +-----------------------------------+
//        | Dest      | Cost      | Next Hop  |
//        +-----------+-----------+-----------+
//        |     x     |     x     |     x     |
//        |     x     |     x     |     x     |
//        |     x     |     x     |     x     |
//        |     x     |     x     |     x     |
//        +-----------------------------------+

        // set variables
        StringBuilder builder = new StringBuilder();
        String top_bot = "+-----------------------------------+\n";
        String labels  = "| Dest      | Cost      | Next Hop  |\n";
        String divider = "+-----------+-----------+-----------+\n";
//        String row     = "|     %d     |     %d     |     %d     |";
        RouteRecord temp;
        String temp2;
        String[] tempArray;
        int key, value1, value2;

        // build table
        builder.append(top_bot).append(labels).append(divider);

        for (int i : distanceVector.keySet()) {
            // per row
            key = i;
            temp = distanceVector.get(i);
            temp2 = temp.toString();
            tempArray = temp2.split(",");
            value1 = Integer.parseInt(tempArray[0]);
            value2 = Integer.parseInt(tempArray[1]);

            builder.append(String.format("|     %d     |     %d     |     %d     |\n" , i, value1, value2));
        }

        builder.append(top_bot);

        return builder.toString();
    }

}
