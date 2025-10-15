import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A 2-D kd-tree that stores {@code City} records by their (x, y) coordinates.
 *
 * <p>Behavioral rules:
 * <ul>
 *   <li><b>Insert</b> rejects duplicate (x, y) coordinates. Ties in the split
 *       key always go to the <i>right</i> child.</li>
 *   <li><b>Delete</b> replaces the removed node with the minimum (in the
 *       current split dimension) from the right subtree, breaking ties by the
 *       other coordinate (lexicographic by (x,y) on X-splits and (y,x) on
 *       Y-splits). The visit count includes nodes touched during replacement
 *       searches.</li>
 *   <li><b>Range search</b> includes a node when
 *       {@code (dx*dx + dy*dy) <= r*r}. The search prunes subtrees using
 *       rectangle-vs-circle intersection checks and reports the number of
 *       visited nodes.</li>
 * </ul>
 *
 * <p>Traversal helpers are provided to support the assignment’s printing
 * utilities.</p>
 *
 * @author Parth
 * @author Anurag
 * @version 2025-10-06
 */
class KDTree {

    /** Tree node holding a {@link City} and links to children. */
    private static final class Node {
        City e;
        Node left;
        Node right;

        /** Creates a node for the given city. */
        Node(City e) {
            this.e = e;
        }
    }

    /** Result wrapper for recursive insert. */
    private static final class Result {
        final Node newRoot;
        final boolean added;

        /** Creates an insert result with new root and add flag. */
        Result(Node n, boolean a) {
            newRoot = n;
            added = a;
        }
    }

    /** Mutable counter used to accumulate visit counts. */
    private static final class Counter {
        int count = 0;
    }

    /** Delete recursion result: new subtree root and removed entry. */
    private static final class DelRes {
        final Node newRoot;
        final City removed;

        /** Creates a delete result. */
        DelRes(Node r, City e) {
            this.newRoot = r;
            this.removed = e;
        }
    }

    /**
     * Outcome of a delete operation.
     */
    public static final class DeleteOutcome {
        /** Number of nodes visited during the delete. */
        public final int visited;
        /** The entry removed, or {@code null} if not found. */
        public final City entry;

        /** Creates a delete outcome. */
        public DeleteOutcome(int visited, City entry) {
            this.visited = visited;
            this.entry = entry;
        }
    }

    /**
     * Outcome of a range search.
     */
    public static final class SearchOutcome {
        /** Number of nodes visited during the search. */
        public final int visited;
        /** Collected listing of matches, one per line. */
        public final String listing;

        /** Creates a range search outcome. */
        public SearchOutcome(int visited, String listing) {
            this.visited = visited;
            this.listing = listing;
        }
    }

    // ----------------------------------------------------------------------
    // fields
    // ----------------------------------------------------------------------

    /** Root of the kd-tree. */
    private Node root;

    /** Number of entries in the tree. */
    private int size;

    // ----------------------------------------------------------------------
    // basic ops
    // ----------------------------------------------------------------------

    /** Removes all entries from the tree. */
    public void clear() {
        root = null;
        size = 0;
    }

    /**
     * Returns {@code true} if the tree has no entries.
     *
     * @return whether the tree is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the number of entries in the tree.
     *
     * @return size of the tree
     */
    public int size() {
        return size;
    }

    /**
     * Inserts a new city record. Duplicate (x, y) coordinates are rejected.
     * Ties in the split key go to the right child.
     *
     * @param name city name (not {@code null})
     * @param x    x coordinate
     * @param y    y coordinate
     * @return {@code true} if inserted; {@code false} on duplicate
     * @throws NullPointerException if {@code name} is {@code null}
     */
    public boolean insert(String name, int x, int y) {
        Objects.requireNonNull(name, "name");
        City rec = new City(name, x, y);
        Result r = insertRec(root, rec, 0);
        if (r.added) {
            root = r.newRoot;
            size = size + 1;
            return true;
        }
        return false;
    }

    /**
     * Convenience overload that inserts an existing {@link City}.
     *
     * @param c city to insert
     * @return whether the city was inserted
     */
    public boolean insert(City c) {
        return insert(c.getName(), c.getX(), c.getY());
    }

