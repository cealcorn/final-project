package setup;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Router {
    private final int _Id;
    private final int _port;
    private final JSONTool _jsonTool;

    // maximum size of a UDP packet allowed
    private final int COMM_BYTE_SIZE = 1048;

    // the router's own distance vector
    private final Table _table = new Table();

    // a list of all neighbors
    private final List<Integer> _neighborIds = new ArrayList<>();

    private final DatagramSocket _datagramSocket;


    /*
       The constructor starts the router:
          - it initializes the distance vector;
	  - it populates the list of neighbors, and
          - it sends out the initial distance vector to all neighbors;
       The constructor also starts a thread to periodically send out its distance vector to all neighbors (keepalive)
     */
    public Router(int routerNumber) throws Exception {
        _Id = routerNumber;
        try {
            _jsonTool = new JSONTool(routerNumber);
        } catch (IOException e) {
            throw new Error(e.getMessage());
        }
        // Get and initialize neighbor links
        List<Link> links = _jsonTool.getLinks();
        initializeTable(links);
        initializeNeighbors(links);

        // Get self port
        _port = _jsonTool.getPort();

        // Create a DataGramSocket to listen for communication
        _datagramSocket = new DatagramSocket(_port);

        sendTablePeriodically(5, 10);
    }

    
    // This method keeps the router running by executing an infinite loop
    @SuppressWarnings("InfiniteLoopStatement")
    public void runRouter() throws Exception{
        while (true) {
            //TODO: implement the distance vector routing protocol
            //Wait to receive a datagram packet from a neighbor. Use datagram socket
            //Create empty datagram packet
            byte[] buffer = new byte[0];
            DatagramPacket request = new DatagramPacket(
                    buffer, // raw data to be encapsulated
                    buffer.length, // size of raw data
                    InetAddress.getLocalHost(), // destination IP address
                    _port // destination port #
            );

            //Send empty packet
            _datagramSocket.send(request);

            //Receive packet
            DatagramPacket response = new DatagramPacket(new byte[COMM_BYTE_SIZE], COMM_BYTE_SIZE);
            _datagramSocket.receive(response);

            //Extract the distance vector table from the received datagram packet
            Table incomingTable = receiveTable(response);
            Table updatedTable = new Table();

            //Use the received DV to optimize our own table. Call optimizeTable
            if(optimizeTable(incomingTable)){
                //Prune table through splitHorizon before sending to neighbors
                //Iterate over neighbor ID's and call splitHorizon
                for(int neighbors: _neighborIds){
                    updatedTable = splitHorizon(neighbors);
                }
                //Send the updated table (our own table) to all neighbors
                sendTable(InetAddress.getLocalHost(), _port, updatedTable);
            }
        }
    }
    /* Private methods */

    private Table splitHorizon(int destinationRouterId) {
        //TODO: implement the split horizon rule technique, as follows:
        //      before sending the distance vector to a neighbor,
        //      remove all the entries for which the neighbor is used as the next hop
        //      (Note that you should first replicate the distance vector, then perform
        //       the removals on the copy, and then return the pruned copy.)

        Table dvCopy = new Table(_table);
        for (Map.Entry<Integer, RouteRecord> entry : dvCopy.getDistanceVector().entrySet()) {
            int dest = entry.getKey();
            int nextHop = entry.getValue().getNextHop();

            if (nextHop == destinationRouterId) {
                dvCopy.getDistanceVector().remove(dest);
            }
        }

        return dvCopy;
    }


    // This method is called whenever a distance vector is received from a neighbor.
    private boolean optimizeTable(Table incomingTable){
        //TODO: complete this method by implementing the Bellman-Ford algorithm
        // Note that this method should return true if the optimization is successful ( i.e.,
        // at least one entry of the router's own distance vector has been optimized.)
        // Otherwise, if the router's own distance vector remains unchanged after the optimization attempt,
        // this method should return false.
        boolean optimized = false;
        Map<Integer, RouteRecord> distanceVector = _table.getDistanceVector();
        for(Map.Entry<Integer, RouteRecord> entry: distanceVector.entrySet()){
            int destination = entry.getKey();
            RouteRecord record = entry.getValue();
            int oldCost = record.getRouteDistance();

            for(int neighbors: _neighborIds){
                int newCost = incomingTable.getDistanceVector().get(neighbors).getRouteDistance();
                if(newCost < oldCost){
                    record.setRouteDistance(newCost);
                    record.setNextHop(neighbors);
                    optimized = true;
                }
            }
        }
        return optimized;
    }

    private void initializeTable(List<Link> links) {
        for (Link l : links) {
            if (l.connectingRouterId().get(0) == _Id) {
                _table.addEntry(l.connectingRouterId().get(1), new RouteRecord(l.weight(), l.connectingRouterId().get(1)));
            } else {
                _table.addEntry(l.connectingRouterId().get(0), new RouteRecord(l.weight(), l.connectingRouterId().get(0)));
            }
        }
    }

    private void initializeNeighbors(List<Link> links) {
        for (Link l : links) {
            if (l.connectingRouterId().get(0) == _Id) {
                _neighborIds.add(l.connectingRouterId().get(1));
            } else {
                _neighborIds.add(l.connectingRouterId().get(0));
            }
        }

    }

    /* BELOW METHOD SHOULD NOT NEED CHANGED */

    /**
     * Receives table from incoming DatagramPacket
     * @param dgp DatagramPacket
     * @return Table
     * @throws Exception
     */
    private Table receiveTable(DatagramPacket dgp) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(dgp.getData(), 0, dgp.getLength());
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        return (Table) objectInputStream.readObject();
    }

    /**
     * Sends table to specified router
     * @param IP
     * @param port
     * @param table
     * @throws Exception
     */
    private void sendTable(InetAddress IP, int port, Table table) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(COMM_BYTE_SIZE);
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        oos.writeObject(table);
        if (outputStream.size() > COMM_BYTE_SIZE) throw new Exception("Message too large");
        _datagramSocket.send(new DatagramPacket(outputStream.toByteArray(), outputStream.size(), IP, port));
    }

    /**
     * Sends the _table member variable every {interval} seconds
     * @param delay
     * @param interval
     */
    private void sendTablePeriodically(int delay, int interval) {
        Runnable helloRunnable = () -> {
            for (int neighborId : _neighborIds) {
                try {
                    InetAddress otherAddress = _jsonTool.getIPById(neighborId);
                    int otherPort = _jsonTool.getPortById(neighborId);
                    sendTable(otherAddress, otherPort, _table);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, delay, interval, TimeUnit.SECONDS);
    }
}
