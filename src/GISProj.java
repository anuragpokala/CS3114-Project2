
/**
 * Mini GIS playground used for testing data structures.
 * 
 * GISDB stores cities (name, x, y) in two synced indexes:
 *   • KDTree by coordinates (insert/find/delete/range; ties go right).
 *   • BST by name (inorder print; remove exact (name,x,y) triples).
 *
 * Key ops:
 *   insert, delete(x,y) → "visited\nname", delete(name) → lines "name (x, y)",
 *   info(x,y)/info(name), search(x,y,r) → matches then visit count,
 *   debug() → KD inorder; print() → BST inorder.
 *
 * Coordinates are validated (0..32767). This main class is a no-op entry point
 * for automated tests.
 */

import java.io.IOException;

/**
 * The class containing the main method.
 *
 *@author Parth Mehta (pmehta24)
 * @author Anurag Pokala (anuragp34)
 * @version 2025-10-06
 */

// On my honor:
//
// - I have not used source code obtained from another current or
// former student, or any other unauthorized source, either
// modified or unmodified.
//
// - All source code and documentation used in my program is either my
// original work, or was derived by me from the source code
// published in the textbook for this course. I understand that I am
// permitted to use an LLM tool to assist me with writing project
// code, under the condition that I submit with the project a text
// file that contains the full transcript of my interactions with
// the LLM (showing my prompts and the LLM's response). I understand
// that I am responsible for being able to complete this work
// without the use of LLM assistance.
//
// - I have not discussed coding details about this project with
// anyone other than my partner (in the case of a joint
// submission), instructor, ACM/UPE tutors or the TAs assigned
// to this course. I understand that I may discuss the concepts
// of this program with other students, and that another student
// may help me debug my program so long as neither of us writes
// anything during the discussion or modifies any computer file
// during the discussion. I have violated neither the spirit nor
// letter of this restriction.

public class GISProj {
    /**
     * This is nothing but a placeholder for test cases that call the interface
     * methods.
     *
     * @param args
     *            Command line parameters: There are none
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // There is nothing that we need to do here
    }
}
