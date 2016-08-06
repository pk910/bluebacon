package de.dhbw.bluebacon.model;

/**
 * Tupel class for coordinates
 * @param <X> Datatype of X coordinate
 * @param <Y> Datatype of Y coordinate
 */
public class Tuple<X, Y> {
    protected final X x;
    protected final Y y;

    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return this.x;
    }

    public Y getY() {
        return this.y;
    }
}
