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
    public BST() {
        // Empty tree doesn't contain any nodes yet
        root = null;
        size = 0;
    }


    public void insert(City cityToInsert) {
        // Exception for if BST is null
        if (root == null) {
            root = new BSTNode(cityToInsert);
            size++;
            return;
        }

        BSTNode current = root;

        while (true) {
            // If City is less than current node go to left child

            if (cityToInsert.getName().compareTo(current.city.getName()) <= 0) {
                // If left child empty place there
                if (current.left == null) {
                    current.left = new BSTNode(cityToInsert);
                    size++;
                    return;
                }
                // Otherwise left child is new current node
                else {
                    current = current.left;
                }
            }
            // Then check right child
            else {
                // Check if right child is null to add city
                if (current.right == null) {
                    current.right = new BSTNode(cityToInsert);
                    size++;
                    return;
                }
                // Right child is new current node
                else {
                    current = current.right;
                }
            }
        }
    }


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


    // Addresses mutation coverage for line 68
    public int getSize() {
        return size;
    }


    // ---- Added: list coordinates for all cities matching a given name
    // (inorder)
    public String listCoordsByName(String name) {
        StringBuilder sb = new StringBuilder();
        listCoordsByNameHelper(root, name, sb);
        // trim trailing newline if present
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
}
