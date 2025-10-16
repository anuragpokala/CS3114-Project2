import student.TestCase;

/**
 * Tests GIS Interface Class against the current GISDB output format.
 *
 * Expected indent format from GISDB:
 *   "<level><NO extra gap><2*level spaces><payload>"
 * Examples:
 *   level 0: "0M ..." or "0Boston (x, y)"
 *   level 1: "1␠␠A ..." (two spaces after the '1')
 *   level 2: "2␠␠␠␠B ..." (four spaces after the '2')
 *
 * This suite avoids spec-mismatch with the reference by:
 *   - Verifying exact spacing your GISDB prints (no extra gap after the level)
 *   - Being tolerant of either "" or NullPointerException on delete(null)
 *
 * @author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */
public class GISTest extends TestCase {

    private GIS db;

    /**
     * Sets up a fresh database for each test.
     */
    public void setUp() {
        db = new GISDB();
    }

    // ------------------------------ helpers ------------------------------

    /**
     * Returns the integer level parsed from the start of the line,
     * or -1 if the line does not begin with digits.
     */
    private static int leadingLevel(String line) {
        int i = 0, lvl = 0;
        boolean any = false;
        while (i < line.length() && Character.isDigit(line.charAt(i))) {
            any = true;
            lvl = 10 * lvl + (line.charAt(i) - '0');
            i++;
        }
        return any ? lvl : -1;
    }

    /**
     * True iff the line begins with:
     * "<level><NO gap><2*level spaces><non-space>"
     */
    private static boolean hasExactIndentNoGap(String line, int level) {
        String lvlStr = Integer.toString(level);
        if (!line.startsWith(lvlStr)) return false;
        int pos = lvlStr.length();
        int need = 2 * level;
        for (int i = 0; i < need; i++) {
            if (pos + i >= line.length() || line.charAt(pos + i) != ' ') {
                return false;
            }
        }
        int next = pos + need;
        return next < line.length() && line.charAt(next) != ' ';
    }

    /** Returns the first non-empty line that starts with the given level. */
    private static String firstLineForLevel(String listing, int level) {
        String want = Integer.toString(level);
        for (String ln : listing.split("\\R")) {
            if (!ln.isEmpty() && ln.startsWith(want)) return ln;
        }
        return "";
    }

    /**
     * Parse a debug/print line into {level, name, x, y}.
     * Accepts either "0A 8 9" or "0A (8, 9)" (and tolerates one extra gap after level).
     */
    private static Object[] parseLine(String line) {
        int i = 0, lvl = 0;
        boolean any = false;
        while (i < line.length() && Character.isDigit(line.charAt(i))) {
            any = true;
            lvl = 10 * lvl + (line.charAt(i) - '0');
            i++;
        }
        if (!any) return new Object[] { -1, "", null, null };
        // optional single gap then optional indent spaces
        if (i < line.length() && line.charAt(i) == ' ') i++;
        while (i < line.length() && line.charAt(i) == ' ') i++;

        // name
        int j = i;
        while (j < line.length() && line.charAt(j) != ' ' && line.charAt(j) != '(') j++;
        String name = line.substring(i, j);

        // skip spaces
        i = j;
        while (i < line.length() && line.charAt(i) == ' ') i++;

        Integer x = null, y = null;
        if (i < line.length() && line.charAt(i) == '(') {
            int k = line.indexOf(')', i + 1);
            if (k > 0) {
                String inside = line.substring(i + 1, k);
                String[] parts = inside.split(",");
                if (parts.length == 2) {
                    try {
                        x = Integer.valueOf(parts[0].trim());
                        y = Integer.valueOf(parts[1].trim());
                    } catch (NumberFormatException ignore) { }
                }
            }
        }
        else {
            int sp = line.indexOf(' ', i);
            if (sp > 0 && sp + 1 < line.length()) {
                try {
                    x = Integer.valueOf(line.substring(i, sp).trim());
                    y = Integer.valueOf(line.substring(sp + 1).trim());
                } catch (NumberFormatException ignore) { }
            }
        }
        return new Object[] { lvl, name, x, y };
    }
    

    // ------------------------------- basics -------------------------------

    /** Clears DB; empty operations return empty strings. */
    public void testClearAndEmptyOutputs() {
        assertTrue(db.clear());
        assertEquals("", db.print());
        assertEquals("", db.debug());
        assertEquals("", db.info("X"));
        assertEquals("", db.info(1, 1));
        assertEquals("", db.delete("X"));
        assertEquals("", db.delete(1, 1));
    }

    /** Insert validation and duplicate coordinate rejection. */
    public void testInsertValidationAndDuplicates() {
        assertTrue(db.insert("A", 10, 10));
        assertFalse(db.insert("B", 10, 10));
        assertFalse(db.insert("Bad", -1, 0));
        assertFalse(db.insert("Bad", 0, -1));
        assertFalse(db.insert("Bad", GISDB.MAXCOORD + 1, 0));
        assertFalse(db.insert("Bad", 0, GISDB.MAXCOORD + 1));
    }

    // ------------------------- print/debug formatting -------------------------

    /** print(): verifies indent: level 0 has no gap, level 1 has two spaces, level 2 has four spaces. */
    public void testPrintIndentationLevels() {
        db.clear();
        db.insert("M", 50, 50);
        db.insert("A", 10, 10);
        db.insert("Z", 90, 90);
        db.insert("B", 20, 20);

        String out = db.print();
        boolean ok0 = false, ok1 = false, ok2 = false;

        for (String ln : out.split("\\R")) {
            int lvl = leadingLevel(ln);
            if (lvl == 0) ok0 |= hasExactIndentNoGap(ln, 0);
            if (lvl == 1) ok1 |= hasExactIndentNoGap(ln, 1);
            if (lvl == 2) ok2 |= hasExactIndentNoGap(ln, 2);
        }
        assertTrue(ok0);
        assertTrue(ok1);
        assertTrue(ok2);
    }

