import student.TestCase;

/**
 * Unit tests for the shared City record object.
 * Assumptions (per project spec and our usage):
 *  - compareTo orders by name ONLY (coords ignored for ordering),
 *  - equals requires exact (name, x, y) triple match,
 *  - toString is "Name (x, y)".
 */
public class CityTest extends TestCase {

    public void testCtorGettersToString() {
        City c = new City("Denver", 100, 200);
        assertEquals("Denver", c.getName());
        assertEquals(100, c.getX());
        assertEquals(200, c.getY());
        assertEquals("Denver (100, 200)", c.toString());
    }

    public void testEqualsReflexiveAndSymmetric() {
        City a1 = new City("A", 1, 2);
        City a2 = new City("A", 1, 2);
        City b  = new City("A", 1, 3);  // y differs

        // reflexive
        assertTrue(a1.equals(a1));

        // symmetric true
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        // symmetric false
        assertFalse(a1.equals(b));
        assertFalse(b.equals(a1));
    }

    public void testEqualsNullAndDifferentClass() {
        City c = new City("Z", 9, 9);
        assertFalse(c.equals(null));
        assertFalse(c.equals("Z (9, 9)")); // different type
    }

    /**
     * Project behavior: compareTo uses ONLY the name.
     * Same name but different coords -> compareTo == 0.
     */
    public void testCompareToByNameOnly() {
        City n1 = new City("Same", 1, 1);
        City n2 = new City("Same", 2, 2);
        City a  = new City("Alpha", 0, 0);
        City z  = new City("Zulu",  0, 0);

        // equal names -> 0 regardless of coordinates
        assertEquals(0, n1.compareTo(n2));

        // lexical ordering by name
        assertTrue(a.compareTo(z) < 0);
        assertTrue(z.compareTo(a) > 0);
        assertTrue(n1.compareTo(a) > 0);
        assertTrue(n1.compareTo(z) < 0);
    }

    /**
     * Sanity on equals vs compareTo contract for sorted collections:
     * equal compareTo (same name) does NOT imply equals unless coords match too.
     * This documents intended design (BST sorts by name, but equality is full triple).
     */
    public void testEqualsVsCompareToContractDocumented() {
        City n1 = new City("Same", 1, 1);
        City n2 = new City("Same", 2, 2);
        assertEquals(0, n1.compareTo(n2));   // same name
        assertFalse(n1.equals(n2));          // coords differ → not equal
    }
}
