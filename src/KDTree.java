import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 2D kd-tree storing {@code City} records by coordinates.
 * 
 * @author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */
class KDTree {

    // ------------------------------- Node --------------------------------
    /**
     * Tree node storing a city entry and two child links.
     */
    private static final class Node {
        City e;
        Node left;
        Node right;

        /**
         * Creates a node for the given city.
         *
         * @param e the city payload
         */
        Node(City e) 
        { 
            this.e = e; 
        }
    }

    // ------------------------------ Fields -------------------------------
    private Node root;
    private int size;

    // ---------------------------- Basic Ops ------------------------------
    /**
     * Removes all entries from the tree.
     */
    public void clear() 
    { 
        root = null; 
        size = 0; 
    }

    /**
     * Returns whether the tree is empty.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() 
    { 
        return size == 0; 
    }

    /**
     * Returns the number of entries stored.
     *
     * @return size of the tree
     */
    public int size() 
    { 
        return size; 
    }

    // ----------------------------- Insert --------------------------------
    /**
     * Inserts a city by coordinates. Duplicate coordinates are rejected.
     * Ties in the split key go to the right subtree.
     *
     * @param name city name
     * @param x    x coordinate
     * @param y    y coordinate
     * @return {@code true} if inserted; {@code false} if duplicate
     */
    public boolean insert(String name, int x, int y) 
    {
        Objects.requireNonNull(name, "name");
        City rec = new City(name, x, y);
        Result r = insertRec(root, rec, 0);
        if (r.added) 
        {
            root = r.newRoot;
            size = size + 1;
            return true;
        }
        return false;
    }

    /**
     * Convenience insert from an existing {@code City}.
     *
     * @param c city instance
     * @return {@code true} if inserted
     */
    public boolean insert(City c) {
        return insert(c.getName(), c.getX(), c.getY());
    }

    /**
     * Result of a recursive insert.
     */
    private static final class Result {
        final Node newRoot;
        final boolean added;

        /**
         * Creates an insert result.
         *
         * @param n new subtree root
         * @param a whether a node was added
         */
        Result(Node n, boolean a) 
        { 
            newRoot = n; 
            added = a; 
        }
    }

    /**
     * Recursive helper for insertion.
     *
     * @param n     subtree root
     * @param e     city to insert
     * @param depth current depth
     * @return result containing the new subtree root and add flag
     */
    private Result insertRec(Node n, City e, int depth) {
        if (n == null) 
        {
            return new Result(new Node(e), true);
        }
        if (e.getX() == n.e.getX() && e.getY() == n.e.getY()) 
        {
            return new Result(n, false);
        }
        boolean splitOnX = (depth % 2 == 0);
        int cmp = splitOnX
            ? Integer.compare(e.getX(), n.e.getX())
            : Integer.compare(e.getY(), n.e.getY());
        if (cmp < 0) 
        {
            Result leftRes = insertRec(n.left, e, depth + 1);
            n.left = leftRes.newRoot;
            return new Result(n, leftRes.added);
        } 
        else 
        {
            Result rightRes = insertRec(n.right, e, depth + 1);
            n.right = rightRes.newRoot;
            return new Result(n, rightRes.added);
        }
    }

    // ---------------------------- Traversals -----------------------------
    /**
     * Inorder traversal that passes (level, city) to {@code visit}.
     *
     * @param visit consumer receiving level and city
     */
    public void inorderWithLevels(BiConsumer<Integer, City> visit) 
    {
        inorderRec(root, 0, visit);
    }

    /**
     * Recursive helper for inorder traversal with levels.
     *
     * @param n     subtree root
     * @param level current level
     * @param visit consumer receiving level and city
     */
    private void inorderRec(
        Node n, int level, BiConsumer<Integer, City> visit) 
    {
        if (n == null) 
        {
            return;
        }
        inorderRec(n.left, level + 1, visit);
        visit.accept(level, n.e);
        inorderRec(n.right, level + 1, visit);
    }

