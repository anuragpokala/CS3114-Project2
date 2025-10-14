import student.TestCase;

/**
 * Tests for the public GIS facade (GISDB). Focuses on spec formatting and behavior.
 */
public class GISTest extends TestCase {

    private GIS db;

    public void setUp() {
        db = new GISDB();
    }

    public void testClearAndEmptyOutputs() {
        assertTrue(db.clear());
        assertEquals("", db.print());
        assertEquals("", db.debug());
        assertEquals("", db.info("X"));
        assertEquals("", db.info(1, 1));
        assertEquals("", db.delete("X"));
        assertEquals("", db.delete(1, 1));
    }

    public void testInsertValidationAndDuplicates() {
        assertTrue(db.insert("A", 10, 10));
        assertFalse(db.insert("B", 10, 10)); // duplicate coord
        assertFalse(db.insert("Bad", -1, 0));
        assertFalse(db.insert("Bad", 0, -1));
        assertFalse(db.insert("Bad", GISDB.MAXCOORD + 1, 0));
        assertFalse(db.insert("Bad", 0, GISDB.MAXCOORD + 1));
    }

    public void testPrintAndDebugFormatting() {
        db.clear();
        db.insert("Denver", 100, 200);
        db.insert("Boston", 50, 100);

        String bst = db.print(); // name BST: "level{2*level spaces}name (x, y)"
        String kd  = db.debug(); // kd inorder: "level{2*level spaces}name x y"

        assertTrue(bst.contains("Denver"));
        assertTrue(bst.contains("Boston"));
        assertTrue(kd.contains("Denver"));
        assertTrue(kd.contains("Boston"));

        assertTrue(bst.startsWith("0") || bst.contains("\n0"));
        assertFalse(bst.startsWith("0 ") || bst.contains("\n0 "));
        assertTrue(kd.startsWith("0") || kd.contains("\n0"));
        assertFalse(kd.startsWith("0 ") || kd.contains("\n0 "));
    }

 // Replace your current testInfoByCoordAndByName with this:
    public void testInfoByCoordAndByName() {
        GIS gisLocal = new GISDB();
        assertTrue(gisLocal.insert("Solo", 10, 20));
        assertEquals("Solo", gisLocal.info(10, 20));

        String byName = gisLocal.info("Solo");
        assertTrue(byName.contains("(10, 20)"));

        String[] lines = byName.split("\\R");
        int nonEmpty = 0;
        for (String s : lines) if (!s.isEmpty()) nonEmpty++;
        assertEquals(1, nonEmpty);
    }




    public void testDeleteByCoordAndVisitCountPrinted() {
        db.clear();
        db.insert("R", 100, 100);
        db.insert("A", 50,  50);
        String out = db.delete(100, 100);
        assertTrue(out.matches("\\d+\\s+R"));
        assertEquals("", db.info(100, 100));
    }

 // Replace your current testDeleteByCoordMissingOnlyCountOrEmpty with this:
    public void testDeleteByCoordMissingOnlyCountOrEmpty() {
        GIS gisLocal = new GISDB();
        gisLocal.insert("R", 40, 40);
        gisLocal.insert("L", 10, 10);

        String out = gisLocal.delete(1, 2);
        assertEquals("", out);  // not found -> empty string
    }


 // Replace your current testDeleteByNameRemovesAll_PreorderPreference with this:
    public void testDeleteByNameRemovesAll_PreorderPreference() {
        GIS gisLocal = new GISDB();
        gisLocal.insert("Dup", 50, 50);
        gisLocal.insert("A",   25, 60);
        gisLocal.insert("Dup", 40, 40);
        gisLocal.insert("Dup", 60, 60);

        String out = gisLocal.delete("Dup");
        assertTrue(out.contains("(50, 50)"));
        assertTrue(out.contains("(40, 40)"));
        assertTrue(out.contains("(60, 60)"));
        assertTrue(out.endsWith("\n"));   // trailing newline is expected
        assertEquals("", gisLocal.info("Dup"));
    }



    public void testSearchCircle_BoundaryInclusionAndCount() {
        db.clear();
        db.insert("C", 3, 4);   // on r=5 boundary from (0,0)
        db.insert("D", 6, 8);   // outside r=5 from (0,0)

        String out = db.search(0, 0, 5);
        assertTrue(out.contains("C (3, 4)"));
        // last line is visit count
        String[] lines = out.split("\\R");
        String last = lines[lines.length - 1];
        assertTrue(last.matches("\\d+"));
    }

    public void testSearchBadRadiusAndZeroRadius() {
        assertEquals("", db.search(0, 0, -1));
        db.clear();
        db.insert("E", 7, 7);
        String hit = db.search(7, 7, 0);
        assertTrue(hit.contains("E (7, 7)"));
        String miss = db.search(7, 6, 0);
        assertTrue(miss.matches("\\d+"));
    }
}
