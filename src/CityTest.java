import student.TestCase;

/**
 * Test class for City class.
 *
 * @author Parth Mehta
 * @author Anurag Pokala
 * @version 10/06/2025
 */
public class CityTest extends TestCase {

    /**
     * Tests constructor, getters, and toString method.
     */
    public void testCtorGettersToString() {
        City c = new City("Denver", 100, 200);
        assertEquals("Denver", c.getName());
        assertEquals(100, c.getX());
        assertEquals(200, c.getY());
        assertEquals("Denver (100, 200)", c.toString());
    }

    /**
     * Tests reflexive and symmetric properties of equals.
     */
    public void testEqualsReflexiveAndSymmetric() {
        City a1 = new City("A", 1, 2);
        City a2 = new City("A", 1, 2);
        City b = new City("A", 1, 3); // y differs

        // reflexive
        assertTrue(a1.equals(a1));

        // symmetric true
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        // symmetric false
        assertFalse(a1.equals(b));
        assertFalse(b.equals(a1));
    }

    /**
     * Tests equals against null and different class.
     */
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
        City a = new City("Alpha", 0, 0);
        City z = new City("Zulu", 0, 0);

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
     * equal compareTo (same name) does NOT imply equals unless
     * coords match too. This documents intended design (BST sorts
     * by name, but equality is full triple).
     */
    public void testEqualsVsCompareToContractDocumented() {
        City n1 = new City("Same", 1, 1);
        City n2 = new City("Same", 2, 2);
        assertEquals(0, n1.compareTo(n2)); // same name
        assertFalse(n1.equals(n2)); // coords differ → not equal
    }

    /**
     * Test that hashCode uses the name field.
     * Kills mutations on line 57 where name.hashCode()
     * contribution is altered.
     */
    public void testHashCodeUsesName() {
        City c1 = new City("Alpha", 10, 20);
        City c2 = new City("Beta", 10, 20); // same coords, different name

        // Different names must produce different hash codes
        // (This is not guaranteed by contract, but very likely
        // with good hash function)
        int h1 = c1.hashCode();
        int h2 = c2.hashCode();

        // If mutation breaks name incorporation, this fails
        assertFalse(h1 == h2);
    }

    /**
     * Test that hashCode uses the x coordinate.
     * Kills mutations on line 58 where x contribution is altered.
     */
    public void testHashCodeUsesX() {
        City c1 = new City("City", 10, 20);
        City c2 = new City("City", 99, 20); // Same name and y, different x

        int h1 = c1.hashCode();
        int h2 = c2.hashCode();

        assertFalse(h1 == h2); // Different x should produce different hash
    }

    /**
     * Test that hashCode uses the y coordinate.
     * Kills mutations on line 59 where y contribution is altered.
     */
    public void testHashCodeUsesY() {
        City c1 = new City("City", 10, 20);
        City c2 = new City("City", 10, 99); // Same name and x, different y

        int h1 = c1.hashCode();
        int h2 = c2.hashCode();

        assertFalse(h1 == h2); // Different y should produce different hash
    }

    /**
     * Test that equal objects have equal hash codes
     * (hash code contract). Kills mutations where
     * the combining formula is broken.
     */
    public void testHashCodeConsistentWithEquals() {
        City c1 = new City("Denver", 100, 200);
        City c2 = new City("Denver", 100, 200);

        assertTrue(c1.equals(c2));
        assertEquals(c1.hashCode(), c2.hashCode()); // Must be equal
    }

    /**
     * Test that hashCode produces same value on repeated calls.
     * Ensures consistency. Kills mutations where h initialization
     * or accumulation is broken.
     */
    public void testHashCodeConsistency() {
        City c = new City("Test", 5, 10);

        int h1 = c.hashCode();
        int h2 = c.hashCode();
        int h3 = c.hashCode();

        assertEquals(h1, h2);
        assertEquals(h2, h3);
    }

