import student.TestCase;

/**
 * Tests for the name-ordered BST that stores City records.
 * Equal keys go LEFT; delete uses max-from-left; removeMatching 
 * removes an exact triple.
 * @author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */
public class BSTTest extends TestCase 
{

    /**
     * Helper: render BST inorder as "level{2*level spaces}name (x, y)\n"
     * * @param bst The BST to traverse.
     * @return The inorder traversal string with levels.
     */
    private static String inorderToString(BST<City> bst) 
    {
        StringBuilder sb = new StringBuilder();
        bst.inorderWithLevels((lvl, c) -> {
            sb.append(lvl);
            for (int i = 0; i < 2 * lvl; i++) 
            {
                sb.append(" ");
            }
            sb.append(c.getName())
                .append(" (").append(c.getX())
                .append(", ").append(c.getY())
                .append(")\n");
        });
        return sb.toString();
    }


    /**
     * Test basic operations on an empty BST.
     */
    public void testEmptyBasics() 
    {
        BST<City> t = new BST<>();
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
        assertFalse(t.contains(new City("X", 0, 0)));
        assertEquals("", inorderToString(t));
    }

    /**
     * Test that equal keys go to the left and verify the inorder order 
     * reflects the left-bias for equal keys.
     */
    public void testInsertEqualsGoLeftInorderOrder() 
    {
        BST<City> t = new BST<>();
        City a1 = new City("Alpha", 1, 1);
        // equal name -> goes LEFT
        City a2 = new City("Alpha", 2, 2); 
        City z  = new City("Z", 9, 9);

        t.insert(a1);
        t.insert(a2);
        t.insert(z);

        String s = inorderToString(t);
        // equal-keys-left means deeper equal appears first in inorder
        int iA2 = s.indexOf("Alpha (2, 2)");
        int iA1 = s.indexOf("Alpha (1, 1)");
        assertTrue(iA2 < iA1);
        // root level has no extra space
        assertTrue(s.contains("0"));            
        // no space between '0' and name
        assertFalse(s.contains("0 "));          
        assertEquals(3, t.size());
    }

    /**
     * Test the contains and clear operations.
     */
    public void testContainsAndClear() 
    {
        BST<City> t = new BST<>();
        City a = new City("A", 1, 1);
        City b = new City("B", 2, 2);
        t.insert(a); 
        t.insert(b);
        // name compare only
        assertTrue(t.contains(new City("A", 999, 999))); 
        assertTrue(t.contains(new City("B", 0, 0)));
        assertFalse(t.contains(new City("C", 3, 3)));
        t.clear();
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
    }

    /**
     * Test the remove operation (by key name) and verify that it 
     * uses the max-from-left predecessor for nodes with two children.
     */
    public void testRemoveByKeyUsesMaxFromLeft() 
    {
        BST<City> t = new BST<>();
        City m = new City("M", 5, 5);
        City c = new City("C", 1, 1);
        // will become predecessor of M
        City k = new City("K", 2, 2); 
        City z = new City("Z", 9, 9);
        t.insert(m); 
        t.insert(c); 
        t.insert(k); 
        t.insert(z);

        // remove by key (name)
        assertTrue(t.remove(new City("M", 0, 0))); 
        String s = inorderToString(t);
        assertFalse(s.contains("M (5, 5)"));
        // predecessor moved up
        assertTrue(s.contains("K (2, 2)"));       
        assertEquals(3, t.size());
    }

    /**
     * Test the removeMatching operation which should only remove 
     * a City record if the name, X, and Y coordinates all match 
     * the predicate.
     */
    public void testRemoveMatchingRemovesExactTripleOnly() 
    {
        BST<City> t = new BST<>();
        City n1 = new City("N", 1, 1);
        City n2 = new City("N", 2, 2);
        City z  = new City("Z", 9, 9);
        t.insert(n1); 
        t.insert(n2); 
        t.insert(z);

        // wrong coords should not delete
        assertFalse(t.removeMatching(new City("N", 123, 456),
            c -> c.getName().equals("N") && c.getX() == 123 
                && c.getY() == 456));
        assertEquals(3, t.size());

        // exact delete of (N,2,2)
        assertTrue(t.removeMatching(new City("N", 2, 2),
            c -> c.getName().equals("N") && c.getX() == 2 
                && c.getY() == 2));
        assertEquals(2, t.size());
        String s = inorderToString(t);
        assertTrue(s.contains("N (1, 1)"));
        assertFalse(s.contains("N (2, 2)"));
    }