    /**
     * Preorder traversal that passes (level, city) to {@code visit}.
     *
     * @param visit consumer receiving level and city
     */
    public void preorderWithLevels(BiConsumer<Integer, City> visit) 
    {
        preorderRec(root, 0, visit);
    }

    /**
     * Recursive helper for preorder traversal with levels.
     *
     * @param n     subtree root
     * @param level current level
     * @param visit consumer receiving level and city
     */
    private void preorderRec(
        Node n, int level, BiConsumer<Integer, City> visit) 
    {
        if (n == null) {
            return;
        }
        visit.accept(level, n.e);
        preorderRec(n.left, level + 1, visit);
        preorderRec(n.right, level + 1, visit);
    }

    // ------------------ Exact Find, Delete, Range Search ------------------
    /**
     * Outcome of a delete operation.
     */
    public static final class DeleteOutcome {
        /**
         * The number of nodes visited during the delete operation.
         */
        public final int visited;

        /**
         * The city entry that was removed, or {@code null}
         */
        public final City entry;

        /**
         * Creates a delete outcome.
         *
         * @param visited nodes visited
         * @param entry   removed city (or {@code null})
         */
        public DeleteOutcome(int visited, City entry) {
            this.visited = visited;
            this.entry = entry;
        }
    }

    /**
     * Mutable visit counter.
     */
    private static final class Counter { int count = 0; }

    /**
     * Internal delete result.
     */
    private static final class DelRes {
        final Node newRoot;
        final City removed;

        /**
         * Creates a delete result.
         *
         * @param r new subtree root
         * @param e removed city (or {@code null})
         */
        DelRes(Node r, City e) 
        { 
            this.newRoot = r; 
            this.removed = e; 
        }
    }

    /**
     * Finds the exact (x,y) city.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return matching city or {@code null}
     */
    public City findExact(int x, int y) {
        Node n = root;
        int depth = 0;
        while (n != null)
        {
            if (n.e.getX() == x && n.e.getY() == y) 
            {
                return n.e;
            }
            boolean splitOnX = (depth % 2 == 0);
            n = splitOnX
                ? ((x < n.e.getX()) ? n.left : n.right)
                : ((y < n.e.getY()) ? n.left : n.right);
            depth = depth + 1;
        }
        return null;
    }

    /**
     * Deletes the city at (x,y). If the tree is empty, returns visited 0 and
     * a {@code null} entry.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return outcome with visit count and removed entry
     */
    public DeleteOutcome delete(int x, int y) {
        if (root == null)
        {
            return new DeleteOutcome(0, null);
        }
        Counter c = new Counter();
        DelRes r = deleteRec(root, x, y, 0, c);
        root = r.newRoot;
        if (r.removed != null && size > 0)
        {
            size = size - 1;    
        }
        return new DeleteOutcome(c.count, r.removed);
    }

    /**
     * Recursive helper for deletion.
     *
     * @param n      subtree root
     * @param x      x target
     * @param y      y target
     * @param depth  current depth
     * @param c      visit counter
     * @return delete result for this subtree
     */
    private DelRes deleteRec(
        Node n, int x, int y, int depth, Counter c) {
        if (n == null) 
        { 
            return new DelRes(null, null);
        }
        c.count = c.count + 1;

        if (n.e.getX() == x && n.e.getY() == y) {
            City removed = n.e;
            if (n.right != null) {
                int splitDim = depth % 2;
                Node minNode = findMin(n.right, depth + 1, splitDim, c);
                n.e = minNode.e;
                DelRes rr = deleteRec(
                    n.right, minNode.e.getX(), minNode.e.getY(),
                    depth + 1, c);
                n.right = rr.newRoot;
                return new DelRes(n, removed);
            }
            else if (n.left != null) 
            {
                int splitDim = depth % 2;
                Node minNode = findMin(n.left, depth + 1, splitDim, c);
                n.e = minNode.e;
                DelRes rl = deleteRec(
                    n.left, minNode.e.getX(), minNode.e.getY(),
                    depth + 1, c);
                n.left = rl.newRoot;
                if (n.right == null) 
                {
                    n.right = n.left;
                    n.left = null;
                }
                return new DelRes(n, removed);
            } 
            else {
                return new DelRes(null, removed);
            }
        } 
        else {
            boolean splitOnX = (depth % 2 == 0);
            int cmp = splitOnX
                ? Integer.compare(x, n.e.getX())
                : Integer.compare(y, n.e.getY());
            if (cmp < 0) {
                DelRes dl = deleteRec(n.left, x, y, depth + 1, c);
                n.left = dl.newRoot;
                return new DelRes(n, dl.removed);
            } 
            else {
                DelRes dr = deleteRec(n.right, x, y, depth + 1, c);
                n.right = dr.newRoot;
                return new DelRes(n, dr.removed);
            }
        }
    }