    /** debug(): verifies indent: level 0 has no gap, level 1 has two spaces, level 2 has four spaces. */
    public void testDebugIndentationLevels() {
        db.clear();
        db.insert("M", 50, 50);
        db.insert("D", 25, 60);
        db.insert("Z", 75, 40);
        db.insert("B", 20, 55);

        String out = db.debug();
        boolean ok0 = false, ok1 = false, ok2 = false;

        for (String ln : out.split("\\R")) {
            int lvl = leadingLevel(ln);
            if (lvl == 0) ok0 |= hasExactIndentNoGap(ln, 0);
            if (lvl == 1) ok1 |= hasExactIndentNoGap(ln, 1);
            if (lvl == 2) ok2 |= hasExactIndentNoGap(ln, 2);
        }
        assertTrue(ok0);
        assertTrue(ok1);
        assertTrue(ok2);
    }

    // -------------------------------- info --------------------------------

    /** info by coordinate and by name. */
    public void testInfoByCoordAndByName() {
        GIS local = new GISDB();
        assertTrue(local.insert("Solo", 10, 20));
        assertEquals("Solo", local.info(10, 20));

        String byName = local.info("Solo");
        assertTrue(byName.contains("(10, 20)"));

        int nonEmpty = 0;
        for (String s : byName.split("\\R")) if (!s.isEmpty()) nonEmpty++;
        assertEquals(1, nonEmpty);
    }

    /** info by name lists multiple entries and only matches. */
    public void testInfoByNameMultipleEntriesOnlyMatches() {
        db.clear();
        db.insert("Alpha", 5, 5);
        db.insert("Target", 10, 10);
        db.insert("Beta", 15, 15);
        db.insert("Target", 20, 20);
        db.insert("Gamma", 25, 25);
        db.insert("Target", 30, 30);

        String res = db.info("Target");
        assertTrue(res.contains("(10, 10)"));
        assertTrue(res.contains("(20, 20)"));
        assertTrue(res.contains("(30, 30)"));
        assertFalse(res.contains("Alpha"));
        assertFalse(res.contains("Beta"));
        assertFalse(res.contains("Gamma"));

        int nonEmpty = 0;
        for (String s : res.split("\\R")) if (!s.isEmpty()) nonEmpty++;
        assertEquals(3, nonEmpty);
    }

    // --------------------------- delete (x, y) ---------------------------

    /** delete(x,y) returns "visited\\nname" and removes the coord. */
    public void testDeleteByCoordAndVisitCountPrinted() {
        db.clear();
        db.insert("R", 100, 100);
        db.insert("A", 50, 50);
        String out = db.delete(100, 100);
        assertTrue(out.matches("\\d+\\s+R"));
        assertEquals("", db.info(100, 100));
    }

    /** delete(x,y) on empty/miss returns empty. */
    public void testDeleteByCoordMissingOrEmpty() {
        GIS local = new GISDB();
        local.insert("R", 40, 40);
        local.insert("L", 10, 10);
        assertEquals("", local.delete(1, 2));

        GIS empty = new GISDB();
        assertEquals("", empty.delete(50, 50));
    }

    /** delete(x,y) removes from KD and BST. */
    public void testDeleteByCoordRemovesFromBothStructures() {
        db.clear();
        db.insert("Target", 30, 40);
        db.insert("Other", 50, 60);

        String result = db.delete(30, 40);
        assertTrue(result.contains("Target"));
        assertEquals("", db.info(30, 40));
        assertEquals("", db.info("Target"));

        assertEquals("Other", db.info(50, 60));
        assertTrue(db.info("Other").contains("(50, 60)"));
    }

    /** delete(x,y) exact triple match required among same-name entries. */
    public void testDeleteByCoordExactTripleMatch() {
        db.clear();
        db.insert("City", 100, 200);
        db.insert("Town", 100, 300);
        db.insert("Village", 150, 200);

        String result = db.delete(100, 200);
        assertTrue(result.contains("City"));

        assertEquals("", db.info(100, 200));
        assertEquals("", db.info("City"));

        assertEquals("Town", db.info(100, 300));
        assertEquals("Village", db.info(150, 200));
    }

    /** delete(x,y) returns a numeric visited line then name. */
    public void testDeleteByCoordReturnsVisitedCount() {
        db.clear();
        db.insert("Root", 50, 50);
        db.insert("Left", 25, 25);
        db.insert("Right", 75, 75);

        String result = db.delete(75, 75);
        String[] lines = result.split("\\R");
        assertEquals(2, lines.length);
        assertTrue(lines[0].matches("\\d+"));
        assertEquals("Right", lines[1]);
    }

    // ----------------------------- delete(name) -----------------------------

    /** delete(null): accept either empty string or NullPointerException. */
    public void testDeleteByNameNullNameReturnsEmptyOrThrows() {
        db.clear();
        db.insert("City", 10, 10);
        try {
            String result = db.delete((String) null);
            assertEquals("", result);
            assertEquals("City", db.info(10, 10));
        } catch (NullPointerException ok) {
            assertTrue(true);
        }
    }

    /** delete(name) removes all matching, using preorder from KDTree. */
    public void testDeleteByNameRemovesAllPreorderPreference() {
        GIS local = new GISDB();
        local.insert("Dup", 50, 50);
        local.insert("A", 25, 60);
        local.insert("Dup", 40, 40);
        local.insert("Dup", 60, 60);

        String out = local.delete("Dup");
        assertTrue(out.contains("(50, 50)"));
        assertTrue(out.contains("(40, 40)"));
        assertTrue(out.contains("(60, 60)"));
        assertTrue(out.endsWith("\n"));
        assertEquals("", local.info("Dup"));
    }

    /** delete(name) removes only the target name; other names stay. */
    public void testDeleteByNameOnlyTargetNameDeleted() {
        db.clear();
        db.insert("Keep1", 10, 10);
        db.insert("Target", 20, 20);
        db.insert("Keep2", 30, 30);
        db.insert("Target", 25, 25);
        db.insert("Keep3", 40, 40);

        String result = db.delete("Target");
        assertTrue(result.contains("(20, 20)"));
        assertTrue(result.contains("(25, 25)"));

        assertEquals("", db.info(20, 20));
        assertEquals("", db.info(25, 25));
        assertEquals("", db.info("Target"));

        assertEquals("Keep1", db.info(10, 10));
        assertEquals("Keep2", db.info(30, 30));
        assertEquals("Keep3", db.info(40, 40));
    }

    /** delete(name) on empty DB returns empty. */
    public void testDeleteByNameEmptyDatabaseReturnsEmpty() {
        db.clear();
        assertEquals("", db.delete("AnyName"));
    }

    // -------------------------------- search --------------------------------

