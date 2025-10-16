/**
 * Immutable city record used by BST & KDTree

 * 
 * @author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */
public final class City implements Comparable<City> {
    private final String name;
    private final int x;
    private final int y;

    /**
     * Creates a new City object with the specified name and coordinates
     *
     * @param name the name of the city
     * @param x    the x-coordinate of the city
     * @param y    the y-coordinate of the city
     */
    public City(String name, int x, int y) 
    {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the name of the city
     *
     * @return the name of the city
     */
    public String getName() 
    {
        return name;
    }


    /**
     * Returns the x coordinate
     *
     * @return x-coord
     */
    public int getX() {
        return x;
    }

    /**
     * Returns y coord
     *
     * @return y-coord
     */
    public int getY() {
        return y;
    }

    /** In BST we order by name only. */
    @Override
    public int compareTo(City other) {
        return this.name.compareTo(other.name);
    }

    /** Convenience string used by debug/print paths. */
    @Override
    public String toString() {
        return name + " (" + x + ", " + y + ")";
    }

    /** Exact triple equality: same name and coordinates. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof City)) return false;
        City o = (City)obj;
        return x == o.x && y == o.y && name.equals(o.name);
    }

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
