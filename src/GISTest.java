import student.TestCase;

/**
 * Tests GIS Interface Class
 * 
 * @author Parth Mehta
 * @author Anurag Pokala
 * @version 10/14/2025
 */
public class GISTest extends TestCase {

    private GIS db;

    /**
     * Sets up test fixture.
     */
    public void setUp() {
        db = new GISDB();
    }

    /**
     * Tests clear operation and empty database outputs.
     */
    public void testClearAndEmptyOutputs() {
        assertTrue(db.clear());
        assertEquals("", db.print());
        assertEquals("", db.debug());
        assertEquals("", db.info("X"));
        assertEquals("", db.info(1, 1));
        assertEquals("", db.delete("X"));
        assertEquals("", db.delete(1, 1));
    }

    /**
     * Tests insert validation and duplicate coordinate rejection.
     */
    public void testInsertValidationAndDuplicates() {
        assertTrue(db.insert("A", 10, 10));
        assertFalse(db.insert("B", 10, 10)); // duplicate coord
        assertFalse(db.insert("Bad", -1, 0));
        assertFalse(db.insert("Bad", 0, -1));
        assertFalse(db.insert("Bad", GISDB.MAXCOORD + 1, 0));
        assertFalse(db.insert("Bad", 0, GISDB.MAXCOORD + 1));
    }