    /** search: boundary inclusion and visit count (last line numeric). */
    public void testSearchCircleBoundaryInclusionAndCount() {
        db.clear();
        db.insert("C", 3, 4);
        db.insert("D", 6, 8);

        String out = db.search(0, 0, 5);
        assertTrue(out.contains("C (3, 4)"));
        String[] lines = out.split("\\R");
        String last = lines[lines.length - 1];
        assertTrue(last.matches("\\d+"));
    }

    /** search: bad radius -> empty; zero radius includes exact center only. */
    public void testSearchBadRadiusAndZeroRadius() {
        assertEquals("", db.search(0, 0, -1));
        db.clear();
        db.insert("E", 7, 7);
        String hit = db.search(7, 7, 0);
        assertTrue(hit.contains("E (7, 7)"));
        String miss = db.search(7, 6, 0);
        assertTrue(miss.matches("\\d+"));
    }

    /**
     * Delete-by-name must remove matching cities in KDTree preorder
     * (root, left, right). This tree puts all "Dup" cities as leaves
     * so the preorder order is stable and observable.
     */
    public void testDeleteDupsPreorderOutputAndBstClean() {
        db.clear();

        db.insert("Root", 50, 50);
        db.insert("I1",   25, 60);
        db.insert("I2",   75, 40);

        db.insert("Dup",  10, 60);
        db.insert("Dup",  30, 60);
        db.insert("Dup",  70, 40);
        db.insert("Dup",  80, 40);

        String out = db.delete("Dup");
        String[] lines = out.split("\\R");

        String[] kept = new String[lines.length];
        int k = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && !lines[i].isEmpty()) {
                kept[k++] = lines[i];
            }
        }

        assertEquals(4, k);
        assertEquals("Dup (10, 60)", kept[0]);
        assertEquals("Dup (30, 60)", kept[1]);
        assertEquals("Dup (70, 40)", kept[2]);
        assertEquals("Dup (80, 40)", kept[3]);

        assertEquals("", db.info("Dup"));
        String bst = db.print();
        assertTrue(bst.contains("Root"));
        assertTrue(bst.contains("I1"));
        assertTrue(bst.contains("I2"));
        assertFalse(bst.contains("Dup"));
    }

    /**
     * Variant with a deeper left subtree to ensure preorder still
     * drives deletion order across multiple levels.
     */
    public void testDeleteDupsPreorderDeepLeftVariant() {
        db.clear();

        db.insert("Root", 50, 50);
        db.insert("I1",   25, 60);
        db.insert("I2",   75, 40);

        db.insert("Dup",  10, 55);
        db.insert("Keep", 20, 65);
        db.insert("Dup",  30, 65);

        db.insert("Dup",  70, 40);
        db.insert("Dup",  80, 40);

        String out = db.delete("Dup");
        String[] lines = out.split("\\R");

        String[] kept = new String[lines.length];
        int k = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && !lines[i].isEmpty()) {
                kept[k++] = lines[i];
            }
        }

        assertEquals(4, k);
        assertEquals("Dup (10, 55)", kept[0]);
        assertEquals("Dup (30, 65)", kept[1]);
        assertEquals("Dup (70, 40)", kept[2]);
        assertEquals("Dup (80, 40)", kept[3]);

        assertEquals("", db.info("Dup"));
        assertEquals("Keep", db.info(20, 65));
        String bst = db.print();
        assertTrue(bst.contains("Root"));
        assertTrue(bst.contains("I1"));
        assertTrue(bst.contains("I2"));
        assertTrue(bst.contains("Keep"));
        assertFalse(bst.contains("Dup"));
    }

    
    /**
     * After deleting all "Dup" by name, BST print must contain no "Dup"
     * lines, must include all non-Dup cities, and every line must have
     * correct "<level><no gap><2*level spaces>" indentation.
     * Targets the "Error in print after BST delete" ref tests.
     */
    public void testDeleteDupsPrintCleanAndIndented() {
        db.clear();

        db.insert("KeepA", 10, 10);
        db.insert("Dup",   20, 20);
        db.insert("KeepB", 30, 30);
        db.insert("Dup",   40, 40);
        db.insert("KeepC", 50, 50);
        db.insert("Dup",   60, 60);

        String del = db.delete("Dup");
        int delCount = 0;
        for (String s : del.split("\\R")) if (s.contains("Dup")) delCount++;
        assertEquals(3, delCount);

        String bst = db.print();
        assertFalse(bst.contains("Dup"));
        assertTrue(bst.contains("KeepA"));
        assertTrue(bst.contains("KeepB"));
        assertTrue(bst.contains("KeepC"));

        int lines = 0;
        for (String ln : bst.split("\\R")) if (!ln.isEmpty()) lines++;
        assertEquals(3, lines);

        boolean sawLvl0 = false;
        for (String ln : bst.split("\\R")) {
            if (ln.isEmpty()) continue;
            int lvl = leadingLevel(ln);
            assertTrue("line should start with a numeric level", lvl >= 0);
            assertTrue("bad indent at level " + lvl + ": [" + ln + "]",
                       hasExactIndentNoGap(ln, lvl));
            if (lvl == 0) sawLvl0 = true;
        }
        assertTrue(sawLvl0);
        assertEquals("", db.info("Dup"));
        assertEquals("KeepA", db.info(10, 10));
        assertEquals("KeepB", db.info(30, 30));
        assertEquals("KeepC", db.info(50, 50));
    }

    /** If all nodes are "Dup", deleting by name should empty BOTH trees. */
    public void testDeleteAllDupsTreeBecomesEmpty() {
        db.clear();

        db.insert("Dup", 50, 50);
        db.insert("Dup", 25, 60);
        db.insert("Dup", 75, 40);
        db.insert("Dup", 10, 55);
        db.insert("Dup", 30, 65);

        String out = db.delete("Dup");
        int count = 0;
        for (String s : out.split("\\R")) if (s.contains("Dup")) count++;
        assertEquals(5, count);

        assertEquals("", db.print());
        assertEquals("", db.debug());
        assertEquals("", db.info("Dup"));
    }
    
    /** Root matches + preorder deletions; verify set and BST clean afterwards. */
    public void testDeleteDupsWhenRootMatchesPreorderAcrossReshapes() {
        db.clear();

        db.insert("Dup", 50, 50);
        db.insert("I1",  25, 60);
        db.insert("I2",  75, 40);

        db.insert("Dup", 10, 70);
        db.insert("Dup", 30, 70);
        db.insert("Dup", 70, 40);
        db.insert("Dup", 80, 40);

        String out = db.delete("Dup");
        String[] lines = out.split("\\R");

        // Expect exactly these five lines (order not enforced)
        String e0 = "Dup (50, 50)";
        String e1 = "Dup (10, 70)";
        String e2 = "Dup (30, 70)";
        String e3 = "Dup (70, 40)";
        String e4 = "Dup (80, 40)";

        boolean f0 = false, f1 = false, f2 = false, f3 = false, f4 = false;
        int countDup = 0;

        for (int i = 0; i < lines.length; i++) {
            String s = lines[i];
            if (s != null && !s.isEmpty() && s.startsWith("Dup ")) {
                countDup++;
                if (s.equals(e0)) f0 = true;
                else if (s.equals(e1)) f1 = true;
                else if (s.equals(e2)) f2 = true;
                else if (s.equals(e3)) f3 = true;
                else if (s.equals(e4)) f4 = true;
            }
        }

        assertEquals("Wrong number of deleted lines", 5, countDup);
        assertTrue("Missing " + e0, f0);
        assertTrue("Missing " + e1, f1);
        assertTrue("Missing " + e2, f2);
        assertTrue("Missing " + e3, f3);
        assertTrue("Missing " + e4, f4);

        assertEquals("", db.info("Dup"));
        String bst = db.print();
        assertTrue(bst.contains("I1"));
        assertTrue(bst.contains("I2"));
        assertFalse(bst.contains("Dup"));
    }


    /** Mixed-depth duplicates across both sides; formatting + structure checks. */
    public void testDeleteDupsMixedDepthsExactLineDiscipline() {
        db.clear();

        assertTrue(db.insert("I0", 50, 50));
        assertTrue(db.insert("I1", 25, 60));
        assertTrue(db.insert("I2", 75, 40));

        assertTrue(db.insert("Dup", 15, 60));
        assertTrue(db.insert("Dup", 35, 62));
        assertTrue(db.insert("Dup", 70, 38));
        assertTrue(db.insert("Dup", 82, 41));
        assertTrue(db.insert("Dup", 51, 90));
        assertTrue(db.insert("Keep", 20, 58));

        String out = db.delete("Dup");
        String[] raw = out.split("\\R");

        // Validate each non-empty "Dup ..." line format and count them
        int dupLines = 0;
        for (int i = 0; i < raw.length; i++) {
            String s = raw[i];
            if (s != null && !s.isEmpty() && s.startsWith("Dup ")) {
                dupLines++;
                // Match "Dup (-?\d+, -?\d+)"
                // (No need for Pattern; simple checks are enough)
                boolean ok = s.matches("Dup \\(-?\\d+, -?\\d+\\)");
                assertTrue("Bad format: " + s, ok);
            }
        }
        assertEquals("Expected 5 deletions", 5, dupLines);

        assertEquals("", db.info("Dup"));
        assertEquals("Keep", db.info(20, 58));

        String bst = db.print();
        assertTrue(bst.contains("I0"));
        assertTrue(bst.contains("I1"));
        assertTrue(bst.contains("I2"));
        assertTrue(bst.contains("Keep"));
        assertFalse(bst.contains("Dup"));
    }

    
    /** Case-sensitive delete(name). */
    public void testDeleteDupsCaseSensitiveExactNameMatch() {
        db.clear();

        assertTrue(db.insert("Dup", 40, 40));
        assertTrue(db.insert("dup", 45, 45));
        assertTrue(db.insert("Dup", 50, 50));

        String out = db.delete("Dup");
        String[] lines = out.split("\\R");
        int nonEmpty = 0;
        for (String s : lines) if (!s.isEmpty()) nonEmpty++;
        assertEquals(2, nonEmpty);

        assertEquals("dup", db.info(45, 45));
        assertEquals("", db.info("Dup"));
    }
    
    /**
     * Visits should be 7 for the duplicate-X replacement scenario; root must be (8,9).
     */
    public void testDeleteVisitsSevenForDuplicateXReplacement() {
        db.clear();
        db.insert("A", 7, 7);
        db.insert("A", 8, 9);
        db.insert("A", 8, 7);
        db.insert("A", 8, 10);

        String del = db.delete(7, 7);
        String[] dlines = del.split("\\R");
        assertEquals(2, dlines.length);
        assertEquals("7", dlines[0]);
        assertEquals("A", dlines[1]);

        String root = firstLineForLevel(db.debug(), 0);
        Object[] p = parseLine(root);
        assertEquals(0, ((Integer)p[0]).intValue());
        assertEquals("A", (String)p[1]);
        assertEquals("missing root with coords (8,9); debug was:\n" + db.debug(),
                     8, ((Integer)p[2]).intValue());
        assertEquals(9, ((Integer)p[3]).intValue());
    }

 // ===================== KD-focused tests via GIS API =====================
 // Helpers scoped to this section to avoid collisions with earlier ones.

 /** Count non-empty lines in a multi-line string. */
 private static int kdCountLines(String s) {
     if (s == null || s.isEmpty()) return 0;
     int n = 0;
     for (String ln : s.split("\\R")) if (!ln.isEmpty()) n++;
     return n;
 }

 /** Match "name (x, y)" in search/info listings. */
 private static boolean kdHasParenEntry(String listing, String name, int x, int y) {
     String A = name + " (" + x + ", " + y + ")";
     String B = name + " (" + x + "," + y + ")";
     return listing.contains(A) || listing.contains(B);
 }

 /** Return the first line of debug/print that starts with the given level digit. */
 private static String kdFirstLineStartingWith(String listing, String levelDigit) {
     for (String ln : listing.split("\\R")) if (ln.startsWith(levelDigit)) return ln;
     return "";
 }

 /** Parse delete(x,y) output "visited\nname". */
 private static String[] kdSplitDelete(String out) { return out.split("\\R"); }

 // ------------------------ Basics / Insert rules ------------------------

 /** Empty → inserts, dup rejection, and clear observed through GIS. */
 public void testKdBasicsEmptyInsertRejectDupClearViaGis() {
     GIS g = new GISDB();
     assertTrue(g.clear());
     assertEquals("", g.debug());
     assertTrue(g.insert("A", 10, 20));
     assertTrue(g.insert("B", 1, 1));
     assertTrue(g.insert("C", 11, 20));
     assertFalse(g.insert("Dup", 10, 20));
     assertTrue(g.clear());
     assertEquals("", g.debug());
 }

 /**
  * Insert ties go RIGHT:
  * tie on X at depth 0 → level 1 on the right.
  */
 public void testKdInsertTiesGoRightParityViaGis() {
     db.clear();
     db.insert("R", 5, 5);
     db.insert("TieX", 5, 4); // X tie at root -> right

     String debug = db.debug();
     boolean ok = false;
     for (String ln : debug.split("\\R")) {
         if (ln.contains("TieX")) {
             ok = (leadingLevel(ln) == 1);
             break;
         }
     }
     assertTrue("TieX should appear at level 1 (right of root); debug was:\n" + debug, ok);
 }

 // ----------------------------- Find exact ------------------------------

 /** KD findExact via GIS.info (left branch hit + nearby miss). */
 public void testKdFindExactViaInfoAlternation() {
     db.clear();
     assertTrue(db.insert("Root", 10, 0));
     assertTrue(db.insert("B", 5, 0));
     assertEquals("B", db.info(5, 0));
     assertEquals("", db.info(5, 1));
 }

 // ------------------------------- Delete --------------------------------

 /** Delete counts on simple chains (visited>0) and removal observable via info. */
 public void testKdDeleteVisitedCountsChainsViaGis() {
     GIS g = new GISDB();
     g.insert("R", 0, 0);
     g.insert("A", 1, 0);
     g.insert("B", 2, 0);
     String out = g.delete(2, 0);
     String[] lines = kdSplitDelete(out);
     assertTrue(lines[0].matches("\\d+"));
     assertEquals("B", lines[1]);
     assertEquals("", g.info(2, 0));
 }

 /**
  * Delete root with two children → replace with min in split dimension from RIGHT (preorder tie).
  * Root should become RX (6,9).
  */
 public void testKdDeleteRootMinFromRightPreorderViaGis() {
     db.clear();
     db.insert("Root", 5, 5);
     db.insert("R",    8, 5);
     db.insert("RX",   6, 9);
     db.insert("RXL",  6, 8);

     String del = db.delete(5, 5);
     assertEquals("Root", del.split("\\R")[1]);

     String root = firstLineForLevel(db.debug(), 0);
     Object[] p = parseLine(root);
     boolean ok = ((Integer)p[0]).intValue() == 0
               && "RX".equals((String)p[1])
               && Integer.valueOf(6).equals((Integer)p[2])
               && Integer.valueOf(9).equals((Integer)p[3]);
     assertTrue("Expected root RX with coords (6,9); debug was:\n" + db.debug(), ok);
 }

 /**
  * Delete root that has only a LEFT subtree: promote min from that subtree and
  * rewire leftover as the remaining child. Root becomes (2,0), and L still present.
  */
 public void testKdDeleteLeftOnlyPromotesAndRewiresViaGis() {
     db.clear();
     db.insert("Root", 5, 5);
     db.insert("L",    3, 0);
     db.insert("LL",   2, 0);

     String del = db.delete(5, 5);
     assertEquals("Root", del.split("\\R")[1]);

     String dbg = db.debug();
     Object[] root = parseLine(firstLineForLevel(dbg, 0));
     assertEquals(0, ((Integer)root[0]).intValue());
     assertEquals("LL", (String)root[1]);
     assertEquals(2, ((Integer)root[2]).intValue());
     assertEquals(0, ((Integer)root[3]).intValue());

     boolean foundL = false;
     for (String ln : dbg.split("\\R")) {
         if (ln.contains(" L ")) { foundL = true; break; }
     }
     assertTrue("Expected 'L' present at level 1 after rewire; debug was:\n" + dbg, foundL);
 }

 /** KD delete miss through GIS. */
 public void testKdDeleteMissLeavesDbViaGis() {
     db.clear();
     assertTrue(db.insert("A", 1, 1));
     assertTrue(db.insert("B", 2, 2));
     assertEquals("", db.delete(9, 9));
     assertEquals("A", db.info(1, 1));
     assertEquals("B", db.info(2, 2));
 }

 /** Piazza duplicate-X case → 7 visits. */
 public void testKdDeleteVisitsSevenDuplicateXCaseViaGis() {
     db.clear();
     db.insert("A", 7, 7);
     db.insert("A", 8, 9);
     db.insert("A", 8, 7);
     db.insert("A", 8, 10);
     String out = db.delete(7, 7);
     String[] lines = kdSplitDelete(out);
     assertEquals("7", lines[0]);
     assertEquals("A", lines[1]);
 }

 // ------------------------------ Range search ---------------------------

 /** Single-node include/exclude and large-boundary arithmetic. */
 public void testKdRangeSingleNodeAndLargeBoundariesViaGis() {
     db.clear();
     db.insert("Only", 10, 10);
     String s1 = db.search(10, 10, 0);
     assertTrue(kdHasParenEntry(s1, "Only", 10, 10));
     String s2 = db.search(0, 0, 0);
     assertFalse(kdHasParenEntry(s2, "Only", 10, 10));

     db.clear();
     db.insert("Edge", 32767, 32767);
     String out1 = db.search(0, 0, 46339);
     String out2 = db.search(0, 0, 46340);
     assertFalse(kdHasParenEntry(out1, "Edge", 32767, 32767));
     assertTrue(kdHasParenEntry(out2,  "Edge", 32767, 32767));
 }

 /** Pruning boundaries + clamp checks (visits last line numeric). */
 public void testKdRangePruningBoundariesViaGis() {
     db.clear();
     db.insert("R", 0, 0);
     db.insert("L", -5, 0);
     db.insert("LR", -6, 0);
     String sLeftOnly = db.search(-1, 0, 0);
     String[] a = sLeftOnly.split("\\R");
     assertTrue(a[a.length - 1].matches("\\d+"));

     db.clear();
     db.insert("R", 0, 0);
     db.insert("RR", 5, 0);
     db.insert("RRR", 4, 0);
     String sRightOnly = db.search(0, 0, 0);
     assertTrue(sRightOnly.startsWith("R (0, 0)"));
     String[] b = sRightOnly.split("\\R");
     assertTrue(b[b.length - 1].matches("\\d+"));
 }

 /** Distance math (3–4–5) and asymmetry boundaries. */
 public void testKdRangeDistanceMathViaGis() {
     db.clear();
     db.insert("P", 3, 4);
     String s4 = db.search(0, 0, 4);
     String s5 = db.search(0, 0, 5);
     assertEquals(0, kdCountLines(s4) - 1);
     assertTrue(kdHasParenEntry(s5, "P", 3, 4));

     db.clear();
     db.insert("Q", 8, 1);
     String r6 = db.search(0, 0, 6);
     String r8 = db.search(0, 0, 8);
     String r9 = db.search(0, 0, 9);
     assertEquals(0, kdCountLines(r6) - 1);
     assertEquals(0, kdCountLines(r8) - 1);
     assertTrue(kdHasParenEntry(r9, "Q", 8, 1));
 }

 /** Zero radius includes only the exact center; pruning keeps other branches out. */
 public void testKdRangeZeroRadiusCenterOnlyViaGis() {
     db.clear();
     db.insert("C", 7, 7);
     String hit  = db.search(7, 7, 0);
     String miss = db.search(8, 8, 0);
     assertTrue(kdHasParenEntry(hit, "C", 7, 7));
     String[] m = miss.split("\\R");
     assertTrue(m[m.length - 1].matches("\\d+"));
 }

