//-------------------------------------------------------------------------
/**
 * Interface class for the GIS project
 *
 * @author CS3114/5040 Staff
 * @version Summer 2025
 *
 */
public interface GIS {

    // ----------------------------------------------------------
    /**
     * Reinitialize the database
     * @return True if its cleared, false otherwise
     */
    public boolean clear();

    // ----------------------------------------------------------
    /**
     * A city at coordinate (x, y) with name name is entered into the database.
     * It is an error to insert two cities with identical coordinates,
     * but not an error to insert two cities with identical names.
     * @param name City name.
     * @param x City x-coordinate. Integer in the range 0 to 2^{15} − 1.
     * @param y City y-coordinate. Integer in the range 0 to 2^{15} − 1.
     * @return True iff the city is successfully entered into the database
     */
    public boolean insert(String name, int x, int y);


    // ----------------------------------------------------------
    /**
     * The city with these coordinates is deleted from the database
     * (if it exists).
     * Print the name of the city if it exists.
     * If no city at this location exists, print the empty string.
     * @param x City x-coordinate.
     * @param y City y-coordinate.
     * @return A string with the number of nodes visited during the deletion
     *          followed by the name of the city (this is blank if nothing
     *          was deleted).
     */
    public String delete(int x, int y);


    // ----------------------------------------------------------
    /**
     * The city with this name is deleted from the database (if it exists).
     * If two or more cities have this name, then ALL such cities must be
     * removed.
     * Print the coordinates of each city that is deleted.
     * If no city with this name exists, print the empty string.
     * @param name City name.
     * @return A string with the coordinates of each city that is deleted
     *          (listed in preorder as they are deleted).
     *          Print the empty string if no cites match.
     */
    public String delete(String name);


    // ----------------------------------------------------------
    /**
     * Display the name of the city at coordinate (x, y) if it exists.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return The city name if there is such a city, empty otherwise
     */
    public String info(int x, int y);


    // ----------------------------------------------------------
    /**
     * Display the coordinates of all cities with this name, if any exist.
     * @param name The city name.
     * @return String representing the list of cities and coordinates,
     *          empty if there are none.
     */
    public String info(String name);


    // ----------------------------------------------------------
    /**
     * All cities within radius distance from location (x, y) are listed.
     * A city that is exactly radius distance from the query point should be
     * listed.
     * This operation should be implemented so that as few nodes as possible in
     * the k-d tree are visited.
     * @param x Search circle center: X coordinate. May be negative.
     * @param y Search circle center: X coordinate. May be negative.
     * @param radius Search radius, must be non-negative.
     * @return String listing the cities found (if any) , followed by the count
     *          of the number of k-d tree nodes looked at during the
     *          search process. If the radius is bad, return an empty string.
     *          If k-d tree is empty, the number of nodes visited is zero.
     */
    public String search(int x, int y, int radius);


    // ----------------------------------------------------------
    /**
     * Print a listing of the database as an inorder traversal of the k-d tree.
     * Each city should be printed on a separate line. Each line should start
     * with the level of the current node, then be indented by 2 * level spaces
     * for a node at a given level, counting the root as level 0.
     * @return String listing the cities as specified.
     */
    public String debug();


    // ----------------------------------------------------------
    /**
    /**
     * Print a listing of the BST in alphabetical order (inorder traversal)
     * on the names.
     * Each city should be printed on a separate line. Each line should start
     * with the level of the current node, then be indented by 2 * level spaces
     * for a node at a given level, counting the root as level 0.
     * @return String listing the cities as specified.
     */
    public String print();
}
