package setup;

import java.io.Serializable;
import java.util.Map;


//This class represents the distance vector that a router initiates, optimizes, and exchanges with neighbors
public class Table implements Serializable {
 //TODO: add any member variables and member methods.
    //Represents <Starting point router, Map<Destination router, cost to travel>
    private Map<Integer, Map<Integer, Integer>> distanceVector;

    @Override
    public String toString() {
        //TODO: you must complete the toString() method to print out the content of the distance vector
    }

}
