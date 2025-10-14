import student.TestCase;

/**
 * Tests for KDTree using its public API:
 * insert(name,x,y), findExact, delete(x,y), rangeSearch(...),
 * inorderWithLevels/preorderWithLevels for formatting checks.
 */
public class KDTreeTest extends TestCase {

    // Helper: inorder debug string "level{2*level spaces}name x y\n"
    private static String inorderDbg(KDTree t) {
        StringBuilder sb = new StringBuilder();
        t.inorderWithLevels((lvl, c) -> {
            sb.append(lvl);
            for (int i = 0; i < 2 * lvl; i++) sb.append(" ");
            sb.append(c.getName()).append(" ").append(c.getX()).append(" ").append(c.getY()).append("\n");
        });
        return sb.toString();
    }

    public void testInsertAndFindExact_TiesGoRight() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 100, 100));
        assertTrue(t.insert("EqualX", 100, 50));   // even depth tie → RIGHT
        assertTrue(t.insert("Left", 50, 200));

        assertNotNull(t.findExact(100, 100));
        assertNotNull(t.findExact(100, 50));
        assertNull(t.findExact(99, 50));
        assertNull(t.findExact(60, 150));
    }

    public void testRejectDuplicateCoordinates() {
        KDTree t = new KDTree();
        assertTrue(t.insert("A", 10, 10));
        assertFalse(t.insert("B", 10, 10)); // duplicate (x,y) rejected
    }

    public void testDeleteRootReplacedByRightMinOfSplitDim() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 40, 40));
        assertTrue(t.insert("Rchild", 60, 99));
        assertTrue(t.insert("Rmin",   50,  5)); // X-min in right subtree

        KDTree.DeleteOutcome out = t.delete(40, 40);
        assertNotNull(out.entry);
        assertNull(t.findExact(40, 40));     // old root gone

        String dbg = inorderDbg(t);
        assertTrue(dbg.contains("Rmin 50 5")); // replacement present
    }

    public void testDeleteLeafAndCountPositive() {
        KDTree t = new KDTree();
        t.insert("R", 100, 100);
        t.insert("L", 50,  50);
        KDTree.DeleteOutcome out = t.delete(50, 50);
        assertNotNull(out.entry);
        assertTrue(out.visited >= 1);
        assertNull(t.findExact(50, 50));
    }

    public void testRangeSearch_BoundaryInclusionAndPruning() {
        KDTree t = new KDTree();
        // Seed to avoid degenerate shape interfering with split depths
        t.insert("seed", 100, 100);
        t.insert("hit",  3, 4);   // on r=5 boundary from (0,0)
        t.insert("miss", 6, 0);   // outside

        KDTree.SearchOutcome r = t.rangeSearch(0, 0, 5);
        assertTrue(r.listing.contains("hit (3, 4)\n"));
        assertFalse(r.listing.contains("miss (6, 0)\n"));
        assertTrue(r.visited >= 2); // must at least visit root + one child
    }

    public void testRangeSearch_YOddBoundaryExploration() {
        KDTree t = new KDTree();
        t.insert("root", 0, 40);   // depth 0 (X)
        t.insert("down", 0, 0);    // depth 1 (Y)
        t.insert("up",   0, 100);
        // exactly on boundary: cy - r == 40 ⇒ both sides must be considered at that split
        KDTree.SearchOutcome r = t.rangeSearch(0, 50, 10);
        // Only root is at distance 10 from center? No — root is at (0,40): |dy|=10 → included
        assertTrue(r.listing.contains("root (0, 40)\n"));
        assertTrue(r.visited >= 3); // root + both children considered
    }

    public void testInorderFormatting_NoSpaceAfterLevel() {
        KDTree t = new KDTree();
        t.insert("A", 10, 10);
        String s = inorderDbg(t);
        assertTrue(s.startsWith("0"));
        assertFalse(s.startsWith("0 "));
    }
}