    /**
     * Returns the preorder-minimum node in the requested dimension.
     * Ties keep the current best (preorder preference). Visits are counted.
     *
     * @param n          subtree root
     * @param depth      current depth
     * @param targetDim  0 for x, 1 for y
     * @param c          visit counter
     * @return node with the minimum value in {@code targetDim}
     */
    private Node findMin(
        Node n, int depth, int targetDim, Counter c) {
        if (n == null) 
        { 
            return null;
        }
        c.count = c.count + 1;

        boolean splitOnX = (depth % 2 == 0);
        int splitDim = splitOnX ? 0 : 1;

        Node best = n;
        Node l = findMin(n.left, depth + 1, targetDim, c);
        if (isBetterDim(l, best, targetDim)) 
        { 
            best = l;
        }

        if (splitDim != targetDim) 
        {
            Node r = findMin(n.right, depth + 1, targetDim, c);
            if (isBetterDim(r, best, targetDim)) 
            {
                best = r;
            }
        }
        return best;
    }

    /**
     * Compares two nodes along a given dimension to determine whether
     * the candidate node is better
     *
     * @param cand the candidate node
     * @param curr the current best node
     * @param dim  the dimension to compare (0 for x, 1 for y)
     * @return if {@code cand} is better than {@code curr}
     */
    private boolean isBetterDim(Node cand, Node curr, int dim) {
        if (cand == null) {
            return false;
        }

        if (curr == null) {
            return true;
        }

        int cv = (dim == 0) ? cand.e.getX() : cand.e.getY();
        int bv = (dim == 0) ? curr.e.getX() : curr.e.getY();

        if (cv < bv) {
            return true;
        }

        if (cv > bv) {
            return false;
        }

        return false;
    }


    // --------------------------- Range Search -----------------------------
    /**
     * Outcome of a range search.
     */
    public static final class SearchOutcome {
        /**
         * The number of nodes visited during the search operation.
         */
        public final int visited;

        /**
         * A newline-delimited list of cities that matched the search criteria.
         */
        public final String listing;

        /**
         * Creates a range outcome.
         *
         * @param visited nodes visited
         * @param listing newline-delimited matches
         */
        public SearchOutcome(int visited, String listing) {
            this.visited = visited;
            this.listing = listing;
        }
    }

    /**
     * Returns all cities within {@code radius} of ({@code cx},{@code cy}).
     * Listing is one city per line. Visits are counted.
     *
     * @param cx     center x
     * @param cy     center y
     * @param radius query radius
     * @return outcome containing visit count and listing
     */
    public SearchOutcome rangeSearch(int cx, int cy, int radius) {
        if (root == null) 
        {
            return new SearchOutcome(0, "");
        }
        StringBuilder sb = new StringBuilder();
        Counter c = new Counter();
        long r2 = (long) radius * (long) radius;

        rangeRec(root, 0,
                 Integer.MIN_VALUE, Integer.MIN_VALUE,
                 Integer.MAX_VALUE, Integer.MAX_VALUE,
                 cx, cy, r2, sb, c);

        return new SearchOutcome(c.count, sb.toString());
    }