/** Ties across multiple levels: X-tie at depth 0 and Y-tie at depth 1 both go RIGHT. */
public void testRefTiesMultiLevelViaGis() {
  db.clear();
  // depth 0: split on X, tie goes right
  assertTrue(db.insert("R", 10, 10));
  assertTrue(db.insert("TX0", 10, 5));  // tie on X at root -> right
  // go left once, then depth 1 split on Y, tie goes right under that left child
  assertTrue(db.insert("L", 5, 10));
  assertTrue(db.insert("TY1", 4, 10));  // tie on Y at depth 1 -> right
  String debug = db.debug();

  // Check TX0 appears at level 1 and TY1 appears at level 2
  boolean okTX0 = false, okTY1 = false;
  for (String ln : debug.split("\\R")) {
      if (ln.contains("TX0")) okTX0 = (leadingLevel(ln) == 1);
      if (ln.contains("TY1")) okTY1 = (leadingLevel(ln) == 2);
  }
  assertTrue(okTX0);
  assertTrue(okTY1);
}

/** "Good dups": same name, different coords — info(name) lists all and only those. */
public void testRefGoodDupsInfoListsAllAndOnly() {
  db.clear();
  assertTrue(db.insert("Dup", 1, 1));
  assertTrue(db.insert("Dup", 2, 2));
  assertTrue(db.insert("Dup", 3, 3));
  assertTrue(db.insert("Keep", 9, 9));

  String res = db.info("Dup");
  assertTrue(res.contains("(1, 1)"));
  assertTrue(res.contains("(2, 2)"));
  assertTrue(res.contains("(3, 3)"));
  assertFalse(res.contains("Keep"));

  // Exactly 3 non-empty lines for the three Dups
  int n = 0; for (String s : res.split("\\R")) if (!s.isEmpty()) n++;
  assertEquals(3, n);
}

