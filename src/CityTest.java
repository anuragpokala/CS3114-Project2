import student.TestCase;

/**
 * Tests for City.equals and basic accessors
 * 
 * @author Parth Mehta
 * @version 10/07/2025
 */
public class CityTest extends TestCase {

    /**
     * Ensures equals is reflexive.
     */
    public void testEqualsReflexive() {
        City a = new City("A", 1, 2);
        assertTrue(a.equals(a));
    }


    /**
     * Ensures equals returns false when compared to null.
     */
    public void testEqualsNull() {
        City a = new City("A", 1, 2);
        assertFalse(a.equals(null));
    }


    /**
     * Ensures equals returns false for different class.
     */
    public void testEqualsDifferentClass() {
        City a = new City("A", 1, 2);
        assertFalse(a.equals("not a city"));
    }


    /**
     * Ensures equals is symmetric for identical fields.
     */
    public void testEqualsAllFieldsEqual() {
        City a = new City("A", 1, 2);
        City b = new City("A", 1, 2);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }


    /**
     * Ensures unequal names lead to inequality.
     */
    public void testEqualsDifferentName() {
        City a = new City("A", 1, 2);
        City b = new City("B", 1, 2);
        assertFalse(a.equals(b));
    }


    /**
     * Ensures unequal x values lead to inequality.
     */
    public void testEqualsDifferentX() {
        City a = new City("A", 1, 2);
        City b = new City("A", 9, 2);
        assertFalse(a.equals(b));
    }


    /**
     * Ensures unequal y values lead to inequality.
     */
    public void testEqualsDifferentY() {
        City a = new City("A", 1, 2);
        City b = new City("A", 1, 9);
        assertFalse(a.equals(b));
    }
}