    /**
     * Recursive insert that preserves kd-tree splitting rules.
     *
     * @param n     subtree root
     * @param e     entry to insert
     * @param depth current depth (parity selects split axis)
     * @return result containing new subtree root and add flag
     */
    private Result insertRec(Node n, City e, int depth) {
        if (n == null) {
            return new Result(new Node(e), true);
        }
        if (e.getX() == n.e.getX() && e.getY() == n.e.getY()) {
            return new Result(n, false);
        }
        boolean splitOnX = (depth % 2 == 0);
        int cmp = splitOnX
            ? Integer.compare(e.getX(), n.e.getX())
            : Integer.compare(e.getY(), n.e.getY());
        if (cmp < 0) {
            Result leftRes = insertRec(n.left, e, depth + 1);
            n.left = leftRes.newRoot;
            return new Result(n, leftRes.added);
        } else {
            Result rightRes = insertRec(n.right, e, depth + 1);
            n.right = rightRes.newRoot;
            return new Result(n, rightRes.added);
        }
    }

    // ----------------------------------------------------------------------
    // traversals used by the assignment’s print/debug helpers
    // ----------------------------------------------------------------------

    /**
     * Inorder traversal with levels (depth). Each visited node is supplied to
     * the given consumer as {@code (level, city)}.
     *
     * @param visit consumer of (level, city)
     */
    public void inorderWithLevels(BiConsumer<Integer, City> visit) {
        inorderRec(root, 0, visit);
    }

    /**
     * Recursive inorder traversal with level tracking.
     *
     * @param n     subtree root
     * @param level current level
     * @param visit consumer of (level, city)
     */
    private void inorderRec(
        Node n,
        int level,
        BiConsumer<Integer, City> visit) {

        if (n == null) {
            return;
        }
        inorderRec(n.left, level + 1, visit);
        visit.accept(level, n.e);
        inorderRec(n.right, level + 1, visit);
    }

    /**
     * Preorder traversal with levels (depth).
     *
     * @param visit consumer of (level, city)
     */
    public void preorderWithLevels(BiConsumer<Integer, City> visit) {
        preorderRec(root, 0, visit);
    }

    /**
     * Recursive preorder traversal with level tracking.
     *
     * @param n     subtree root
     * @param level current level
     * @param visit consumer of (level, city)
     */
    private void preorderRec(
        Node n,
        int level,
        BiConsumer<Integer, City> visit) {

        if (n == null) {
            return;
        }
        visit.accept(level, n.e);
        preorderRec(n.left, level + 1, visit);
        preorderRec(n.right, level + 1, visit);
    }

    // ----------------------------------------------------------------------
    // exact find, delete, and range search
    // ----------------------------------------------------------------------

    /**
     * Finds the city with the exact coordinates, or {@code null} if missing.
     *
     * @param x target x
     * @param y target y
     * @return matching city or {@code null}
     */
    public City findExact(int x, int y) {
        Node n = root;
        int depth = 0;
        while (n != null) {
            if (n.e.getX() == x && n.e.getY() == y) {
                return n.e;
            }
            boolean splitOnX = (depth % 2 == 0);
            if (splitOnX) {
                n = (x < n.e.getX()) ? n.left : n.right;
            } else {
                n = (y < n.e.getY()) ? n.left : n.right;
            }
            depth = depth + 1;
        }
        return null;
    }

    /**
     * Deletes the city at (x, y).
     *
     * <p>If the tree is empty, returns visited {@code 0} and {@code null}
     * entry.</p>
     *
     * @param x target x
     * @param y target y
     * @return outcome with visit count and removed entry
     */
    public DeleteOutcome delete(int x, int y) {
        if (root == null) {
            return new DeleteOutcome(0, null);
        }
        Counter c = new Counter();
        DelRes r = deleteRec(root, x, y, 0, c);
        root = r.newRoot;
        if (r.removed != null && size > 0) {
            size = size - 1;
        }
        return new DeleteOutcome(c.count, r.removed);
    }