/** "Bad dups": duplicate coordinates (even if name differs) must be rejected. */
public void testRefBadDupsRejectSameCoordsDifferentNames() {
  db.clear();
  assertTrue(db.insert("A", 5, 5));
  assertFalse(db.insert("B", 5, 5)); // same coords -> reject
  // verify only A exists at that coord and only one line for name A
  assertEquals("A", db.info(5, 5));
  String listA = db.info("A");
  int n = 0; for (String s : listA.split("\\R")) if (!s.isEmpty()) n++;
  assertEquals(1, n);
}

/** Delete five by NAME impacts BST only where appropriate; no "Dup" remains, counts match. */
public void testRefDeleteFiveBSTStyle() {
  db.clear();
  // Build a small mixed tree
  db.insert("Keep1", 30, 30);
  db.insert("Dup",   20, 20);
  db.insert("Dup",   40, 40);
  db.insert("Keep2", 10, 10);
  db.insert("Dup",   35, 35);

  String out = db.delete("Dup");
  // Exactly 3 Dup lines, no blanks
  int d = 0; for (String s : out.split("\\R")) if (s.startsWith("Dup ")) d++;
  assertEquals(3, d);

  // BST print should contain only the two "Keep*" entries; no "Dup"
  String bst = db.print();
  assertTrue(bst.contains("Keep1"));
  assertTrue(bst.contains("Keep2"));
  assertFalse(bst.contains("Dup"));

  // Two non-empty lines remain in print (the two keeps)
  int lines = 0; for (String ln : bst.split("\\R")) if (!ln.isEmpty()) lines++;
  assertEquals(2, lines);
}

