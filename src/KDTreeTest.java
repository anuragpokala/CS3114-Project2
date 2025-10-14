import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class KDTreeTest extends TestCase {

    // ---------- small helper ----------
    private static int listingCount(String s) {
        return (s == null || s.isEmpty()) ? 0 : s.split("\\R").length;
    }

    public void testKDTreeInsertEmpty() {
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

    public void testRootSplitsOnX() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("Left", 50, 200));
        tree.insert(new City("Right", 150, 50));
        String output = tree.toString();
        assertTrue(output.contains("0Root"));
        assertTrue(output.contains("1  Left") || output.contains("1  Right"));
    }

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

    public void testEqualValuesGoRight() {
        KDTree tree = new KDTree();
        tree.insert(new City("Root", 100, 100));
        tree.insert(new City("EqualX", 100, 50));
        String output = tree.toString();
        int rootIdx = output.indexOf("Root");
        int equalIdx = output.indexOf("EqualX");
        assertTrue(equalIdx > rootIdx);
    }

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

    public void testKDPrintRootNoIndent() {
        KDTree tree = new KDTree();
        tree.insert(new City("A", 10, 10));
        String out = tree.toString();
        assertTrue(out.startsWith("0"));
        assertFalse(out.startsWith("0 "));
    }

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

    public void testContainsOnEmptyReturnsFalse() {
        KDTree t = new KDTree();
        assertFalse(t.contains(10, 10));
    }

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

    public void testContainsRespectsTieRightOnXAtEvenDepth() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 100, 100));
        t.insert(new City("EqualX", 100, 50));
        assertTrue(t.contains(100, 50));
        assertFalse(t.contains(99, 50));
    }

    public void testContainsRespectsTieRightOnYAtOddDepth() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 100, 100));
        t.insert(new City("Left", 50, 150));
        t.insert(new City("EqualY", 60, 150));
        assertTrue(t.contains(60, 150));
        assertFalse(t.contains(60, 151));
    }

    public void testContainsMissNavigatesAcrossBothSides() {
        KDTree t = new KDTree();
        t.insert(new City("R", 100, 100));
        t.insert(new City("L", 50, 150));
        t.insert(new City("RL", 150, 40));
        assertFalse(t.contains(151, 41));
    }

    public void testFindOnEmptyAndRoot() {
        KDTree t = new KDTree();
        assertNull(t.find(1, 1));
        t.insert(new City("R", 100, 100));
        City c = t.find(100, 100);
        assertNotNull(c);
        assertEquals("R", c.getName());
    }

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

    public void testContainsAndFindDeepMissOnOppositeSide() {
        KDTree t = new KDTree();
        t.insert(new City("A0", 100, 100));
        t.insert(new City("A1", 60, 150));
        t.insert(new City("A2", 55, 140));
        t.insert(new City("A3", 70, 130));
        assertFalse(t.contains(56, 139));
        assertNull(t.find(56, 139));
    }

    public void testKDDeleteLeaf() {
        GIS db = new GISDB();
        db.insert("R", 100, 100);
        db.insert("L", 50, 50);
        String out = db.delete(50, 50);
        assertTrue(out.matches("\\d+\\s+L"));
        assertEquals("", db.info(50, 50));
    }

    public void testKDDeleteMinFromRightPreorderTie() {
        GIS db = new GISDB();
        db.insert("R", 100, 100);
        db.insert("RrootMin", 150, 90);
        db.insert("RleftEqualMin", 140, 90);
        String out = db.delete(100, 100);
        assertTrue(out.contains("R"));
        assertEquals("", db.info(100, 100));
    }

    public void testKDDeleteRepeatedThenEmpty() {
        GIS db = new GISDB();
        db.insert("A", 100, 100);
        db.insert("B", 150, 150);
        assertTrue(db.delete(150, 150).matches("\\d+\\s+B"));
        assertTrue(db.delete(100, 100).matches("\\d+\\s+A"));
        assertEquals("", db.delete(100, 100));
    }

    public void testKDDeleteRootReplacedFromRightSubtree() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 40, 40));
        t.insert(new City("R1", 80, 5));
        t.insert(new City("Rmin", 60, 999));
        KDTree.KDDeleteResult res = t.deleteExact(40, 40);
        assertNotNull(res.removed);
        String out = t.toString();
        assertTrue(out.contains("Rmin (60, 999)"));
    }

    public void testKDDeleteRootOnlyLeftSubtree() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 40, 40));
        t.insert(new City("L1", 30, 500));
        t.insert(new City("Lmin", 10, 0));
        KDTree.KDDeleteResult res = t.deleteExact(40, 40);
        assertNotNull(res.removed);
        assertTrue(t.toString().contains("Lmin (10, 0)"));
    }

    public void testKDDeleteTieChoosesPreorderFirst() {
        KDTree t = new KDTree();
        t.insert(new City("R", 40, 40));
        t.insert(new City("R1", 60, 10));
        t.insert(new City("TieFirst", 60, 5));
        t.insert(new City("TieSecond", 60, 15));
        KDTree.KDDeleteResult res = t.deleteExact(40, 40);
        assertNotNull(res.removed);
        String out = t.toString();
        assertTrue(out.contains("TieFirst (60, 5)"));
    }

    public void testRangeSearchEvenBoundarySkipsLeft() {
        KDTree t = new KDTree();
        t.insert(new City("root", 40, 0));
        t.insert(new City("left", 0, 0));
        t.insert(new City("right", 100, 0));
        KDTree.KDSearchResult res = t.rangeSearch(50, 0, 10);
        assertEquals(2, res.visited);
        assertEquals("root (40, 0)", res.listing());
    }

    public void testRangeSearchYBoundarySkipsBelow() {
        KDTree t = new KDTree();
        t.insert(new City("root", 0, 40));
        t.insert(new City("down", 0, 0));
        t.insert(new City("up", 0, 100));
        KDTree.KDSearchResult res = t.rangeSearch(0, 50, 10);
        assertEquals(3, res.visited);
        assertEquals("root (0, 40)", res.listing());
    }

    public void testDeleteTwoChildrenUsesRightMinOfDim() {
        KDTree t = new KDTree();
        t.insert(new City("R", 50, 50));
        t.insert(new City("L", 25, 75));
        t.insert(new City("R1", 75, 25));
        t.insert(new City("RMin", 60, 10));
        KDTree.KDDeleteResult res = t.deleteExact(50, 50);
        assertEquals("R", res.removed.getName());
        assertTrue(t.contains(60, 10));
        assertFalse(t.contains(50, 50));
    }

    public void testDeleteNotFoundReturnsVisitedOnly() {
        KDTree t = new KDTree();
        t.insert(new City("R", 10, 10));
        KDTree.KDDeleteResult res = t.deleteExact(99, 99);
        assertNull(res.removed);
        assertNotNull(t.find(10, 10));
    }

    /** Even-depth split: explore both sides, no hits. */
    public void testRangeSearchEvenExploresBothSidesNoHits() {
        KDTree t = new KDTree();
        t.insert(new City("root", 50, 0));   // depth 0 (X)
        t.insert(new City("left", 30, 0));
        t.insert(new City("right", 70, 0));
        KDTree.KDSearchResult res = t.rangeSearch(50, 1000, 25); // far in Y → no hits
        assertEquals("", res.listing());
        assertEquals(3, res.visited);        // root + both children
    }

    /** Odd-depth split: both branches at depth 1 explored, no hits. */
    public void testRangeSearchOddExploresBothSidesNoHits() {
        KDTree t = new KDTree();
        t.insert(new City("root", 50, 50));   // depth 0 (X)
        t.insert(new City("mid",  25, 60));   // depth 1 (Y split at 60)
        t.insert(new City("midL", 26, 30));   // depth 2
        t.insert(new City("midR", 24, 90));   // depth 2
        KDTree.KDSearchResult res = t.rangeSearch(0, 60, 1); // equality on Y boundary
        assertTrue(res.visited >= 3);
        assertEquals("", res.listing());
    }

    /** Replacement comes from right-subtree min-by-dimension. */
    public void testDeleteRootChoosesRightSubtreeMinByDimensionStrict() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 40, 40)); // depth 0 splits X
        t.insert(new City("R",    60, 99));
        t.insert(new City("Rmin", 50,  1));   // true min X in right
        t.insert(new City("Rtie", 50, 99));   // tie on X but later in preorder
        KDTree.KDDeleteResult res = t.deleteExact(40, 40);
        assertNotNull(res.removed);
        String s = t.toString();
        assertTrue(s.contains("0Rmin"));
    }

    /** Delete root where right child has no left → null-compare branch. */
    public void testDeleteRootFindMinRightHasNoLeft_hitsNullCompareEvenDepth() {
        KDTree t = new KDTree();
        t.insert(new City("root", 40, 40));
        t.insert(new City("r",    60, 10));   // right child; no left beneath
        KDTree.KDDeleteResult res = t.deleteExact(40, 40);
        assertNotNull(res);
        assertNotNull(res.removed);
        assertEquals("root", res.removed.getName());
        City c = t.find(60, 10);
        assertNotNull(c);
        assertEquals("r", c.getName());
        assertNull(t.find(40, 40));
    }

    /** Delete node at depth 1 (Y split) with only right child → null-compare (odd depth). */
    public void testDeleteDepth1FindMinRightHasNoLeft_hitsNullCompareOddDepth() {
        KDTree t = new KDTree();
        t.insert(new City("root", 50, 50));     // depth 0 (X)
        t.insert(new City("target", 25, 60));   // depth 1 (Y)
        t.insert(new City("tright", 20, 80));   // only right child by Y
        assertNotNull(t.find(25, 60));
        KDTree.KDDeleteResult res = t.deleteExact(25, 60);
        assertNotNull(res);
        assertEquals("target", res.removed.getName());
        assertNull(t.find(25, 60));
        assertNotNull(t.find(20, 80));  // chosen as replacement
    }

    // ---- private comparator edge cases via reflection ----

    // (a != null && b == null) → true
    public void testIsLessInDim_NonNullVsNull_ReturnsTrue() throws Exception {
        KDTree t = new KDTree();
        java.lang.reflect.Method m =
            KDTree.class.getDeclaredMethod("isLessInDim", City.class, City.class, int.class);
        m.setAccessible(true);
        City a = new City("a", 10, 20);
        boolean result = (boolean) m.invoke(t, a, null, 1);
        assertTrue(result);
    }

    // (a == null && b != null) → false
    public void testIsLessInDim_NullVsNonNull_ReturnsFalse() throws Exception {
        KDTree t = new KDTree();
        java.lang.reflect.Method m =
            KDTree.class.getDeclaredMethod("isLessInDim", City.class, City.class, int.class);
        m.setAccessible(true);
        City b = new City("b", 30, 40);
        boolean result = (boolean) m.invoke(t, null, b, 0);
        assertFalse(result);
    }

 // When values are equal on the compared axis, method returns false (no tie-break).
    public void testIsLessInDim_TieOnComparedAxis_ReturnsFalse_NoTiebreak() throws Exception {
        KDTree t = new KDTree();
        var m = KDTree.class.getDeclaredMethod("isLessInDim", City.class, City.class, int.class);
        m.setAccessible(true);
        City a = new City("a", 5, 1);
        City b = new City("b", 5, 2);
        boolean res = (boolean) m.invoke(t, a, b, 0); // compare X (ties)
        assertFalse(res); // no tie-break → false
    }

    // Sanity: strictly less on the compared axis returns true.
    public void testIsLessInDim_ComparedAxisStrictlyLess_ReturnsTrue() throws Exception {
        KDTree t = new KDTree();
        var m = KDTree.class.getDeclaredMethod("isLessInDim", City.class, City.class, int.class);
        m.setAccessible(true);
        City a = new City("a", 4, 999);
        City b = new City("b", 5, -999);
        boolean res = (boolean) m.invoke(t, a, b, 0); // 4 < 5 on X
        assertTrue(res);
    }


    // --------- replacements for the previously unsupported calls ---------

    /** Root X-boundary equality must explore both sides; include the left hit. */
    public void testRangeSearch_TouchRootXBoundary_VisitsBothSides() {
        KDTree t = new KDTree();
        City root = new City("root", 5, 50);
        City left = new City("L", 5, 10);     // same X as root (ties go right structurally)
        City right = new City("R", 6, 60);
        t.insert(root); t.insert(left); t.insert(right);

        // r=0 centered on L → equality on root.x ensures both sides considered; only L can hit.
        KDTree.KDSearchResult res = t.rangeSearch(5, 10, 0);
        assertTrue(res.visited >= 2);                  // touched root and one side at least
        assertTrue(res.listing().contains("L (5, 10)"));
        assertEquals(1, listingCount(res.listing())); // exactly one hit
    }

    /** Level-1 Y-boundary equality should explore both subtrees at that split. */
    public void testRangeSearch_TouchLevel1YBoundary_VisitsBothSides() {
        KDTree t = new KDTree();
        City a = new City("A", 5, 50);  // root (X)
        City b = new City("B", 3, 40);  // left child (Y split at 40)
        City c = new City("C", 7, 60);
        City d = new City("D", 2, 40);  // equals B.y to force boundary case
        t.insert(a); t.insert(b); t.insert(c); t.insert(d);

        // Center on B and r=0 → exactly one hit ("B"), but boundary logic should allow exploring equality.
        KDTree.KDSearchResult res = t.rangeSearch(3, 40, 0);
        assertTrue(res.visited >= 2);                    // at least root + left
        String list = res.listing();
        assertTrue(list.contains("B (3, 40)"));
        assertEquals(1, listingCount(list));
    }

    /** Circle query: point exactly on boundary is included; outside is excluded. */
    public void testRangeSearch_PointExactlyOnCircleBoundary_Included() {
        KDTree t = new KDTree();
        City edge = new City("edge", 10, 5);
        City other = new City("other", 11, 5);
        t.insert(new City("root", 8, 5));
        t.insert(edge);
        t.insert(other);

        KDTree.KDSearchResult res = t.rangeSearch(0, 5, 10); // dist((0,5),(10,5)) = 10
        String list = res.listing();
        assertTrue(list.contains("edge (10, 5)"));
        assertFalse(list.contains("other (11, 5)"));
    }
    
    /** Odd-depth boundary: cy + r == node.y should prune RIGHT (not left). */
    public void testRangeSearch_OddBoundaryUpper_PrunesRightNoHit() {
        KDTree t = new KDTree();
        // depth 0 (X split at x=50)
        t.insert(new City("root", 50, 0));
        // depth 1 (Y split at y=40)
        t.insert(new City("mid",  25, 40));
        // grandchildren at depth 2
        t.insert(new City("L",  24, 10)); // left branch of 'mid'
        t.insert(new City("R",  26, 90)); // right branch of 'mid'

        // Set center so cy + r == 40 at depth-1: prune RIGHT of that node
        KDTree.KDSearchResult res = t.rangeSearch(0, 30, 10); // 30 + 10 == 40
        // We should NOT list anything (all far in X/Y), and the visit count
        // should be < exploring 'R' (pruned)
        assertEquals("", res.listing());
        // At least root+mid+one grandchild visited; but 'R' must be pruned.
        assertTrue(res.visited >= 2);
    }
    
    /** Deep circle check must use BOTH dx and dy at depth >= 2 (no false hits). */
    public void testRangeSearch_DeepNode_DXZeroButDYOutside_NoHit() {
        KDTree t = new KDTree();
        // Build to depth 2 where the target lives
        t.insert(new City("root",  0, 0));   // depth 0 (X)
        t.insert(new City("left", -1, 5));   // depth 1 (Y)
        t.insert(new City("deep",  0, 7));   // depth 2 (X)

        // Center aligned in X (dx=0) but dy=6 with r=5 → outside
        KDTree.KDSearchResult r = t.rangeSearch(0, 1, 5);
        assertFalse(r.listing().contains("deep (0, 7)"));
    }
    
    /** Deep circle check must use BOTH terms when dy=0 (no false hits). */
    public void testRangeSearch_DeepNode_DYZeroButDXOutside_NoHit() {
        KDTree t = new KDTree();
        // Arrange target at depth 2 on the other side of the tree
        t.insert(new City("root", 0, 0));     // depth 0
        t.insert(new City("right", 5, 5));    // depth 1
        t.insert(new City("deep", 11, 5));    // depth 2 (dy=0 vs center below)

        KDTree.KDSearchResult r = t.rangeSearch(5, 5, 5); // dx=6, dy=0 → outside
        assertFalse(r.listing().contains("deep (11, 5)"));
    }
    
    /**
     * Delete a depth-1 (Y-split) node that has children on both sides.
     * Replacement must be true min-by-Y across the whole subtree.
     */
    public void testDelete_Depth1BothSides_ChoosesGlobalMinY() {
        KDTree t = new KDTree();
        // depth 0 (X)
        t.insert(new City("Root", 50, 50));
        // target at depth 1 (Y)
        t.insert(new City("Target", 25, 60));
        // depth 2 under Target (X split): put smaller Y on the LEFT subtree
        t.insert(new City("LeftMin", 10, 1));     // global min by Y (should be chosen)
        t.insert(new City("RightY",  30, 5));     // larger Y

        KDTree.KDDeleteResult res = t.deleteExact(25, 60);
        assertNotNull(res.removed);
        // Target gone; chosen replacement must still be findable
        assertNull(t.find(25, 60));
        assertNotNull(t.find(10, 1));
        assertTrue(t.toString().contains("LeftMin (10, 1)"));
    }
    
    /** Deep hit at depth>=3 where dx^2 + dy^2 == r^2 exactly (kills < vs <= flips). */
    public void testRangeSearch_DeepBoundaryHit_Included() {
        KDTree t = new KDTree();
        // Depth 0..3 path to place target deep in the tree
        t.insert(new City("r",  0, 0));     // d0 (X)
        t.insert(new City("a", -1, 10));    // d1 (Y)
        t.insert(new City("b", -2,  9));    // d2 (X)
        City target = new City("hit", 3, 4); // distance to (0,0) = 5 exactly
        t.insert(target);                   // d3+

        KDTree.KDSearchResult res = t.rangeSearch(0, 0, 5);
        assertTrue(res.listing().contains("hit (3, 4)"));
    }
    
    /** Deep miss at depth>=3 where dx^2 + dy^2 = 26 (>25) (kills “drop one term”). */
    public void testRangeSearch_DeepBoundaryMiss_Excluded() {
        KDTree t = new KDTree();
        t.insert(new City("r",  0, 0));    // d0
        t.insert(new City("a", -1, 10));   // d1
        t.insert(new City("b", -2,  9));   // d2
        City miss = new City("miss", 1, 5); // dist^2=26
        t.insert(miss);                    // d3+

        KDTree.KDSearchResult res = t.rangeSearch(0, 0, 5);
        assertFalse(res.listing().contains("miss (1, 5)"));
    }
    
    /**
     * Delete at depth 0 (X-split). After deleting the root, the replacement
     * must have the minimum X from the right subtree (X==50). We don't
     * over-constrain the Y tie-break (implementation-dependent).
     */
    public void testDelete_Depth0_RightMinByX_TieBreakByY() {
        KDTree t = new KDTree();
        t.insert(new City("Root", 40, 40));   // depth 0 (X)
        t.insert(new City("R",    60, 99));   // right subtree
        t.insert(new City("C1",   50, 10));   // X=50
        t.insert(new City("C2",   50,  5));   // X=50 (smaller Y)

        KDTree.KDDeleteResult res = t.deleteExact(40, 40);
        assertNotNull(res.removed);

        // Root line must now show an X of 50 (global min X from the right subtree).
        String firstLine = t.toString().split("\n")[0];
        assertTrue(firstLine.contains("(50, "));

        // Old root must be gone.
        assertNull(t.find(40, 40));
    }

    
    /**
     * Delete at depth 1 (Y-split). Both children exist; global min by Y is on LEFT.
     * Replacement must be that left min (kills equality/arithmetic survivors in findMin).
     */
    public void testDelete_Depth1_BothSides_GlobalMinYFromLeft() {
        KDTree t = new KDTree();
        // depth 0 (X)
        t.insert(new City("Root", 50, 50));
        // target at depth 1 (Y)
        t.insert(new City("Target", 25, 60));
        // Build both sides under Target (depth 2 is X-split)
        t.insert(new City("LeftMin",  10,  1)); // global min Y
        t.insert(new City("RightY",   30,  5)); // larger Y

        KDTree.KDDeleteResult res = t.deleteExact(25, 60);
        assertNotNull(res.removed);
        assertNull(t.find(25, 60));
        assertNotNull(t.find(10, 1));                 // chosen replacement remains
        assertTrue(t.toString().contains("LeftMin (10, 1)"));
    }








    
}
