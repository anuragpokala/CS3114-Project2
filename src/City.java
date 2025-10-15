/**
 * City record used by BST and KDTree
 *
 * @author Parth Mehta
 * @author Anurag Pokala
 * @version 10-14-2025
 */
public final class City implements Comparable<City> {

    private final String name;
    private final int x;
    private final int y;

    /**
     * Constructs a new City with the given name and coordinates.
     *
     * @param name the name of the city
     * @param x the x-coordinate of the city
     * @param y the y-coordinate of the city
     */
    public City(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the city name (key for BST ordering).
     *
     * @return the name of this city
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the x-coordinate of this city.
     *
     * @return the x-coordinate value
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of this city.
     *
     * @return the y-coordinate value
     */
    public int getY() {
        return y;
    }

    /**
     * Compares this city to another city by name only
     * (used for BST ordering).
     *
     * @param other the other city to compare with
     * @return a negative integer, zero, or a positive integer as this
     *         city's name is lexicographically less than, equal to,
     *         or greater than the other city's name
     */
    @Override
    public int compareTo(City other) {
        return this.name.compareTo(other.name);
    }

    /**
     * Returns a string representation of the city in the format:
     * "name (x, y)".
     *
     * @return a string representing this city
     */
    @Override
    public String toString() {
        return name + " (" + x + ", " + y + ")";
    }

    /**
     * Checks equality between this city and another object.
     * Two cities are equal if their names and coordinates
     * (x and y) are identical.
     *
     * @param obj the object to compare with
     * @return true if both represent the same city,
     *         false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof City)) {
            return false;
        }
        City o = (City) obj;
        return x == o.x && y == o.y && name.equals(o.name);
    }

    /**
     * Returns a hash code for this city.
     *
     * @return a hash code value for this city
     */
    @Override
    public int hashCode() {
        // Not required by the spec, but harmless if present.
        int h = 17;
        h = 31 * h + name.hashCode();
        h = 31 * h + x;
        h = 31 * h + y;
        return h;
    }
}
