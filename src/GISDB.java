/**
 * Implementation of the GIS interface. This is what calls the BST and the
 * Bintree to do the work.
 *
 * @author Parth Mehta
 * @version 09/30/2025
 *
 */
public class GISDB implements GIS {

    /**
     * The maximum allowable value for a coordinate
     */
    public static final int MAXCOORD = 32767;

    /**
     * Dimension of the points stored in the tree
     */
    public static final int DIMENSION = 2;

    private BST nameIndex;
    private KDTree locationIndex;

    // ----------------------------------------------------------
    /**
     * Create a GISDB object.
     */
    GISDB() {
        nameIndex = new BST();
        locationIndex = new KDTree();
    }


    // ----------------------------------------------------------
    /**
     * Reinitialize the database
     * 
     * @return True if the database has been cleared
     */
    public boolean clear() {
        nameIndex = new BST();
        locationIndex = new KDTree();
        return true;
    }


    // ----------------------------------------------------------
    /**
     * A city at coordinate (x, y) with name name is entered into the database.
     * It is an error to insert two cities with identical coordinates,
     * but not an error to insert two cities with identical names.
     * 
     * @param name
     *            City name.
     * @param x
     *            City x-coordinate. Integer in the range 0 to 2^{15} − 1.
     * @param y
     *            City y-coordinate. Integer in the range 0 to 2^{15} − 1.
     * @return True iff the city is successfully entered into the database
     */
    public boolean insert(String name, int x, int y) {
        if (x < 0 || y < 0 || x > MAXCOORD || y > MAXCOORD) {
            return false;
        }

        // Reject exact duplicate coordinates per spec
        if (locationIndex.contains(x, y)) {
            return false;
        }

        City city = new City(name, x, y);
        nameIndex.insert(city);
        locationIndex.insert(city);
        return true;
    }


    // ----------------------------------------------------------
    /**
     * The city with these coordinates is deleted from the database
     * (if it exists).
     * Print the name of the city if it exists.
     * If no such city at this location exist, print that.
     * 
     * @param x
     *            City x-coordinate.
     * @param y
     *            City y-coordinate.
     * @return A string with the number of nodes visited during the deletion
     *         followed by the name of the city (this is blank if nothing
     *         was deleted).
     */
    @Override
    public String delete(int x, int y) {
        if (locationIndex.getSize() == 0) {
            return "";
        }
        KDTree.KDDeleteResult res = locationIndex.deleteExact(x, y);
        if (res.removed == null) {
            return String.valueOf(res.visited);
        }
        nameIndex.removeCity(res.removed);
        return res.visited + " " + res.removed.getName();
    }



    // ----------------------------------------------------------
    /**
     * The city with this name is deleted from the database (if it exists).
     * If two or more cities have this name, then ALL such cities must be
     * removed.
     * Print the coordinates of each city that is deleted.
     * If no such city at this location exists, print that.
     * 
     * @param name
     *            City name.
     * @return A string with the coordinates of each city that is deleted
     *         (listed in preorder as they are deleted).
     */
    public String delete(String name) {
        StringBuilder sb = new StringBuilder();
        while (true) {
            City next = nameIndex.findFirstByNamePreorder(name);
            if (next == null) {
                break;
            }
            KDTree.KDDeleteResult res = locationIndex.deleteExact(next.getX(), next.getY());
            nameIndex.removeCity(next);
            sb.append("(").append(next.getX()).append(", ").append(next.getY()).append(")\n");
        }
        int len = sb.length();
        if (len == 0) {
            return "";
        }
        if (sb.charAt(len - 1) == '\n') {
            sb.setLength(len - 1);
        }
        return sb.toString();
    }


    // ----------------------------------------------------------
    /**
     * Display the name of the city at coordinate (x, y) if it exists.
     * 
     * @param x
     *            X coordinate.
     * @param y
     *            Y coordinate.
     * @return The city name if there is such a city, empty otherwise
     */
    public String info(int x, int y) {
        City c = locationIndex.find(x, y);
        return (c == null) ? "" : c.getName();
    }


    // ----------------------------------------------------------
    /**
     * Display the coordinates of all cities with this name, if any exist.
     * 
     * @param name
     *            The city name.
     * @return String representing the list of cities and coordinates,
     *         empty if there are none.
     */
    public String info(String name) {
        String out = nameIndex.listCoordsByName(name);
        if (out == null || out.isEmpty()) {
            return "";
        }
        out = out.replace("\r\n", "\n").replace("\r", "\n");
        int end = out.length();
        while (end > 0 && out.charAt(end - 1) == '\n') {
            end--;
        }
        return (end == out.length()) ? out : out.substring(0, end);
    }


 // ----------------------------------------------------------
    /**
     * All cities within radius distance from location (x, y) are listed,
     * followed by the number of k-d tree nodes visited during the search.
     * If the radius is negative, returns the empty string. If the tree is
     * empty, returns "0".
     * 
     * Cities on the exact circle boundary are included.
     * 
     * @param x Search circle center X.
     * @param y Search circle center Y.
     * @param radius Non-negative radius.
     * @return Listing of cities (each on its own line) followed by the
     *         visited count; or "0" if none and tree empty; or "" if radius
     *         is invalid.
     */
    public String search(int x, int y, int radius) {
        if (radius < 0) {
            return "";
        }
        if (locationIndex.getSize() == 0) {
            return "0";
        }

        KDTree.KDSearchResult res = locationIndex.rangeSearch(x, y, radius);
        String hits = res.listing();
        if (hits.isEmpty()) {
            return String.valueOf(res.visited);
        }
        return hits + "\n" + res.visited;
    }



    // ----------------------------------------------------------
    /**
     * Print a listing of the database as an inorder traversal of the k-d tree.
     * Each city should be printed on a separate line. Each line should start
     * with the level of the current node, then be indented by 2 * level spaces
     * for a node at a given level, counting the root as level 0.
     * 
     * @return String listing the cities as specified.
     */
    public String debug() {
        return locationIndex.toString();
    }


    // ----------------------------------------------------------
    /**
     * Print a listing of the BST in alphabetical order (inorder traversal)
     * on the names.
     * Each city should be printed on a separate line. Each line should start
     * with the level of the current node, then be indented by 2 * level spaces
     * for a node at a given level, counting the root as level 0.
     * 
     * @return String listing the cities as specified.
     */
    public String print() {
        return nameIndex.toString();
    }

}
