// Stores City Location Coordinates in a KD Tree
/**
 * KD Tree Class
 * 
 * @author Parth Mehta
 * @version 10/3/2025
 */
public class KDTree {
    private KDNode root;
    private int size;

    // Subclass to build nodes within KDTree
    private class KDNode {
        private City city;
        private KDNode left;
        private KDNode right;

        // Initializes new nodes
        public KDNode(City city) {
            this.city = city;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Construct an empty 2D kd-tree of {@link City} records.
     */
    public KDTree() {
        root = null;
        size = 0;
    }

    /**
     * Insert a {@link City} into the kd-tree. The discriminator alternates
     * (even depth: X, odd depth: Y), and ties go to the right.
     *
     * @param cityToInsert
     *            the city record to insert
     */
    public void insert(City cityToInsert) {
        root = insertHelperMethod(root, cityToInsert, 0);
        size++;
    }

    /**
     * Recursive insertion helper alternating X/Y discriminators and sending
     * equal-valued keys to the right subtree.
     *
     * @param node
     *            current subtree root
     * @param city
     *            city to insert
     * @param depth
     *            current depth (root = 0)
     * @return updated subtree root
     */
    private KDNode insertHelperMethod(KDNode node, City city, int depth) {
        if (node == null) {
            return new KDNode(city);
        }
        if (depth % 2 == 0) {
            if (city.getX() < node.city.getX()) {
                node.left = insertHelperMethod(node.left, city, depth + 1);
            }
            else {
                node.right = insertHelperMethod(node.right, city, depth + 1);
            }
        }
        else {
            if (city.getY() < node.city.getY()) {
                node.left = insertHelperMethod(node.left, city, depth + 1);
            }
            else {
                node.right = insertHelperMethod(node.right, city, depth + 1);
            }
        }
        return node;
    }

    /**
     * Return the number of nodes currently stored in this kd-tree.
     *
     * @return the size of the kd-tree
     */
    public int getSize() {
        return size;
    }

    /**
     * Inorder listing of the kd-tree with level/indentation as specified.
     * The root has no indentation.
     *
     * @return the formatted inorder traversal string (empty if the tree is empty)
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        toStringHelper(root, 0, result);
        return result.toString();
    }

    private void toStringHelper(KDNode node, int depth, StringBuilder result) {
        if (node == null) {
            return;
        }
        toStringHelper(node.left, depth + 1, result);
        result.append(depth);
        for (int i = 0; i < depth * 2; i++) {
            result.append(" ");
        }
        result.append(node.city.toString());
        result.append("\n");
        toStringHelper(node.right, depth + 1, result);
    }

    /**
     * Determine whether the kd-tree contains an exact coordinate.
     *
     * @param x
     *            x-coordinate to test
     * @param y
     *            y-coordinate to test
     * @return {@code true} if a city at {@code (x, y)} exists; else {@code false}
     */
    public boolean contains(int x, int y) {
        return containsHelper(root, x, y, 0);
    }

    private boolean containsHelper(KDNode node, int x, int y, int depth) {
        if (node == null) {
            return false;
        }
        if (node.city.getX() == x && node.city.getY() == y) {
            return true;
        }
        if (depth % 2 == 0) {
            if (x < node.city.getX()) {
                return containsHelper(node.left, x, y, depth + 1);
            }
            else {
                return containsHelper(node.right, x, y, depth + 1);
            }
        }
        else {
            if (y < node.city.getY()) {
                return containsHelper(node.left, x, y, depth + 1);
            }
            else {
                return containsHelper(node.right, x, y, depth + 1);
            }
        }
    }

    // ---- Added: exact lookup that returns the City at (x, y) or null

    /**
     * Locate and return the city stored at the exact coordinate
     * {@code (x, y)}, or {@code null} if the coordinate is not present in
     * the tree. The search alternates discriminators by depth (even: X,
     * odd: Y) and follows the project rule that ties go to the right.
     *
     * @param x
     *            target x-coordinate
     * @param y
     *            target y-coordinate
     * @return the {@link City} at {@code (x, y)} if present; otherwise
     *         {@code null}
     */
    public City find(int x, int y) {
        return findHelper(root, x, y, 0);
    }

    /**
     * Recursive helper for {@link #find(int, int)} alternating X/Y splits
     * and respecting the tie-to-right policy.
     *
     * @param node
     *            current node
     * @param x
     *            target x-coordinate
     * @param y
     *            target y-coordinate
     * @param depth
     *            current depth
     * @return matching {@link City} or {@code null}
     */
    private City findHelper(KDNode node, int x, int y, int depth) {
        if (node == null) {
            return null;
        }
        if (node.city.getX() == x && node.city.getY() == y) {
            return node.city;
        }
        if (depth % 2 == 0) {
            if (x < node.city.getX()) {
                return findHelper(node.left, x, y, depth + 1);
            }
            else {
                return findHelper(node.right, x, y, depth + 1);
            }
        }
        else {
            if (y < node.city.getY()) {
                return findHelper(node.left, x, y, depth + 1);
            }
            else {
                return findHelper(node.right, x, y, depth + 1);
            }
        }
    }

    /**
     * Result container for kd-tree coordinate deletion.
     */
    public static final class KDDeleteResult {
        /** Nodes visited during the delete process. */
        public final int visited;
        /** The city that was removed, or {@code null} if none. */
        public final City removed;

        /**
         * Create a new deletion result.
         *
         * @param visited
         *            nodes visited count
         * @param removed
         *            city removed or {@code null}
         */
        public KDDeleteResult(int visited, City removed) {
            this.visited = visited;
            this.removed = removed;
        }
    }

    /**
     * Delete the city at the exact coordinate {@code (x, y)} if present.
     * Deletion follows the kd-tree rule: replace with the minimum (in the
     * current discriminator) from the right subtree when possible; otherwise
     * replace with the minimum from the left subtree. Among equal minima,
     * choose the one that appears first in a preorder traversal.
     *
     * @param x
     *            x-coordinate of the target
     * @param y
     *            y-coordinate of the target
     * @return {@link KDDeleteResult} containing nodes visited and the removed
     *         {@link City} (or {@code null} if not found)
     */
    public KDDeleteResult deleteExact(int x, int y) {
        int[] visited = new int[] { 0 };
        Wrapper w = new Wrapper();
        root = deleteRec(root, x, y, 0, visited, w);
        return new KDDeleteResult(visited[0], w.removed);
    }

    private static final class Wrapper {
        City removed;
    }

    private KDNode deleteRec(
        KDNode node,
        int x,
        int y,
        int depth,
        int[] visited,
        Wrapper out) {
        if (node == null) {
            return null;
        }
        visited[0]++;

        if (node.city.getX() == x && node.city.getY() == y) {
            out.removed = node.city;
            if (node.right != null) {
                City rep = findMinByDim(node.right, depth % 2, depth + 1, visited);
                node.city = rep;
                node.right = deleteRec(node.right, rep.getX(), rep.getY(), depth + 1, visited, new Wrapper());
                return node;
            }
            else if (node.left != null) {
                City rep = findMinByDim(node.left, depth % 2, depth + 1, visited);
                node.city = rep;
                node.left = deleteRec(node.left, rep.getX(), rep.getY(), depth + 1, visited, new Wrapper());
                return node;
            }
            else {
                size--;
                return null;
            }
        }

        if (depth % 2 == 0) {
            if (x < node.city.getX()) {
                node.left = deleteRec(node.left, x, y, depth + 1, visited, out);
            }
            else {
                node.right = deleteRec(node.right, x, y, depth + 1, visited, out);
            }
        }
        else {
            if (y < node.city.getY()) {
                node.left = deleteRec(node.left, x, y, depth + 1, visited, out);
            }
            else {
                node.right = deleteRec(node.right, x, y, depth + 1, visited, out);
            }
        }
        return node;
    }

    /**
     * Find the minimum (by discriminator {@code dim}, where 0 = X and 1 = Y)
     * within a subtree. If multiple records share the same minimum value,
     * return the one that appears first in a preorder traversal (root, left,
     * right).
     *
     * @param node
     *            subtree root
     * @param dim
     *            discriminator dimension (0 for X, 1 for Y)
     * @param depth
     *            current depth
     * @param visited
     *            node-visit counter to update
     * @return the {@link City} with the minimum value in dimension {@code dim}
     */
    private City findMinByDim(KDNode node, int dim, int depth, int[] visited) {
        if (node == null) {
            return null;
        }
        visited[0]++;

        City best = node.city;

        if (depth % 2 == dim) {
            City leftMin = findMinByDim(node.left, dim, depth + 1, visited);
            if (isLessInDim(leftMin, best, dim)) {
                best = leftMin;
            }
        }
        else {
            City leftMin = findMinByDim(node.left, dim, depth + 1, visited);
            if (isLessInDim(leftMin, best, dim)) {
                best = leftMin;
            }
            City rightMin = findMinByDim(node.right, dim, depth + 1, visited);
            if (isLessInDim(rightMin, best, dim)) {
                best = rightMin;
            }
        }

        return best;
    }

    /**
     * Compare two cities by a dimension.
     *
     * @param a
     *            left operand (may be {@code null})
     * @param b
     *            right operand (may be {@code null})
     * @param dim
     *            0 for X, 1 for Y
     * @return {@code true} if {@code a} is strictly less than {@code b} in {@code dim}
     */
    private boolean isLessInDim(City a, City b, int dim) {
        if (a == null || b == null) {
            return a != null && b == null;
        }
        int va = (dim == 0) ? a.getX() : a.getY();
        int vb = (dim == 0) ? b.getX() : b.getY();
        return va < vb;
    }
    

    /**
     * Encapsulates the result of a KD radius search: the number of nodes
     * visited and a lazily-built listing of matching cities.
     */
    public static final class KDSearchResult {
        /** Number of tree nodes visited during the search. */
        public int visited;
        private final StringBuilder sb = new StringBuilder();

        /** Append a matching city to the result listing. */
        void add(City c) {
            sb.append(c.toString()).append('\n');
        }

        /**
         * Finalize the listing, trimming any trailing newline.
         * 
         * @return The formatted list of matching cities (possibly empty).
         */
        public String listing() {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
    }

    /**
     * Perform a radius search centered at (cx, cy) with radius r, counting
     * how many kd-tree nodes are examined. Points on the circle boundary
     * are included. Border pruning honors the tie-to-right policy.
     * 
     * @param cx Center X
     * @param cy Center Y
     * @param r  Non-negative radius
     * @return   The search result (hits + visited count)
     */
    public KDSearchResult rangeSearch(int cx, int cy, int r) {
        KDSearchResult res = new KDSearchResult();
        rangeSearchHelper(root, cx, cy, r, 0, res);
        return res;
    }

    private void rangeSearchHelper(KDNode node, int cx, int cy, int r, int depth,
        KDSearchResult res) {
        if (node == null) {
            return;
        }
        res.visited++;

        int dx = node.city.getX() - cx;
        int dy = node.city.getY() - cy;
        if ((long)dx * dx + (long)dy * dy <= (long)r * r) {
            res.add(node.city);
        }

        boolean even = (depth % 2) == 0;
        if (even) {
            if (cx - r < node.city.getX()) {
                rangeSearchHelper(node.left, cx, cy, r, depth + 1, res);
            }
            if (cx + r >= node.city.getX()) {
                rangeSearchHelper(node.right, cx, cy, r, depth + 1, res);
            }
        }
        else {
            if (cy - r < node.city.getY()) {
                rangeSearchHelper(node.left, cx, cy, r, depth + 1, res);
            }
            if (cy + r >= node.city.getY()) {
                rangeSearchHelper(node.right, cx, cy, r, depth + 1, res);
            }
        }
    }
}
