import java.io.IOException;
import student.TestCase;

/**
 * @author {Your Name Here}
 * @version {Put Something Here}
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
     * Insert some records and check output requirements for various commands
     * @throws IOException
     */
    public void testRefOutput()
        throws IOException
    {
        assertTrue(it.insert("Chicago", 100, 150));
        assertTrue(it.insert("Atlanta", 10, 500));
        assertTrue(it.insert("Tacoma", 1000, 100));
        assertTrue(it.insert("Baltimore", 0, 300));
        assertTrue(it.insert("Washington", 5, 350));
        assertFalse(it.insert("X", 100, 150));
        assertTrue(it.insert("L", 101, 150));
        assertTrue(it.insert("L", 11, 500));
        assertFuzzyEquals("1  Atlanta (10, 500)\n"
            + "2    Baltimore (0, 300)\n"
            + "0Chicago (100, 150)\n"
            + "3      L (11, 500)\n"
            + "2    L (101, 150)\n"
            + "1  Tacoma (1000, 100)\n"
            + "2    Washington (5, 350)\n", it.print());
        assertFuzzyEquals("2    Baltimore (0, 300)\n"
            + "3      Washington (5, 350)\n"
            + "1  Atlanta (10, 500)\n"
            + "2    L (11, 500)\n"
            + "0Chicago (100, 150)\n"
            + "1  Tacoma (1000, 100)\n"
            + "2    L (101, 150)\n", it.debug());
        assertFuzzyEquals("L (101, 150)\nL (11, 500)", it.info("L"));
        assertFuzzyEquals("L", it.info(101, 150));
        assertFuzzyEquals("Tacoma (1000, 100)", it.delete("Tacoma"));
        assertFuzzyEquals("3\nChicago", it.delete(100, 150));
        assertFuzzyEquals("L (101, 150)\n"
                + "Atlanta (10, 500)\n"
                + "Baltimore (0, 300)\n"
                + "Washington (5, 350)\n"
                + "L (11, 500)\n5", it.search(0, 0, 2000));
        assertFuzzyEquals("Baltimore (0, 300)\n4", it.search(0, 300, 0));
    }
}