/** "Search five easy": a small circle should include exactly the intended subset; last line numeric. */
public void testRefSearchFiveEasy() {
  db.clear();
  // Points around origin
  db.insert("A", 0, 0);
  db.insert("B", 3, 4);  // dist 5
  db.insert("C", 4, 3);  // dist 5
  db.insert("D", 6, 0);  // dist 6
  db.insert("E", 0, 8);  // dist 8

  String out = db.search(0, 0, 5);
  // Expect A, B, C (order not enforced); last line is visits
  assertTrue(out.contains("A (0, 0)"));
  assertTrue(out.contains("B (3, 4)"));
  assertTrue(out.contains("C (4, 3)"));
  assertFalse(out.contains("D (6, 0)"));
  assertFalse(out.contains("E (0, 8)"));
  String[] lines = out.split("\\R");
  assertTrue(lines[lines.length - 1].matches("\\d+"));
}

/** KD remove stress: repeated root deletions should keep both structures consistent and non-empty lines equal count. */
public void testRefRemoveStressKDLike() {
  db.clear();
  // Seed
  int[][] pts = { {50,50},{25,60},{75,40},{10,55},{30,65},{60,70},{80,30} };
  for (int[] p : pts) assertTrue(db.insert("N", p[0], p[1]));
  // Delete three coordinates that will likely hit root or internal nodes
  assertTrue(db.delete(50, 50).contains("N"));
  assertTrue(db.delete(60, 70).contains("N"));
  assertTrue(db.delete(30, 65).contains("N"));

  // No duplicates, remaining count matches print lines
  String bst = db.print();
  int remaining = 0; for (String ln : bst.split("\\R")) if (!ln.isEmpty()) remaining++;
  // We inserted 7, removed 3 => expect 4 remain
  assertEquals(4, remaining);

  // debug should still start with a level 0 root line
  String rootLine = "";
  for (String ln : db.debug().split("\\R")) { if (leadingLevel(ln) == 0) { rootLine = ln; break; } }
  assertTrue(rootLine.length() > 0);
}

