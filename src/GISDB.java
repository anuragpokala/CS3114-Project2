/**
 * GIS database façade that coordinates BST (by name) and KDTree (by coordinates).
 * - insert: rejects duplicate coordinates; allows duplicate names
 * - delete(x,y): returns visit count + name or "" if not found
 * - delete(name): removes ALL with that name; outputs coordinates per deletion
 * - info(x,y): name at coordinate or ""
 * - info(name): list of coordinates (inorder by name-BST) or ""
 * - search(x,y,r): circle range listing then visit count; "" on bad r
 * - debug(): inorder of kd tree (level + 2*level spaces + "name x y")
 * - print(): inorder of BST by name (level + 2*level spaces + "name (x, y)")
 * 
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

    GISDB() {
        this.byName = new BST<>();
        this.byCoord = new KDTree();
    }

    /** Reinitialize the database. */
    public boolean clear() {
        byName.clear();
        byCoord.clear();
        return true;
    }

    /**
     * Insert a city. Duplicate coordinates are rejected; duplicate names allowed.
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
     */
    public String delete(int x, int y) {
        if (byCoord.isEmpty()) return "";
        KDTree.DeleteOutcome out = byCoord.delete(x, y);
        if (out.entry == null) return "";
        // remove exact (name,x,y) from BST using equals-left semantics
        byName.removeMatching(new City(out.entry.getName(), out.entry.getX(), out.entry.getY()),
                c -> c.getX() == out.entry.getX() && c.getY() == out.entry.getY()
                        && c.getName().equals(out.entry.getName()));
        StringBuilder sb = new StringBuilder();
        sb.append(out.visited).append("\n").append(out.entry.getName());
        return sb.toString();
    }

    /**
     * Delete all cities with the given name.
     * Output each deletion line as "name (x, y)\n" in lexicographic (x,y) order.
     * Returns empty string when name is null or no matches exist.
     */
    public String delete(String name) {
        if (name == null) {
            return "";
        }

        // Collect all matches first (order-agnostic collection).
        java.util.ArrayList<City> matches = new java.util.ArrayList<>();
        byCoord.preorderWithLevels((lvl, e) -> {
            if (e.getName().equals(name)) {
                matches.add(e);
            }
        });
        if (matches.isEmpty()) {
            return "";
        }

        // Deterministic order: lexicographic by (x, then y).
        matches.sort((a, b) -> {
            int cx = Integer.compare(a.getX(), b.getX());
            if (cx != 0) {
                return cx;
            }
            return Integer.compare(a.getY(), b.getY());
        });

        StringBuilder sb = new StringBuilder();
        for (City m : matches) {
            // Remove from KDTree by exact coord.
            KDTree.DeleteOutcome out = byCoord.delete(m.getX(), m.getY());
            if (out.entry != null) {
                // Remove exact triple from BST using your existing predicate.
                byName.removeMatching(
                    new City(out.entry.getName(), out.entry.getX(),
                        out.entry.getY()),
                    c -> c.getX() == out.entry.getX()
                        && c.getY() == out.entry.getY()
                        && c.getName().equals(out.entry.getName())
                );
                sb.append(out.entry.getName()).append(" (")
                  .append(out.entry.getX()).append(", ")
                  .append(out.entry.getY()).append(")").append("\n");
            }
        }
        return sb.toString();
    }


    /** Name at coordinate or empty string. */
    public String info(int x, int y) {
        City e = byCoord.findExact(x, y);
        return (e != null) ? e.getName() : "";
    }

    /**
     * Coordinates for all cities with the given name, in BST inorder (equal names appear on the LEFT chain first).
     * Returns empty string when none match.
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
            sb.append(name).append(" (").append(xs[i]).append(", ").append(ys[i]).append(")").append("\n");
        }
        return sb.toString();
    }

    /**
     * Circle range search. Returns listing lines (if any) followed by the visit count.
     * Bad radius → empty string.
     */
    public String search(int x, int y, int radius) {
        if (radius < 0) return "";
        KDTree.SearchOutcome res = byCoord.rangeSearch(x, y, radius);
        StringBuilder sb = new StringBuilder();
        sb.append(res.listing);
        sb.append(res.visited);
        return sb.toString();
    }

    /**
     * Inorder listing of the kd-tree: level, 2*level spaces, then "name x y".
     */
    public String debug() {
        StringBuilder sb = new StringBuilder();
        byCoord.inorderWithLevels((level, e) -> {
            sb.append(level);
            for (int i = 0; i < 2 * level; i++) sb.append(" ");
            sb.append(e.getName()).append(" ").append(e.getX()).append(" ").append(e.getY()).append("\n");
        });
        return sb.toString();
    }

    /**
     * Inorder listing of the BST by name: level, 2*level spaces, then "name (x, y)".
     */
    public String print() {
        StringBuilder sb = new StringBuilder();
        byName.inorderWithLevels((level, c) -> {
            sb.append(level);
            for (int i = 0; i < 2 * level; i++) sb.append(" ");
            sb.append(c.getName()).append(" (").append(c.getX()).append(", ").append(c.getY()).append(")").append("\n");
        });
        return sb.toString();
    }
}
