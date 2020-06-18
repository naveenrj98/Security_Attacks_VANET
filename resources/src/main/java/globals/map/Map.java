package globals.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import globals.Vector2D;



public class Map {
    private List<Waypoint> waypoints = new ArrayList<Waypoint>();

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public Waypoint getRandomWaypoint() {
        Random randomGenerator = new Random();
        int randomIndex = randomGenerator.nextInt(waypoints.size());
        return waypoints.get(randomIndex);
    }
    public Waypoint getClosestWaypoint(Vector2D pos) {
        Waypoint min = null;
        double minDistance = Double.MAX_VALUE;

        for(Waypoint w: waypoints) {
            double currentDistance = pos.distance(w.getPosition());
            if(currentDistance < minDistance) {
                min = w;
                minDistance = currentDistance;
            }
        }
        return min;
    }

    public void addWaypoint(Waypoint w) {
        waypoints.add(w);
    }

}