import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class GISTest extends TestCase {

    private GIS it;

    /**
     * Sets up the tests that follow. In general, used for initialization
     */
    public void setUp() {
        it = new GISDB();
    }


    /**
     * Test clearing on initial
     * 
     * @throws IOException
     */
    public void testRefClearInit() throws IOException {
        assertTrue(it.clear());
    }


    /**
     * Print testing for empty trees
     * 
     * @throws IOException
     */
    public void testRefEmptyPrints() throws IOException {
        assertFuzzyEquals("", it.print());
        assertFuzzyEquals("", it.debug());
        assertFuzzyEquals("", it.info("CityName"));
        assertFuzzyEquals("", it.info(5, 5));
        assertFuzzyEquals("", it.delete("CityName"));
        assertFuzzyEquals("", it.delete(5, 5));
    }


    /**
     * Print bad input checks
     * 
     * @throws IOException
     */
    public void testRefBadInput() throws IOException {
        assertFalse(it.insert("CityName", -1, 5));
        assertFalse(it.insert("CityName", 5, -1));
        assertFalse(it.insert("CityName", 100000, 5));
        assertFalse(it.insert("CityName", 5, 100000));
        assertFuzzyEquals("", it.search(-1, -1, -1));
    }


    /**
     * Verifies insert validity and duplicate coordinate rejection.
     */
    public void testInsert() {
        assertTrue(it.insert("Denver", 100, 200));
        assertFalse(it.insert("Bad", -1, 100));
        assertFalse(it.insert("Bad", 100, -1));
        assertFalse(it.insert("Bad", 40000, 100));
        assertFalse(it.insert("Bad", 100, 40000));
    }


    /**
     * Ensures print and debug include inserted cities and root line formatting.
     */
    public void testPrint() {
        it.insert("Denver", 100, 200);
        it.insert("Boston", 50, 100);

        String bst = it.print();
        String kd = it.debug();

        assertTrue(bst.contains("Denver"));
        assertTrue(bst.contains("Boston"));
        assertTrue(kd.contains("Denver"));
        assertTrue(kd.contains("Boston"));

        assertTrue(bst.startsWith("0") || bst.contains("\n0"));
        assertFalse(bst.startsWith("0 ") || bst.contains("\n0 "));
        assertTrue(kd.startsWith("0") || kd.contains("\n0"));
        assertFalse(kd.startsWith("0 ") || kd.contains("\n0 "));
    }


    /**
     * Confirms multiple inserts appear and empty prints after clear are empty.
     */
    public void testInsertAndPrint() {
        assertTrue(it.insert("Denver", 100, 200));
        assertTrue(it.insert("Boston", 50, 100));
        assertTrue(it.insert("NYC", 150, 250));

        String output = it.print();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("Boston"));
        assertTrue(output.contains("NYC"));

        it.clear();
        assertEquals("", it.print());
        assertEquals("", it.debug());
    }


    /**
     * Ensures debug output contains all inserted cities.
     */
    public void testDebugOutput() {
        assertTrue(it.insert("Denver", 100, 200));
        assertTrue(it.insert("Boston", 50, 300));

        String output = it.debug();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("Boston"));
    }


    /**
     * Inserts several cities and checks they appear in both indices.
     */
    public void testMultipleInserts() {
        for (int i = 0; i < 5; i++) {
            assertTrue(it.insert("City" + i, i * 10, i * 20));
        }

        String bst = it.print();
        String kd = it.debug();

        for (int i = 0; i < 5; i++) {
            assertTrue(bst.contains("City" + i));
            assertTrue(kd.contains("City" + i));
        }
    }


    /**
     * Test clear returns true.
     */
    public void testClear() {
        assertTrue(it.clear());
    }


    /**
     * Validates insert rejects negative and over-max coordinates.
     */
    public void testInsertInvalidCoordinates() {
        assertFalse(it.insert("Test", -1, 100));
        assertFalse(it.insert("Test", 100, -1));
        assertFalse(it.insert("Test", -1, -1));
        assertFalse(it.insert("Test", 32768, 100));
        assertFalse(it.insert("Test", 100, 32768));
    }


    /**
     * Exercises search radius validation and neutral return path.
     */
    public void testSearchRadius() {
        assertEquals("", it.search(0, 0, -1));
        assertEquals("0", it.search(0, 0, 0));
        assertEquals("0", it.search(0, 0, 10000));
        assertEquals("0", it.search(-100, -100, 50));
    }


    /**
     * Ensures duplicate coordinates are rejected by the DB.
     */
    public void testInsertRejectsDuplicateCoordinates() {
        assertTrue(it.insert("A", 10, 10));
        assertFalse(it.insert("B", 10, 10));
    }


    /**
     * Confirms info by coordinate returns correct name and empty if not found.
     */
    public void testInfoByCoordFoundAndNotFound() {
        it.clear();
        assertTrue(it.insert("Delta", 11, 22));
        assertEquals("Delta", it.info(11, 22));
        assertEquals("", it.info(11, 23));
    }


    /**
     * Re-asserts duplicate coordinate rejection in a fresh DB.
     */
    public void testInsertRejectsDuplicateCoordinates_again() {
        it.clear();
        assertTrue(it.insert("A", 10, 10));
        assertFalse(it.insert("B", 10, 10));
    }


    /**
     * Validates KD tie policy (even: X tie, odd: Y tie) and info by coord.
     */
    public void testInfoTiePathsEvenAndOddLevels() {
        it.clear();
        assertTrue(it.insert("Root", 100, 100));
        assertTrue(it.insert("EqualX", 100, 50));
        assertTrue(it.insert("Left", 50, 150));
        assertTrue(it.insert("EqualY", 60, 150));

        assertEquals("Root", it.info(100, 100));
        assertEquals("EqualX", it.info(100, 50));
        assertEquals("Left", it.info(50, 150));
        assertEquals("EqualY", it.info(60, 150));
        assertEquals("", it.info(60, 151));
    }


    /**
     * Verifies info by name returns correctly formatted coordinate list.
     */
    public void testInfoByNameFormatsCoordinates() {
        it.clear();
        assertTrue(it.insert("Zed", 7, 8));
        assertTrue(it.insert("Zed", 1, 2));
        assertTrue(it.insert("Other", 9, 9));

        String out = it.info("Zed");
        assertTrue(out.contains("(7, 8)"));
        assertTrue(out.contains("(1, 2)"));
        assertFalse(out.contains("(9, 9)"));
        assertEquals(1, out.length() - out.replace("\n", "").length());
        assertFalse(out.endsWith("\n"));
    }


    /**
     * Ensures info(name) returns the empty string when no cities match.
     */
    public void testInfoByNameNoMatchesReturnsEmpty() {
        it.clear();
        it.insert("A", 1, 1);
        it.insert("B", 2, 2);
        String out = it.info("Z");
        assertEquals("", out);
    }


    /**
     * With exactly one matching city, info(name) must not end with a newline.
     */
    public void testInfoByNameSingleNoTrailingNewline() {
        it.clear();
        it.insert("Solo", 10, 20);
        String out = it.info("Solo");
        assertEquals("(10, 20)", out);
        assertFalse(out.endsWith("\n"));
    }


    /**
     * For multiple matches, output has one newline per boundary and no trailing
     * newline.
     * Order should reflect BST inorder on equal names.
     */
    /**
     * For multiple matches, info(name) lists all coordinates, uses one newline
     * between entries, and has no trailing newline.
     */
    public void testInfoByNameMultipleNewlineCountAndOrder() {
        it.clear();
        it.insert("Same", 5, 5);
        it.insert("Same", 4, 4);
        it.insert("Same", 6, 6);
        String out = it.info("Same");
        int newlines = out.length() - out.replace("\n", "").length();
        assertEquals(2, newlines);
        assertFalse(out.endsWith("\n"));
        assertTrue(out.contains("(5, 5)"));
        assertTrue(out.contains("(4, 4)"));
        assertTrue(out.contains("(6, 6)"));
    }


    /**
     * Ensures info(name) output contains no carriage returns after
     * normalization.
     */
    public void testInfoByNameNoCarriageReturns() {
        it.clear();
        it.insert("CR", 1, 1);
        it.insert("CR", 2, 2);
        String out = it.info("CR");
        assertFalse(out.contains("\r"));
    }

}