    /**
     * Recursive helper for range search with rectangle pruning.
     *
     * @param n     subtree root
     * @param depth current depth
     * @param minX  rectangle min x
     * @param minY  rectangle min y
     * @param maxX  rectangle max x
     * @param maxY  rectangle max y
     * @param cx    center x
     * @param cy    center y
     * @param r2    radius squared
     * @param out   result builder
     * @param c     visit counter
     */
    private void rangeRec(
        Node n, int depth,
        int minX, int minY, int maxX, int maxY,
        int cx, int cy, long r2,
        StringBuilder out, Counter c) {
        if (n == null) return;
        c.count = c.count + 1;

        long dx = (long) n.e.getX() - (long) cx;
        long dy = (long) n.e.getY() - (long) cy;
        long d2 = dx * dx + dy * dy;
        if (d2 <= r2) {
            out.append(n.e.getName()).append(" (")
               .append(n.e.getX()).append(", ")
               .append(n.e.getY()).append(")\n");
        }

        boolean splitOnX = (depth % 2 == 0);
        if (splitOnX) {
            int split = n.e.getX();
            int leftMaxX = split - 1;
            int rightMinX = split;
            if (rectIntersectsCircle(minX, minY, leftMaxX, maxY, cx, cy, r2)) {
                rangeRec(
                    n.left, depth + 1,
                    minX, minY, leftMaxX, maxY,
                    cx, cy, r2, out, c);
            }
            if (rectIntersectsCircle(rightMinX, minY, maxX, maxY, cx, cy, r2)) {
                rangeRec(
                    n.right, depth + 1,
                    rightMinX, minY, maxX, maxY,
                    cx, cy, r2, out, c);
            }
        } 
        else {
            int split = n.e.getY();
            int lowerMaxY = split - 1;
            int upperMinY = split;
            if (rectIntersectsCircle(minX, minY, maxX, lowerMaxY, cx, cy, r2)) {
                rangeRec(
                    n.left, depth + 1,
                    minX, minY, maxX, lowerMaxY,
                    cx, cy, r2, out, c);
            }
            if (rectIntersectsCircle(minX, upperMinY, maxX, maxY, cx, cy, r2)) {
                rangeRec(
                    n.right, depth + 1,
                    minX, upperMinY, maxX, maxY,
                    cx, cy, r2, out, c);
            }
        }
    }

    /**
     * Returns whether an axis-aligned rectangle intersects or touches a circle.
     *
     * @param minX rectangle min x
     * @param minY rectangle min y
     * @param maxX rectangle max x
     * @param maxY rectangle max y
     * @param cx   center x
     * @param cy   center y
     * @param r2   radius squared
     * @return {@code true} if the rectangle and circle intersect or touch
     */
    private boolean rectIntersectsCircle(int minX, int minY,
        int maxX, int maxY,
        int cx, int cy, long r2) 
    {

        if (minX > maxX) {
            int t = minX;
            minX = maxX;
            maxX = t;
        }

        if (minY > maxY) {
            int t = minY;
            minY = maxY;
            maxY = t;
        }

        int nx = clamp(cx, minX, maxX);
        int ny = clamp(cy, minY, maxY);
        long dx = (long) cx - (long) nx;
        long dy = (long) cy - (long) ny;
        long d2 = dx * dx + dy * dy;
        
        return d2 <= r2;
    }



    /**
     * Clamps {@code v} to the inclusive range [{@code lo}, {@code hi}].
     *
     * @param v  value
     * @param lo low bound
     * @param hi high bound
     * @return clamped value
     */
    private int clamp(int v, int lo, int hi) {
        if (v < lo) {
            return lo;
        }

        if (v > hi) {
            return hi;
        }

        return v;
    }

}