    /**
     * Recursive delete. When removing a node, the replacement is the
     * lexicographic minimum of the right subtree in the current split
     * dimension (ties broken by the other coordinate). If the right subtree is
     * empty, a left-only variant is used and rewired to keep shape stable.
     *
     * @param n     subtree root
     * @param x     target x
     * @param y     target y
     * @param depth current depth
     * @param c     visit counter
     * @return new subtree root and removed entry
     */
    private DelRes deleteRec(
        Node n,
        int x,
        int y,
        int depth,
        Counter c) {

        if (n == null) {
            return new DelRes(null, null);
        }
        c.count = c.count + 1;

        if (n.e.getX() == x && n.e.getY() == y) {
            City removed = n.e;
            if (n.right != null) {
                boolean wantX = (depth % 2 == 0);
                Node minNode = findMinLex(n.right, depth + 1, wantX, c);
                n.e = minNode.e;
                DelRes rr = deleteRec(
                    n.right,
                    minNode.e.getX(),
                    minNode.e.getY(),
                    depth + 1,
                    c);
                n.right = rr.newRoot;
                return new DelRes(n, removed);
            } else if (n.left != null) {
                boolean wantX = (depth % 2 == 0);
                Node minNode = findMinLex(n.left, depth + 1, wantX, c);
                n.e = minNode.e;
                DelRes rl = deleteRec(
                    n.left,
                    minNode.e.getX(),
                    minNode.e.getY(),
                    depth + 1,
                    c);
                n.left = rl.newRoot;
                if (n.right == null) {
                    n.right = n.left;
                    n.left = null;
                }
                return new DelRes(n, removed);
            } else {
                return new DelRes(null, removed);
            }
        } else {
            boolean splitOnX = (depth % 2 == 0);
            int cmp = splitOnX
                ? Integer.compare(x, n.e.getX())
                : Integer.compare(y, n.e.getY());
            if (cmp < 0) {
                DelRes dl = deleteRec(n.left, x, y, depth + 1, c);
                n.left = dl.newRoot;
                return new DelRes(n, dl.removed);
            } else {
                DelRes dr = deleteRec(n.right, x, y, depth + 1, c);
                n.right = dr.newRoot;
                return new DelRes(n, dr.removed);
            }
        }
    }

    /**
     * Returns the lexicographic minimum under a subtree according to the
     * requested primary dimension.
     *
     * <p>If {@code wantX} is {@code true}, compares by {@code (x, y)}.
     * Otherwise compares by {@code (y, x)}. Both children are searched so
     * equal-primary values that were inserted to the right are handled.</p>
     *
     * @param n      subtree root
     * @param depth  current depth (tracked only for visit counting symmetry)
     * @param wantX  whether the primary dimension is x
     * @param c      visit counter
     * @return node containing the lexicographic minimum
     */
    private Node findMinLex(Node n, int depth, boolean wantX, Counter c) {
        if (n == null) {
            return null;
        }
        c.count = c.count + 1;

        Node best = n;

        Node l = findMinLex(n.left, depth + 1, wantX, c);
        if (isBetterLex(l, best, wantX)) {
            best = l;
        }

        Node r = findMinLex(n.right, depth + 1, wantX, c);
        if (isBetterLex(r, best, wantX)) {
            best = r;
        }

        return best;
    }

    /**
     * Returns {@code true} if {@code cand} is lexicographically smaller than
     * {@code curr} under the chosen key order.
     *
     * @param cand candidate node
     * @param curr current best node
     * @param byX  {@code true} for (x,y); {@code false} for (y,x)
     * @return whether {@code cand} is better than {@code curr}
     */
    private boolean isBetterLex(Node cand, Node curr, boolean byX) {
        if (cand == null) {
            return false;
        }
        if (curr == null) {
            return true;
        }

        if (byX) {
            int cx = cand.e.getX();
            int cy = cand.e.getY();
            int bx = curr.e.getX();
            int by = curr.e.getY();
            if (cx != bx) {
                return cx < bx;
            }
            return cy < by;
        } else {
            int cy = cand.e.getY();
            int cx = cand.e.getX();
            int by = curr.e.getY();
            int bx = curr.e.getX();
            if (cy != by) {
                return cy < by;
            }
            return cx < bx;
        }
    }

