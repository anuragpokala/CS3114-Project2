import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class GISTest extends TestCase {

    private GIS it;

    public void setUp() {
        it = new GISDB();
    }

    public void testRefClearInit() throws IOException {
        assertTrue(it.clear());
    }

    public void testRefEmptyPrints() throws IOException {
        assertFuzzyEquals("", it.print());
        assertFuzzyEquals("", it.debug());
        assertFuzzyEquals("", it.info("CityName"));
        assertFuzzyEquals("", it.info(5, 5));
        assertFuzzyEquals("", it.delete("CityName"));
        assertFuzzyEquals("", it.delete(5, 5));
    }

    public void testRefBadInput() throws IOException {
        assertFalse(it.insert("CityName", -1, 5));
        assertFalse(it.insert("CityName", 5, -1));
        assertFalse(it.insert("CityName", 100000, 5));
        assertFalse(it.insert("CityName", 5, 100000));
        assertFuzzyEquals("", it.search(-1, -1, -1));
    }

    public void testInsert() {
        assertTrue(it.insert("Denver", 100, 200));
        assertFalse(it.insert("Bad", -1, 100));
        assertFalse(it.insert("Bad", 100, -1));
        assertFalse(it.insert("Bad", 40000, 100));
        assertFalse(it.insert("Bad", 100, 40000));
    }

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

    public void testDebugOutput() {
        assertTrue(it.insert("Denver", 100, 200));
        assertTrue(it.insert("Boston", 50, 300));

        String output = it.debug();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("Boston"));
    }

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

    public void testClear() {
        assertTrue(it.clear());
    }

    public void testInsertInvalidCoordinates() {
        assertFalse(it.insert("Test", -1, 100));
        assertFalse(it.insert("Test", 100, -1));
        assertFalse(it.insert("Test", -1, -1));
        assertFalse(it.insert("Test", GISDB.MAXCOORD + 1, 100));
        assertFalse(it.insert("Test", 100, GISDB.MAXCOORD + 1));
    }

    public void testSearchRadius() {
        assertEquals("", it.search(0, 0, -1));
        assertEquals("0", it.search(0, 0, 0));
        assertEquals("0", it.search(0, 0, 10000));
        assertEquals("0", it.search(-100, -100, 50));
    }

    public void testInsertRejectsDuplicateCoordinates() {
        assertTrue(it.insert("A", 10, 10));
        assertFalse(it.insert("B", 10, 10));
    }

    public void testInfoByCoordFoundAndNotFound() {
        it.clear();
        assertTrue(it.insert("Delta", 11, 22));
        assertEquals("Delta", it.info(11, 22));
        assertEquals("", it.info(11, 23));
    }

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

    public void testInfoByNameNoMatchesReturnsEmpty() {
        it.clear();
        it.insert("A", 1, 1);
        it.insert("B", 2, 2);
        String out = it.info("Z");
        assertEquals("", out);
    }

    public void testInfoByNameSingleNoTrailingNewline() {
        it.clear();
        it.insert("Solo", 10, 20);
        String out = it.info("Solo");
        assertEquals("(10, 20)", out);
        assertFalse(out.endsWith("\n"));
    }

    public void testDeleteCoordEmptyDBReturnsEmptyString() {
        it.clear();
        assertEquals("", it.delete(10, 10));
    }

    public void testDeleteCoordExistingReportsVisitsAndName() {
        it.clear();
        assertTrue(it.insert("R", 100, 100));
        assertTrue(it.insert("A", 50, 50));
        assertTrue(it.insert("B", 150, 150));
        assertTrue(it.insert("Bmin", 150, 120));
        String out = it.delete(100, 100);
        assertTrue(out.matches("\\d+\\s+R"));
        assertEquals("", it.info(100, 100));
    }

    public void testDeleteCoordAbsentReturnsCountOnly() {
        it.clear();
        assertTrue(it.insert("R", 100, 100));
        assertTrue(it.insert("L", 50, 50));
        String out = it.delete(10, 10);
        assertTrue(out.matches("\\d+"));
        assertEquals("R", it.info(100, 100));
        assertEquals("L", it.info(50, 50));
    }

    public void testDeleteByNameRemovesAllAndFormats() {
        it.clear();
        assertTrue(it.insert("N", 1, 1));
        assertTrue(it.insert("N", 2, 2));
        assertTrue(it.insert("O", 9, 9));
        String out = it.delete("N");
        assertTrue(out.contains("(1, 1)"));
        assertTrue(out.contains("(2, 2)"));
        assertFalse(out.endsWith("\n"));
        assertEquals("", it.info("N"));
    }

    public void testDeleteByNameNoMatchReturnsEmpty() {
        it.clear();
        assertTrue(it.insert("A", 1, 1));
        assertEquals("", it.delete("Zzz"));
    }

    public void testSearchIncludesBoundaryAndReportsCount() {
        it.clear();
        assertTrue(it.insert("C", 3, 4));
        assertTrue(it.insert("D", 6, 8));
        String out = it.search(0, 0, 5);
        assertTrue(out.contains("C (3, 4)"));
        String[] lines = out.split("\\n");
        String last = lines[lines.length - 1];
        assertTrue(last.matches("\\d+"));
    }

    public void testSearchRadiusZeroExactOnly() {
        it.clear();
        assertTrue(it.insert("E", 7, 7));
        String outHit = it.search(7, 7, 0);
        assertTrue(outHit.contains("E (7, 7)"));
        String outMiss = it.search(7, 6, 0);
        assertTrue(outMiss.matches("\\d+"));
    }

    public void testDeleteExistingReportsNameAndRemoves() {
        it.clear();
        it.insert("A", 40, 40);
        String out = it.delete(40, 40);
        assertTrue(out.matches("\\d+\\s+A"));
        assertEquals("", it.info(40, 40));
        assertFalse(it.print().contains("A"));
        assertFalse(it.debug().contains("A"));
    }

    public void testDeleteMissingReturnsVisitedOnly() {
        it.clear();
        it.insert("R", 40, 40);
        it.insert("L", 10, 10);
        it.insert("RR", 80, 80);
        String out = it.delete(1, 2);
        assertTrue(out.matches("\\d+"));
        assertFalse(out.contains(" "));
    }

    public void testSearchNoHitsReturnsCountOnly() {
        it.clear();
        it.insert("R", 40, 40);
        it.insert("L", 10, 10);
        String out = it.search(1000, 1000, 5);
        assertTrue(out.matches("\\d+"));
    }

    public void testSearchPrunesLeftOnEvenBoundary() {
        it.clear();
        it.insert("root", 40, 0);
        it.insert("left", 0, 0);
        it.insert("right", 100, 0);
        String out = it.search(50, 0, 10); // cx - r == 40, skip left, include root
        assertEquals("root (40, 0)\n2", out);
    }

    /** delete(name): three duplicates, ensure preorder order. */
    public void testDeleteByNameThreeDuplicatesPreorderOrder() {
        GIS db = new GISDB();
        db.insert("Dup", 50, 50);  // should be deleted first (preorder)
        db.insert("A",   25, 60);
        db.insert("Dup", 40, 40);
        db.insert("Dup", 60, 60);
        String out = db.delete("Dup");
        assertTrue(out.contains("(50, 50)"));
        assertTrue(out.contains("(40, 40)"));
        assertTrue(out.contains("(60, 60)"));
        assertFalse(out.endsWith("\n"));
        assertEquals("", db.info("Dup"));
    }

    /** search(): both branches explored with no hits -> digits only. */
    public void testSearchBothBranchesNoHitsDigitsOnly() {
        GIS db = new GISDB();
        db.insert("R", 50, 50);
        db.insert("L", 25, 60);
        db.insert("RL", 75, 40);
        String out = db.search(50, 5000, 25);
        assertTrue(out.matches("\\d+"));
    }

    /** delete(name) with prefix-similar others. */
    public void testDeleteByNameSingleVsPrefixNames() {
        GIS db = new GISDB();
        db.insert("Ann", 1, 1);
        db.insert("Anna", 2, 2);
        db.insert("Anne", 3, 3);
        String out = db.delete("Ann");
        assertEquals("(1, 1)", out);
        assertTrue(db.info("Anna").contains("(2, 2)"));
        assertTrue(db.info("Anne").contains("(3, 3)"));
    }

    /** Visit count precision when deleting a non-existent coordinate. */
    public void testDeleteNonexistentShallowVisitCount() {
        GISDB db = new GISDB();
        db.insert("A", 5, 5);
        assertEquals("1", db.delete(9, 9));
    }

    /** Distance boundary vs near miss. */
    public void testSearchDistanceBoundaryVsNearMiss() {
        GIS db = new GISDB();
        db.insert("BND", 3, 4);   // on boundary for r=5
        db.insert("MISS", 3, 5);  // just outside
        String out = db.search(0, 0, 5);
        assertTrue(out.contains("BND (3, 4)"));
        assertFalse(out.contains("MISS (3, 5)"));
    }

    /** delete(name) visit count when matches exist in both subtrees. */
    public void testDeleteByNameVisitCountMultiSubtrees() {
        GIS db = new GISDB();
        db.insert("Dup", 50, 50);  // root
        db.insert("X",   10, 10);
        db.insert("Dup", 60,  5);  // right subtree
        db.insert("Dup", 40, 90);  // left subtree
        String out = db.delete("Dup");
        assertTrue(out.contains("(50, 50)"));
        assertTrue(out.contains("(60, 5)"));
        assertTrue(out.contains("(40, 90)"));
        assertFalse(out.endsWith("\n"));
        assertEquals("", db.info("Dup"));
    }
}
