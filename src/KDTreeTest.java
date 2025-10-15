import student.TestCase;

/**
 * Tests for the reworked KDTree that stores City records and exposes
 * insert/findExact/delete/rangeSearch plus traversal callbacks.
 */
public class KDTreeTest extends TestCase {

    /** System under test created fresh for each test. */
    private KDTree kd;

    /** Creates a fresh tree and clears it. */
    public void setUp() {
        kd = new KDTree();
        kd.clear();
    }

    // ---------- Helpers -------------------------------------------------------

    /**
     * Returns a stable textual snapshot using inorder traversal with levels:
     * {@code "<level><space><2*level spaces><name> (x, y)\n"} for every node.
     */
    private static String snapshot(KDTree t) {
        StringBuilder sb = new StringBuilder();
        t.inorderWithLevels((lvl, c) -> {
            sb.append(lvl).append(' ');
            for (int i = 0; i < 2 * lvl; i++) sb.append(' ');
            sb.append(c.getName())
              .append(" (").append(c.getX()).append(", ").append(c.getY()).append(")")
              .append('\n');
        });
        return sb.toString();
    }

    /**
     * Returns the level where a given name appears in {@link #snapshot}, or -1.
     */
    private static int levelOf(String listing, String name) {
        for (String ln : listing.split("\n")) {
            if (ln.isEmpty()) continue;
            int sp = ln.indexOf(' ');
            if (sp < 0) continue;
            String rest = ln.substring(sp + 1);
            if (rest.contains(" " + name + " (") || rest.startsWith(name + " (")) {
                return Integer.parseInt(ln.substring(0, sp));
            }
        }
        return -1;
    }

    /**
     * Returns the first line in {@link #snapshot} that begins with level 0,
     * or an empty string if none.
     */
    private static String rootLine(String listing) {
        for (String ln : listing.split("\n")) {
            if (ln.startsWith("0 ")) return ln;
        }
        return "";
    }

    /**
     * Counts lines in a string, treating empty as zero.
     */
    private static int lineCount(String s) {
        return s.isEmpty() ? 0 : s.split("\\R").length;
    }

    /**
     * True if the listing contains the exact tuple "name (x, y)" with either a
     * comma+space or a comma without a space.
     */
    private static boolean containsEntry(String listing, String name, int x, int y) {
        String a = name + " (" + x + ", " + y + ")";
        String b = name + " (" + x + "," + y + ")";
        return listing.contains(a) || listing.contains(b);
    }

    // ---------- Basics / Structure -------------------------------------------

    /** Verifies empty tree, size, simple inserts, duplicate rejection, clear. */
    public void testBasics_Empty_Insert_RejectDup_Clear() {
        assertTrue(kd.isEmpty());
        assertEquals(0, kd.size());
        assertEquals("", snapshot(kd));

        assertTrue(kd.insert("A", 10, 20));
        assertTrue(kd.insert("B", 1, 1));
        assertTrue(kd.insert("C", 11, 20));
        assertFalse(kd.insert("Dup", 10, 20));
        assertEquals(3, kd.size());

        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("Right", 1, 0));
        String lv = snapshot(t);
        assertEquals(1, levelOf(lv, "Right"));
        assertEquals(0, levelOf(lv, "R"));

