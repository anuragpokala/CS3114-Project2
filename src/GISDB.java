/**
 * GIS database that coordinates BST and KDTree  * 
 * @author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */
public class GISDB implements GIS {

    private final BST<City> byName;
    private final KDTree byCoord;

    /** The maximum allowable coordinate value. */
    public static final int MAXCOORD = 32767;

    /** Number of coordinate dimensions. Kept for parity with spec. */
    public static final int DIMENSION = 2;

    /**
     * Creates a new GIS database with empty BST and KDTree.
     */
    public GISDB() {
        this.byName = new BST<>();
        this.byCoord = new KDTree();
    }

    /** 
     * Reinitialize the database. 
     * @return boolean after method has been run
     * */
    public boolean clear() {
        byName.clear();
        byCoord.clear();
        return true;
    }

    /**
     * Insert a city. Duplicate coordinates are rejected
     * @return true after method has been completed
     */
    public boolean insert(String name, int x, int y) {
        if (name == null || x < 0 || y < 0 || x > MAXCOORD || y > MAXCOORD) {
            return false;
        }
        boolean added = byCoord.insert(name, x, y);
        if (added) {
            byName.insert(new City(name, x, y));
        }
        return added;
    }

    /**
     * Delete by coordinate. Returns "visited\\nname" if found, else "".
     * For empty kd-tree, returns "" (visited not printed).
     * @return String with result after running method
     */
    public String delete(int x, int y) {
        if (byCoord.isEmpty()) return "";
        KDTree.DeleteOutcome out = byCoord.delete(x, y);
        if (out.entry == null) return "";
        // remove exact (name,x,y) from BST using equals-left semantics
        byName.removeMatching(new City(out.entry.getName(),
            out.entry.getX(), out.entry.getY()),
            c -> c.getX() == out.entry.getX() 
                && c.getY() == out.entry.getY()
                        && c.getName().equals(out.entry.getName()));
        StringBuilder sb = new StringBuilder();
        sb.append(out.visited).append("\n").append(out.entry.getName());
        return sb.toString();
    }

    /**
     * Delete all cities with the given name.
     * Output each deletion line as "name (x, y)\n" in (x,y) order.
     * @return Empty string when name is null or no matches exist.
     */
    public String delete(String name) {
        if (name == null) {
            return "";
        }

        // 1) First pass: count matches (preorder over KDTree)
        final int[] count = new int[] { 0 };
        byCoord.preorderWithLevels((lvl, e) -> {
            if (e.getName().equals(name)) {
                count[0] = count[0] + 1;
            }
        });
        int n = count[0];
        if (n == 0) {
            return "";
        }

        // 2) Second pass: collect coords into parallel arrays
        int[] xs = new int[n];
        int[] ys = new int[n];
        final int[] idx = new int[] { 0 };
        byCoord.preorderWithLevels((lvl, e) -> {
            if (e.getName().equals(name)) {
                xs[idx[0]] = e.getX();
                ys[idx[0]] = e.getY();
                idx[0] = idx[0] + 1;
            }
        });

        // 3) Deterministic order: lexicographic by using insertion sort
        for (int i = 1; i < n; i++) {
            int kx = xs[i];
            int ky = ys[i];
            int j = i - 1;
            while (j >= 0) {
                // compare (kx,ky) < (xs[j],ys[j]) ?
                boolean less = (kx < xs[j]) || (kx == xs[j] && ky < ys[j]);
                if (!less) break;
                xs[j + 1] = xs[j];
                ys[j + 1] = ys[j];
                j--;
            }
            xs[j + 1] = kx;
            ys[j + 1] = ky;
        }

        // 4) Delete each coord; remove exact triple from BST; build output
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            KDTree.DeleteOutcome out = byCoord.delete(xs[i], ys[i]);
            if (out.entry != null) {
                byName.removeMatching(
                    new City(out.entry.getName(), out.entry.getX(), 
                        out.entry.getY()),
                    c -> c.getX() == out.entry.getX()
                      && c.getY() == out.entry.getY()
                      && c.getName().equals(out.entry.getName())
                );
                sb.append(out.entry.getName()).append(" (")
                  .append(out.entry.getX()).append(", ")
                  .append(out.entry.getY()).append(")\n");
            }
        }
        return sb.toString();
    }



    /** Name at coordinate or empty string. 
     *@return  name at specified coordinates
     */
    public String info(int x, int y) {
        City e = byCoord.findExact(x, y);
        return (e != null) ? e.getName() : "";
    }

    /**
     * Coordinates for all cities with the given name, 
     * in BST inorder (equal names appear on the LEFT chain first).
     * @return Empty string when none match.
     */
    public String info(String name) {
        if (name == null) return "";
        final int[] count = new int[] { 0 };
        byName.inorderWithLevels((lvl, c) -> {
            if (c.getName().equals(name)) count[0] = count[0] + 1;
        });
        if (count[0] == 0) return "";

        int[] xs = new int[count[0]];
        int[] ys = new int[count[0]];
        final int[] idx = new int[] { 0 };
        byName.inorderWithLevels((lvl, c) -> {
            if (c.getName().equals(name)) {
                xs[idx[0]] = c.getX();
                ys[idx[0]] = c.getY();
                idx[0] = idx[0] + 1;
            }
        });

        StringBuilder sb = new StringBuilder();
        for (int i = xs.length - 1; i >= 0; i = i - 1) {
            sb.append(name).append(" (").append(xs[i])
            .append(", ").append(ys[i]).append(")").append("\n");
        }
        return sb.toString();
    }

    /**
     * Performs a circular range search centered at the given coordinates.
     * Returns all matching city listings (if any), followed by the number
     * of nodes visited during the search. If the radius is negative, an
     * empty string is returned.
     *
     * @param x       the x-coordinate of the search center
     * @param y       the y-coordinate of the search center
     * @param radius  the search radius (must be non-negative)
     * @return a string containing the matching city listings (one per line),
     *         followed by the visit count; or an empty string if the radius
     *         is invalid
     */
    public String search(int x, int y, int radius) {
        if (radius < 0) {
            return "";
        }
        KDTree.SearchOutcome res = byCoord.rangeSearch(x, y, radius);
        StringBuilder sb = new StringBuilder();
        sb.append(res.listing);
        sb.append(res.visited);
        return sb.toString();
    }


    /**
     * Returns an inorder listing of the kd-tree.
     * Each line starts with the node's level, followed by 2*level spaces,
     * then the city information in the format "name x y".
     *
     * @return a string containing the kd-tree nodes in inorder, one per line
     */
    public String debug() {
        StringBuilder sb = new StringBuilder();
        byCoord.inorderWithLevels((level, e) -> {
            sb.append(level);
            for (int i = 0; i < 2 * level; i++) {
                sb.append(" ");
            }
            sb.append(e.getName())
              .append(" ")
              .append(e.getX())
              .append(" ")
              .append(e.getY())
              .append("\n");
        });
        return sb.toString();
    }

    /**
     * Returns an inorder listing of the BST by city name.
     * Each line starts with the node's level, followed by 2*level spaces,
     * then the city information in the format "name (x, y)".
     *
     * @return a string containing the BST nodes in inorder, one per line
     */
    public String print() {
        StringBuilder sb = new StringBuilder();
        byName.inorderWithLevels((level, c) -> {
            sb.append(level);
            for (int i = 0; i < 2 * level; i++) {
                sb.append(" ");
            }
            sb.append(c.getName())
              .append(" (")
              .append(c.getX())
              .append(", ")
              .append(c.getY())
              .append(")")
              .append("\n");
        });
        return sb.toString();
    }

}
