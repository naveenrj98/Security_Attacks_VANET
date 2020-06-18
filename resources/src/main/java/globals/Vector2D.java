package globals;

import java.io.Serializable;

/**
 * Two dimensinal vector with double precision
 */
public class Vector2D implements Serializable {
    public double x;
    public double y;
    public static final long serialVersionUID = 0;

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Vector2D other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    public double length() {
        return Math.sqrt((x * x) + (y * y));
    }

    public void normalize() {
        double length = length();
        x = x / length;
        y = y / length;
    }

    public void scale(double size) {
        normalize();
        x = x * size;
        y = y * size;
    }

    public boolean inRange(Vector2D other, double range) {
        return distance(other) <= range;
    }

    public void update(Vector2D velocity, double deltaSeconds) {
        x += velocity.x * deltaSeconds;
	    y += velocity.y * deltaSeconds;
    }

    public Vector2D predictedNext(Vector2D velocity, double delta) {
        Vector2D predicted = new Vector2D(x + (velocity.x * delta), y + (velocity.y * delta));
        return predicted;
    }

    @Override
    public String toString() {
        return "["+Double.toString(this.x)+","+Double.toString(this.y)+"]";
    }

}
