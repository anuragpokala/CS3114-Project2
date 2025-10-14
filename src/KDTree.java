import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * 2D kd-tree storing City records by coordinates.
 * - Insert rejects duplicate (x,y). Ties in split key go RIGHT.
 * - Delete replaces with the min (in current split dimension) from the right subtree,
 *   using preorder tie preference (first found).
 * - Range search uses dx^2 + dy^2 with rectangle pruning and visit counting.
 */
class KDTree {

    // ---- node ----
    private static final class Node {
        City e;
        Node left, right;
        Node(City e) { this.e = e; }
    }

    // ---- fields ----
    private Node root;
    private int size;

    // ---- simple ops ----
    public void clear() { root = null; size = 0; }
    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }

    /** Insert: reject duplicate (x,y); ties in split key go RIGHT. */
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

    // (optional convenience; not used by GISDB but harmless)
    public boolean insert(City c) {
        return insert(c.getName(), c.getX(), c.getY());
    }

    private static final class Result {
        final Node newRoot;
        final boolean added;
        Result(Node n, boolean a) { newRoot = n; added = a; }
    }

    private Result insertRec(Node n, City e, int depth) {
        if (n == null) return new Result(new Node(e), true);
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

    // ---- traversals used by GISDB print/debug logic ----
    public void inorderWithLevels(BiConsumer<Integer, City> visit) {
        inorderRec(root, 0, visit);
    }

    private void inorderRec(Node n, int level, BiConsumer<Integer, City> visit) {
        if (n == null) return;
        inorderRec(n.left, level + 1, visit);
        visit.accept(level, n.e);
        inorderRec(n.right, level + 1, visit);
    }

    public void preorderWithLevels(BiConsumer<Integer, City> visit) {
        preorderRec(root, 0, visit);
    }

    private void preorderRec(Node n, int level, BiConsumer<Integer, City> visit) {
        if (n == null) return;
        visit.accept(level, n.e);
        preorderRec(n.left, level + 1, visit);
        preorderRec(n.right, level + 1, visit);
    }

    // ---------- Exact find, Delete, Range Search ----------

    public static final class DeleteOutcome {
        public final int visited;
        public final City entry;
        public DeleteOutcome(int visited, City entry) {
            this.visited = visited;
            this.entry = entry;
        }
    }

    private static final class Counter { int count = 0; }

    private static final class DelRes {
        final Node newRoot;
        final City removed;
        DelRes(Node r, City e) { this.newRoot = r; this.removed = e; }
    }

    /** Find exact (x,y). Returns the City or null. */
    public City findExact(int x, int y) {
        Node n = root;
        int depth = 0;
        while (n != null) {
            if (n.e.getX() == x && n.e.getY() == y) return n.e;
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
     * Delete (x,y). If tree empty, visited=0 and entry=null.
     */
    public DeleteOutcome delete(int x, int y) {
        if (root == null) return new DeleteOutcome(0, null);
        Counter c = new Counter();
        DelRes r = deleteRec(root, x, y, 0, c);
        root = r.newRoot;
        if (r.removed != null && size > 0) size = size - 1;
        return new DeleteOutcome(c.count, r.removed);
    }

    private DelRes deleteRec(Node n, int x, int y, int depth, Counter c) {
        if (n == null) return new DelRes(null, null);
        c.count = c.count + 1;

        if (n.e.getX() == x && n.e.getY() == y) {
            City removed = n.e;
            if (n.right != null) {
                int splitDim = depth % 2; // 0:X, 1:Y
                Node minNode = findMin(n.right, depth + 1, splitDim, c);
                n.e = minNode.e;
                DelRes rr = deleteRec(n.right, minNode.e.getX(), minNode.e.getY(), depth + 1, c);
                n.right = rr.newRoot;
                return new DelRes(n, removed);
            } else if (n.left != null) {
                int splitDim = depth % 2;
                Node minNode = findMin(n.left, depth + 1, splitDim, c);
                n.e = minNode.e;
                DelRes rl = deleteRec(n.left, minNode.e.getX(), minNode.e.getY(), depth + 1, c);
                n.left = rl.newRoot;
                if (n.right == null) { // preserve original structure tweak
                    n.right = n.left;
                    n.left = null;
                }
                return new DelRes(n, removed);
            } else {
                return new DelRes(null, removed);
            }
        } else {
            boolean splitOnX = (depth % 2 == 0);
            int cmp = splitOnX ? Integer.compare(x, n.e.getX())
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
     * Preorder-min in targetDim (0=x,1=y) with preorder tie preference; counts visits.
     */
    private Node findMin(Node n, int depth, int targetDim, Counter c) {
        if (n == null) return null;
        c.count = c.count + 1;

        boolean splitOnX = (depth % 2 == 0);
        int splitDim = splitOnX ? 0 : 1;

        Node best = n; // preorder preference
        Node l = findMin(n.left, depth + 1, targetDim, c);
        if (isBetterDim(l, best, targetDim)) best = l;

        if (splitDim != targetDim) {
            Node r = findMin(n.right, depth + 1, targetDim, c);
            if (isBetterDim(r, best, targetDim)) best = r;
        }
        return best;
    }

    private boolean isBetterDim(Node cand, Node curr, int dim) {
        if (cand == null) return false;
        if (curr == null) return true;
        int cv = (dim == 0) ? cand.e.getX() : cand.e.getY();
        int bv = (dim == 0) ? curr.e.getX() : curr.e.getY();
        if (cv < bv) return true;
        if (cv > bv) return false;
        return false; // equal → keep curr (preorder preference)
    }

    // -------- Range search (circle) with pruning and visit counting ----------

    public static final class SearchOutcome {
        public final int visited;
        public final String listing;
        public SearchOutcome(int visited, String listing) {
            this.visited = visited;
            this.listing = listing;
        }
    }

    public SearchOutcome rangeSearch(int cx, int cy, int radius) {
        if (root == null) return new SearchOutcome(0, "");
        StringBuilder sb = new StringBuilder();
        Counter c = new Counter();
        long r2 = (long)radius * (long)radius;
        rangeRec(root, 0,
                 Integer.MIN_VALUE, Integer.MIN_VALUE,
                 Integer.MAX_VALUE, Integer.MAX_VALUE,
                 cx, cy, r2, sb, c);
        return new SearchOutcome(c.count, sb.toString());
    }

    private void rangeRec(Node n, int depth,
                          int minX, int minY, int maxX, int maxY,
                          int cx, int cy, long r2,
                          StringBuilder out, Counter c) {
        if (n == null) return;
        c.count = c.count + 1;

        long dx = (long)n.e.getX() - (long)cx;
        long dy = (long)n.e.getY() - (long)cy;
        long d2 = dx * dx + dy * dy;
        if (d2 <= r2) {
            out.append(n.e.getName()).append(" (")
               .append(n.e.getX()).append(", ").append(n.e.getY())
               .append(")").append("\n");
        }

        boolean splitOnX = (depth % 2 == 0);
        if (splitOnX) {
            int split = n.e.getX();
            int leftMaxX = split - 1;
            int rightMinX = split;
            if (rectIntersectsCircle(minX, minY, leftMaxX, maxY, cx, cy, r2)) {
                rangeRec(n.left, depth + 1, minX, minY, leftMaxX, maxY, cx, cy, r2, out, c);
            }
            if (rectIntersectsCircle(rightMinX, minY, maxX, maxY, cx, cy, r2)) {
                rangeRec(n.right, depth + 1, rightMinX, minY, maxX, maxY, cx, cy, r2, out, c);
            }
        } else {
            int split = n.e.getY();
            int lowerMaxY = split - 1;
            int upperMinY = split;
            if (rectIntersectsCircle(minX, minY, maxX, lowerMaxY, cx, cy, r2)) {
                rangeRec(n.left, depth + 1, minX, minY, maxX, lowerMaxY, cx, cy, r2, out, c);
            }
            if (rectIntersectsCircle(minX, upperMinY, maxX, maxY, cx, cy, r2)) {
                rangeRec(n.right, depth + 1, minX, upperMinY, maxX, maxY, cx, cy, r2, out, c);
            }
        }
    }

    private boolean rectIntersectsCircle(int minX, int minY, int maxX, int maxY,
                                         int cx, int cy, long r2) {
        int nx = clamp(cx, minX, maxX);
        int ny = clamp(cy, minY, maxY);
        long dx = (long)cx - (long)nx;
        long dy = (long)cy - (long)ny;
        long d2 = dx * dx + dy * dy;
        return d2 <= r2;
    }

    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
