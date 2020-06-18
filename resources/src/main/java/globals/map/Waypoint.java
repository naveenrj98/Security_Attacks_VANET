package globals.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import globals.Vector2D;



public class Waypoint {
    private String name;
    private Vector2D position;
    private List<Waypoint> adjancies = new ArrayList<Waypoint>();

    public Waypoint(double x, double y, String name) {
        this.name = name;
        this.position = new Vector2D(x, y);
    }

    public void addAdjancie(Waypoint waypoint) {
        adjancies.add(waypoint);
    }

    public List<Waypoint> getAdjancies() {
        return adjancies;
    }
    public Vector2D getPosition() {
        return position;
    }
    public String getName() {
        return name;
    }

    public Waypoint getRandomAdjancie() {
        if(adjancies.size() == 1) {
            return adjancies.get(0);
        } else {
            Random randomGenerator = new Random();
            int randomIndex = randomGenerator.nextInt(adjancies.size());
            return adjancies.get(randomIndex);
        }
    }
}