import student.TestCase;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for the name-ordered BST that stores City records.
 * Equal keys go LEFT; delete uses max-from-left; removeMatching removes an exact triple.
 */
public class BSTTest extends TestCase {

    // Helper: render BST inorder as "level{2*level spaces}name (x, y)\n"
    private static String inorderToString(BST<City> bst) {
        StringBuilder sb = new StringBuilder();
        bst.inorderWithLevels((lvl, c) -> {
            sb.append(lvl);
            for (int i = 0; i < 2 * lvl; i++) sb.append(" ");
            sb.append(c.getName()).append(" (").append(c.getX()).append(", ").append(c.getY()).append(")\n");
        });
        return sb.toString();
    }

    public void testEmptyBasics() {
        BST<City> t = new BST<>();
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
        assertFalse(t.contains(new City("X", 0, 0)));
        assertEquals("", inorderToString(t));
    }

    public void testInsertEqualsGoLeft_InorderOrder() {
        BST<City> t = new BST<>();
        City a1 = new City("Alpha", 1, 1);
        City a2 = new City("Alpha", 2, 2); // equal name → goes LEFT
        City z  = new City("Z", 9, 9);

        t.insert(a1);
        t.insert(a2);
        t.insert(z);

        String s = inorderToString(t);
        // equal-keys-left means deeper equal appears first in inorder
        int iA2 = s.indexOf("Alpha (2, 2)");
        int iA1 = s.indexOf("Alpha (1, 1)");
        assertTrue(iA2 < iA1);
        assertTrue(s.contains("0"));            // root level has no extra space
        assertFalse(s.contains("0 "));          // no space between '0' and name
        assertEquals(3, t.size());
    }

    public void testContainsAndClear() {
        BST<City> t = new BST<>();
        City a = new City("A", 1, 1);
        City b = new City("B", 2, 2);
        t.insert(a); t.insert(b);
        assertTrue(t.contains(new City("A", 999, 999))); // name compare only
        assertTrue(t.contains(new City("B", 0, 0)));
        assertFalse(t.contains(new City("C", 3, 3)));
        t.clear();
        assertTrue(t.isEmpty());
        assertEquals(0, t.size());
    }

    public void testRemoveByKey_UsesMaxFromLeft() {
        BST<City> t = new BST<>();
        City m = new City("M", 5, 5);
        City c = new City("C", 1, 1);
        City k = new City("K", 2, 2); // will become predecessor of M
        City z = new City("Z", 9, 9);
        t.insert(m); t.insert(c); t.insert(k); t.insert(z);

        assertTrue(t.remove(new City("M", 0, 0))); // remove by key (name)
        String s = inorderToString(t);
        assertFalse(s.contains("M (5, 5)"));
        assertTrue(s.contains("K (2, 2)"));       // predecessor moved up
        assertEquals(3, t.size());
    }

    public void testRemoveMatching_RemovesExactTripleOnly() {
        BST<City> t = new BST<>();
        City n1 = new City("N", 1, 1);
        City n2 = new City("N", 2, 2);
        City z  = new City("Z", 9, 9);
        t.insert(n1); t.insert(n2); t.insert(z);

        // wrong coords should not delete
        assertFalse(t.removeMatching(new City("N", 123, 456),
                c -> c.getName().equals("N") && c.getX() == 123 && c.getY() == 456));
        assertEquals(3, t.size());

        // exact delete of (N,2,2)
        assertTrue(t.removeMatching(new City("N", 2, 2),
                c -> c.getName().equals("N") && c.getX() == 2 && c.getY() == 2));
        assertEquals(2, t.size());
        String s = inorderToString(t);
        assertTrue(s.contains("N (1, 1)"));
        assertFalse(s.contains("N (2, 2)"));
    }

    public void testIndentationExactAcrossDepths() {
        BST<City> t = new BST<>();
        t.insert(new City("D0", 0, 0));     // depth 0
        t.insert(new City("D1", -1, -1));   // depth 1 (equal-left via name order)
        t.insert(new City("D2", -2, -2));   // depth 2
        t.insert(new City("D3", -3, -3));   // depth 3
        String s = inorderToString(t);
        assertTrue(s.contains("0D0"));
        assertTrue(s.contains("1  D1"));
        assertTrue(s.contains("2    D2"));
        assertTrue(s.contains("3      D3"));
    }
}