    /**
     * Performs a circular range search centered at {@code (cx, cy)} with
     * radius {@code r}. The result lists matching cities, one per line.
     *
     * @param cx     center x
     * @param cy     center y
     * @param radius radius (non-negative)
     * @return outcome with visit count and textual listing
     */
    public SearchOutcome rangeSearch(int cx, int cy, int radius) {
        if (root == null) {
            return new SearchOutcome(0, "");
        }
        StringBuilder sb = new StringBuilder();
        Counter c = new Counter();
        long r2 = (long) radius * (long) radius;
        rangeRec(
            root,
            0,
            Integer.MIN_VALUE,
            Integer.MIN_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            cx,
            cy,
            r2,
            sb,
            c);
        return new SearchOutcome(c.count, sb.toString());
    }

    /**
     * Recursive range search with rectangle pruning.
     *
     * @param n     subtree root
     * @param depth current depth
     * @param minX  rectangle min x
     * @param minY  rectangle min y
     * @param maxX  rectangle max x
     * @param maxY  rectangle max y
     * @param cx    circle center x
     * @param cy    circle center y
     * @param r2    squared radius
     * @param out   output buffer
     * @param c     visit counter
     */
    private void rangeRec(
        Node n,
        int depth,
        int minX,
        int minY,
        int maxX,
        int maxY,
        int cx,
        int cy,
        long r2,
        StringBuilder out,
        Counter c) {

        if (n == null) {
            return;
        }
        c.count = c.count + 1;

        long dx = (long) n.e.getX() - (long) cx;
        long dy = (long) n.e.getY() - (long) cy;
        long d2 = dx * dx + dy * dy;
        if (d2 <= r2) {
            out.append(n.e.getName())
               .append(" (")
               .append(n.e.getX())
               .append(", ")
               .append(n.e.getY())
               .append(")")
               .append("\n");
        }

        boolean splitOnX = (depth % 2 == 0);
        if (splitOnX) {
            int split = n.e.getX();
            int leftMaxX = split - 1;
            int rightMinX = split;
            if (rectIntersectsCircle(minX, minY, leftMaxX, maxY, cx, cy, r2)) {
                rangeRec(
                    n.left,
                    depth + 1,
                    minX,
                    minY,
                    leftMaxX,
                    maxY,
                    cx,
                    cy,
                    r2,
                    out,
                    c);
            }
            if (rectIntersectsCircle(rightMinX, minY, maxX, maxY, cx, cy, r2)) {
                rangeRec(
                    n.right,
                    depth + 1,
                    rightMinX,
                    minY,
                    maxX,
                    maxY,
                    cx,
                    cy,
                    r2,
                    out,
                    c);
            }
        } else {
            int split = n.e.getY();
            int lowerMaxY = split - 1;
            int upperMinY = split;
            if (rectIntersectsCircle(minX, minY, maxX, lowerMaxY, cx, cy, r2)) {
                rangeRec(
                    n.left,
                    depth + 1,
                    minX,
                    minY,
                    maxX,
                    lowerMaxY,
                    cx,
                    cy,
                    r2,
                    out,
                    c);
            }
            if (rectIntersectsCircle(minX, upperMinY, maxX, maxY, cx, cy, r2)) {
                rangeRec(
                    n.right,
                    depth + 1,
                    minX,
                    upperMinY,
                    maxX,
                    maxY,
                    cx,
                    cy,
                    r2,
                    out,
                    c);
            }
        }
    }

    /**
     * Returns {@code true} if a rectangle intersects (or touches) a circle.
     *
     * @param minX rectangle min x
     * @param minY rectangle min y
     * @param maxX rectangle max x
     * @param maxY rectangle max y
     * @param cx   circle center x
     * @param cy   circle center y
     * @param r2   squared radius
     * @return whether the rectangle intersects the circle
     */
    private boolean rectIntersectsCircle(
        int minX,
        int minY,
        int maxX,
        int maxY,
        int cx,
        int cy,
        long r2) {

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
     * @param lo lower bound
     * @param hi upper bound
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
