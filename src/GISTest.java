import java.io.IOException;
import student.TestCase;

/**
 * @author Parth Mehta
 * @version 09/30/2025
 */
public class GISTest extends TestCase {

    private GIS it;

    /**
     * Sets up the tests that follow. In general, used for initialization
     */
    public void setUp() {
        it = new GISDB();
    }
    

    /**
     * Test clearing on initial
     * @throws IOException
     */
    public void testRefClearInit()
        throws IOException
    {
        assertTrue(it.clear());
    }


    /**
     * Print testing for empty trees
     * @throws IOException
     */
    public void testRefEmptyPrints()
        throws IOException
    {
        assertFuzzyEquals("", it.print());
        assertFuzzyEquals("", it.debug());
        assertFuzzyEquals("", it.info("CityName"));
        assertFuzzyEquals("", it.info(5, 5));
        assertFuzzyEquals("", it.delete("CityName"));
        assertFuzzyEquals("", it.delete(5, 5));
    }


    /**
     * Print bad input checks
     * @throws IOException
     */
    public void testRefBadInput()
        throws IOException
    {
        assertFalse(it.insert("CityName", -1, 5));
        assertFalse(it.insert("CityName", 5, -1));
        assertFalse(it.insert("CityName", 100000, 5));
        assertFalse(it.insert("CityName", 5, 100000));
        assertFuzzyEquals("", it.search(-1, -1, -1));
    }
    
    /**
     * Test insert and duplicate coordinate checking
     */
    public void testInsert() {
        assertTrue(it.insert("Denver", 100, 200));
        assertFalse(it.insert("Bad", -1, 100));      // Negative x
        assertFalse(it.insert("Bad", 100, -1));      // Negative y
        assertFalse(it.insert("Bad", 40000, 100));   // x > MAXCOORD
        assertFalse(it.insert("Bad", 100, 40000));   // y > MAXCOORD
    }

    public void testPrint() {
        it.insert("Denver", 100, 200);
        it.insert("Boston", 50, 100);
        
        String bst = it.print();
        String kd = it.debug();
        
        assertTrue(bst.contains("Denver"));
        assertTrue(bst.contains("Boston"));
        assertTrue(kd.contains("Denver"));
        assertTrue(kd.contains("Boston"));

        // Root line may NOT be first in inorder if there’s a left subtree.
        // So: accept either start-of-string "0..." OR a newline followed by "0..."
        assertTrue(bst.startsWith("0") || bst.contains("\n0"));
        assertFalse(bst.startsWith("0 ") || bst.contains("\n0 "));
        assertTrue(kd.startsWith("0") || kd.contains("\n0"));
        assertFalse(kd.startsWith("0 ") || kd.contains("\n0 "));
    }

    
    public void testInsertAndPrint() {
        assertTrue(it.insert("Denver", 100, 200));
        assertTrue(it.insert("Boston", 50, 100));
        assertTrue(it.insert("NYC", 150, 250));
        
        String output = it.print();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("Boston"));
        assertTrue(output.contains("NYC"));

        // Empty DB prints should be exactly empty
        it.clear();
        assertEquals("", it.print());
        assertEquals("", it.debug());
    }

    public void testDebugOutput() {
        assertTrue(it.insert("Denver", 100, 200));
        assertTrue(it.insert("Boston", 50, 300));
        
        String output = it.debug();
        assertTrue(output.contains("Denver"));
        assertTrue(output.contains("Boston"));
    }

    public void testMultipleInserts() {
        for (int i = 0; i < 5; i++) {
            assertTrue(it.insert("City" + i, i * 10, i * 20));
        }
        
        String bst = it.print();
        String kd = it.debug();
        
        for (int i = 0; i < 5; i++) {
            assertTrue(bst.contains("City" + i));
            assertTrue(kd.contains("City" + i));
        }
    }

    /**
     * Test clear returns true
     */
    public void testClear() {
        assertTrue(it.clear());
    }

    /**
     * Test insert with invalid coordinates (negative and over max)
     */
    public void testInsertInvalidCoordinates() {
        // Negative coordinates
        assertFalse(it.insert("Test", -1, 100));
        assertFalse(it.insert("Test", 100, -1));
        assertFalse(it.insert("Test", -1, -1));
        
        // Over max coordinates
        assertFalse(it.insert("Test", 32768, 100));
        assertFalse(it.insert("Test", 100, 32768));
    }

    /**
     * Test search with various radius values
     */
    public void testSearchRadius() {
        // Negative radius
        assertEquals("", it.search(0, 0, -1));
        
        // Zero radius
        assertEquals("0", it.search(0, 0, 0));
        
        // Large radius
        assertEquals("0", it.search(0, 0, 10000));
        
        // Negative coordinates with valid radius
        assertEquals("0", it.search(-100, -100, 50));
    }
    
    public void testInsertRejectsDuplicateCoordinates() {
        assertTrue(it.insert("A", 10, 10));
        // Same (x,y) must be rejected per spec
        assertFalse(it.insert("B", 10, 10));
    }

}