    /**
     * Tests print and debug formatting for BST and KDTree.
     */
    public void testPrintAndDebugFormatting() {
        db.clear();
        db.insert("Denver", 100, 200);
        db.insert("Boston", 50, 100);

        String bst = db.print();
        String kd  = db.debug();

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
     * Tests info retrieval by coordinate and by name.
     */
    public void testInfoByCoordAndByName() {
        GIS gisLocal = new GISDB();
        assertTrue(gisLocal.insert("Solo", 10, 20));
        assertEquals("Solo", gisLocal.info(10, 20));

        String byName = gisLocal.info("Solo");
        assertTrue(byName.contains("(10, 20)"));

        String[] lines = byName.split("\\R");
        int nonEmpty = 0;
        for (String s : lines) {
            if (!s.isEmpty()) {
                nonEmpty++;
            }
        }
        assertEquals(1, nonEmpty);
    }

    /**
     * Tests delete by coordinate returns visit count and name.
     */
    public void testDeleteByCoordAndVisitCountPrinted() {
        db.clear();
        db.insert("R", 100, 100);
        db.insert("A", 50,  50);
        String out = db.delete(100, 100);
        assertTrue(out.matches("\\d+\\s+R"));
        assertEquals("", db.info(100, 100));
    }

    /**
     * Tests delete by coordinate returns empty when not found.
     */
    public void testDeleteByCoordMissingOnlyCountOrEmpty() {
        GIS gisLocal = new GISDB();
        gisLocal.insert("R", 40, 40);
        gisLocal.insert("L", 10, 10);

        String out = gisLocal.delete(1, 2);
        assertEquals("", out);
    }

    /**
     * Tests delete by name removes all matching entries.
     */
    public void testDeleteByNameRemovesAllPreorderPreference() {
        GIS gisLocal = new GISDB();
        gisLocal.insert("Dup", 50, 50);
        gisLocal.insert("A",   25, 60);
        gisLocal.insert("Dup", 40, 40);
        gisLocal.insert("Dup", 60, 60);

        String out = gisLocal.delete("Dup");
        assertTrue(out.contains("(50, 50)"));
        assertTrue(out.contains("(40, 40)"));
        assertTrue(out.contains("(60, 60)"));
        assertTrue(out.endsWith("\n"));
        assertEquals("", gisLocal.info("Dup"));
    }

    /**
     * Tests search circle boundary inclusion and visit count.
     */
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

    /**
     * Tests search with bad radius and zero radius.
     */
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
     * Tests print indentation uses exactly two spaces per level.
     */
    public void testPrintIndentationExactlyTwoSpacesPerLevel() {
        db.clear();
        db.insert("M", 50, 50);
        db.insert("A", 10, 10);
        db.insert("Z", 90, 90);
        
        String output = db.print();
        String[] lines = output.split("\\n");
        
        boolean foundRoot = false;
        for (String line : lines) {
            if (line.contains("M (50, 50)")) {
                assertTrue(line.startsWith("0M"));
                foundRoot = true;
                break;
            }
        }
        assertTrue(foundRoot);
        
        boolean foundLevel1 = false;
        for (String line : lines) {
            if (line.contains("A (10, 10)")) {
                assertTrue(line.startsWith("1  A"));
                assertFalse(line.startsWith("1 A"));
                foundLevel1 = true;
                break;
            }
        }
        assertTrue(foundLevel1);
    }

    /**
     * Tests print with deep tree uses four spaces at level 2.
     */
    public void testPrintWithDeepTreeFourSpacesAtLevel2() {
        db.clear();
        db.insert("M", 50, 50);
        db.insert("D", 20, 20);
        db.insert("Z", 90, 90);
        db.insert("B", 10, 10);
        
        String output = db.print();
        String[] lines = output.split("\\n");
        
        boolean foundLevel2 = false;
        for (String line : lines) {
            if (line.contains("B (10, 10)")) {
                assertTrue(line.startsWith("2    B"));
                assertFalse(line.startsWith("2  B"));
                assertFalse(line.startsWith("2 B"));
                foundLevel2 = true;
                break;
            }
        }
        assertTrue(foundLevel2);
    }

    /**
     * Tests print does not hang on normal tree.
     */
    public void testPrintDoesNotHangOnNormalTree() {
        db.clear();
        db.insert("A", 10, 10);
        db.insert("B", 20, 20);
        db.insert("C", 30, 30);
        
        String output = db.print();
        
        assertNotNull(output);
        assertTrue(output.contains("A"));
        assertTrue(output.contains("B"));
        assertTrue(output.contains("C"));
        
        assertTrue(output.length() < 500);
    }
    
    /**
     * Tests debug indentation uses exactly two spaces per level.
     */
    public void testDebugIndentationExactlyTwoSpacesPerLevel() {
        db.clear();
        db.insert("M", 50, 50);
        db.insert("A", 25, 60);
        db.insert("Z", 75, 40);

        String output = db.debug();
        String[] lines = output.split("\\n");

        boolean foundRoot = false;
        for (String line : lines) {
            if (line.contains("M 50 50")) {
                assertTrue(line.startsWith("0M"));
                foundRoot = true;
                break;
            }
        }
        assertTrue(foundRoot);

        boolean foundLevel1 = false;
        for (String line : lines) {
            if (line.contains("A 25 60") || line.contains("Z 75 40")) {
                assertTrue(line.startsWith("1  A") 
                    || line.startsWith("1  Z"));
                foundLevel1 = true;
                break;
            }
        }
        assertTrue(foundLevel1);
    }

    /**
     * Tests debug with deep KDTree uses four spaces at level 2.
     */
    public void testDebugWithDeepKDTreeFourSpacesAtLevel2() {
        db.clear();
        db.insert("M", 50, 50);
        db.insert("D", 25, 60);
        db.insert("Z", 75, 40);
        db.insert("B", 20, 55);

        String output = db.debug();
        System.out.println("=== DEBUG OUTPUT ===");
        System.out.println(output.replace(' ', '·'));
        System.out.println("=== END ===");
        
        String[] lines = output.split("\\n");

        boolean foundLevel2 = false;
        for (String line : lines) {
            if (line.contains("B 20 55")) {
                System.out.println("Found B line: [" + line + "]");
                System.out.println("With dots: [" 
                    + line.replace(' ', '·') + "]");
                
                assertTrue("Level 2 should start with '2    B'", 
                           line.startsWith("2    B"));
                
                assertFalse("Should not have only 1 space", 
                            line.startsWith("2 B"));
                assertFalse("Should not have only 2 spaces", 
                            line.startsWith("2  B"));
                assertFalse("Should not have only 3 spaces", 
                            line.startsWith("2   B"));
                
                foundLevel2 = true;
                break;
            }
        }
        assertTrue("Level 2 node not found", foundLevel2);
    }

    /**
     * Tests debug does not hang on normal tree.
     */
    public void testDebugDoesNotHangOnNormalTree() {
        db.clear();
        db.insert("A", 10, 10);
        db.insert("B", 20, 20);
        db.insert("C", 30, 30);
        
        String output = db.debug();
        
        assertNotNull(output);
        assertTrue(output.contains("A"));
        assertTrue(output.contains("B"));
        assertTrue(output.contains("C"));
        
        assertTrue(output.length() < 500);
    }
    
    /**
     * Tests info by name returns only matching cities.
     */
    public void testInfoByNameOnlyMatchingCitiesReturned() {
        db.clear();
        db.insert("Boston", 10, 20);
        db.insert("Denver", 30, 40);
        db.insert("Austin", 50, 60);
        db.insert("Boston", 15, 25);
        
        String result = db.info("Boston");
        
        assertTrue(result.contains("(10, 20)"));
        assertTrue(result.contains("(15, 25)"));
        
        assertFalse(result.contains("Denver"));
        assertFalse(result.contains("Austin"));
        assertFalse(result.contains("(30, 40)"));
        assertFalse(result.contains("(50, 60)"));
        
        String[] lines = result.split("\\R");
        int nonEmpty = 0;
        for (String s : lines) {
            if (!s.isEmpty()) {
                nonEmpty++;
            }
        }
        assertEquals(2, nonEmpty);
    }

    /**
     * Tests info by name lists multiple entries in order.
     */
    public void testInfoByNameMultipleEntriesAllListedInOrder() {
        db.clear();
        db.insert("Dup", 100, 100);
        db.insert("Dup", 200, 200);
        db.insert("Dup", 300, 300);
        
        String result = db.info("Dup");
        
        assertTrue(result.contains("(100, 100)"));
        assertTrue(result.contains("(200, 200)"));
        assertTrue(result.contains("(300, 300)"));
        
        String[] lines = result.split("\\R");
        int nonEmpty = 0;
        for (String s : lines) {
            if (!s.isEmpty()) {
                nonEmpty++;
            }
        }
        assertEquals(3, nonEmpty);
    }

    /**
     * Tests info by name with four duplicates all present.
     */
    public void testInfoByNameFourDuplicatesAllPresent() {
        db.clear();
        db.insert("City", 10, 10);
        db.insert("City", 20, 20);
        db.insert("City", 30, 30);
        db.insert("City", 40, 40);
        
        String result = db.info("City");
        
        assertTrue(result.contains("(10, 10)"));
        assertTrue(result.contains("(20, 20)"));
        assertTrue(result.contains("(30, 30)"));
        assertTrue(result.contains("(40, 40)"));
        
        int cityCount = 0;
        for (String line : result.split("\\R")) {
            if (line.contains("City")) {
                cityCount++;
            }
        }
        assertEquals(4, cityCount);
    }

    /**
     * Tests info by name with other cities present returns only match.
     */
    public void testInfoByNameWithOtherCitiesPresentOnlyMatchReturned() {
        db.clear();
        db.insert("Alpha", 5, 5);
        db.insert("Target", 10, 10);
        db.insert("Beta", 15, 15);
        db.insert("Target", 20, 20);
        db.insert("Gamma", 25, 25);
        db.insert("Target", 30, 30);
        db.insert("Delta", 35, 35);
        
        String result = db.info("Target");
        
        assertTrue(result.contains("(10, 10)"));
        assertTrue(result.contains("(20, 20)"));
        assertTrue(result.contains("(30, 30)"));
        
        assertFalse(result.contains("Alpha"));
        assertFalse(result.contains("Beta"));
        assertFalse(result.contains("Gamma"));
        assertFalse(result.contains("Delta"));
        
        String[] lines = result.split("\\R");
        int nonEmpty = 0;
        for (String s : lines) {
            if (!s.isEmpty()) {
                nonEmpty++;
            }
        }
        assertEquals(3, nonEmpty);
    }

    /**
     * Tests insert duplicate coordinate only in KDTree not BST.
     */
    public void testInsertDuplicateCoordOnlyInKDTreeNotBST() {
        db.clear();
        
        assertTrue(db.insert("First", 50, 50));
        
        assertFalse(db.insert("Second", 50, 50));
        
        String bst = db.print();
        assertTrue(bst.contains("First"));
        assertFalse(bst.contains("Second"));
        
        String kd = db.debug();
        assertTrue(kd.contains("First"));
        assertFalse(kd.contains("Second"));
        
        assertEquals("", db.info("Second"));
        
        assertEquals("First", db.info(50, 50));
    }

    /**
     * Tests insert duplicate coordinate does not corrupt BST.
     */
    public void testInsertDuplicateCoordBSTNotCorrupted() {
        db.clear();
        
        db.insert("Alpha", 10, 10);
        db.insert("Beta", 20, 20);
        db.insert("Gamma", 30, 30);
        
        assertFalse(db.insert("DupAtBeta", 20, 20));
        
        String bst = db.print();
        assertTrue(bst.contains("Alpha"));
        assertTrue(bst.contains("Beta"));
        assertTrue(bst.contains("Gamma"));
        assertFalse(bst.contains("DupAtBeta"));
        
        String[] lines = bst.split("\\R");
        int count = 0;
        for (String line : lines) {
            if (!line.isEmpty()) {
                count++;
            }
        }
        assertEquals(3, count);
    }

    /**
     * Tests insert boundary values at zero and max coordinates.
     */
    public void testInsertBoundaryValuesZeroAndMax() {
        db.clear();
        
        assertTrue(db.insert("BottomLeft", 0, 0));
        assertTrue(db.insert("BottomRight", GISDB.MAXCOORD, 0));
        assertTrue(db.insert("TopLeft", 0, GISDB.MAXCOORD));
        assertTrue(db.insert("TopRight", GISDB.MAXCOORD, GISDB.MAXCOORD));
        
        assertEquals("BottomLeft", db.info(0, 0));
        assertEquals("BottomRight", db.info(GISDB.MAXCOORD, 0));
        assertEquals("TopLeft", db.info(0, GISDB.MAXCOORD));
        assertEquals("TopRight", db.info(GISDB.MAXCOORD, GISDB.MAXCOORD));
    }
    
    /**
     * Tests delete by coordinate on empty tree returns empty.
     */
    public void testDeleteByCoordEmptyTreeReturnsEmpty() {
        db.clear();
        
        String result = db.delete(10, 10);
        assertEquals("", result);
        
        result = db.delete(50, 50);
        assertEquals("", result);
    }

    /**
     * Tests delete by coordinate removes from both structures.
     */
    public void testDeleteByCoordRemovesFromBothStructures() {
        db.clear();
        db.insert("Target", 30, 40);
        db.insert("Other", 50, 60);
        
        String result = db.delete(30, 40);
        assertTrue(result.contains("Target"));
        
        assertEquals("", db.info(30, 40));
        
        assertEquals("", db.info("Target"));
        
        assertEquals("Other", db.info(50, 60));
        String otherByName = db.info("Other");
        assertTrue(otherByName.contains("(50, 60)"));
    }

    /**
     * Tests delete by coordinate with multiple same name deletes exact.
     */
    public void testDeleteByCoordMultipleWithSameNameOnlyDeletesExactCoord() {
        db.clear();
        db.insert("Dup", 10, 10);
        db.insert("Dup", 20, 20);
        db.insert("Dup", 30, 30);
        
        String result = db.delete(20, 20);
        assertTrue(result.contains("Dup"));
        
        assertEquals("", db.info(20, 20));
        
        assertEquals("Dup", db.info(10, 10));
        assertEquals("Dup", db.info(30, 30));
        
        String byName = db.info("Dup");
        assertTrue(byName.contains("(10, 10)"));
        assertTrue(byName.contains("(30, 30)"));
        assertFalse(byName.contains("(20, 20)"));
        
        String[] lines = byName.split("\\R");
        int count = 0;
        for (String line : lines) {
            if (line.contains("Dup")) {
                count++;
            }
        }
        assertEquals(2, count);
    }

    /**
     * Tests delete by coordinate requires exact triple match.
     */
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
        
        String townByName = db.info("Town");
        assertTrue(townByName.contains("(100, 300)"));
        
        String villageByName = db.info("Village");
        assertTrue(villageByName.contains("(150, 200)"));
    }

