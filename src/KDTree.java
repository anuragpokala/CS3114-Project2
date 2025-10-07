// Stores City Location Coordinates in a KD Tree
/**
 * KD Tree Class
 * @author Parth Mehta
 * @version 10/3/2025
 */
public class KDTree
{
    private KDNode root;
    private int size;
    
    
    // Subclass to build nodes within KDTree
    private class KDNode
    {
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
    
    public KDTree()
    {
     // Empty tree doesn't contain any nodes yet
        root = null;
        size = 0;
    }
    
    // KD Tree Insert Class
    public void insert(City cityToInsert)
    {
        root = insertHelperMethod(root, cityToInsert, 0);
        size++;
    }
    
    public KDNode insertHelperMethod(KDNode node, City city, int depth)
    {
        // Root is null so inserts city at root node 
        if (node == null) 
        {
            return new KDNode(city); 
        }
        
        // Node not null & on an even level - determines which child to traverse to
        if (depth % 2 == 0)
        {
            if (city.getX() < node.city.getX()) 
            {
                node.left = insertHelperMethod(node.left, city, depth + 1);
            } 
            else 
            {
                node.right = insertHelperMethod(node.right, city, depth + 1);
            }
        }
        // Node not null and on odd level - compare's y attribute & determines child to traverse to 
        else
        {
            if (city.getY() < node.city.getY()) 
            {
                node.left = insertHelperMethod(node.left, city, depth + 1);
            } 
            else 
            {
                node.right = insertHelperMethod(node.right, city, depth + 1);
            }
        }
        
        return node;
    }
    
    public int getSize()
    {
        return size;
    }
    
    
    // Print method for KDTree
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
            } else {
                return containsHelper(node.right, x, y, depth + 1);
            }
        } else {
            if (y < node.city.getY()) {
                return containsHelper(node.left, x, y, depth + 1);
            } else {
                return containsHelper(node.right, x, y, depth + 1);
            }
        }
    }

    
    
}