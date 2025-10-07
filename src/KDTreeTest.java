import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class KDTreeTest extends TestCase 
{
    public void testKDTreeInsertEmpty() 
    {
        KDTree tree = new KDTree();
        tree.insert(new City("Denver", 100, 200));
        String output = tree.toString();
        assertTrue(output.contains("Denver"));
    }
        
    public void testKDTreeInsertX() {
        KDTree tree = new KDTree();
        tree.insert(new City("Denver", 100, 200));
        tree.insert(new City("Boston", 50, 300));
        tree.insert(new City("NYC", 150, 100));

        String output = tree.toString();
        
        int bostonIdx = output.indexOf("Boston");
        int denverIdx = output.indexOf("Denver");
        int nycIdx = output.indexOf("NYC");
        
        assertTrue(bostonIdx < denverIdx); 
        assertTrue(denverIdx < nycIdx);  
    }
    
    public void testKDTreeInsertY() {
        KDTree tree = new KDTree();
        tree.insert(new City("Denver", 100, 200));
        tree.insert(new City("Boston", 50, 300));     
        tree.insert(new City("Atlanta", 60, 150));
        tree.insert(new City("Chicago", 70, 400)); 
                                                    
        
        String output = tree.toString();
        assertTrue(output.contains("Atlanta"));
        assertTrue(output.contains("Chicago"));

    }
    
    public void testKDTreeSize() {
        KDTree tree = new KDTree();
        assertEquals(0, tree.getSize());
        tree.insert(new City("Denver", 100, 200));
        assertEquals(1, tree.getSize());
        tree.insert(new City("Boston", 50, 100));
        assertEquals(2, tree.getSize());
    }
    
    public void testKDTreePrintEmpty() {
        KDTree tree = new KDTree();
        assertEquals("", tree.toString());
    }
    
    // Test that root splits on X (even level)
    public void testRootSplitsOnX() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("Left", 50, 200));   // X < 100, should go left
        tree.insert(new City("Right", 150, 50));  // X > 100, should go right
        
        String output = tree.toString();
        
        // Root should be at level 0
        assertTrue(output.contains("0Root"));
        // Both children should be at level 1
        assertTrue(output.contains("1  Left") || output.contains("1  Right"));
    }

    // Test that level 1 splits on Y (odd level)
    public void testLevel1SplitsOnY() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("Child", 50, 150));     // Goes left on X
        tree.insert(new City("GrandL", 60, 100));    // Y < 150, should go left of Child
        tree.insert(new City("GrandR", 40, 200));    // Y > 150, should go right of Child
        
        String output = tree.toString();
        
        // Verify depth progression exists
        assertTrue(output.contains("0Root"));
        assertTrue(output.contains("1  Child"));
        assertTrue(output.contains("2    Grand")); // Either grandchild at level 2
    }

    // Test equal values go right
    public void testEqualValuesGoRight() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("EqualX", 100, 50));  // Equal X, should go RIGHT
        
        String output = tree.toString();
        
        // In inorder: left subtree, root, right subtree
        // If EqualX went right, it appears after Root in inorder
        int rootIdx = output.indexOf("Root");
        int equalIdx = output.indexOf("EqualX");
        assertTrue(equalIdx > rootIdx); // EqualX appears after Root in inorder
    }
    
    public void testYDiscriminationAtLevel1() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 100, 100));
        tree.insert(new City("B", 50, 50));   // Left child (X < 100)
        tree.insert(new City("C", 75, 25));   // Should be child of B
        
        // At level 1, should compare Y: C.Y=25 < B.Y=50, goes left
        // If always comparing X: C.X=75 > B.X=50, goes right
        
        String output = tree.toString();
        int bIdx = output.indexOf("B");
        int cIdx = output.indexOf("C");
        
        assertTrue(cIdx < bIdx);  // C before B in inorder = C is left child
    }
    
    public void testDepthIncrementsCorrectly() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 100, 100));
        tree.insert(new City("B", 50, 50));
        tree.insert(new City("C", 25, 25));
        
        String output = tree.toString();
        
        // Verify each level has the correct depth number
        assertTrue(output.contains("0A"));       // Level 0
        assertTrue(output.contains("1  B"));     // Level 1 (2 spaces)
        assertTrue(output.contains("2    C"));   // Level 2 (4 spaces)
        
        // Also verify they don't have wrong depth
        assertFalse(output.contains("0B"));
        assertFalse(output.contains("0C"));
    }

    public void testRightSubtreeDepth() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 100, 100));
        tree.insert(new City("R", 150, 150));
        tree.insert(new City("RR", 175, 175));
        
        String output = tree.toString();
        
        // Verify right path increments depth correctly
        assertTrue(output.contains("0A"));
        assertTrue(output.contains("1  R"));
        assertTrue(output.contains("2    RR"));
    }

    /**
     * Tie on the ODD level (compare Y at depth 1) should go RIGHT.
     * Build: Root(100,100) -> Left(50,150) at depth 1; then EqualY(60,150).
     * EqualY must become RIGHT child of Left, so inorder prints Left before EqualY.
     */
    public void testEqualYAtOddLevelGoesRight()
    {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));   // depth 0 -> split on X
        tree.insert(new City("Left", 50, 150));    // goes left; depth 1 -> split on Y
        tree.insert(new City("EqualY", 60, 150));  // Y ties at depth 1 -> RIGHT of Left

        String out = tree.toString();
        assertTrue(out.contains("1  Left"));
        assertTrue(out.contains("2    EqualY"));
        // Inorder within Left's subtree: Left THEN EqualY (since EqualY is right child)
        assertTrue(out.indexOf("Left") < out.indexOf("EqualY"));

        // Root line might not be first if left subtree exists
        assertTrue(out.startsWith("0") || out.contains("\n0"));
        assertFalse(out.startsWith("0 ") || out.contains("\n0 "));
    }


    /**
     * Guard against accidental indentation of the root in KD print.
     */
    public void testKDPrintRootNoIndent()
    {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 10, 10));
        String out = tree.toString();
        assertTrue(out.startsWith("0"));
        assertFalse(out.startsWith("0 "));
    }
    
    public void testDeepLevelsBothSides() {
        KDTree t = new KDTree();
        // depth 0 (split X)
        t.insert(new City("R", 100, 100));

        // LEFT subtree path to depth 3:
        // depth 1 (Y):  y=150 > 100 -> goes RIGHT of the "left" child if tie, but here we control strictly
        t.insert(new City("L1",  50, 150));    // goes LEFT of R (x < 100), depth 1 (split Y)
        t.insert(new City("L2",  40, 140));    // depth 2 under L1: y < 150 -> LEFT of L1 (since depth 1 compares Y)
        t.insert(new City("L3",  41, 140));    // depth 3 under L2 (even level compares X): x > 40 -> RIGHT of L2

        // RIGHT subtree path to depth 3:
        t.insert(new City("R1", 150,  50));    // goes RIGHT of R (x > 100), depth 1 (split Y)
        t.insert(new City("R2", 160,  40));    // depth 2 under R1: y < 50 -> LEFT of R1
        t.insert(new City("R3", 161,  40));    // depth 3 under R2 (even level compares X): x > 160 -> RIGHT of R2

        String out = t.toString();

        // Verify depth labels and spacing for both sides at depth 3 (6 spaces)
        assertTrue(out.contains("0R"));

        // Left chain
        assertTrue(out.contains("1  L1"));
        assertTrue(out.contains("2    L2"));
        assertTrue(out.contains("3      L3"));

        // Right chain
        assertTrue(out.contains("1  R1"));
        assertTrue(out.contains("2    R2"));
        assertTrue(out.contains("3      R3"));

        // Basic inorder sanity for each local subtree:
        assertTrue(out.indexOf("L2") < out.indexOf("L3")); // L3 is right child of L2
        assertTrue(out.indexOf("R2") < out.indexOf("R3")); // R3 is right child of R2
    }
    
    public void testContainsOnEmptyReturnsFalse() {
        KDTree t = new KDTree();
        assertFalse(t.contains(10, 10));
    }

    public void testContainsFindsRootAndDeepNodes() {
        KDTree t = new KDTree();
        // depth 0 root
        t.insert(new City("R", 100, 100));
        // go left (x < 100), depth 1 (split Y)
        t.insert(new City("L1", 50, 150));
        // under L1, depth 2 (split X): go left (x < 50)
        t.insert(new City("L2", 40, 140));
        // under L2, depth 3 (split Y): go right (y >= 140)
        t.insert(new City("L3", 41, 140));

        // Root present
        assertTrue(t.contains(100, 100));
        // Deep node present
        assertTrue(t.contains(41, 140));
        // Non-existent
        assertFalse(t.contains(999, 999));
    }

    public void testContainsRespectsTieRightOnXAtEvenDepth() {
        KDTree t = new KDTree();
        // depth 0 (even) compares X; tie must go RIGHT
        t.insert(new City("Root", 100, 100));
        // same x as root -> right
        t.insert(new City("EqualX", 100, 50));

        // Should be found following the right branch on tie
        assertTrue(t.contains(100, 50));
        // A value that would be on left if tie were mishandled
        assertFalse(t.contains(99, 50));
    }

    public void testContainsRespectsTieRightOnYAtOddDepth() {
        KDTree t = new KDTree();
        // Build left child so depth 1 discriminates by Y
        t.insert(new City("Root", 100, 100));     // depth 0 (X)
        t.insert(new City("Left", 50, 150));      // depth 1 (Y)
        // Same Y as 'Left' -> must go RIGHT at odd depth
        t.insert(new City("EqualY", 60, 150));    // depth 2 under Left's RIGHT

        assertTrue(t.contains(60, 150));          // follows odd-level tie to RIGHT
        // A near miss that forces traversal but should be absent
        assertFalse(t.contains(60, 151));
    }

    public void testContainsMissNavigatesAcrossBothSides() {
        KDTree t = new KDTree();
        t.insert(new City("R", 100, 100));
        t.insert(new City("L", 50, 150));     // left of R
        t.insert(new City("RL", 150, 40));    // right of R, then left by Y
        // Query forces: root -> right (x>100) -> right (y>=40) -> null
        assertFalse(t.contains(151, 41));
    }


}
