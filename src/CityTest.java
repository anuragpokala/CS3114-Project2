import student.TestCase;

/**
 * Tests for City.equals and basic accessors
 * 
 * @author Parth Mehta
 * @version 10/07/2025
 */
public class CityTest extends TestCase {

    public void testEqualsReflexive() {
        City a = new City("A", 1, 2);
        assertTrue(a.equals(a));
    }

    public void testEqualsNull() {
        City a = new City("A", 1, 2);
        assertFalse(a.equals(null));
    }

    public void testEqualsDifferentClass() {
        City a = new City("A", 1, 2);
        assertFalse(a.equals("not a city"));
    }

    public void testEqualsAllFieldsEqual() {
        City a = new City("A", 1, 2);
        City b = new City("A", 1, 2);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
    }

    public void testEqualsDifferentName() {
        City a = new City("A", 1, 2);
        City b = new City("B", 1, 2);
        assertFalse(a.equals(b));
    }

    public void testEqualsDifferentX() {
        City a = new City("A", 1, 2);
        City b = new City("A", 9, 2);
        assertFalse(a.equals(b));
    }

    public void testEqualsDifferentY() {
        City a = new City("A", 1, 2);
        City b = new City("A", 1, 9);
        assertFalse(a.equals(b));
    }
}