    /**
     * Test the indentation format from inorderToString to ensure 
     * it matches the level (depth * 2 spaces).
     */
    public void testIndentationExactAcrossDepths() 
    {
        BST<City> t = new BST<>();
        // depth 0
        t.insert(new City("D0", 0, 0));     
        // depth 1 (equal-left via name order)
        t.insert(new City("D1", -1, -1));   
        // depth 2
        t.insert(new City("D2", -2, -2));   
        // depth 3
        t.insert(new City("D3", -3, -3));   
        String s = inorderToString(t);
        assertTrue(s.contains("0D0"));
        assertTrue(s.contains("1  D1"));
        assertTrue(s.contains("2    D2"));
        assertTrue(s.contains("3      D3"));
    }
    
    /**
     * Test removeMatching on a node with ONLY a right child.
     * Kills mutations on "if (n.left == null)" line.
     */
    public void testRemoveMatchingNodeWithOnlyRightChild() 
    {
        BST<City> t = new BST<>();
        
        // Build tree where we can remove a node with only right child
        // Root
        t.insert(new City("M", 50, 50));       
        // Left of M - this will have only right child
        t.insert(new City("Parent", 25, 25)); 
        // Right child of Parent (only child)
        t.insert(new City("RChild", 30, 30));  
        
        assertEquals(3, t.size());
        
        // Remove Parent which has ONLY a right child
        assertTrue(t.removeMatching(
            new City("Parent", 25, 25),
            c -> c.getName().equals("Parent") && c.getX() == 25 
                && c.getY() == 25
        ));
        
        assertEquals(2, t.size());
        String s = inorderToString(t);
        
        // Parent should be gone
        assertFalse(s.contains("Parent (25, 25)"));
        
        // RChild should be promoted to Parent's position
        assertTrue(s.contains("RChild (30, 30)"));
        
        // M should still exist
        assertTrue(s.contains("M (50, 50)"));
    }

    /**
     * Test removeMatching on a node with ONLY a left child.
     * Kills mutations on "if (n.right == null)" line.
     */
    public void testRemoveMatchingNodeWithOnlyLeftChild() 
    {
        BST<City> t = new BST<>();
        
        // Build tree where we can remove a node with only left child
        // Root
        t.insert(new City("M", 50, 50));      
        // Right of M - this will have only left child
        t.insert(new City("Parent", 75, 75)); 
        // Left child of Parent (only child)
        t.insert(new City("LChild", 60, 60)); 
        
        assertEquals(3, t.size());
        
        // Remove Parent which has ONLY a left child
        assertTrue(t.removeMatching(
            new City("Parent", 75, 75),
            c -> c.getName().equals("Parent") && c.getX() == 75 
                && c.getY() == 75
        ));
        
        assertEquals(2, t.size());
        String s = inorderToString(t);
        
        // Parent should be gone
        assertFalse(s.contains("Parent (75, 75)"));
        
        // LChild should be promoted to Parent's position
        assertTrue(s.contains("LChild (60, 60)"));
        
        // M should still exist
        assertTrue(s.contains("M (50, 50)"));
    }

