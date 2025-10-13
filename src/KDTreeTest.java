import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class KDTreeTest extends TestCase {

    /**
     * Confirms insert into empty tree prints the city.
     */
    public void testKDTreeInsertEmpty() {
        KDTree tree = new KDTree();
        tree.insert(new City("Denver", 100, 200));
        String output = tree.toString();
        assertTrue(output.contains("Denver"));
    }


    /**
     * Verifies even-level X split ordering via inorder.
     */
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


    /**
     * Verifies odd-level Y split influences ordering.
     */
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


    /**
     * Ensures size increments on inserts.
     */
    public void testKDTreeSize() {
        KDTree tree = new KDTree();
        assertEquals(0, tree.getSize());
        tree.insert(new City("Denver", 100, 200));
        assertEquals(1, tree.getSize());
        tree.insert(new City("Boston", 50, 100));
        assertEquals(2, tree.getSize());
    }


    /**
     * Ensures printing an empty KD tree yields empty string.
     */
    public void testKDTreePrintEmpty() {
        KDTree tree = new KDTree();
        assertEquals("", tree.toString());
    }


    /**
     * Root at depth 0 must split on X and both children appear at level 1.
     */
    public void testRootSplitsOnX() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("Left", 50, 200));
        tree.insert(new City("Right", 150, 50));
        String output = tree.toString();
        assertTrue(output.contains("0Root"));
        assertTrue(output.contains("1  Left") || output.contains("1  Right"));
    }


    /**
     * At level 1, discriminator is Y; verify left/right placement by Y.
     */
    public void testLevel1SplitsOnY() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("Child", 50, 150));
        tree.insert(new City("GrandL", 60, 100));
        tree.insert(new City("GrandR", 40, 200));
        String output = tree.toString();
        assertTrue(output.contains("0Root"));
        assertTrue(output.contains("1  Child"));
        assertTrue(output.contains("2    Grand"));
    }


    /**
     * Ties on even levels (X) must go right.
     */
    public void testEqualValuesGoRight() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("EqualX", 100, 50));
        String output = tree.toString();
        int rootIdx = output.indexOf("Root");
        int equalIdx = output.indexOf("EqualX");
        assertTrue(equalIdx > rootIdx);
    }


    /**
     * Verifies odd-level Y comparison routes left child correctly.
     */
    public void testYDiscriminationAtLevel1() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 100, 100));
        tree.insert(new City("B", 50, 50));
        tree.insert(new City("C", 75, 25));
        String output = tree.toString();
        int bIdx = output.indexOf("B");
        int cIdx = output.indexOf("C");
        assertTrue(cIdx < bIdx);
    }


    /**
     * Confirms depth labels and spacing for a left chain to level 2.
     */
    public void testDepthIncrementsCorrectly() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 100, 100));
        tree.insert(new City("B", 50, 50));
        tree.insert(new City("C", 25, 25));
        String output = tree.toString();
        assertTrue(output.contains("0A"));
        assertTrue(output.contains("1  B"));
        assertTrue(output.contains("2    C"));
        assertFalse(output.contains("0B"));
        assertFalse(output.contains("0C"));
    }


    /**
     * Confirms right subtree depth labels up to level 2.
     */
    public void testRightSubtreeDepth() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 100, 100));
        tree.insert(new City("R", 150, 150));
        tree.insert(new City("RR", 175, 175));
        String output = tree.toString();
        assertTrue(output.contains("0A"));
        assertTrue(output.contains("1  R"));
        assertTrue(output.contains("2    RR"));
    }


    /**
     * Tie on odd-level Y must go right; verifies level and inorder order.
     */
    public void testEqualYAtOddLevelGoesRight() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("Left", 50, 150));
        tree.insert(new City("EqualY", 60, 150));
        String out = tree.toString();
        assertTrue(out.contains("1  Left"));
        assertTrue(out.contains("2    EqualY"));
        assertTrue(out.indexOf("Left") < out.indexOf("EqualY"));
        assertTrue(out.startsWith("0") || out.contains("\n0"));
        assertFalse(out.startsWith("0 ") || out.contains("\n0 "));
    }


    /**
     * Ensures root line has no indentation in KD print.
     */
    public void testKDPrintRootNoIndent() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 10, 10));
        String out = tree.toString();
        assertTrue(out.startsWith("0"));
        assertFalse(out.startsWith("0 "));
    }


    /**
     * Builds to depth 3 on both sides and checks spacing and local inorder.
     */
    public void testDeepLevelsBothSides() {
        KDTree t = new KDTree();
        t.insert(new City("R", 100, 100));
        t.insert(new City("L1", 50, 150));
        t.insert(new City("L2", 40, 140));
        t.insert(new City("L3", 41, 140));
        t.insert(new City("R1", 150, 50));
        t.insert(new City("R2", 160, 40));
        t.insert(new City("R3", 161, 40));
        String out = t.toString();
        assertTrue(out.contains("0R"));
        assertTrue(out.contains("1  L1"));
        assertTrue(out.contains("2    L2"));
        assertTrue(out.contains("3      L3"));
        assertTrue(out.contains("1  R1"));
        assertTrue(out.contains("2    R2"));
        assertTrue(out.contains("3      R3"));
        assertTrue(out.indexOf("L2") < out.indexOf("L3"));
        assertTrue(out.indexOf("R2") < out.indexOf("R3"));
    }


    /**
     * contains() on empty tree returns false.
     */
    public void testContainsOnEmptyReturnsFalse() {
        KDTree t = new KDTree();
        assertFalse(t.contains(10, 10));
    }


    /**
     * contains() finds both root and deep node and rejects absent target.
     */
    public void testContainsFindsRootAndDeepNodes() {
        KDTree t = new KDTree();
        t.insert(new City("R", 100, 100));
        t.insert(new City("L1", 50, 150));
        t.insert(new City("L2", 40, 140));
        t.insert(new City("L3", 41, 140));
        assertTrue(t.contains(100, 100));
        assertTrue(t.contains(41, 140));
        assertFalse(t.contains(999, 999));
    }


    /**
     * contains() respects even-depth X tie-go-right rule.
     */
    public void testContainsRespectsTieRightOnXAtEvenDepth() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 100, 100));
        t.insert(new City("EqualX", 100, 50));
        assertTrue(t.contains(100, 50));
        assertFalse(t.contains(99, 50));
    }


    /**
     * contains() respects odd-depth Y tie-go-right rule.
     */
    public void testContainsRespectsTieRightOnYAtOddDepth() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 100, 100));
        t.insert(new City("Left", 50, 150));
        t.insert(new City("EqualY", 60, 150));
        assertTrue(t.contains(60, 150));
        assertFalse(t.contains(60, 151));
    }


    /**
     * Miss case traverses across sides and returns false.
     */
    public void testContainsMissNavigatesAcrossBothSides() {
        KDTree t = new KDTree();
        t.insert(new City("R", 100, 100));
        t.insert(new City("L", 50, 150));
        t.insert(new City("RL", 150, 40));
        assertFalse(t.contains(151, 41));
    }


    /**
     * find() returns null on empty, then locates root correctly.
     */
    public void testFindOnEmptyAndRoot() {
        KDTree t = new KDTree();
        assertNull(t.find(1, 1));
        t.insert(new City("R", 100, 100));
        City c = t.find(100, 100);
        assertNotNull(c);
        assertEquals("R", c.getName());
    }


    /**
     * find() reaches deep nodes and respects tie-go-right on both parities.
     */
    public void testFindDeepBothSidesAndTiesRight() {
        KDTree t = new KDTree();
        t.insert(new City("R", 100, 100));
        t.insert(new City("L1", 50, 150));
        t.insert(new City("L2", 40, 140));
        t.insert(new City("L3", 41, 140));
        t.insert(new City("RX", 100, 50));
        assertEquals("L3", t.find(41, 140).getName());
        assertEquals("RX", t.find(100, 50).getName());
        assertNull(t.find(41, 139));
        assertNull(t.find(99, 50));
    }


    /**
     * Prints deep left chain spacing and verifies inorder position of deepest.
     */
    public void testDeepLeftChainExactSpacingLevels0to4() {
        KDTree t = new KDTree();
        t.insert(new City("D0", 100, 100));
        t.insert(new City("D1", 50, 50));
        t.insert(new City("D2", 40, 75));
        t.insert(new City("D3", 35, 60));
        t.insert(new City("D4", 30, 60));
        String out = t.toString();
        assertTrue(out.contains("0D0"));
        assertTrue(out.contains("1  D1"));
        assertTrue(out.contains("2    D2"));
        assertTrue(out.contains("3      D3"));
        assertTrue(out.contains("4        D4"));
        assertTrue(out.indexOf("4        D4") < out.indexOf("0D0"));
    }


    /**
     * find() along deep left chain returns exact hits and rejects near misses.
     */
    public void testDeepLeftChainFindsAllAndMissesNearHits() {
        KDTree t = new KDTree();
        t.insert(new City("D0", 100, 100));
        t.insert(new City("D1", 50, 50));
        t.insert(new City("D2", 40, 75));
        t.insert(new City("D3", 35, 60));
        t.insert(new City("D4", 30, 60));
        assertEquals("D4", t.find(30, 60).getName());
        assertEquals("D3", t.find(35, 60).getName());
        assertEquals("D2", t.find(40, 75).getName());
        assertEquals("D1", t.find(50, 50).getName());
        assertEquals("D0", t.find(100, 100).getName());
        assertNull(t.find(30, 59));
        assertNull(t.find(34, 60));
    }


    /**
     * Prints deep right chain spacing and inorder order across levels 0–4.
     */
    public void testDeepRightChainExactSpacingLevels0to4() {
        KDTree t = new KDTree();
        t.insert(new City("R0", 100, 100));
        t.insert(new City("R1", 150, 150));
        t.insert(new City("R2", 160, 160));
        t.insert(new City("R3", 165, 170));
        t.insert(new City("R4", 170, 170));
        String out = t.toString();
        assertTrue(out.contains("0R0"));
        assertTrue(out.contains("1  R1"));
        assertTrue(out.contains("2    R2"));
        assertTrue(out.contains("3      R3"));
        assertTrue(out.contains("4        R4"));
        assertTrue(out.indexOf("0R0") < out.indexOf("1  R1"));
        assertTrue(out.indexOf("1  R1") < out.indexOf("2    R2"));
        assertTrue(out.indexOf("2    R2") < out.indexOf("3      R3"));
        assertTrue(out.indexOf("3      R3") < out.indexOf("4        R4"));
    }


    /**
     * Tie at odd depth goes right; verifies placement and lookup.
     */
    public void testDeepOddLevelTieGoesRightAtLevel3AndFindsIt() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 100, 100));
        t.insert(new City("L1", 50, 150));
        t.insert(new City("L2", 40, 140));
        t.insert(new City("TieY", 41, 140));
        String out = t.toString();
        assertTrue(out.contains("1  L1"));
        assertTrue(out.contains("2    L2"));
        assertTrue(out.contains("3      TieY"));
        assertTrue(out.indexOf("L2") < out.indexOf("TieY"));
        assertEquals("TieY", t.find(41, 140).getName());
        assertNull(t.find(41, 139));
    }


    /**
     * Verifies find() follows right path on both even/odd levels and misses.
     */
    public void testFindTraversesRightPathOnEvenAndOddLevels() {
        KDTree t = new KDTree();
        t.insert(new City("R0", 100, 100));
        t.insert(new City("R1", 150, 150));
        t.insert(new City("R2", 160, 120));
        t.insert(new City("R3", 170, 120));
        assertEquals("R0", t.find(100, 100).getName());
        assertEquals("R1", t.find(150, 150).getName());
        assertEquals("R2", t.find(160, 120).getName());
        assertEquals("R3", t.find(170, 120).getName());
        assertNull(t.find(170, 119));
        assertNull(t.find(159, 120));
    }


    /**
     * Ensures deep miss after multiple recursions returns false/null.
     */
    public void testContainsAndFindDeepMissOnOppositeSide() {
        KDTree t = new KDTree();
        t.insert(new City("A0", 100, 100));
        t.insert(new City("A1", 60, 150));
        t.insert(new City("A2", 55, 140));
        t.insert(new City("A3", 70, 130));
        assertFalse(t.contains(56, 139));
        assertNull(t.find(56, 139));
    }
}