/** search on EMPTY DB should return only a visit-count line (numeric), not "" */
public void testSearchOnEmptyReturnsJustCount() {
    db.clear();
    String out = db.search(0, 0, 5);
    assertTrue("Empty search should yield a numeric visit count line",
               out.matches("\\d+"));
}

/** delete(name) when name NOT present: output should be exactly "" (no stray newlines) */
public void testDeleteByNameNoMatchesPrintsEmpty() {
    db.clear();
    db.insert("A", 1, 1);
    String out = db.delete("Missing");
    assertEquals("", out);
    // and A is still there
    assertEquals("A", db.info(1, 1));
}

/** delete(name) when SOME match: exactly N non-empty lines, no trailing blank */
public void testDeleteByNameNoTrailingBlankLines() {
    db.clear();
    db.insert("T", 1, 1);
    db.insert("T", 2, 2);
    db.insert("K", 9, 9);
    String out = db.delete("T");
    String[] raw = out.split("\\R",-1); // keep trailing empty if any
    int nonEmpty = 0;
    for (String s : raw) if (!s.isEmpty()) nonEmpty++;
    assertEquals(2, nonEmpty);      // exactly those two T lines
    assertFalse("No trailing blank line expected", out.endsWith("\n\n"));
    assertEquals("", db.info("T")); // all T removed
}

//==== New tests to improve mutation coverage for GISDB ====

//Line 39: every clause of the insert validation + boundary accepts
public void testInsertBoundsAndNullValidation() {
 db.clear();

 // Good boundaries must be accepted
 assertTrue(db.insert("B0", 0, 0));
 assertTrue(db.insert("Bmax", GISDB.MAXCOORD, GISDB.MAXCOORD));

 // Each invalid clause independently rejects
 assertFalse(db.insert(null, 10, 10));                    // name == null
 assertFalse(db.insert("negX", -1, 0));                   // x < 0
 assertFalse(db.insert("negY", 0, -1));                   // y < 0
 assertFalse(db.insert("xTooBig", GISDB.MAXCOORD + 1, 0));// x > MAX
 assertFalse(db.insert("yTooBig", 0, GISDB.MAXCOORD + 1));// y > MAX

 // Sanity: only the two valid inserts should be present
 String bst = db.print();
 assertTrue(bst.contains("B0 (0, 0)"));
 assertTrue(bst.contains("Bmax (" + GISDB.MAXCOORD + ", " + GISDB.MAXCOORD + ")"));
 assertEquals("", db.info("negX"));
 assertEquals("", db.info("negY"));
 assertEquals("", db.info("xTooBig"));
 assertEquals("", db.info("yTooBig"));
}

//Line 43: if (added) guard must prevent BST pollution on duplicate coord
public void testInsertDuplicateDoesNotPolluteBST() {
 db.clear();
 assertTrue(db.insert("A", 10, 10));
 // Duplicate coordinate rejected by KD
 assertFalse(db.insert("A_dup", 10, 10));

 // If the guard were mutated to always-insert into BST, we'd see 2 lines for x=10,y=10
 String byName = db.print();
 int count = 0;
 for (String ln : byName.split("\\R")) if (ln.contains("(10, 10)")) count++;
 assertEquals(1, count);                 // only one record at (10,10)
 assertEquals("A", db.info(10, 10));     // name stayed original
}

//Lines 59–60: exact triple match (same NAME, different COORD remains)
public void testDeleteByCoordSameNameOnlyThatCoordRemoved() {
 db.clear();
 assertTrue(db.insert("N", 1, 1));
 assertTrue(db.insert("N", 2, 2));

 // Delete only (1,1)
 String out = db.delete(1, 1);
 assertTrue(out.endsWith("\nN"));            // deleted name
 assertEquals("", db.info(1, 1));            // gone
 assertEquals("N", db.info(2, 2));           // other same-name entry remains

 // BST should still have exactly one "N" line now
 int lines = 0;
 for (String ln : db.info("N").split("\\R")) if (!ln.isEmpty()) lines++;
 assertEquals(1, lines);
}

//Line 72: delete(name) must return "" for null and NOT throw
public void testDeleteByNameNullReturnsEmptyNoException() {
 db.clear();
 assertTrue(db.insert("City", 10, 10));
 // No try/catch on purpose: mutated false would NPE here and fail this test
 assertEquals("", db.delete((String) null));
 // Database unchanged
 assertEquals("City", db.info(10, 10));
}

//Line 83: matches.isEmpty() branch when DB is non-empty but name absent
public void testDeleteByNameNoMatchesReturnsEmptyAndNoChange() {
 db.clear();
 assertTrue(db.insert("Keep1", 1, 1));
 assertTrue(db.insert("Keep2", 2, 2));

 String res = db.delete("MissingName");
 assertEquals("", res);                   // no output because no matches

 // Nothing changed
 assertEquals("Keep1", db.info(1, 1));
 assertEquals("Keep2", db.info(2, 2));
 String bst = db.print();
 assertTrue(bst.contains("Keep1"));
 assertTrue(bst.contains("Keep2"));
}

//----- Kill GISDB line 90: ordering by x, then y when x ties -----
public void testDeleteNameOrdersByXThenY() {
    db.clear();
    // Different x and tie on x=5 with different y
    assertTrue(db.insert("Dup", 4, 100));
    assertTrue(db.insert("Dup", 5, 9));
    assertTrue(db.insert("Dup", 5, 7));
    // Also a keeper to ensure only the Dups are listed/removed
    assertTrue(db.insert("Keep", 1, 1));

    String out = db.delete("Dup");
    String[] lines = out.split("\\R");

    // Collect exactly the non-empty "Dup ..." lines in order into a fixed array
    String[] kept = new String[3];
    int n = 0;
    for (int i = 0; i < lines.length; i++) {
        String s = lines[i];
        if (s != null && !s.isEmpty() && s.startsWith("Dup ")) {
            if (n < kept.length) {
                kept[n++] = s;
            }
        }
    }

    // Must be sorted lexicographically by (x, then y)
    assertEquals(3, n);
    assertEquals("Dup (4, 100)", kept[0]);
    assertEquals("Dup (5, 7)",   kept[1]);  // y=7 before y=9 when x ties
    assertEquals("Dup (5, 9)",   kept[2]);

    // Non-target remains
    assertEquals("Keep", db.info(1, 1));
}


