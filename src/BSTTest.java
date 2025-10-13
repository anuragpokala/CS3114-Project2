import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class BSTTest extends TestCase {

    /**
     * Verifies a single insert and inorder placement.
     */
    public void testBSTSingleInsert() {
        BST tree = new BST();
        tree.insert(new City("Denver", 100, 200));
        String output = tree.toString();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("100"));
        assertTrue(output.contains("200"));

        BST treeLeft = new BST();
        treeLeft.insert(new City("Denver", 100, 200));
        treeLeft.insert(new City("Boston", 200, 350));
        String output1 = treeLeft.toString();
        assertTrue(output1.contains("Denver"));
        assertTrue(output1.contains("Boston"));
        int bostonIndex = output1.indexOf("Boston");
        int denverIndex = output1.indexOf("Denver");
        assertTrue(bostonIndex < denverIndex);

        BST treeRight = new BST();
        treeRight.insert(new City("Denver", 100, 200));
        treeRight.insert(new City("NYC", 200, 350));
        String output11 = treeRight.toString();
        assertTrue(output11.contains("Denver"));
        assertTrue(output11.contains("NYC"));
        int denver = output11.indexOf("Denver");
        int nYCIndex = output11.indexOf("NYC");
        assertTrue(denver < nYCIndex);
    }


    /**
     * Confirms ordering across multiple inserts in two scenarios.
     */
    public void testBSTMultipleInserts() {
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


    /**
     * Ensures size reflects count of inserted nodes.
     */
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


    /**
     * Validates correct indentation and depth labeling in print.
     */
    public void testPrintDepthSpacing() {
        BST tree = new BST();
        tree.insert(new City("Denver", 100, 200));
        tree.insert(new City("Boston", 50, 100));
        tree.insert(new City("Atlanta", 25, 75));
        String output = tree.toString();
        assertTrue(output.contains("0Denver"));
        assertTrue(output.contains("1  Boston"));
        assertTrue(output.contains("2    Atlanta"));
    }


    /**
     * Exercises equal-key path: ties go left and appear first in inorder.
     */
    public void testBSTEqualNameGoesLeftInorderOrder() {
        BST tree = new BST();
        tree.insert(new City("Alpha", 100, 100));
        tree.insert(new City("Alpha", 200, 200));
        String out = tree.toString();
        assertTrue(out.contains("Alpha (100, 100)"));
        assertTrue(out.contains("Alpha (200, 200)"));
        assertTrue(out.indexOf("Alpha (200, 200)") < out.indexOf(
            "Alpha (100, 100)"));
        assertTrue(out.startsWith("0") || out.contains("\n0"));
        assertFalse(out.startsWith("0 ") || out.contains("\n0 "));
    }


    /**
     * Verifies right-heavy chain spacing and inorder order.
     */
    public void testRightChainDepthSpacing() {
        BST tree = new BST();
        tree.insert(new City("M", 0, 0));
        tree.insert(new City("Z", 0, 0));
        tree.insert(new City("ZZ", 0, 0));
        String out = tree.toString();
        assertTrue(out.contains("0M"));
        assertTrue(out.contains("1  Z"));
        assertTrue(out.contains("2    ZZ"));
        assertTrue(out.indexOf("0M") < out.indexOf("1  Z"));
        assertTrue(out.indexOf("1  Z") < out.indexOf("2    ZZ"));
    }


    /**
     * Ensures listing by name returns empty string if none match.
     */
    public void testListCoordsByNameNone() {
        BST t = new BST();
        t.insert(new City("Alpha", 1, 1));
        String out = t.listCoordsByName("Missing");
        assertEquals("", out);
    }


    /**
     * Checks single match formatting and absence of trailing newline.
     */
    public void testListCoordsByNameSingleAndNoTrailingNewline() {
        BST t = new BST();
        t.insert(new City("Same", 10, 20));
        String out = t.listCoordsByName("Same");
        assertEquals("(10, 20)", out);
        assertFalse(out.endsWith("\n"));
    }


    /**
     * Confirms multiple matches include all and end without trailing newline.
     */
    public void testListCoordsByNameMultipleLinesAndTrim() {
        BST t = new BST();
        t.insert(new City("X", 5, 5));
        t.insert(new City("X", 4, 4));
        t.insert(new City("X", 3, 3));
        t.insert(new City("Y", 9, 9));
        String out = t.listCoordsByName("X");
        assertTrue(out.contains("(5, 5)"));
        assertTrue(out.contains("(4, 4)"));
        assertTrue(out.contains("(3, 3)"));
        assertEquals(2, out.length() - out.replace("\n", "").length());
        assertFalse(out.endsWith("\n"));
    }
}
