/**
 * Immutable city record used by both BST (name order) and KDTree (coordinate order).
 *
 * Ordering for BST is by city name (lexicographic).
 */
public final class City implements Comparable<City> {
    private final String name;
    private final int x;
    private final int y;

    public City(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    /** City name (key for BST ordering). */
    public String getName() {
        return name;
    }

    /** X coordinate. */
    public int getX() {
        return x;
    }

    /** Y coordinate. */
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