        kd.clear();
        assertTrue(kd.isEmpty());
        assertEquals(0, kd.size());
        assertEquals("", snapshot(kd));
    }

    /** Confirms tie-breaks go right on X at even depths and Y at odd depths. */
    public void testInsert_TiesGoRight_ByDepthParity() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 5, 5));
        assertTrue(t.insert("TieX", 5, 4));
        String s1 = snapshot(t);
        assertEquals(1, levelOf(s1, "TieX"));
        assertEquals(0, levelOf(s1, "R"));

        KDTree u = new KDTree();
        assertTrue(u.insert("R", 5, 5));
        assertTrue(u.insert("A", 3, 3));
        assertTrue(u.insert("TieY", 2, 3));
        String s2 = snapshot(u);
        assertEquals(2, levelOf(s2, "TieY"));
        assertEquals(1, levelOf(s2, "A"));
        assertEquals(0, levelOf(s2, "R"));
    }

    /** Checks inorder order and indentation on a deeper alternating tree. */
    public void testShape_Indentation_AndOrder() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 5, 5));
        assertTrue(t.insert("A", 3, 5));
        assertTrue(t.insert("B", 7, 5));
        assertTrue(t.insert("C", 3, 7));
        assertTrue(t.insert("D", 3, 1));
        assertTrue(t.insert("E", 7, 4));
        assertTrue(t.insert("F", 7, 6));

        String out = snapshot(t);
        int iD = out.indexOf(" D (3, 1)");
        int iA = out.indexOf(" A (3, 5)");
        int iC = out.indexOf(" C (3, 7)");
        int iR = out.indexOf(" R (5, 5)");
        int iE = out.indexOf(" E (7, 4)");
        int iB = out.indexOf(" B (7, 5)");
        int iF = out.indexOf(" F (7, 6)");
        assertTrue(iD >= 0 && iA > iD && iC > iA && iR > iC && iE > iR && iB > iE && iF > iB);

        for (String ln : out.split("\n")) {
            if (ln.isEmpty()) continue;
            int sp = ln.indexOf(' ');
            int lvl = Integer.parseInt(ln.substring(0, sp));
            String rest = ln.substring(sp + 1);
            int i = 0, spaces = 0;
            while (i < rest.length() && rest.charAt(i) == ' ') { spaces++; i++; }
            assertEquals(2 * lvl, spaces);
        }
    }

    /** Ensures depth parity propagates correctly down the right spine. */
    public void testInsert_DepthParityAlongRightSide() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("B", 5, 0));
        assertTrue(t.insert("BL", 5, -1));
        assertTrue(t.insert("BR", 5, 1));
        String s = snapshot(t);
        int iBL = s.indexOf(" BL (5, -1)");
        int iB = s.indexOf(" B (5, 0)");
        int iBR = s.indexOf(" BR (5, 1)");
        assertTrue(iBL >= 0 && iB > iBL && iBR > iB);
    }

    /** Confirms preorder traversal reports increasing levels on both sides. */
    public void testPreorder_LevelsIncrementOnBothSides() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("L", -1, 0));
        assertTrue(t.insert("LL", -2, 0));
        assertTrue(t.insert("R1", 1, 0));
        final int[] lvR = { -1 }, lvL = { -1 }, lvLL = { -1 }, lvR1 = { -1 };
        t.preorderWithLevels((lvl, e) -> {
            if (e.getName().equals("R"))  lvR[0]  = lvl;
            if (e.getName().equals("L"))  lvL[0]  = lvl;
            if (e.getName().equals("LL")) lvLL[0] = lvl;
            if (e.getName().equals("R1")) lvR1[0] = lvl;
        });
        assertEquals(0, lvR[0]);
        assertEquals(1, lvL[0]);
        assertEquals(2, lvLL[0]);
        assertEquals(1, lvR1[0]);
    }

    // ---------- Find ----------------------------------------------------------

    /** Verifies findExact hit and miss on both sides with alternating splits. */
    public void testFindExact_HitAndMiss_WithAlternation() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 5, 5));
        assertTrue(t.insert("N1", 6, 7));
        assertTrue(t.insert("Goal", 5, 7));
        assertNotNull(t.findExact(5, 7));
        assertNull(t.findExact(9, 9));

        KDTree u = new KDTree();
        assertTrue(u.insert("R", 0, 0));
        assertTrue(u.insert("B", 5, 0));
        assertTrue(u.insert("BL", 5, -2));
        assertTrue(u.insert("BLL", 4, -2));
        assertNotNull(u.findExact(5, -2));
        assertNull(u.findExact(4, -3));
    }

    /** Confirms path alternation X→Y→X is sufficient to reach a right target. */
    public void testFindExact_AlternationOnRightBranch() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("B", 5, 0));
        assertTrue(t.insert("BL", 5, -2));
        assertTrue(t.insert("BLL", 4, -2));
        assertNotNull(t.findExact(5, -2));
        assertNull(t.findExact(4, -3));
    }

    // ---------- Delete --------------------------------------------------------

    /** Reports visited count for empty and simple chains on both sides. */
    public void testDelete_VisitedCounts_EmptyAndChains() {
        KDTree t = new KDTree();
        KDTree.DeleteOutcome d0 = t.delete(1, 1);
        assertEquals(0, d0.visited);
        assertNull(d0.entry);

        KDTree r = new KDTree();
        assertTrue(r.insert("R", 0, 0));
        assertTrue(r.insert("A", 1, 0));
        assertTrue(r.insert("B", 2, 0));
        KDTree.DeleteOutcome d1 = r.delete(2, 0);
        assertNotNull(d1.entry);
        assertEquals(3, d1.visited);

        KDTree l = new KDTree();
        assertTrue(l.insert("R", 0, 0));
        assertTrue(l.insert("A", -1, 0));
        assertTrue(l.insert("B", -2, 0));
        KDTree.DeleteOutcome d2 = l.delete(-2, 0);
        assertNotNull(d2.entry);
        assertEquals(3, d2.visited);
    }

    /** Replaces root from the right subtree and prefers preorder-first on ties. */
    public void testDelete_RootTwoChildren_MinFromRight_PreorderTie() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 5, 5));
        assertTrue(t.insert("R", 8, 5));
        assertTrue(t.insert("RX", 6, 9));
        assertTrue(t.insert("RXL", 6, 8));
        KDTree.DeleteOutcome d = t.delete(5, 5);
        assertNotNull(d.entry);
        String lvl0 = rootLine(snapshot(t));
        assertTrue(lvl0.contains("RX") || lvl0.contains("(6, 9)"));
        assertFalse(snapshot(t).contains("Root (5, 5)"));
    }

    /** Replaces root from the left subtree and rewires leftover as right child. */
    public void testDelete_RootOnlyLeft_RewireLeftoverRight() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 5, 5));
        assertTrue(t.insert("L", 3, 0));
        assertTrue(t.insert("LL", 2, 0));
        KDTree.DeleteOutcome d = t.delete(5, 5);
        assertNotNull(d.entry);
        String listing = snapshot(t);
        String lvl0 = rootLine(listing);
        assertTrue(lvl0.contains("(2, 0)"));
        assertTrue(listing.contains("\n1  "));
        assertFalse(listing.contains("Root (5, 5)"));
    }

    /** Does not change size when target is missing and reports positive visits. */
    public void testDelete_Miss_SizeUnchanged_VisitsPositive() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("A", -1, -1));
        assertTrue(t.insert("B", 1, 1));
        int before = t.size();
        KDTree.DeleteOutcome d = t.delete(9, 9);
        assertNull(d.entry);
        assertEquals(before, t.size());
        assertTrue(d.visited >= 2);
    }

    /** Deletes internal node at depth two and keeps parity via min-from-right. */
    public void testDelete_InternalDepthTwo_ParityPreserved() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("A", 1, 5));
        assertTrue(t.insert("T", 1, 2));
        assertTrue(t.insert("NR", 2, 1));
        KDTree.DeleteOutcome d = t.delete(1, 2);
        assertNotNull(d.entry);
        String out = snapshot(t);
        assertFalse(out.contains(" T (1, 2)"));
        assertTrue(out.contains(" NR (2, 1)"));
    }

    /** Confirms delete uses Y at depth minus one on both sides. */
    public void testDelete_SearchUsesYAtDepthMinusOne() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("A", 1, 5));
        assertTrue(t.insert("TL", 1, 3));
        int before = t.size();
        KDTree.DeleteOutcome dL = t.delete(1, 3);
        assertNotNull(dL.entry);
        assertEquals(before - 1, t.size());
        assertNotNull(t.findExact(1, 5));

        KDTree u = new KDTree();
        assertTrue(u.insert("R", 0, 0));
        assertTrue(u.insert("A", 1, 5));
        assertTrue(u.insert("TR", 1, 7));
        int before2 = u.size();
        KDTree.DeleteOutcome dR = u.delete(1, 7);
        assertNotNull(dR.entry);
        assertEquals(before2 - 1, u.size());
        assertNotNull(u.findExact(1, 5));
    }

    /** Validates equality check during delete bookkeeping. */
    public void testDelete_EqualityCheck_RemovesCorrectEntry() {
        KDTree t = new KDTree();
        assertTrue(t.insert("A", 10, 10));
        assertTrue(t.insert("B", 20, 20));
        KDTree.DeleteOutcome d = t.delete(10, 10);
        assertNotNull(d.entry);
        assertEquals("A", d.entry.getName());
        assertEquals(1, t.size());
    }

    // ---------- Range search --------------------------------------------------

    /** Covers single node include/exclude and large-math boundaries. */
    public void testRange_SingleNodeAndLargeBoundaries() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Only", 10, 10));
        KDTree.SearchOutcome s1 = t.rangeSearch(10, 10, 0);
        assertEquals("Only (10, 10)\n", s1.listing);
        assertEquals(1, s1.visited);
        KDTree.SearchOutcome s2 = t.rangeSearch(0, 0, 0);
        assertEquals("", s2.listing);
        assertEquals(1, s2.visited);

        KDTree d = new KDTree();
        assertTrue(d.insert("P", 3, 4));
        assertEquals("", d.rangeSearch(0, 0, 4).listing);
        assertEquals("P (3, 4)\n", d.rangeSearch(0, 0, 5).listing);

        KDTree edge = new KDTree();
        assertTrue(edge.insert("Edge", 32767, 32767));
        assertEquals("", edge.rangeSearch(0, 0, 46339).listing);
        KDTree.SearchOutcome inside = edge.rangeSearch(0, 0, 46340);
        assertTrue(inside.listing.contains("Edge (32767, 32767)"));
        assertTrue(inside.visited >= 1);
    }

    /** Exercises pruning boundaries for X split and Y levels including clamp. */
    public void testRange_PruningBoundaries_AndClamp() {
        KDTree t1 = new KDTree();
        assertTrue(t1.insert("R", 0, 0));
        assertTrue(t1.insert("L", -5, 0));
        assertTrue(t1.insert("LR", -6, 0));
        KDTree.SearchOutcome sLeftOnly = t1.rangeSearch(-1, 0, 0);
        assertEquals("", sLeftOnly.listing);
        assertEquals(3, sLeftOnly.visited);

        KDTree t2 = new KDTree();
        assertTrue(t2.insert("R", 0, 0));
        assertTrue(t2.insert("RR", 5, 0));
        assertTrue(t2.insert("RRR", 4, 0));
        KDTree.SearchOutcome sRightOnly = t2.rangeSearch(0, 0, 0);
        assertEquals("R (0, 0)\n", sRightOnly.listing);
        assertEquals(3, sRightOnly.visited);

        KDTree t3 = new KDTree();
        assertTrue(t3.insert("R", 0, 0));
        assertTrue(t3.insert("L", -1, 0));
        assertEquals("R (0, 0)\n", t3.rangeSearch(0, 0, 0).listing);
        assertEquals(1, t3.rangeSearch(0, 0, 0).visited);

        KDTree t4 = new KDTree();
        assertTrue(t4.insert("R", 0, 0));
        assertTrue(t4.insert("X", 1, 0));
        assertTrue(t4.insert("LOW", 1, -1));
        KDTree.SearchOutcome sY = t4.rangeSearch(0, 0, 0);
        assertEquals("R (0, 0)\n", sY.listing);
        assertEquals(2, sY.visited);

        KDTree t5 = new KDTree();
        assertTrue(t5.insert("R", 0, 0));
        assertTrue(t5.insert("L", -10, 0));
        assertTrue(t5.insert("LL", -10, -10));
        assertTrue(t5.insert("LU", -10, 10));
        KDTree.SearchOutcome sBelow = t5.rangeSearch(-10, -100, 1);
        assertEquals("", sBelow.listing);
        assertEquals(3, sBelow.visited);

        KDTree t6 = new KDTree();
        assertTrue(t6.insert("R", 5, 5));
        assertTrue(t6.insert("L", 1, 1));
        KDTree.SearchOutcome s0 = t6.rangeSearch(0, 0, 0);
        assertEquals("", s0.listing);
        KDTree.SearchOutcome s1 = t6.rangeSearch(0, 0, 1);
        assertTrue(s1.visited >= 1);
    }

    /** Checks canonical 3-4-5 and asymmetric cases to validate dx^2+dy^2 math. */
    public void testRange_DistanceMath_BoundariesAndAsymmetry() {
        KDTree t = new KDTree();
        assertTrue(t.insert("P", 3, 4));
        KDTree.SearchOutcome s4 = t.rangeSearch(0, 0, 4);
        assertEquals(0, lineCount(s4.listing));
        KDTree.SearchOutcome s5 = t.rangeSearch(0, 0, 5);
        assertTrue(containsEntry(s5.listing, "P", 3, 4));

        KDTree u = new KDTree();
        assertTrue(u.insert("Q", 8, 1));
        KDTree.SearchOutcome r6 = u.rangeSearch(0, 0, 6);
        KDTree.SearchOutcome r8 = u.rangeSearch(0, 0, 8);
        KDTree.SearchOutcome r9 = u.rangeSearch(0, 0, 9);
        assertEquals(0, lineCount(r6.listing));
        assertEquals(0, lineCount(r8.listing));
        assertTrue(containsEntry(r9.listing, "Q", 8, 1));
    }

    /** Validates long-radius boundary to catch overflow-style arithmetic swaps. */
    public void testRange_LargeBoundary_NoOverflow() {
        KDTree t = new KDTree();
        assertTrue(t.insert("W", 32767, 32767));
        KDTree.SearchOutcome r46339 = t.rangeSearch(0, 0, 46339);
        KDTree.SearchOutcome r46340 = t.rangeSearch(0, 0, 46340);
        assertEquals(0, lineCount(r46339.listing));
        assertTrue(containsEntry(r46340.listing, "W", 32767, 32767));
    }

    /** Ensures rectangle pruning still reaches a far target when it should. */
    public void testRange_PruningStillReachesFarQuadrant() {
        KDTree t = new KDTree();
        assertTrue(t.insert("O",   0,   0));
        assertTrue(t.insert("NE", 100, 100));
        assertTrue(t.insert("SE", 100, -100));
        assertTrue(t.insert("NW", -100, 100));
        assertTrue(t.insert("SW", -100, -100));
        KDTree.SearchOutcome s = t.rangeSearch(100, 100, 1);
        assertEquals(1, lineCount(s.listing));
        assertTrue(containsEntry(s.listing, "NE", 100, 100));
    }

    /** Covers mixed-sign and negative coordinate distance checks. */
    public void testRange_Distance_WithNegativesAndMixedSigns() {
        KDTree t1 = new KDTree();
        assertTrue(t1.insert("B", -3, -4));
        KDTree.SearchOutcome s4 = t1.rangeSearch(0, 0, 4);
        KDTree.SearchOutcome s5 = t1.rangeSearch(0, 0, 5);
        assertEquals(0, lineCount(s4.listing));
        assertTrue(containsEntry(s5.listing, "B", -3, -4));

        KDTree t2 = new KDTree();
        assertTrue(t2.insert("C", 6, -8));
        KDTree.SearchOutcome s9 = t2.rangeSearch(0, 0, 9);
        KDTree.SearchOutcome s10 = t2.rangeSearch(0, 0, 10);
        assertEquals(0, lineCount(s9.listing));
        assertTrue(containsEntry(s10.listing, "C", 6, -8));
    }

    /** Verifies zero-radius behavior includes only exact center. */
    public void testRange_ZeroRadius_ExactCenterOnly() {
        KDTree t = new KDTree();
        assertTrue(t.insert("P", 7, 7));
        KDTree.SearchOutcome s = t.rangeSearch(7, 7, 0);
        assertTrue(containsEntry(s.listing, "P", 7, 7));
        KDTree.SearchOutcome s2 = t.rangeSearch(8, 8, 0);
        assertEquals(0, lineCount(s2.listing));
    }

    /** Ensures multiple points inside a circle are all listed. */
    public void testRange_MultiplePoints_AllIncluded() {
        KDTree t = new KDTree();
        assertTrue(t.insert("A", 0, 0));
        assertTrue(t.insert("B", 3, 4));
        assertTrue(t.insert("C", 4, 3));
        KDTree.SearchOutcome s = t.rangeSearch(0, 0, 5);
        assertTrue(containsEntry(s.listing, "A", 0, 0));
        assertTrue(containsEntry(s.listing, "B", 3, 4));
        assertTrue(containsEntry(s.listing, "C", 4, 3));
        assertEquals(3, lineCount(s.listing));
    }

    /** Checks small off-by-one around the circle boundary. */
    public void testRange_OffByOne_Boundary() {
        KDTree t = new KDTree();
        assertTrue(t.insert("X", 1, 0));
        KDTree.SearchOutcome s0 = t.rangeSearch(0, 0, 0);
        KDTree.SearchOutcome s1 = t.rangeSearch(0, 0, 1);
        assertEquals(0, lineCount(s0.listing));
        assertTrue(containsEntry(s1.listing, "X", 1, 0));
    }

    /** Confirms pruning can discard entire left or right subtrees when safe. */
    public void testRange_Pruning_LeftOrRightDiscarded() {
        KDTree left = new KDTree();
        assertTrue(left.insert("R", 0, 0));
        assertTrue(left.insert("L", -10, 0));
        assertTrue(left.insert("LL", -20, 0));
        KDTree.SearchOutcome sL = left.rangeSearch(15, 0, 4);
        assertEquals(0, lineCount(sL.listing));
        assertTrue(sL.visited < 3);

        KDTree right = new KDTree();
        assertTrue(right.insert("R", 0, 0));
        assertTrue(right.insert("Right", 10, 0));
        assertTrue(right.insert("RR", 20, 0));
        KDTree.SearchOutcome sR = right.rangeSearch(-15, 0, 4);
        assertEquals(0, lineCount(sR.listing));
        assertTrue(sR.visited < 3);
    }

    /** Basic verification that pruning happens at all in a far query. */
    public void testRange_PruningOccursOnFarQuery() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 50, 50));
        assertTrue(t.insert("Far", 200, 200));
        KDTree.SearchOutcome s = t.rangeSearch(0, 0, 10);
        assertEquals(0, lineCount(s.listing));
    }

    /** Validates that both axes contribute to distance inclusion. */
    public void testRange_BothAxesRequiredForInclusion() {
        KDTree t = new KDTree();
        assertTrue(t.insert("T", 20, 21));
        KDTree.SearchOutcome s28 = t.rangeSearch(0, 0, 28);
        KDTree.SearchOutcome s29 = t.rangeSearch(0, 0, 29);
        assertEquals(0, lineCount(s28.listing));
        assertEquals(1, lineCount(s29.listing));
        assertTrue(containsEntry(s29.listing, "T", 20, 21));
    }
}