    /**
     * Tests delete by coordinate with same coord different names.
     */
    public void testDeleteByCoordSameCoordDifferentNamesImpossible() {
        db.clear();
        db.insert("First", 50, 50);
        assertFalse(db.insert("Second", 50, 50));
        
        assertEquals("First", db.info(50, 50));
        
        String result = db.delete(50, 50);
        assertTrue(result.contains("First"));
        
        assertEquals("", db.info(50, 50));
        assertEquals("", db.info("First"));
        
        assertEquals("", db.info("Second"));
    }

    /**
     * Tests delete by coordinate predicate must match all three fields.
     */
    public void testDeleteByCoordPredicateMustMatchAllThree() {
        db.clear();
        db.insert("Alpha", 10, 20);
        db.insert("Alpha", 10, 30);
        db.insert("Alpha", 15, 20);
        db.insert("Beta", 10, 20);
        
        String result = db.delete(10, 20);
        assertTrue(result.contains("Alpha"));
        
        assertEquals("", db.info(10, 20));
        
        assertEquals("Alpha", db.info(10, 30));
        assertEquals("Alpha", db.info(15, 20));
        
        String alphaInfo = db.info("Alpha");
        assertTrue(alphaInfo.contains("(10, 30)"));
        assertTrue(alphaInfo.contains("(15, 20)"));
        assertFalse(alphaInfo.contains("(10, 20)"));
        
        String[] lines = alphaInfo.split("\\R");
        int count = 0;
        for (String line : lines) {
            if (line.contains("Alpha")) {
                count++;
            }
        }
        assertEquals(2, count);
    }