    /**
     * Test removeMatching on a leaf node (both children null).
     * Ensures the checks work correctly when both are null.
     */
    public void testRemoveMatchingLeafNode() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("Root", 50, 50));
        // Left leaf
        t.insert(new City("Leaf", 25, 25));  
        
        assertEquals(2, t.size());
        
        // Remove the leaf
        assertTrue(t.removeMatching(
            new City("Leaf", 25, 25),
            c -> c.getName().equals("Leaf") && c.getX() == 25 
                && c.getY() == 25
        ));
        
        assertEquals(1, t.size());
        String s = inorderToString(t);
        assertFalse(s.contains("Leaf"));
        assertTrue(s.contains("Root"));
    }

    /**
     * Test removeMatching with two children to ensure we don't accidentally
     * take the single-child path when both children exist.
     * This is a defensive test for the mutation where checks become false.
     */
    public void testRemoveMatchingNodeWithTwoChildren() 
    {
        BST<City> t = new BST<>();
        // Root - will have two children
        t.insert(new City("M", 50, 50));      
        // Left child
        t.insert(new City("L", 25, 25));      
        // Right child
        t.insert(new City("R", 75, 75));      
        // Left-left (so L has a child)
        t.insert(new City("LL", 10, 10));     
        
        assertEquals(4, t.size());
        
        // Remove M which has TWO children
        assertTrue(t.removeMatching(
            new City("M", 50, 50),
            c -> c.getName().equals("M") && c.getX() == 50 
                && c.getY() == 50
        ));
        
        assertEquals(3, t.size());
        String s = inorderToString(t);
        
        // M should be gone
        assertFalse(s.contains("M (50, 50)"));
        
        // The predecessor (max from left = L) should have moved up
        assertTrue(s.contains("L (25, 25)"));
        
        // Other nodes should still exist
        assertTrue(s.contains("R (75, 75)"));
        assertTrue(s.contains("LL (10, 10)"));
    }
    
    /**
     * Test remove() on a node with ONLY a right child.
     * Kills mutation on "if (n.left == null)" line in removeRec.
     */
    public void testRemoveNodeWithOnlyRightChild() 
    {
        BST<City> t = new BST<>();
        
        // Build tree where we remove a node with only right child
        // Root
        t.insert(new City("M", 50, 50));       
        // Left of M
        t.insert(new City("Parent", 25, 25)); 
        // Right child of Parent (only child)
        t.insert(new City("RChild", 30, 30)); 
        
        assertEquals(3, t.size());
        
        // Remove Parent which has ONLY a right child
        assertTrue(t.remove(new City("Parent", 25, 25)));
        
        assertEquals(2, t.size());
        String s = inorderToString(t);
        
        // Parent should be gone
        assertFalse(s.contains("Parent (25, 25)"));
        
        // RChild should be promoted to Parent's position
        assertTrue(s.contains("RChild (30, 30)"));
        
        // M should still exist
        assertTrue(s.contains("M (50, 50)"));
    }

    /**
     * Test remove() on a node with ONLY a left child.
     * Kills mutation on "if (n.right == null)" line in removeRec.
     */
    public void testRemoveNodeWithOnlyLeftChild() 
    {
        BST<City> t = new BST<>();
        
        // Build tree where we remove a node with only left child
        // Root
        t.insert(new City("M", 50, 50));      
        // Right of M
        t.insert(new City("Parent", 75, 75)); 
        // Left child of Parent (only child)
        t.insert(new City("LChild", 60, 60)); 
        
        assertEquals(3, t.size());
        
        // Remove Parent which has ONLY a left child
        assertTrue(t.remove(new City("Parent", 75, 75)));
        
        assertEquals(2, t.size());
        String s = inorderToString(t);
        
        // Parent should be gone
        assertFalse(s.contains("Parent (75, 75)"));
        
        // LChild should be promoted to Parent's position
        assertTrue(s.contains("LChild (60, 60)"));
        
        // M should still exist
        assertTrue(s.contains("M (50, 50)"));
    }

    /**
     * Test remove() on a leaf node (both children null).
     * Ensures both null checks work correctly.
     */
    public void testRemoveLeafNode() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("Root", 50, 50));
        // Left leaf
        t.insert(new City("Leaf", 25, 25));  
        
        assertEquals(2, t.size());
        
        // Remove the leaf
        assertTrue(t.remove(new City("Leaf", 25, 25)));
        
        assertEquals(1, t.size());
        String s = inorderToString(t);
        assertFalse(s.contains("Leaf"));
        assertTrue(s.contains("Root"));
    }

    /**
     * Test remove() on a node with two children - should use max from left.
     * Ensures we don't accidentally enter single-child path when both exist.
     * Kills mutations where equality checks become true/false incorrectly.
     */
    public void testRemoveNodeWithTwoChildren() 
    {
        BST<City> t = new BST<>();
        // Root - will have two children
        t.insert(new City("M", 50, 50));      
        // Left child
        t.insert(new City("L", 25, 25));      
        // Right child
        t.insert(new City("R", 75, 75));      
        // Left-left (so L has a child)
        t.insert(new City("LL", 10, 10));     
        
        assertEquals(4, t.size());
        
        // Remove M which has TWO children
        assertTrue(t.remove(new City("M", 50, 50)));
        
        assertEquals(3, t.size());
        String s = inorderToString(t);
        
        // M should be gone
        assertFalse(s.contains("M (50, 50)"));
        
        // The predecessor (max from left = L) should have moved up
        assertTrue(s.contains("L (25, 25)"));
        
        // Other nodes should still exist
        assertTrue(s.contains("R (75, 75)"));
        assertTrue(s.contains("LL (10, 10)"));
    }
    
    /**
     * Test to explicitly catch mutations in: 
     * inorderRec(n.left, level + 1, visit)
     * * Mutation 1: level + 1 -> level (removed increment)
     * Mutation 2: level + 1 -> level - 1 (wrong operation)
     */
    public void testInorderRecLevelIncrement() {
        BST<City> t = new BST<>();
        t.insert(new City("M", 50, 50));
        t.insert(new City("K", 25, 25));
        t.insert(new City("R", 75, 75));
        t.insert(new City("A", 10, 10));  // "A" < "K" -> left of K

        // Collect inorder results without using ArrayList/StringBag
        String[] names = new String[4];
        int[] levels = new int[4];
        final int[] idx = {0};
        t.inorderWithLevels((lvl, c) -> {
            names[idx[0]] = c.getName();
            levels[idx[0]] = lvl;
            idx[0]++;
        });

        // expected inorder: A(2), K(1), M(0), R(1)
        assertEquals(4, idx[0]);
        assertEquals("A", names[0]);
        assertEquals("K", names[1]);
        assertEquals("M", names[2]);
        assertEquals("R", names[3]);

        assertEquals(2, levels[0]);
        assertEquals(1, levels[1]);
        assertEquals(0, levels[2]);
        assertEquals(1, levels[3]);
    }


    /**
     * Catch mutation: level + 1 -> level (no increment)
     * This would make all descendants report same level as parent
     */
    public void testLevelIncrementNotMissing() {
        // Three-level chain: A(root) -> B(left) -> C(left)
        BST<City> t = new BST<>();
        t.insert(new City("A", 50, 50));  // depth 0
        t.insert(new City("B", 25, 25));  // depth 1
        t.insert(new City("C", 10, 10));  // depth 2

        // Capture the reported level for node "C" during inorder
        final int[] maxLevel = new int[] { -1 };
        t.inorderWithLevels((lvl, c) -> {
            if ("C".equals(c.getName())) {
                maxLevel[0] = lvl;
            }
        });

        // If level+1 were mutated away, C would not be at 2
        assertEquals(2, maxLevel[0]);
    }


    /**
     * Catch mutation: level + 1 -> level - 1 (decrement instead of increment)
     * This would make deeper levels have LOWER level numbers
     */
    public void testLevelMustIncrement() {
        BST<City> t = new BST<>();

        // Build a balanced tree to test multiple levels
        t.insert(new City("D", 50, 50));  // level 0
        t.insert(new City("B", 25, 25));  // level 1
        t.insert(new City("F", 75, 75));  // level 1
        t.insert(new City("A", 10, 10));  // level 2
        t.insert(new City("C", 30, 30));  // level 2

        // Use a single-element array as a mutable int
        final int[] minLevelForLeaves = new int[] { Integer.MAX_VALUE };

        t.inorderWithLevels((lvl, c) -> {
            // A and C are leaves (deepest)
            if ("A".equals(c.getName()) || "C".equals(c.getName())) {
                if (lvl < minLevelForLeaves[0]) {
                    minLevelForLeaves[0] = lvl;
                }
            }
        });

        // Leaves should be at level 2, not level 0, 1, or negative
        assertEquals("Leaves should be at level 2", 2, minLevelForLeaves[0]);
        assertTrue("Level should be positive", minLevelForLeaves[0] >= 0);
    }


    /**
     * Comprehensive test: verify exact level sequence matches tree depth
     * Catches any arithmetic error in level calculation
     */
    public void testLevelSequenceCorrectness() {
        // Left-skewed tree via descending names: C -> B -> A
        BST<City> t = new BST<>();
        t.insert(new City("C", 50, 50));  // depth 0
        t.insert(new City("B", 40, 40));  // depth 1 (left of C)
        t.insert(new City("A", 30, 30));  // depth 2 (left of B)

        // Collect exactly three levels in inorder into a fixed array
        final int[] levels = new int[3];
        final int[] idx = new int[] { 0 };
        t.inorderWithLevels((lvl, c) -> {
            // A, B, C will arrive in that order (inorder of left-skew)
            if (idx[0] < levels.length) {
                levels[idx[0]++] = lvl;
            }
        });

        // Inorder of left-skew: A(2), B(1), C(0)
        assertEquals(3, idx[0]);      // captured exactly three nodes
        assertEquals(2, levels[0]);   // A
        assertEquals(1, levels[1]);   // B
        assertEquals(0, levels[2]);   // C
    }

    
    /**
     * Test to catch mutation in: public boolean isEmpty() { return size == 0; }
     * * Mutation: size == 0 replaced with true
     * Effect: isEmpty() would always return true, even when tree has nodes
     */
    public void testIsEmptyMutationAlwaysTrue() 
    {
        BST<City> t = new BST<>();
        
        // Empty tree should be empty
        assertTrue("Empty tree should be empty", t.isEmpty());
        
        // After inserting one node, tree should NOT be empty
        t.insert(new City("A", 1, 1));
        assertFalse("Tree with 1 node should not be empty", t.isEmpty());
        
        // After inserting multiple nodes, tree should NOT be empty
        t.insert(new City("B", 2, 2));
        t.insert(new City("C", 3, 3));
        assertFalse("Tree with 3 nodes should not be empty", t.isEmpty());
        
        // After clearing, tree should be empty again
        t.clear();
        assertTrue("Cleared tree should be empty", t.isEmpty());
    }

    /**
     * Test to catch mutation in: 
     * if (x == null) throw new IllegalArgumentException(...)
     * * Mutation: x == null replaced with false (null check always fails)
     * Effect: insert(null) would succeed instead of throwing exception
     */
    public void testInsertNullMutationCheckAlwaysFalse() 
    {
        BST<City> t = new BST<>();
        
        // Inserting null should throw IllegalArgumentException
        try {
            t.insert(null);
            fail("insert(null) should throw IllegalArgumentException");
        } 
        catch (IllegalArgumentException e) {
            // Expected behavior
            assertTrue("Exception message should mention 'null'", 
                       e.getMessage().contains("null"));
        }
        
        // Tree should remain empty and size should be 0
        assertTrue("Tree should still be empty after null insert attempt", 
            t.isEmpty());
        assertEquals("Size should remain 0", 0, t.size());
    }

    /**
     * Additional defensive test: ensure isEmpty() 
     * correctly reflects size changes
     * This catches any mutation to isEmpty() logic
     */
    public void testIsEmptyTracksSize() 
    {
        BST<City> t = new BST<>();
        
        // Start empty
        assertEquals("Size should be 0", 0, t.size());
        assertTrue("isEmpty should match size == 0", 
            t.isEmpty() == (t.size() == 0));
        
        // Add node
        t.insert(new City("X", 0, 0));
        assertEquals("Size should be 1", 1, t.size());
        assertTrue("isEmpty should match size == 0", 
            t.isEmpty() == (t.size() == 0));
        
        // Add more nodes
        t.insert(new City("Y", 1, 1));
        t.insert(new City("Z", 2, 2));
        assertEquals("Size should be 3", 3, t.size());
        assertTrue("isEmpty should match size == 0", 
            t.isEmpty() == (t.size() == 0));
        
        // Clear
        t.clear();
        assertEquals("Size should be 0", 0, t.size());
        assertTrue("isEmpty should match size == 0", 
            t.isEmpty() == (t.size() == 0));
    }

    /**
     * Test that null insertion consistently fails regardless of tree state
     * Catches mutations where null check might be bypassed
     */
    public void testInsertNullConsistentlyThrows() 
    {
        BST<City> t = new BST<>();
        
        // Null should throw on empty tree
        try {
            t.insert(null);
            fail("insert(null) on empty tree should throw");
        } 
        catch (IllegalArgumentException e) {
            // Expected
        }
        
        // Add some nodes
        t.insert(new City("A", 1, 1));
        t.insert(new City("B", 2, 2));
        
        // Null should still throw on non-empty tree
        try {
            t.insert(null);
            fail("insert(null) on non-empty tree should throw");
        } 
        catch (IllegalArgumentException e) {
            // Expected
        }
        
        // Verify size didn't change
        assertEquals("Size should be 2", 2, t.size());
    }
    
    /**
     * Test to catch mutation in: if (key == null) return false;
     * * Mutation: key == null replaced with false
     * Effect: null key check is never performed; remove(null) 
     * proceeds instead of returning false
     */
    public void testRemoveNullKeyMutation() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("A", 1, 1));
        t.insert(new City("B", 2, 2));
        
        // Remove with null key should return false without crashing
        assertFalse("remove(null) should return false", t.remove(null));
        
        // Tree should be unchanged
        assertEquals("Size should still be 2", 2, t.size());
        assertTrue("A should still be in tree", 
            t.contains(new City("A", 1, 1)));
        assertTrue("B should still be in tree", 
            t.contains(new City("B", 2, 2)));
    }

    /**
     * Additional test: null removal on empty tree should return false
     * Catches mutation where null check is bypassed
     */
    public void testRemoveNullOnEmptyTree() 
    {
        BST<City> t = new BST<>();
        
        // Remove null from empty tree should return false
        assertFalse("remove(null) on empty tree should return false", 
            t.remove(null));
        
        // Tree should remain empty
        assertTrue("Tree should still be empty", t.isEmpty());
        assertEquals("Size should be 0", 0, t.size());
    }

    /**
     * Test to catch mutation in: if (r.removed) { size = size - 1; }
     * * Mutation: r.removed replaced with true
     * Effect: size decremented even when node wasn't actually removed
     */
    public void testRemoveSizeDecrementMutation() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("A", 1, 1));
        t.insert(new City("B", 2, 2));
        t.insert(new City("C", 3, 3));
        
        assertEquals("Initial size should be 3", 3, t.size());
        
        // Try to remove a node that doesn't exist
        assertFalse("Remove non-existent node should return false", 
                    t.remove(new City("Z", 99, 99)));
        
        // Size should NOT have changed
        assertEquals("Size should still be 3 "
            + "after failed remove", 3, t.size());
        
        // All original nodes should still exist
        assertTrue("A should still exist", t.contains(new City("A", 1, 1)));
        assertTrue("B should still exist", t.contains(new City("B", 2, 2)));
        assertTrue("C should still exist", t.contains(new City("C", 3, 3)));
    }

    /**
     * Test: size tracking across multiple failed removals
     * Catches mutation where size decrements regardless of success
     */
    public void testSizeTracksFailedRemovals() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("A", 1, 1));
        
        assertEquals("Initial size should be 1", 1, t.size());
        
        // Attempt multiple failed removals
        assertFalse("First failed remove", t.remove(new City("X", 0, 0)));
        assertEquals("Size should still be 1", 1, t.size());
        
        assertFalse("Second failed remove", t.remove(new City("Y", 0, 0)));
        assertEquals("Size should still be 1", 1, t.size());
        
        assertFalse("Third failed remove", t.remove(new City("Z", 0, 0)));
        assertEquals("Size should still be 1", 1, t.size());
        
        // A should still be there
        assertTrue("A should still exist", t.contains(new City("A", 1, 1)));
    }

    /**
     * Test: size only decrements on successful removal
     * Interleaves successful and failed removals
     */
    public void testSizeDecrementsOnlyOnSuccess() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("A", 1, 1));
        t.insert(new City("B", 2, 2));
        t.insert(new City("C", 3, 3));
        
        assertEquals("Initial size should be 3", 3, t.size());
        
        // Failed removal
        assertFalse("Remove non-existent X", t.remove(new City("X", 0, 0)));
        assertEquals("Size should still be 3", 3, t.size());
        
        // Successful removal
        assertTrue("Remove existing A", t.remove(new City("A", 1, 1)));
        assertEquals("Size should be 2 after successful remove", 2, t.size());
        
        // Failed removal
        assertFalse("Remove non-existent Y", t.remove(new City("Y", 0, 0)));
        assertEquals("Size should still be 2", 2, t.size());
        
        // Successful removal
        assertTrue("Remove existing B", t.remove(new City("B", 2, 2)));
        assertEquals("Size should be 1 after second successful remove", 1, 
            t.size());
        
        // Failed removal
        assertFalse("Remove non-existent Z", t.remove(new City("Z", 0, 0)));
        assertEquals("Size should still be 1", 1, t.size());
    }

    /**
     * Test: null key removal doesn't affect size
     * Specifically catches mutation where key==null check is bypassed
     */
    public void testRemoveNullDoesNotAffectSize() 
    {
        BST<City> t = new BST<>();
        t.insert(new City("A", 1, 1));
        t.insert(new City("B", 2, 2));
        
        int originalSize = t.size();
        
        // Multiple null removals should not change size
        t.remove(null);
        assertEquals("Size unchanged after remove(null)", originalSize, 
            t.size());
        
        t.remove(null);
        assertEquals("Size unchanged "
            + "after second remove(null)", originalSize, 
            t.size());
        
        t.remove(null);
        assertEquals("Size unchanged after third remove(null)", originalSize, 
            t.size());
    }

    /**
     * Comprehensive test: remove tracks both success and size correctly
     * Combines both mutations into one scenario
     */
    public void testRemoveReturnValueAndSize() 
    {
        BST<City> t = new BST<>();
        City a = new City("A", 1, 1);
        City b = new City("B", 2, 2);
        t.insert(a);
        t.insert(b);
        
        assertEquals("Initial size 2", 2, t.size());
        
        // Remove with null returns false, size unchanged
        boolean resultNull = t.remove(null);
        assertFalse("remove(null) returns false", resultNull);
        assertEquals("Size unchanged after remove(null)", 2, t.size());
        
        // Remove non-existent returns false, size unchanged
        boolean resultNone = t.remove(new City("Z", 99, 99));
        assertFalse("remove(non-existent) returns false", resultNone);
        assertEquals("Size unchanged after "
            + "remove(non-existent)", 2, t.size());
        
        // Remove existing returns true, size decrements
        boolean resultA = t.remove(a);
        assertTrue("remove(existing) returns true", resultA);
        assertEquals("Size decremented to 1", 1, t.size());
        
        // Remove last returns true, size decrements
        boolean resultB = t.remove(b);
        assertTrue("remove(last) returns true", resultB);
        assertEquals("Size decremented to 0", 0, t.size());
        
        // Remove from empty returns false, size stays 0
        boolean resultEmpty = t.remove(a);
        assertFalse("remove(from empty) returns false", resultEmpty);
        assertEquals("Size stays 0", 0, t.size());
    }
    
    /**
     * Test to catch mutation in contains: cur = (cmp < 0) 
     * ? cur.left : cur.right;
     * * Mutation: cmp < 0 replaced with false
     * Effect: the left branch of ternary is never taken
     * cur always goes right, breaking binary search 
     * for nodes in left subtree
     */
    public void testContainsLeftSubtreeMutation() 
    {
        BST<City> t = new BST<>();

        
        t.insert(new City("M", 50, 50));
        t.insert(new City("C", 25, 25));
        t.insert(new City("Z", 90, 90));
        t.insert(new City("A", 10, 10));
        t.insert(new City("K", 30, 30));
        
        // These nodes are in the LEFT subtree and require cmp < 0 to find them
        assertTrue("Should contain C (left of root)", 
            t.contains(new City("C", 0, 0)));
        assertTrue("Should contain A (left-left)", 
            t.contains(new City("A", 0, 0)));
        assertTrue("Should contain K (left-right)", 
            t.contains(new City("K", 0, 0)));
        
    }

    /**
     * Test: verify contains works for nodes requiring left traversal
     * Directly tests the critical left branch
     */
    public void testContainsRequiresLeftBranch() 
    {
        BST<City> t = new BST<>();
        
        // Build right-skewed tree at root level to force left searches
        // root
        t.insert(new City("Z", 99, 99));    
        // must go left from Z
        t.insert(new City("A", 1, 1));      
        
        // A is in the left subtree—if cmp < 0 is false, contains fails
        assertTrue("Should find A (left of Z)", 
            t.contains(new City("A", 0, 0)));
        assertFalse("Should not find B", t.contains(new City("B", 0, 0)));
    }

    /**
     * Test: ensure contains correctly rejects non-existent left-side nodes
     * If mutation exists, false positives may occur
     */
    public void testContainsRejectsNonExistentLeft() 
    {
        BST<City> t = new BST<>();
        
        t.insert(new City("M", 50, 50));
        t.insert(new City("C", 25, 25));
        t.insert(new City("Z", 90, 90));
        t.insert(new City("A", 10, 10));
        t.insert(new City("K", 30, 30));
        
        // These should NOT be found (not in tree)
        assertFalse("Should not contain B", t.contains(new City("B", 0, 0)));
        assertFalse("Should not contain D", t.contains(new City("D", 0, 0)));
        assertFalse("Should not contain J", t.contains(new City("J", 0, 0)));
        
        // But these SHOULD be found (in tree)
        assertTrue("Should contain A", t.contains(new City("A", 0, 0)));
        assertTrue("Should contain C", t.contains(new City("C", 0, 0)));
        assertTrue("Should contain K", t.contains(new City("K", 0, 0)));
    }

    /**
     * Test: deep left subtree search
     * Forces multiple left comparisons in a chain
     */
    public void testContainsDeepLeftChain() 
    {
        BST<City> t = new BST<>();
        
        // Create a left-leaning chain: Z -> Y -> X -> W -> V
        t.insert(new City("Z", 99, 99));
        t.insert(new City("Y", 80, 80));
        t.insert(new City("X", 60, 60));
        t.insert(new City("W", 40, 40));
        t.insert(new City("V", 20, 20));
        
        assertEquals("Size should be 5", 5, t.size());
        
        // All of these require left traversals; 
        // if cmp < 0 is false, they won't be found
        assertTrue("Should find Y (1 left from root)", 
            t.contains(new City("Y", 0, 0)));
        assertTrue("Should find X (2 lefts from root)", 
            t.contains(new City("X", 0, 0)));
        assertTrue("Should find W (3 lefts from root)", 
            t.contains(new City("W", 0, 0)));
        assertTrue("Should find V (4 lefts from root)", 
            t.contains(new City("V", 0, 0)));
        
        // These should not be found
        assertFalse("Should not find U", t.contains(new City("U", 0, 0)));
        assertFalse("Should not find T", t.contains(new City("T", 0, 0)));
    }

    /**
     * Test: balanced tree with left and right searches
     * Ensures both branches work correctly
     */
    public void testContainsMixedLeftRight() 
    {
        BST<City> t = new BST<>();
        
        t.insert(new City("M", 50, 50));
        t.insert(new City("E", 25, 25));
        t.insert(new City("S", 75, 75));
        t.insert(new City("A", 10, 10));
        t.insert(new City("H", 35, 35));
        t.insert(new City("P", 60, 60));
        t.insert(new City("Z", 90, 90));
        
        // Test all nodes are found
        for (String name : new String[]{"A", "E", "H", "M", "P", "S", "Z"}) {
            assertTrue("Should contain " + name, 
                       t.contains(new City(name, 0, 0)));
        }
        
        // Test non-existent nodes are not found
        String[] nonExistent = {"B", "D", "F", "G", "K", "L", "N", "O", "Q", 
            "R", "T", "U", "V", "W", "X", "Y"};
        for (String name : nonExistent) {
            assertFalse("Should not contain " + name, 
                        t.contains(new City(name, 0, 0)));
        }
    }

    /**
     * Comprehensive test: boundary cases for left branch
     * Nodes at the extreme left should require cmp < 0
     */
    public void testContainsExtremeLefts() 
    {
        BST<City> t = new BST<>();
        
        // Insert nodes with deliberately chosen names to force left branches
        // root
        t.insert(new City("Z", 99, 99));  
        // left of Z
        t.insert(new City("M", 50, 50));  
        // left of M
        t.insert(new City("A", 1, 1));    
        
        // Each requires a left branch from parent
        assertTrue("Should find M (left of Z)", 
            t.contains(new City("M", 0, 0)));
        assertTrue("Should find A (left of M)", 
            t.contains(new City("A", 0, 0)));
        
        // Nodes between shouldn't be found
        assertFalse("Should not find B", t.contains(new City("B", 0, 0)));
        assertFalse("Should not find N", t.contains(new City("N", 0, 0)));
    }
       
}
