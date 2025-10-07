/**
 * City class - stores city data
 * @author Parth Mehta
 * @version 10/3/2025
 */
public class City {
    private String name;
    private int x;
    private int y;
    
    /**
     * Constructor
     * @param name city name
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public City(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get city name
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get x-coordinate
     * @return x
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get y-coordinate
     * @return y
     */
    public int getY() {
        return y;
    }
    
    /**
     * String representation
     * @return formatted city info
     */
    @Override
    public String toString() {
        return name + " (" + x + ", " + y + ")";
    }
    
    /**
     * Check equality based on name and coordinates
     * @param obj object to compare
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        City other = (City) obj;
        return x == other.x && 
               y == other.y && 
               name.equals(other.name);
    }
}