    /**
     * Tests delete by coordinate returns visited count.
     */
    public void testDeleteByCoordReturnsVisitedCount() {
        db.clear();
        db.insert("Root", 50, 50);
        db.insert("Left", 25, 25);
        db.insert("Right", 75, 75);
        
        String result = db.delete(75, 75);
        
        String[] lines = result.split("\\n");
        assertEquals(2, lines.length);
        
        assertTrue(lines[0].matches("\\d+"));
        
        assertEquals("Right", lines[1]);
    }

    /**
     * Tests delete by coordinate not found returns empty.
     */
    public void testDeleteByCoordNotFoundReturnsEmpty() {
        db.clear();
        db.insert("City", 10, 10);
        
        String result = db.delete(99, 99);
        assertEquals("", result);
        
        assertEquals("City", db.info(10, 10));
    }    
    
    /**
     * Tests delete by name with null name returns empty.
     */
    public void testDeleteByNameNullNameReturnsEmpty() {
        db.clear();
        db.insert("City", 10, 10);
        
        String result = db.delete((String)null);
        assertEquals("", result);
        
        assertEquals("City", db.info(10, 10));
    }

    /**
     * Tests delete by name with non-existent name returns empty.
     */
    public void testDeleteByNameNonExistentNameReturnsEmpty() {
        db.clear();
        db.insert("Boston", 10, 20);
        db.insert("Denver", 30, 40);
        
        String result = db.delete("Chicago");
        assertEquals("", result);
        
        assertEquals("Boston", db.info(10, 20));
        assertEquals("Denver", db.info(30, 40));
    }

