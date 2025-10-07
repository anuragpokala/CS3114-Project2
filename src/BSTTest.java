import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class BSTTest extends TestCase 
{
    public void testBSTSingleInsert()
    {
        // Tests inserting into empty BST
        BST tree = new BST();
        tree.insert(new City("Denver", 100, 200));
        String output = tree.toString();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("100"));
        assertTrue(output.contains("200"));
        
        // Test inserting left
        BST treeLeft = new BST();
        treeLeft.insert(new City("Denver", 100, 200));
        treeLeft.insert(new City("Boston", 200, 350));
        
        String output1 = treeLeft.toString();
        
        assertTrue(output1.contains("Denver"));
        assertTrue(output1.contains("Boston"));
        
        int bostonIndex = output1.indexOf("Boston");
        int denverIndex = output1.indexOf("Denver");
        assertTrue(bostonIndex < denverIndex);
        
        // Test inserting right
        BST treeRight = new BST();
        treeRight.insert(new City("Denver", 100, 200));
        treeRight.insert(new City("NYC", 200, 350));
        
        String output11 = treeRight.toString();
        
        assertTrue(output11.contains("Denver"));
        assertTrue(output11.contains("NYC"));
        
        int denver = output11.indexOf("Denver");
        int nYCIndex = output11.indexOf("NYC");
        // Inorder: root (Denver) before right child (NYC)
        assertTrue(denver < nYCIndex);
    }

    
    public void testBSTMultipleInserts()
    {
        // Each one greater than the previous one
        BST tree2 = new BST();
        tree2.insert(new City("Denver", 100, 200));
        tree2.insert(new City("NYC", 200, 350));
        tree2.insert(new City("Seattle", 200, 350));
        
        String multiOutput = tree2.toString();
        
        assertTrue(multiOutput.contains("Denver"));
        assertTrue(multiOutput.contains("NYC"));
        assertTrue(multiOutput.contains("Seattle"));

        
        int denver = multiOutput.indexOf("Denver");
        int nYCIndex = multiOutput.indexOf("NYC");
        int seattleIndex = multiOutput.indexOf("Seattle");

        assertTrue(denver < nYCIndex);
        assertTrue(denver < seattleIndex);
        assertTrue(nYCIndex < seattleIndex);
       
        // Each one less than the previous one
        BST treeLess = new BST();
        treeLess.insert(new City("Denver", 100, 200));
        treeLess.insert(new City("Boston", 200, 350));
        treeLess.insert(new City("Atlanta", 200, 350));
        
        String lessOutput = treeLess.toString();
        
        assertTrue(lessOutput.contains("Denver"));
        assertTrue(lessOutput.contains("Boston"));
        assertTrue(lessOutput.contains("Atlanta"));

        
        int denver2 = lessOutput.indexOf("Denver");
        int bostonIndex = lessOutput.indexOf("Boston");
        int atlantaIndex = lessOutput.indexOf("Atlanta");

        assertTrue(denver2 > bostonIndex);
        assertTrue(bostonIndex > atlantaIndex);
        assertTrue(denver2 > atlantaIndex);
    }
    
    public void testSizeIncrement() {
        BST tree = new BST();
        assertEquals(0, tree.getSize());
        
        tree.insert(new City("Denver", 100, 200));
        assertEquals(1, tree.getSize());
        
        tree.insert(new City("Boston", 50, 100));
        assertEquals(2, tree.getSize());
        
        tree.insert(new City("NYC", 150, 250));
        assertEquals(3, tree.getSize());
    }
    
    public void testPrintDepthSpacing() {
        BST tree = new BST();
        tree.insert(new City("Denver", 100, 200));
        tree.insert(new City("Boston", 50, 100));
        tree.insert(new City("Atlanta", 25, 75));
        
        String output = tree.toString();
        
        // Level 0: "0Denver" (no spaces)
        assertTrue(output.contains("0Denver"));
        
        // Level 1: "1  Boston" (2 spaces)
        assertTrue(output.contains("1  Boston"));
        
        // Level 2: "2    Atlanta" (4 spaces)
        assertTrue(output.contains("2    Atlanta"));
    }

    /**
     * Explicitly exercises the BST equal-key path (ties go LEFT).
     * Inserts two cities with the SAME name so the second goes to the left child.
     * In inorder, the left child must appear BEFORE the parent.
     */
    public void testBSTEqualNameGoesLeftInorderOrder()
    {
        BST tree = new BST();
        // Same name, different coords
        tree.insert(new City("Alpha", 100, 100));
        tree.insert(new City("Alpha", 200, 200)); // equal by name -> goes LEFT
        String out = tree.toString();
        // Both present
        assertTrue(out.contains("Alpha (100, 100)"));
        assertTrue(out.contains("Alpha (200, 200)"));
        // Left child (200,200) must print BEFORE parent (100,100) in inorder
        assertTrue(out.indexOf("Alpha (200, 200)") < out.indexOf("Alpha (100, 100)"));
        // Root line might not be first; allow "\n0" as well
        assertTrue(out.startsWith("0") || out.contains("\n0"));
        assertFalse(out.startsWith("0 ") || out.contains("\n0 "));
    }
    
    public void testRightChainDepthSpacing() {
        BST tree = new BST();
        // Build a right-heavy chain to exercise right subtree spacing
        tree.insert(new City("M", 0, 0));     // root
        tree.insert(new City("Z", 0, 0));     // right of M
        tree.insert(new City("ZZ", 0, 0));    // right of Z

        String out = tree.toString();

        // Level 0: "0M" (no spaces)
        assertTrue(out.contains("0M"));

        // Level 1: two spaces before "Z"
        assertTrue(out.contains("1  Z"));

        // Level 2: four spaces before "ZZ"
        assertTrue(out.contains("2    ZZ"));

        // Inorder must be M, then Z, then ZZ for this right chain
        assertTrue(out.indexOf("0M") < out.indexOf("1  Z"));
        assertTrue(out.indexOf("1  Z") < out.indexOf("2    ZZ"));
    }


}