//----- Kill GISDB line 100: out.entry != null must be respected -----
public void testDeleteNameProducesOneLineAndActuallyDeletes() {
 db.clear();
 assertTrue(db.insert("Only", 10, 10));
 String out = db.delete("Only");
 assertEquals("Only (10, 10)\n", out); // if the guard were mutated false, this would be ""
 assertEquals("", db.info("Only"));
 assertEquals("", db.info(10, 10));
}

//----- Kill GISDB lines 105–107: exact triple match for BST removal -----
public void testDeleteNameRemovesExactTriplesOnly() {
    db.clear();

    // Two N entries we plan to delete
    assertTrue(db.insert("N", 1, 1));
    assertTrue(db.insert("N", 2, 2));

    // Different name, DIFFERENT coords (no KD dup)
    assertTrue(db.insert("M", 1, 2));
    assertTrue(db.insert("M", 3, 3));

    String out = db.delete("N");

    // Exactly the two N lines should be listed
    int nCount = 0;
    for (String s : out.split("\\R")) {
        if (s.startsWith("N ")) nCount++;
    }
    assertEquals(2, nCount);

    // M entries must still be present; N is gone
    assertEquals("M", db.info(1, 2));
    assertEquals("M", db.info(3, 3));
    assertEquals("", db.info("N"));
}


//----- Kill GISDB line 129: info(null) returns "" (no exception) -----
public void testInfoNullNameReturnsEmptyNoException() {
 db.clear();
 assertTrue(db.insert("City", 3, 3));
 // Should not throw; must return empty
 assertEquals("", db.info((String) null));
 // DB unchanged
 assertEquals("City", db.info(3, 3));
}

//----- Kill GISDB line 134: info(missing) returns "" on non-empty DB -----
public void testInfoMissingNameReturnsEmptyOnNonEmptyDb() {
 db.clear();
 assertTrue(db.insert("A", 1, 1));
 assertTrue(db.insert("B", 2, 2));
 assertEquals("", db.info("NoSuch")); // if mutated to always non-empty, this fails
}

/** Deleting (x,y) removes only that exact (name,x,y) triple from BST. */
/** Deleting (x,y) removes only that exact (name,x,y) triple from BST. */
public void testDeleteByCoordRemovesOnlyExactTripleDespiteNearMisses() {
    db.clear();

    // Target and near-misses:
    assertTrue(db.insert("T", 1, 1));  // exact target
    assertTrue(db.insert("T", 1, 2));  // same name, different Y
    assertTrue(db.insert("T", 2, 1));  // same name, different X
    assertTrue(db.insert("U", 9, 9));  // different name at different coords (can't duplicate coords)

    // Delete the exact coordinate (1,1); should remove only T(1,1)
    String out = db.delete(1, 1);
    String[] lines = out.split("\\R");
    assertEquals("T", lines[1]); // deleted name reported

    // (1,1) is now empty; the other T entries remain listed by info("T")
    assertEquals("", db.info(1, 1));

    String tlist = db.info("T");
    assertTrue(tlist.contains("T (1, 2)"));
    assertTrue(tlist.contains("T (2, 1)"));
    assertFalse(tlist.contains("T (1, 1)"));

    // BST still has U(9,9)
    String bst = db.print();
    assertTrue(bst.contains("U (9, 9)"));
}

/**
 * A delete miss must NOT affect the KD size. If size were decremented on a miss
 * (mutant at KDTree:259), a follow-up delete would be blocked by
 * GISDB's byCoord.isEmpty() guard and incorrectly return "".
 */
public void testKdDeleteMissDoesNotFalselyEmptyTree() {
    db.clear();

    // One real city
    assertTrue(db.insert("A", 1, 1));

    // Miss: nothing removed; should NOT decrement KD size.
    assertEquals("", db.delete(9, 9));

    // Follow-up delete of the existing coord must still work.
    String out = db.delete(1, 1);
    String[] lines = out.split("\\R");
    assertEquals(2, lines.length);
    assertTrue("first line should be the visit count", lines[0].matches("\\d+"));
    assertEquals("A", lines[1]);

    // Tree is empty now; both views are empty.
    assertEquals("", db.print());
    assertEquals("", db.debug());
}

/** At depth 1 (Y split), delete must replace with Y-min from the RIGHT subtree. */
public void testKdDeleteAtDepthOneUsesYMinFromRightSubtree() {
    db.clear();

    // Build a shape using ONLY non-negative coords (GISDB constraint).
    // depth 0 (root, X split)
    assertTrue(db.insert("R", 10, 10));

    // depth 1 (LEFT of R): Y split at y=5
    assertTrue(db.insert("T", 5, 5));

    // RIGHT subtree of T (y >= 5)
    assertTrue(db.insert("A", 6, 7));  // right child of T (greater Y)

    // Put a smaller Y (but still >= 5) in T's RIGHT subtree under A.
    // depth 2 under A is an X split (A.x = 6). x=4 goes to A's left.
    assertTrue(db.insert("B", 4, 5));  // candidate Y-min in T's RIGHT subtree

    // Delete the depth-1 node T(5,5). Replacement should be B(4,5)
    String out = db.delete(5, 5);
    String[] lines = out.split("\\R");
    assertEquals("T", lines[1]);    // deleted name reported

    // Inspect debug: look for a *level 1* line for node B with coords (4,5)
    String debug = db.debug();
    boolean sawReplacement = false;
    for (String ln : debug.split("\\R")) {
        if (ln.isEmpty()) continue;
        if (leadingLevel(ln) == 1 && ln.contains("B")) {
            // Accept either raw or paren formatting
            if (ln.contains(" 4 5") || ln.contains("(4, 5)")) {
                sawReplacement = true;
                break;
            }
        }
    }

    assertTrue("Expected depth-1 replacement with coords (4,5). Debug was:\n" + debug,
               sawReplacement);
}



















}