    /**
     * Tests delete by name removes single city.
     */
    public void testDeleteByNameSingleCityRemoved() {
        db.clear();
        db.insert("Solo", 50, 50);
        
        String result = db.delete("Solo");
        
        assertTrue(result.contains("Solo (50, 50)"));
        assertTrue(result.endsWith("\n"));
        
        assertEquals("", db.info(50, 50));
        assertEquals("", db.info("Solo"));
    }

    /**
     * Tests delete by name deletes only target name.
     */
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
        
        String bst = db.print();
        assertTrue(bst.contains("Keep1"));
        assertTrue(bst.contains("Keep2"));
        assertTrue(bst.contains("Keep3"));
        assertFalse(bst.contains("Target"));
    }

    /**
     * Tests delete by name removes multiple while others remain.
     */
    public void testDeleteByNameMultipleDeletedOthersRemain() {
        db.clear();
        db.insert("Alpha", 5, 5);
        db.insert("Bravo", 10, 10);
        db.insert("Bravo", 20, 20);
        db.insert("Charlie", 30, 30);
        db.insert("Bravo", 15, 15);
        db.insert("Delta", 40, 40);
        
        String result = db.delete("Bravo");
        
        assertTrue(result.contains("(10, 10)"));
        assertTrue(result.contains("(20, 20)"));
        assertTrue(result.contains("(15, 15)"));
        
        String[] lines = result.split("\\R");
        int bravoCount = 0;
        for (String line : lines) {
            if (line.contains("Bravo")) {
                bravoCount++;
            }
        }
        assertEquals(3, bravoCount);
        
        assertEquals("", db.info(10, 10));
        assertEquals("", db.info(20, 20));
        assertEquals("", db.info(15, 15));
        assertEquals("", db.info("Bravo"));
        
        assertEquals("Alpha", db.info(5, 5));
        assertEquals("Charlie", db.info(30, 30));
        assertEquals("Delta", db.info(40, 40));
    }

    /**
     * Tests delete by name uses preorder and stops at first match.
     */
    public void testDeleteByNamePreorderStopsAtFirstMatch() {
        db.clear();
        db.insert("Target", 50, 50);
        db.insert("Target", 25, 60);
        db.insert("Target", 75, 40);
        db.insert("Other", 30, 55);
        
        String result = db.delete("Target");
        
        assertTrue(result.contains("(50, 50)"));
        assertTrue(result.contains("(25, 60)"));
        assertTrue(result.contains("(75, 40)"));
        
        assertEquals("", db.info(50, 50));
        assertEquals("", db.info(25, 60));
        assertEquals("", db.info(75, 40));
        assertEquals("", db.info("Target"));
        
        assertEquals("Other", db.info(30, 55));
    }

    /**
     * Tests delete by name first match logic.
     */
    public void testDeleteByNameFirstMatchLogic() {
        db.clear();
        db.insert("Remove", 100, 100);
        db.insert("Remove", 50, 50);
        db.insert("Remove", 150, 150);
        db.insert("Keep", 75, 75);
        
        String result = db.delete("Remove");
        
        String[] lines = result.split("\\R");
        int removeCount = 0;
        for (String line : lines) {
            if (line.contains("Remove")) {
                removeCount++;
            }
        }
        assertEquals(3, removeCount);
        
        assertEquals("", db.info("Remove"));
        
        assertEquals("Keep", db.info(75, 75));
    }

    /**
     * Tests delete by name does not delete wrong names.
     */
    public void testDeleteByNameDoesNotDeleteWrongNames() {
        db.clear();
        db.insert("Alice", 10, 10);
        db.insert("Bob", 20, 20);
        db.insert("Alice", 30, 30);
        db.insert("Charlie", 40, 40);
        db.insert("Alice", 50, 50);
        
        String result = db.delete("Alice");
        
        assertTrue(result.contains("Alice"));
        assertFalse(result.contains("Bob"));
        assertFalse(result.contains("Charlie"));
        
        assertEquals("", db.info("Alice"));
        
        assertEquals("Bob", db.info(20, 20));
        assertEquals("Charlie", db.info(40, 40));
        
        String bst = db.print();
        assertFalse(bst.contains("Alice"));
        assertTrue(bst.contains("Bob"));
        assertTrue(bst.contains("Charlie"));
    }

    /**
     * Tests delete by name on empty database returns empty.
     */
    public void testDeleteByNameEmptyDatabaseReturnsEmpty() {
        db.clear();
        
        String result = db.delete("AnyName");
        assertEquals("", result);
    }

    /**
     * Tests delete by name removes all matching leaving none.
     */
    public void testDeleteByNameAllMatchingRemovedNoneLeft() {
        db.clear();
        db.insert("OnlyThis", 10, 10);
        db.insert("OnlyThis", 20, 20);
        db.insert("OnlyThis", 30, 30);
        db.insert("OnlyThis", 40, 40);
        
        String result = db.delete("OnlyThis");
        
        String[] lines = result.split("\\R");
        int count = 0;
        for (String line : lines) {
            if (line.contains("OnlyThis")) {
                count++;
            }
        }
        assertEquals(4, count);
        
        assertEquals("", db.print());
        assertEquals("", db.debug());
        assertEquals("", db.info("OnlyThis"));
    }

    /**
     * Tests delete by coordinate predicate Y coordinate must match.
     */
    public void testDeleteByCoordPredicateCoordinatesMustMatchY() {
        db.clear();
        db.insert("Town", 100, 50);
        db.insert("Town", 100, 60);
        db.insert("Town", 100, 70);
        
        String result = db.delete(100, 60);
        assertTrue(result.contains("Town"));
        
        assertEquals("", db.info(100, 60));
        
        assertEquals("Town", db.info(100, 50));
        assertEquals("Town", db.info(100, 70));
        
        String byName = db.info("Town");
        assertTrue(byName.contains("(100, 50)"));
        assertTrue(byName.contains("(100, 70)"));
        assertFalse(byName.contains("(100, 60)"));
        
        String[] lines = byName.split("\\R");
        int count = 0;
        for (String line : lines) {
            if (line.contains("Town")) {
                count++;
            }
        }
        assertEquals(2, count);
    }

    /**
     * Tests delete by coordinate all three predicates must work.
     */
    public void testDeleteByCoordAllThreePredicatesMustWork() {
        db.clear();
        db.insert("Same", 10, 20);
        db.insert("Same", 10, 25);
        db.insert("Same", 15, 20);
        db.insert("Same", 10, 30);
        db.insert("Same", 20, 20);
        
        String result = db.delete(10, 20);
        assertTrue(result.contains("Same"));
        
        assertEquals("", db.info(10, 20));
        
        assertEquals("Same", db.info(10, 25));
        assertEquals("Same", db.info(15, 20));
        assertEquals("Same", db.info(10, 30));
        assertEquals("Same", db.info(20, 20));
        
        String byName = db.info("Same");
        assertFalse(byName.contains("(10, 20)"));
        assertTrue(byName.contains("(10, 25)"));
        assertTrue(byName.contains("(15, 20)"));
        assertTrue(byName.contains("(10, 30)"));
        assertTrue(byName.contains("(20, 20)"));
        
        String[] lines = byName.split("\\R");
        int count = 0;
        for (String line : lines) {
            if (line.contains("Same")) {
                count++;
            }
        }
        assertEquals(4, count);

    }

    /**
     * Tests delete by name predicate name check is required.
     */
    public void testDeleteByNamePredicateNameCheckRequired() {
        db.clear();
        db.insert("Target", 50, 50);
        db.insert("Other", 60, 60);
        db.insert("Target", 70, 70);
        
        String result = db.delete("Target");
        
        assertTrue(result.contains("(50, 50)"));
        assertTrue(result.contains("(70, 70)"));
        assertFalse(result.contains("Other"));
        
        assertEquals("", db.info("Target"));
        assertEquals("", db.info(50, 50));
        assertEquals("", db.info(70, 70));
        
        assertEquals("Other", db.info(60, 60));
        
        String otherInfo = db.info("Other");
        assertTrue(otherInfo.contains("(60, 60)"));
        
        String bst = db.print();
        assertTrue(bst.contains("Other"));
        assertFalse(bst.contains("Target"));
    }

    /**
     * Tests delete by name predicate all three fields must match.
     */
    public void testDeleteByNamePredicateAllThreeFieldsMustMatch() {
        db.clear();
        db.insert("Dup", 100, 200);
        db.insert("Dup", 100, 300);
        db.insert("Dup", 200, 200);
        db.insert("Dup", 150, 250);
        db.insert("Safe", 100, 200);
        db.insert("Safe", 175, 275);
        
        String result = db.delete("Dup");
        
        assertTrue(result.contains("(100, 200)"));
        assertTrue(result.contains("(100, 300)"));
        assertTrue(result.contains("(200, 200)"));
        assertTrue(result.contains("(150, 250)"));
        
        String[] resultLines = result.split("\\R");
        int dupCount = 0;
        for (String line : resultLines) {
            if (line.contains("Dup")) {
                dupCount++;
            }
        }
        assertEquals(4, dupCount);
        
        assertEquals("", db.info("Dup"));
        assertEquals("", db.info(100, 200));
        assertEquals("", db.info(100, 300));
        assertEquals("", db.info(200, 200));
        assertEquals("", db.info(150, 250));
        
        assertEquals("Safe", db.info(175, 275));
        String safeByName = db.info("Safe");
        assertTrue(safeByName.contains("(175, 275)"));
        
        String bst = db.print();
        assertTrue(bst.contains("Safe"));
        assertFalse(bst.contains("Dup"));
    }

    /**
     * Tests delete by name verifies BST sync after multiple deletions.
     */
    public void testDeleteByNameVerifyBSTSyncAfterMultipleDeletions() {
        db.clear();
        db.insert("Remove", 100, 100);
        db.insert("Remove", 200, 200);
        db.insert("Remove", 300, 300);
        db.insert("Keep1", 150, 150);
        db.insert("Keep2", 250, 250);
        
        String result = db.delete("Remove");
        
        assertTrue(result.contains("(100, 100)"));
        assertTrue(result.contains("(200, 200)"));
        assertTrue(result.contains("(300, 300)"));
        
        assertEquals("", db.info(100, 100));
        assertEquals("", db.info(200, 200));
        assertEquals("", db.info(300, 300));
        
        assertEquals("", db.info("Remove"));
        
        String keep1Info = db.info("Keep1");
        assertTrue(keep1Info.contains("(150, 150)"));
        
        String keep2Info = db.info("Keep2");
        assertTrue(keep2Info.contains("(250, 250)"));
        
        String bst = db.print();
        assertTrue(bst.contains("Keep1"));
        assertTrue(bst.contains("Keep2"));
        assertFalse(bst.contains("Remove"));
        
        String[] bstLines = bst.split("\\R");
        int bstCount = 0;
        for (String line : bstLines) {
            if (!line.isEmpty()) {
                bstCount++;
            }
        }
        assertEquals(2, bstCount);
    }
}
