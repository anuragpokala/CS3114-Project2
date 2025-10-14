// Stores City objects - each node holds one city object
/**
 * BST Class
 * 
 * @author Parth Mehta
 * @version 10/3/2025
 */
public class BST {

    private BSTNode root;
    private int size;

    // Inner class for just BST Nodes
    private class BSTNode {
        private City city;
        private BSTNode left;
        private BSTNode right;

        // Initializes new nodes
        public BSTNode(City city) {
            this.city = city;
            this.left = null;
            this.right = null;
        }
    }

    // BST Constructor Class - Initialize BST Object
    /**
     * Construct an empty BST of {@link City} records.
     */
    public BST() {
        root = null;
        size = 0;
    }

    /**
     * Insert a {@link City} into the BST ordered by name. Ties on the
     * name go to the left (equal keys left).
     *
     * @param cityToInsert
     *            the city record to insert
     */
    public void insert(City cityToInsert) {
        if (root == null) {
            root = new BSTNode(cityToInsert);
            size++;
            return;
        }
        BSTNode current = root;
        while (true) {
            if (cityToInsert.getName().compareTo(current.city.getName()) <= 0) {
                if (current.left == null) {
                    current.left = new BSTNode(cityToInsert);
                    size++;
                    return;
                }
                else {
                    current = current.left;
                }
            }
            else {
                if (current.right == null) {
                    current.right = new BSTNode(cityToInsert);
                    size++;
                    return;
                }
                else {
                    current = current.right;
                }
            }
        }
    }

    /**
     * Inorder listing of the BST with level and indentation as specified:
     * each line begins with the node level, followed by two spaces per level,
     * followed by the city's {@code toString()}. The root has no indentation.
     *
     * @return the formatted inorder traversal string (empty if the tree is empty)
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        toStringHelper(root, 0, result);
        return result.toString();
    }

    private void toStringHelper(BSTNode node, int depth, StringBuilder result) {
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
     * Return the number of nodes currently stored in this BST.
     *
     * @return the size of the BST
     */
    public int getSize() {
        return size;
    }

    // ---- Added: list coordinates for all cities matching a given name (inorder)

    /**
     * Produce a newline-separated, inorder listing of coordinates for all
     * cities whose name equals the given {@code name}. If there are no
     * matches, the empty string is returned. The returned string has no
     * trailing newline.
     *
     * @param name
     *            the city name to match (exact string equality)
     * @return a concatenation of coordinates in the form "(x, y)" separated
     *         by single newlines, or the empty string if none match
     */
    public String listCoordsByName(String name) {
        StringBuilder sb = new StringBuilder();
        listCoordsByNameHelper(root, name, sb);
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    private void listCoordsByNameHelper(
        BSTNode node,
        String name,
        StringBuilder sb) {
        if (node == null) {
            return;
        }
        listCoordsByNameHelper(node.left, name, sb);
        if (node.city.getName().equals(name)) {
            sb.append("(").append(node.city.getX()).append(", ").append(
                node.city.getY()).append(")\n");
        }
        listCoordsByNameHelper(node.right, name, sb);
    }

    /**
     * Return the first node (preorder) whose city name equals {@code name},
     * or {@code null} if none match.
     *
     * @param name
     *            name to match
     * @return the first matching {@link City} in preorder, or {@code null}
     */
    public City findFirstByNamePreorder(String name) {
        return findFirstByNamePreorder(root, name);
    }

    private City findFirstByNamePreorder(BSTNode node, String name) {
        if (node == null) {
            return null;
        }
        if (node.city.getName().equals(name)) {
            return node.city;
        }
        City left = findFirstByNamePreorder(node.left, name);
        if (left != null) {
            return left;
        }
        return findFirstByNamePreorder(node.right, name);
    }

    /**
     * Collect into {@code out} all cities whose name equals {@code name}
     * using an inorder traversal. The array is filled from index 0 and this
     * method returns the number of matches placed into the array.
     *
     * @param name
     *            name to match
     * @param out
     *            destination array (must be large enough)
     * @return number of elements written to {@code out}
     */
    public int collectByName(String name, City[] out) {
        int[] count = new int[] { 0 };
        collectByName(root, name, out, count);
        return count[0];
    }

    private void collectByName(
        BSTNode node,
        String name,
        City[] out,
        int[] count) {
        if (node == null) {
            return;
        }
        collectByName(node.left, name, out, count);
        if (node.city.getName().equals(name)) {
            out[count[0]++] = node.city;
        }
        collectByName(node.right, name, out, count);
    }

    /**
     * Remove from the BST the node whose {@link City} equals {@code target}.
     * If the node has two children, it is replaced by the maximum value in
     * the left subtree (BST rule in the project). If no such node exists,
     * the tree is unchanged and {@code false} is returned.
     *
     * @param target
     *            exact {@link City} record to remove (name, x, y must match)
     * @return {@code true} iff a node was removed
     */
    public boolean removeCity(City target) {
        int before = size;
        root = removeCityRec(root, target);
        return size < before;
    }

    private BSTNode removeCityRec(BSTNode node, City target) {
        if (node == null) {
            return null;
        }
        int cmp = target.getName().compareTo(node.city.getName());
        if (cmp < 0 || (cmp == 0 && !node.city.equals(target))) {
            node.left = removeCityRec(node.left, target);
            return node;
        }
        if (cmp > 0) {
            node.right = removeCityRec(node.right, target);
            return node;
        }
        if (!node.city.equals(target)) {
            node.left = removeCityRec(node.left, target);
            return node;
        }
        if (node.left == null && node.right == null) {
            size--;
            return null;
        }
        if (node.left == null) {
            size--;
            return node.right;
        }
        if (node.right == null) {
            size--;
            return node.left;
        }
        BSTNode pred = node.left;
        while (pred.right != null) {
            pred = pred.right;
        }
        node.city = pred.city;
        node.left = removeCityRec(node.left, pred.city);
        return node;
    }
}