    /**
     * Test that all three fields contribute to different hash values.
     * Kills mutations where arithmetic operations are replaced with
     * operands.
     */
    public void testHashCodeUsesAllThreeFields() {
        City c1 = new City("A", 1, 1);
        City c2 = new City("B", 1, 1); // Different name
        City c3 = new City("A", 2, 1); // Different x
        City c4 = new City("A", 1, 2); // Different y

        int h1 = c1.hashCode();
        int h2 = c2.hashCode();
        int h3 = c3.hashCode();
        int h4 = c4.hashCode();

        // All should be different (high probability with good hash)
        assertFalse(h1 == h2);
        assertFalse(h1 == h3);
        assertFalse(h1 == h4);
        assertFalse(h2 == h3);
        assertFalse(h2 == h4);
        assertFalse(h3 == h4);
    }

    /**
     * Test hashCode with extreme values to ensure arithmetic is correct.
     * Kills mutations where multiplication constant or accumulation fails.
     */
    public void testHashCodeWithExtremeValues() {
        City c1 = new City("X", 0, 0);
        City c2 = new City("X", 32767, 32767); // MAXCOORD
        City c3 = new City("Y", 0, 0);

        int h1 = c1.hashCode();
        int h2 = c2.hashCode();
        int h3 = c3.hashCode();

        // Same name but different coords -> different hash
        assertFalse(h1 == h2);

        // Different name but same coords -> different hash
        assertFalse(h1 == h3);
    }

    /**
     * Test that the multiplication by 31 actually matters.
     * Kills mutations where "31 * h" is replaced with just "31"
     * or just "h".
     */
    public void testHashCodeMultiplicationMatters() {
        City c1 = new City("Test", 1, 2);
        City c2 = new City("Test", 2, 1); // Swapped x and y

        int h1 = c1.hashCode();
        int h2 = c2.hashCode();

        // With proper accumulation, they should be different
        assertFalse(h1 == h2);
    }

    /**
     * Test equals returns false when ONLY x coordinate differs.
     * Kills mutation where "x == o.x" is replaced with true.
     */
    public void testEqualsReturnsFalseWhenOnlyXDiffers() {
        City c1 = new City("Same", 10, 20);
        City c2 = new City("Same", 99, 20); // Only x differs

        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1)); // Symmetric
    }

    /**
     * Test equals returns false when ONLY y coordinate differs.
     * Kills mutation where "y == o.y" is replaced with true.
     */
    public void testEqualsReturnsFalseWhenOnlyYDiffers() {
        City c1 = new City("Same", 10, 20);
        City c2 = new City("Same", 10, 99); // Only y differs

        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1)); // Symmetric
    }

    /**
     * Test equals returns false when ONLY name differs.
     * Kills mutation where "name.equals(o.name)" is replaced with true.
     */
    public void testEqualsReturnsFalseWhenOnlyNameDiffers() {
        City c1 = new City("Alpha", 10, 20);
        City c2 = new City("Beta", 10, 20); // Only name differs

        assertFalse(c1.equals(c2));
        assertFalse(c2.equals(c1)); // Symmetric
    }

    /**
     * Test equals requires ALL three fields to match.
     * Kills compound mutations where multiple checks are broken.
     */
    public void testEqualsRequiresAllThreeFieldsMatch() {
        City base = new City("City", 50, 100);

        City diffX = new City("City", 51, 100);
        City diffY = new City("City", 50, 101);
        City diffName = new City("Other", 50, 100);
        City diffXY = new City("City", 51, 101);
        City diffXName = new City("Other", 51, 100);
        City diffYName = new City("Other", 50, 101);
        City diffAll = new City("Other", 51, 101);

        // Only exact match should be true
        City match = new City("City", 50, 100);
        assertTrue(base.equals(match));

        // Any difference should be false
        assertFalse(base.equals(diffX));
        assertFalse(base.equals(diffY));
        assertFalse(base.equals(diffName));
        assertFalse(base.equals(diffXY));
        assertFalse(base.equals(diffXName));
        assertFalse(base.equals(diffYName));
        assertFalse(base.equals(diffAll));
    }
}
