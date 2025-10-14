import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class BSTTest extends TestCase {

    /**
     * Verifies a single insert and inorder placement.
     */
    public void testBSTSingleInsert() {
        BST tree = new BST();
        tree.insert(new City("Denver", 100, 200));
        String output = tree.toString();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("100"));
        assertTrue(output.contains("200"));

        BST treeLeft = new BST();
        treeLeft.insert(new City("Denver", 100, 200));
        treeLeft.insert(new City("Boston", 200, 350));
        String output1 = treeLeft.toString();
        assertTrue(output1.contains("Denver"));
        assertTrue(output1.contains("Boston"));
        int bostonIndex = output1.indexOf("Boston");
        int denverIndex = output1.indexOf("Denver");
        assertTrue(bostonIndex < denverIndex);

        BST treeRight = new BST();
        treeRight.insert(new City("Denver", 100, 200));
        treeRight.insert(new City("NYC", 200, 350));
        String output11 = treeRight.toString();
        assertTrue(output11.contains("Denver"));
        assertTrue(output11.contains("NYC"));
        int denver = output11.indexOf("Denver");
        int nYCIndex = output11.indexOf("NYC");
        assertTrue(denver < nYCIndex);
    }

    /**
     * Confirms ordering across multiple inserts in two scenarios.
     */
    public void testBSTMultipleInserts() {
        BST tree2 = new BST();
        tree2.insert(new City("Denver", 100, 200));
        tree2.insert(new City("NYC", 200, 350));
        tree2.insert(new City("Seattle", 200, 350));
        String multiOutput = tree2.toString();
        assertTrue(multiOutput.contains("Denver"));
        assertTrue(multiOutput.contains("NYC"));
        assertTrue(multiOutput.contains("Seattle"));
        int denver = multiOutput.indexOf("Denver");
        int nYCIndex = multiOutput.indexOf("NYC");
        int seattleIndex = multiOutput.indexOf("Seattle");
        assertTrue(denver < nYCIndex);
        assertTrue(denver < seattleIndex);
        assertTrue(nYCIndex < seattleIndex);

        BST treeLess = new BST();
        treeLess.insert(new City("Denver", 100, 200));
        treeLess.insert(new City("Boston", 200, 350));
        treeLess.insert(new City("Atlanta", 200, 350));
        String lessOutput = treeLess.toString();
        assertTrue(lessOutput.contains("Denver"));
        assertTrue(lessOutput.contains("Boston"));
        assertTrue(lessOutput.contains("Atlanta"));
        int denver2 = lessOutput.indexOf("Denver");
        int bostonIndex = lessOutput.indexOf("Boston");
        int atlantaIndex = lessOutput.indexOf("Atlanta");
        assertTrue(denver2 > bostonIndex);
        assertTrue(bostonIndex > atlantaIndex);
        assertTrue(denver2 > atlantaIndex);
    }

    /**
     * Ensures size reflects count of inserted nodes.
     */
    public void testSizeIncrement() {
        BST tree = new BST();
        assertEquals(0, tree.getSize());
        tree.insert(new City("Denver", 100, 200));
        assertEquals(1, tree.getSize());
        tree.insert(new City("Boston", 50, 100));
        assertEquals(2, tree.getSize());
        tree.insert(new City("NYC", 150, 250));
        assertEquals(3, tree.getSize());
    }

    /**
     * Validates correct indentation and depth labeling in print.
     */
    public void testPrintDepthSpacing() {
        BST tree = new BST();
        tree.insert(new City("Denver", 100, 200));
        tree.insert(new City("Boston", 50, 100));
        tree.insert(new City("Atlanta", 25, 75));
        String output = tree.toString();
        assertTrue(output.contains("0Denver"));
        assertTrue(output.contains("1  Boston"));
        assertTrue(output.contains("2    Atlanta"));
    }

    /**
     * Exercises equal-key path: ties go left and appear first in inorder.
     */
    public void testBSTEqualNameGoesLeftInorderOrder() {
        BST tree = new BST();
        tree.insert(new City("Alpha", 100, 100));
        tree.insert(new City("Alpha", 200, 200));
        String out = tree.toString();
        assertTrue(out.contains("Alpha (100, 100)"));
        assertTrue(out.contains("Alpha (200, 200)"));
        assertTrue(out.indexOf("Alpha (200, 200)") < out.indexOf("Alpha (100, 100)"));
        assertTrue(out.startsWith("0") || out.contains("\n0"));
        assertFalse(out.startsWith("0 ") || out.contains("\n0 "));
    }

    /**
     * Verifies right-heavy chain spacing and inorder order.
     */
    public void testRightChainDepthSpacing() {
        BST tree = new BST();
        tree.insert(new City("M", 0, 0));
        tree.insert(new City("Z", 0, 0));
        tree.insert(new City("ZZ", 0, 0));
        String out = tree.toString();
        assertTrue(out.contains("0M"));
        assertTrue(out.contains("1  Z"));
        assertTrue(out.contains("2    ZZ"));
        assertTrue(out.indexOf("0M") < out.indexOf("1  Z"));
        assertTrue(out.indexOf("1  Z") < out.indexOf("2    ZZ"));
    }

    /**
     * Ensures listing by name returns empty string if none match.
     */
    public void testListCoordsByNameNone() {
        BST t = new BST();
        t.insert(new City("Alpha", 1, 1));
        String out = t.listCoordsByName("Missing");
        assertEquals("", out);
    }

    /**
     * Checks single match formatting and absence of trailing newline.
     */
    public void testListCoordsByNameSingleAndNoTrailingNewline() {
        BST t = new BST();
        t.insert(new City("Same", 10, 20));
        String out = t.listCoordsByName("Same");
        assertEquals("(10, 20)", out);
        assertFalse(out.endsWith("\n"));
    }

    /**
     * Confirms multiple matches include all and end without trailing newline.
     */
    public void testListCoordsByNameMultipleLinesAndTrim() {
        BST t = new BST();
        t.insert(new City("X", 5, 5));
        t.insert(new City("X", 4, 4));
        t.insert(new City("X", 3, 3));
        t.insert(new City("Y", 9, 9));
        String out = t.listCoordsByName("X");
        assertTrue(out.contains("(5, 5)"));
        assertTrue(out.contains("(4, 4)"));
        assertTrue(out.contains("(3, 3)"));
        assertEquals(2, out.length() - out.replace("\n", "").length());
        assertFalse(out.endsWith("\n"));
    }
    
    /**
     * Printing an empty BST yields the empty string.
     */
    public void testBSTPrintEmpty() {
        BST t = new BST();
        assertEquals("", t.toString());
    }

    /**
     * listCoordsByName must report entries from both sides in inorder.
     */
    public void testListCoordsByNameOrderAcrossSubtrees() {
        BST t = new BST();
        t.insert(new City("M", 0, 0));
        t.insert(new City("Z", 1, 1));
        t.insert(new City("A", 2, 2));
        t.insert(new City("Z", 3, 3));
        String out = t.listCoordsByName("Z");
        assertTrue(out.contains("(3, 3)"));
        assertTrue(out.contains("(1, 1)"));
        assertTrue(out.indexOf("(3, 3)") < out.indexOf("(1, 1)"));
    }

    /**
     * BST toString ends with a newline when non-empty, verifying spacing loop.
     */
    public void testBSTToStringEndsWithNewline() {
        BST t = new BST();
        t.insert(new City("Solo", 10, 10));
        String out = t.toString();
        assertTrue(out.endsWith("\n"));
    }
    
    public void testRemoveLeaf() {
        BST t = new BST();
        t.insert(new City("M", 5, 5));
        t.insert(new City("A", 1, 1));
        assertTrue(t.removeCity(new City("A", 1, 1)));
        assertFalse(t.toString().contains("A (1, 1)"));
    }

    public void testRemoveOneChild() {
        BST t = new BST();
        t.insert(new City("M", 5, 5));
        t.insert(new City("A", 1, 1));
        t.insert(new City("B", 2, 2));
        assertTrue(t.removeCity(new City("A", 1, 1)));
        String s = t.toString();
        assertFalse(s.contains("A (1, 1)"));
        assertTrue(s.contains("B (2, 2)"));
    }

    public void testRemoveTwoChildrenUsesMaxFromLeft() {
        BST t = new BST();
        t.insert(new City("M", 5, 5));
        t.insert(new City("C", 3, 3));
        t.insert(new City("Z", 26, 26));
        t.insert(new City("E", 4, 4));
        t.insert(new City("A", 1, 1));
        assertTrue(t.removeCity(new City("M", 5, 5)));
        String s = t.toString();
        assertFalse(s.contains("M (5, 5)"));
        assertTrue(s.indexOf("E (4, 4)") != -1);
    }

    public void testFindFirstByNamePreorder() {
        BST t = new BST();
        t.insert(new City("Same", 3, 3));
        t.insert(new City("Same", 1, 1));
        t.insert(new City("Same", 2, 2));
        City c = t.findFirstByNamePreorder("Same");
        assertNotNull(c);
        assertEquals(3, c.getX());
        assertEquals(3, c.getY());
    }

    public void testRemoveCityNoMatch() {
        BST t = new BST();
        t.insert(new City("A", 1, 1));
        assertFalse(t.removeCity(new City("B", 2, 2)));
        assertTrue(t.toString().contains("A (1, 1)"));
    }
    
    public void testDeleteUsesMaxFromLeft() {
        BST b = new BST();
        City r = new City("M", 5, 5);
        City l = new City("D", 1, 1);
        City lr = new City("K", 2, 2);
        City rr = new City("Z", 9, 9);
        b.insert(r);
        b.insert(l);
        b.insert(lr);
        b.insert(rr);
        assertTrue(b.toString().contains("M"));
        b.removeCity(r);
        String out = b.toString();
        assertFalse(out.contains("M"));
        assertTrue(out.contains("K"));
    }

    public void testInsertEqualNameGoesLeftAndInorder() {
        BST b = new BST();
        City a1 = new City("Same", 1, 1);
        City a2 = new City("Same", 2, 2);
        City t  = new City("T", 3, 3);
        b.insert(a1);
        b.insert(a2);
        b.insert(t);
        String s = b.toString();
        int iLeft  = s.indexOf("(2, 2)");
        int iRoot  = s.indexOf("(1, 1)");
        assertTrue(iLeft < iRoot);
    }

    public void testRemoveCityOnlyRemovesExactCity() {
        BST b = new BST();
        City a = new City("N", 1, 1);
        City bCity = new City("N", 2, 2);
        b.insert(a);
        b.insert(bCity);
        b.removeCity(a);
        String s = b.toString();
        assertFalse(s.contains("(1, 1)"));
        assertTrue(s.contains("(2, 2)"));
    }
    
    /**
     * When names tie but cities differ, deletion must traverse left (cmp==0 && !equals).
     */
    public void testRemoveCitySameNameDifferentCoordsGoesLeft() {
        BST b = new BST();
        City root = new City("M", 5, 5);
        City leftSameName = new City("M", 4, 4);
        City rightOther = new City("Z", 9, 9);
        b.insert(root);
        b.insert(leftSameName);
        b.insert(rightOther);

        assertTrue(b.removeCity(new City("M", 4, 4)));
        String s = b.toString();
        assertTrue(s.contains("M (5, 5)"));
        assertFalse(s.contains("M (4, 4)"));
        assertTrue(s.contains("Z (9, 9)"));
    }

    /**
     * Deleting a node on the right branch exercises the cmp>0 path in removeCityRec.
     */
    public void testRemoveCityRightBranch() {
        BST b = new BST();
        City r = new City("M", 0, 0);
        City z = new City("Z", 1, 1);
        b.insert(r);
        b.insert(z);
        assertTrue(b.removeCity(z));
        assertTrue(b.toString().contains("M (0, 0)"));
        assertFalse(b.toString().contains("Z (1, 1)"));
    }

    /**
     * Replace with predecessor that is several steps to the right inside left subtree.
     */
    public void testRemoveCityPredecessorDeepRightChain() {
        BST b = new BST();
        City r  = new City("M", 0, 0);
        City l  = new City("C", 0, 0);
        City lr = new City("K", 0, 0);
        City lrr = new City("L", 0, 0);
        b.insert(r);
        b.insert(l);
        b.insert(lr);
        b.insert(lrr);

        assertTrue(b.removeCity(r));
        String s = b.toString();
        assertTrue(s.contains("L (0, 0)"));
        assertFalse(s.contains("M (0, 0)"));
    }

    /**
     * collectByName fills the array in inorder and returns the exact count.
     * Also verifies non-matching names are not included.
     */
    public void testCollectByNameInorderAndCount() {
        BST b = new BST();
        // Shape: names cause left/right placements by lexicographic order.
        b.insert(new City("M", 0, 0));   // root
        b.insert(new City("A", 1, 1));   // left of M
        b.insert(new City("Z", 2, 2));   // right of M
        b.insert(new City("M", 3, 3));   // equal to root name -> goes LEFT of "M"
        b.insert(new City("M", 4, 4));   // equal again -> continues LEFT chain

        City[] out = new City[5];
        int n = b.collectByName("M", out);
        assertEquals(3, n);

        // Inorder with equal-name-at-left means the deepest equal goes first.
        // The three "M" entries should be exactly those x,y pairs (4,4), (3,3) and (0,0) in that inorder.
        assertEquals(4, out[0].getX());
        assertEquals(4, out[0].getY());
        assertEquals(3, out[1].getX());
        assertEquals(3, out[1].getY());
        assertEquals(0, out[2].getX());
        assertEquals(0, out[2].getY());

        // Ensure "A" / "Z" did not sneak in
        for (int i = 0; i < n; i++) {
            assertEquals("M", out[i].getName());
        }
    }


    /**
     * findFirstByNamePreorder returns null when no match exists.
     */
    public void testFindFirstByNamePreorderNoMatch() {
        BST b = new BST();
        b.insert(new City("A", 1, 1));
        assertNull(b.findFirstByNamePreorder("Z"));
    }
    
    /**
     * Deleting a node that has ONLY a right child (and same-name comparison at ancestor)
     * exercises the cmp==0 path and the "only right child" replacement in removeCityRec.
     */
    public void testRemoveCityOnlyRightChildSameNamePath() {
        BST b = new BST();
        // Root and a left subtree that shares the same name for cmp==0 logic at the ancestor
        City root = new City("M", 0, 0);
        City leftSame = new City("M", -1, -1); // equal name goes left of root
        City target = new City("P", 1, 1);     // right child of root; will have only a right child
        City targetsRight = new City("Q", 2, 2);

        b.insert(root);
        b.insert(leftSame);
        b.insert(target);
        b.insert(targetsRight); // so "target" has only a right child

        assertTrue(b.removeCity(target));
        String s = b.toString();
        // target gone; its right child remains
        assertFalse(s.contains("P (1, 1)"));
        assertTrue(s.contains("Q (2, 2)"));
        // root and leftSame still present
        assertTrue(s.contains("M (0, 0)"));
        assertTrue(s.contains("M (-1, -1)"));
    }

    /**
     * listCoordsByName must use EXACT string equality (no substring/partial matches)
     * and should traverse both sides without false positives.
     */
    public void testListCoordsByNameExactMatchNoSubstring() {
        BST b = new BST();
        b.insert(new City("Ann", 1, 1));
        b.insert(new City("Anna", 2, 2));
        b.insert(new City("Anne", 3, 3));
        String out = b.listCoordsByName("Ann");
        // Only the exact "Ann" coordinate should appear
        assertEquals("(1, 1)", out);
    }
    
    

    /**
     * When cmp==0 but the City is not equal to the target (different coords),
     * removeCity must continue searching LEFT and must NOT remove the wrong node.
     */
    public void testRemoveCitySameNameDifferentCoordsSearchesLeftOnlyIfNeeded() {
        BST b = new BST();
        City mRoot = new City("M", 0, 0);
        City mLeft = new City("M", -1, -1);
        City mLeftDeep = new City("M", -2, -2);
        City z = new City("Z", 9, 9);

        b.insert(mRoot);
        b.insert(mLeft);
        b.insert(mLeftDeep);
        b.insert(z);

        // Try to remove an M that does not exist (same name, new coords)
        City missing = new City("M", 123, 456);
        assertFalse(b.removeCity(missing));

        // Tree unchanged for the M nodes, and Z remains.
        String s = b.toString();
        assertTrue(s.contains("M (0, 0)"));
        assertTrue(s.contains("M (-1, -1)"));
        assertTrue(s.contains("M (-2, -2)"));
        assertTrue(s.contains("Z (9, 9)"));
    }

    /**
     * Mirror of the previous: ensure we don't accidentally remove on the RIGHT
     * path when cmp>0 (name greater than node's) and there is no exact match.
     */
    public void testRemoveCityRightPathNoAccidentalDeletion() {
        BST b = new BST();
        City a = new City("A", 1, 1);
        City m = new City("M", 5, 5);
        City z = new City("Z", 9, 9);
        b.insert(m);  // root
        b.insert(a);  // left
        b.insert(z);  // right

        // Target has a greater name than root, but does not exist; must not delete anything.
        assertFalse(b.removeCity(new City("Z", 100, 100))); // wrong coords for Z
        String s = b.toString();
        assertTrue(s.contains("A (1, 1)"));
        assertTrue(s.contains("M (5, 5)"));
        assertTrue(s.contains("Z (9, 9)"));
    }
    
    /**
     * Depth indentation must be exactly 2*depth spaces before names.
     * Kills arithmetic mutants that swap depth with constant/second member.
     */
    public void testIndentSpacingExactAcrossDepths() {
        BST t = new BST();
        t.insert(new City("D0", 0, 0));     // depth 0
        t.insert(new City("D1", -1, -1));   // depth 1 (goes left)
        t.insert(new City("D2", -2, -2));   // depth 2
        t.insert(new City("D3", -3, -3));   // depth 3

        String s = t.toString();
        // depth label immediately followed by name (no extra space):
        assertTrue(s.contains("0D0"));
        // exactly 2 spaces for depth 1, 4 for depth 2, 6 for depth 3
        assertTrue(s.contains("1  D1"));
        assertTrue(s.contains("2    D2"));
        assertTrue(s.contains("3      D3"));
    }

    /**
     * Collect by name must return inorder across both subtrees and exact count.
     * Strengthens equality branches during name collection.
     */
    public void testCollectByNameInorderAndExactCount() {
        BST t = new BST();
        // ensure same name appears left and right of root by key ordering
        t.insert(new City("M", 0, 0));        // root
        t.insert(new City("A", 1, 1));        // left branch
        t.insert(new City("Z", 2, 2));        // right branch
        t.insert(new City("Z", 3, 3));        // equal-name goes LEFT under Z

        String out = t.listCoordsByName("Z");
        // Expect exactly two coordinates, left one first (from Z's left)
        assertTrue(out.startsWith("(3, 3)"));
        assertTrue(out.endsWith("(2, 2)"));
        assertEquals(1, out.length() - out.replace("\n", "").length());
    }

    /**
     * Removing a city must require an exact (name,x,y) match; a same-name/same-x
     * different-y should not be removed. Tightens equality checks in remove path.
     */
    public void testRemoveCityRequiresExactTriple() {
        BST t = new BST();
        t.insert(new City("N", 10, 10));
        t.insert(new City("N", 10, 11));   // similar but different y
        assertFalse(t.removeCity(new City("N", 10, 12))); // no exact triple
        String s = t.toString();
        assertTrue(s.contains("N (10, 10)"));
        assertTrue(s.contains("N (10, 11)"));
    }




}
