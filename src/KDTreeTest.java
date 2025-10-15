import student.TestCase;

/**
 * Tests for the reworked KDTree that stores City records and exposes
 * insert/findExact/delete/rangeSearch plus traversal callbacks.
 * 
 * @author Parth Mehta and Anurag Pokala
 * @version 10/6/25
 */
public class KDTreeTest extends TestCase {

    /** System under test created fresh for each test. */
    private KDTree kd;

    /** Creates a fresh tree and clears it. */
    public void setUp() {
        kd = new KDTree();
        kd.clear();
    }

    // ---------- Helpers
    // -------------------------------------------------------


    /**
     * Returns a stable textual snapshot using inorder traversal with levels:
     * {@code "<level><space><2*level spaces><name> (x, y)\n"} for every node.
     */
    private static String snapshot(KDTree t) {
        StringBuilder sb = new StringBuilder();
        t.inorderWithLevels((lvl, c) -> {
            sb.append(lvl).append(' ');
            for (int i = 0; i < 2 * lvl; i++)
                sb.append(' ');
            sb.append(c.getName()).append(" (").append(c.getX()).append(", ")
                .append(c.getY()).append(")").append('\n');
        });
        return sb.toString();
    }


    /**
     * Returns the level where a given name appears in {@link #snapshot}, or -1.
     */
    private static int levelOf(String listing, String name) {
        for (String ln : listing.split("\n")) {
            if (ln.isEmpty())
                continue;
            int sp = ln.indexOf(' ');
            if (sp < 0)
                continue;
            String rest = ln.substring(sp + 1);
            if (rest.contains(" " + name + " (") || rest.startsWith(name
                + " (")) {
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
            if (ln.startsWith("0 "))
                return ln;
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
    private static boolean containsEntry(
        String listing,
        String name,
        int x,
        int y) {
        String a = name + " (" + x + ", " + y + ")";
        String b = name + " (" + x + "," + y + ")";
        return listing.contains(a) || listing.contains(b);
    }


    /**
     * Builds an inorder-with-levels listing (e.g., "0 R (x, y)\n1 L (x, y)\n").
     */
    private static String toLevelsListing(KDTree t) {
        StringBuilder sb = new StringBuilder();
        t.inorderWithLevels((lvl, e) -> {
            sb.append(lvl).append(' ').append(" ".repeat(Math.max(0, 2 * lvl)))
                .append(e.getName()).append(" (").append(e.getX()).append(", ")
                .append(e.getY()).append(")").append('\n');
        });
        return sb.toString();
    }


    // --- helpers (safe with student.TestCase) ---
    private static String listInorder(KDTree t) {
        StringBuilder sb = new StringBuilder();
        t.inorderWithLevels((lvl, e) -> {
            sb.append(lvl).append(' ');
            for (int i = 0; i < 2 * lvl; i++)
                sb.append(' ');
            sb.append(e.getName()).append(' ').append(e.getX()).append(' ')
                .append(e.getY()).append('\n');
        });
        return sb.toString();
    }


    private static int indexOfLine(String listing, String needle) {
        return listing.indexOf(needle);
    }


    /**
     * Returns the depth/level for a given city name from an inorder-with-levels
     * listing.
     */
    private static int extractLevel(String listing, String name) {
        for (String ln : listing.split("\\R")) {
            if (ln.isEmpty())
                continue;

            // parse the leading level number
            int i = 0, lvl = 0;
            while (i < ln.length() && Character.isDigit(ln.charAt(i))) {
                lvl = 10 * lvl + (ln.charAt(i) - '0');
                i++;
            }
            if (i >= ln.length() || ln.charAt(i) != ' ')
                continue;

            // skip the single space + (2*lvl) spaces of indent
            String rest = ln.substring(i + 1);
            int k = 0;
            while (k < rest.length() && rest.charAt(k) == ' ')
                k++;
            String after = rest.substring(k);

            // Our listing format is: "<level> <indent><name> (<x>, <y>)"
            if (after.startsWith(name + " ") || after.startsWith(name + "(") // tolerate
                                                                             // no
                                                                             // space
                                                                             // before
                                                                             // '('
                || after.equals(name)) {
                return lvl;
            }
        }
        return -1;
    }


    /**
     * True if listing contains "name (x, y)" (with or without the space after
     * the comma).
     */
    private static boolean containsListingEntry(
        String listing,
        String name,
        int x,
        int y) {
        String a = name + " (" + x + ", " + y + ")";
        String b = name + " (" + x + "," + y + ")";
        return listing.contains(a) || listing.contains(b);
    }


    /** Line count utility used for quick equals/empty checks on listings. */
    private static int countLines(String s) {
        return s.isEmpty() ? 0 : s.split("\\R").length;
    }

    // ---------- Basics / Structure -------------------------------------------


    /**
     * Verifies empty tree, size, simple inserts, duplicate rejection, clear.
     */
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
        assertTrue(iD >= 0 && iA > iD && iC > iA && iR > iC && iE > iR
            && iB > iE && iF > iB);

        for (String ln : out.split("\n")) {
            if (ln.isEmpty())
                continue;
            int sp = ln.indexOf(' ');
            int lvl = Integer.parseInt(ln.substring(0, sp));
            String rest = ln.substring(sp + 1);
            int i = 0, spaces = 0;
            while (i < rest.length() && rest.charAt(i) == ' ') {
                spaces++;
                i++;
            }
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
            if (e.getName().equals("R"))
                lvR[0] = lvl;
            if (e.getName().equals("L"))
                lvL[0] = lvl;
            if (e.getName().equals("LL"))
                lvLL[0] = lvl;
            if (e.getName().equals("R1"))
                lvR1[0] = lvl;
        });
        assertEquals(0, lvR[0]);
        assertEquals(1, lvL[0]);
        assertEquals(2, lvLL[0]);
        assertEquals(1, lvR1[0]);
    }

    // ---------- Find
    // ----------------------------------------------------------


    /**
     * Verifies findExact hit and miss on both sides with alternating splits.
     */
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


    /**
     * Confirms path alternation X→Y→X is sufficient to reach a right target.
     */
    public void testFindExact_AlternationOnRightBranch() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0));
        assertTrue(t.insert("B", 5, 0));
        assertTrue(t.insert("BL", 5, -2));
        assertTrue(t.insert("BLL", 4, -2));
        assertNotNull(t.findExact(5, -2));
        assertNull(t.findExact(4, -3));
    }

    // ---------- Delete
    // --------------------------------------------------------


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


    /**
     * Replaces root from the right subtree and prefers preorder-first on ties.
     */
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


    /**
     * Replaces root from the left subtree and rewires leftover as right child.
     */
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


    /**
     * Does not change size when target is missing and reports positive visits.
     */
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


    /**
     * Deletes internal node at depth two and keeps parity via min-from-right.
     */
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

    // ---------- Range search
    // --------------------------------------------------


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


    /**
     * Exercises pruning boundaries for X split and Y levels including clamp.
     */
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


    /**
     * Checks canonical 3-4-5 and asymmetric cases to validate dx^2+dy^2 math.
     */
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


    /**
     * Validates long-radius boundary to catch overflow-style arithmetic swaps.
     */
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
        assertTrue(t.insert("O", 0, 0));
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


    /**
     * On an X split (depth 0), replacement min should be by (x, then y).
     * We delete the root; right subtree has equal x values with different y.
     * Expected new root is the smallest (x,y) in right subtree.
     */
    public void testDeleteRoot_XSplit_EqualX_TieOnY() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 10, 10)); // depth 0: X split
        assertTrue(t.insert("R1", 10, 12)); // equal X, higher Y
        assertTrue(t.insert("R2", 10, 5)); // equal X, LOWER Y -> should win
        assertTrue(t.insert("R3", 12, 0)); // larger X, ignored for min

        KDTree.DeleteOutcome out = t.delete(10, 10);
        assertNotNull(out.entry);

        City newRoot = t.findExact(10, 5);
        assertNotNull("new root should be (10,5)", newRoot);
        assertEquals("R2", newRoot.getName());
    }


    /**
     * On a Y split (depth 1), replacement min should be by (y, then x).
     * We make the target at depth 1 by inserting a right child under root,
     * then delete that node. Its right subtree contains points tied on Y with
     * different X; we must pick the smaller X.
     */
    public void testDeleteAtDepth1_YSplit_EqualY_TieOnX() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 0, 0)); // depth 0 (X)
        assertTrue(t.insert("T", 5, 5)); // depth 1 (Y) — will delete this
        // Right subtree of T (depth 2 then 3...) contains equal Y=7 with
        // different X.
        assertTrue(t.insert("C1", 6, 7)); // (y=7, x=6) -> should be chosen
        assertTrue(t.insert("C2", 8, 7)); // (y=7, x=8) -> should lose
        assertTrue(t.insert("C3", 9, 9)); // larger y, ignored

        KDTree.DeleteOutcome out = t.delete(5, 5);
        assertNotNull(out.entry);

        // Node T should be replaced by (6,7); it must exist now.
        City replaced = t.findExact(6, 7);
        assertNotNull("replacement (6,7) must remain in tree as promoted",
            replaced);
    }


    /**
     * Sanity: findExact must not return the root when coords don't match.
     * Kills the mutant that flips the root equality check to always true.
     */
    public void testFindExact_NoFalsePositiveAtRoot() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 0, 0));
        assertTrue(t.insert("R", 10, 0));
        assertNull(t.findExact(999, 888));
        assertNotNull(t.findExact(10, 0));
    }


    /**
     * X-branch compare must send search LEFT when x is smaller at depth 0.
     * Kills the “comparison -> false (always go right)” mutant in findExact.
     */
    public void testFindExact_MustGoLeftOnX() {
        KDTree t = new KDTree();
        assertTrue(t.insert("Root", 10, 0));
        assertTrue(t.insert("L", 5, 0)); // left by X at depth 0
        assertNotNull(t.findExact(5, 0));
    }


    /**
     * Parity (depth % 2) must switch the comparison axis: at depth 1 use Y.
     * Kills parity/operand arithmetic mutants around the split decision.
     */
    public void testFindExact_ParityAtDepth1UsesY() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth 0: split X
        assertTrue(t.insert("X", 5, 7)); // right of root
        assertTrue(t.insert("YLeft", 5, 1)); // under X; depth 1 compares Y ->
                                             // goes LEFT
        assertNotNull(t.findExact(5, 1));
    }


    /**
     * At depth 1 (Y-split), an equality on Y must go RIGHT.
     * Shape we build:
     *
     * depth 0 (split X): R (0,0)
     * \
     * depth 1 (split Y): A (5,5)
     * \
     * depth 2 (split X): TY (7,5) // y-tie with A -> goes RIGHT at depth 1
     */
    public void testInsert_ParityAndTiePolicyUnderDepth1_GoesRight() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth 0 (split X)
        assertTrue(t.insert("A", 5, 5)); // right of R -> depth 1 (split Y)
        assertTrue(t.insert("TY", 7, 5)); // y equals A.y -> must go RIGHT of A

        // Build the level listing using the tree’s inorderWithLevels
        StringBuilder sb = new StringBuilder();
        t.inorderWithLevels((lvl, e) -> {
            sb.append(lvl).append(' ');
            for (int i = 0; i < 2 * lvl; i++)
                sb.append(' ');
            sb.append(e.getName()).append(" (").append(e.getX()).append(", ")
                .append(e.getY()).append(")\n");
        });
        String listing = sb.toString();

        // Extract the level of TY; should be 2 if it is indeed right child of A
        int levelTY = extractLevel(listing, "TY");
        assertTrue("Expected TY at level 2 but was " + levelTY + "\nListing:\n"
            + listing, levelTY == 2);

        // Sanity: A should be at level 1, R at level 0
        assertTrue(extractLevel(listing, "A") == 1);
        assertTrue(extractLevel(listing, "R") == 0);
    }


    /**
     * Delete on an empty tree must short-circuit: visited==0 and entry==null.
     * Kills the mutant that breaks the root==null guard in delete().
     */
    public void testDelete_EmptyGuard_VisitedZeroAndNullEntry() {
        KDTree d = new KDTree();
        KDTree.DeleteOutcome out = d.delete(1, 2);
        assertEquals(0, out.visited);
        assertNull(out.entry);
    }


    /**
     * Delete recursion must branch LEFT when the compare says so.
     * Protects the compare in deleteRec from “always go right” style mutations.
     */
    public void testDelete_CompareChoosesLeftBranch() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 10, 0));
        assertTrue(t.insert("L", 5, 0));
        assertTrue(t.insert("RR", 20, 0));
        int before = t.size();
        KDTree.DeleteOutcome out = t.delete(5, 0);
        assertNotNull(out.entry);
        assertEquals(before - 1, t.size());
        assertNull(t.findExact(5, 0));
    }


    /**
     * Range with zero radius includes the exact center only.
     * Also re-validates distance^2 boundary arithmetic.
     */
    public void testRange_ZeroRadius_IncludesOnlyCenter() {
        KDTree t = new KDTree();
        assertTrue(t.insert("C", 7, 7));
        KDTree.SearchOutcome in = t.rangeSearch(7, 7, 0);
        KDTree.SearchOutcome out = t.rangeSearch(8, 8, 0);
        assertEquals(1, countLines(in.listing));
        assertTrue(containsListingEntry(in.listing, "C", 7, 7));
        assertEquals(0, countLines(out.listing));
    }

    // ---------- EXTRA TESTS TO TARGET WEAKLY-COVERED LINES IN KDTree.java
    // ----------


    /**
     * Verifies insert parity at depth 0 (X split) and the “ties go RIGHT” rule
     * on X.
     */
    public void testInsert_TieOnXAtRoot_GoesRight() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth 0 (split X)
        assertTrue(t.insert("TX", 0, -1)); // same X as root -> must go RIGHT

        final int[] lvlR = { -1 }, lvlTX = { -1 };
        t.preorderWithLevels((lvl, e) -> {
            if ("R".equals(e.getName()))
                lvlR[0] = lvl;
            if ("TX".equals(e.getName()))
                lvlTX[0] = lvl;
        });
        // Root at level 0; tie on X must be right child at level 1.
        assertEquals(0, lvlR[0]);
        assertEquals(1, lvlTX[0]);
    }


    /**
     * At depth 1 (Y split), ties on Y must go RIGHT; confirms depth parity and
     * tie policy.
     */
    public void testInsert_TieOnYAtDepth1_GoesRight() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth 0 (X)
        assertTrue(t.insert("RX", 5, 5)); // goes to RIGHT of root; depth 1 (Y)
        assertTrue(t.insert("TY", 9, 5)); // same Y under depth 1 -> must go
                                          // RIGHT

        final int[] lvlRX = { -1 }, lvlTY = { -1 };
        t.preorderWithLevels((lvl, e) -> {
            if ("RX".equals(e.getName()))
                lvlRX[0] = lvl;
            if ("TY".equals(e.getName()))
                lvlTY[0] = lvl;
        });
        assertEquals(1, lvlRX[0]); // RX just under root
        assertEquals(2, lvlTY[0]); // TY is RX's right child due to Y tie
    }


    /**
     * Duplicate coordinate insert must be rejected (equality at lines
     * 55/204-ish).
     */
    public void testInsert_RejectsDuplicateCoords() {
        KDTree t = new KDTree();
        assertTrue(t.insert("A", 7, 7));
        assertFalse(t.insert("B", 7, 7)); // exact same (x,y) -> reject
        assertEquals(1, t.size());
        KDTree.SearchOutcome s = t.rangeSearch(7, 7, 0);
        assertEquals("A (7, 7)\n", s.listing);
    }


    /**
     * Deleting the root with a right subtree must choose the min in current
     * split dim.
     * Uses a case where the lexicographic min sits to the RIGHT (same X,
     * smaller Y),
     * forcing findMinLex to scan both children.
     */
    public void testDelete_RootReplacedByRightLexicographicMin() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // root
        assertTrue(t.insert("A", 5, 0)); // right child
        assertTrue(t.insert("B", 5, -1)); // right-right (same X, smaller Y) ->
                                          // true lexicographic min

        KDTree.DeleteOutcome d = t.delete(0, 0);
        assertNotNull(d.entry); // removed root
        // After replacement, new root must be (5,-1) – the lexicographic min (x
        // then y).
        final City[] newRoot = { null };
        t.preorderWithLevels((lvl, e) -> {
            if (lvl == 0)
                newRoot[0] = e;
        });
        assertNotNull(newRoot[0]);
        assertEquals(5, newRoot[0].getX());
        assertEquals(-1, newRoot[0].getY());
    }


    /**
     * Delete miss should take the correct branch (cmp < 0 path) and keep size
     * unchanged.
     * Also ensures visited>0 in non-empty tree.
     */
    public void testDeleteMiss_TakesLeftBranchAndKeepsSize() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 10, 0));
        assertTrue(t.insert("L", 5, 0)); // will require cmp<0 to reach
        int before = t.size();
        KDTree.DeleteOutcome d = t.delete(4, 0); // not present, but less than
                                                 // L, forces left comparisons
        assertNull(d.entry);
        assertEquals(before, t.size());
        assertTrue("visited should be positive", d.visited > 0);
    }


    /**
     * findExact must alternate correctly: root X, depth1 Y, depth2 X…
     * Target is at depth 2 on the LEFT; miss still returns null.
     */
    public void testFindExact_AlternatingAxesLeftPath() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth0 (X)
        assertTrue(t.insert("A", -2, 5)); // depth1 (Y)
        assertTrue(t.insert("B", -3, 5)); // depth2 (X) – left of A
        assertNotNull(t.findExact(-3, 5));
        assertNull(t.findExact(-3, 6)); // nearby miss
    }


    /**
     * Zero-radius range must include exactly the center if present, nothing
     * else.
     */
    public void testRange_ZeroRadius_OnlyCenterIncluded() {
        KDTree t = new KDTree();
        assertTrue(t.insert("C", 2, 3));
        assertTrue(t.insert("D", 3, 3));
        KDTree.SearchOutcome s1 = t.rangeSearch(2, 3, 0);
        assertEquals("C (2, 3)\n", s1.listing);
        KDTree.SearchOutcome s2 = t.rangeSearch(3, 4, 0);
        assertEquals("", s2.listing);
    }


    /**
     * Rectangle/circle intersection: clamp-to-corner EXACT boundary (dx^2+dy^2
     * == r^2)
     * must be considered INSIDE. Kills equality→false mutants in
     * rectIntersectsCircle.
     */
    public void testRectIntersection_ClampCornerIncludedOnBoundary() {
        KDTree t = new KDTree();
        assertTrue(t.insert("E", 32767, 32767)); // far quadrant corner
        // Distance from (0,0) to (32767,32767) = floor(sqrt(2*32767^2)) ->
        // boundary at 46340
        KDTree.SearchOutcome inside = t.rangeSearch(0, 0, 46340);
        assertTrue(inside.listing.contains("E (32767, 32767)"));
    }


    /**
     * Complement of previous: radius just below the boundary must EXCLUDE.
     * Kills equality→true and arithmetic-swap mutants around d2 and r2.
     */
    public void testRectIntersection_ClampCornerExcludedBelowBoundary() {
        KDTree t = new KDTree();
        assertTrue(t.insert("E", 32767, 32767));
        KDTree.SearchOutcome outside = t.rangeSearch(0, 0, 46339); // one less
                                                                   // than
                                                                   // boundary
        assertEquals("", outside.listing);
    }


    /**
     * On an X-split at the root with split = 0, equality (x == 0) is assigned
     * to the
     * RIGHT rectangle. The LEFT rectangle has maxX = -1 and must be pruned for
     * a
     * zero-radius query centered on (0,0). Only the root should be visited and
     * only
     * the root should appear in the results.
     */
    public void testRange_PrunesLeftOnXSplitEquality() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth 0 (X split)
        assertTrue(t.insert("L", -5, 0)); // goes to the left subtree

        KDTree.SearchOutcome out = t.rangeSearch(0, 0, 0);

        // Left subtree should be pruned at the root, so only the root is
        // visited.
        assertEquals("visited should be only the root", 1, out.visited);
        // Only the root lies on the circle of radius 0 centered at (0,0).
        assertEquals("R (0, 0)\n", out.listing);
        // Sanity: ensure left entry is not accidentally listed.
        assertFalse(out.listing.contains("L (-5, 0)"));
    }


    /**
     * At depth 1 the split is on Y. When split Y == 0, equality belongs to the
     * UPPER
     * rectangle (y >= 0). For a zero-radius query at (0,0), the LOWER rectangle
     * (maxY = -1) must be pruned; a node at y < 0 under that branch should not
     * be
     * reached or listed.
     */
    public void testRange_PrunesLowerOnYSplitEquality() {
        KDTree t = new KDTree();
        assertTrue(t.insert("R", 0, 0)); // depth 0 (X split)
        assertTrue(t.insert("X", 1, 0)); // right of root; depth 1 (Y split at
                                         // y=0)
        assertTrue(t.insert("LOW", 1, -5)); // would be lower child by Y

        KDTree.SearchOutcome out = t.rangeSearch(0, 0, 0);

        // We should visit the root and the right child (LOWER is pruned at that
        // child).
        assertEquals("root + right child only", 2, out.visited);
        // Only the root is on the circle (radius 0 at (0,0)).
        assertEquals("R (0, 0)\n", out.listing);
        // Ensure the lower branch wasn’t explored/listed.
        assertFalse(out.listing.contains("LOW (1, -5)"));
    }

}
