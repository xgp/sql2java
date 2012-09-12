//$Id: UserCodeParser.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java;

import java.io.*;
import java.util.*;

/**
 * Parses an existing source file for special sql2code comment blocks.
 * <br>
 * Stores the source code that it finds in between those blocks in a
 * hashtable. This allows CodeWriter classes to rewrite existing files
 * while preserving code written by the user.
 *
 * @author James Cooper <pixel@bitmechanic.com>
 * @version $Id: UserCodeParser.java,v 1.1 2005/10/12 18:44:24 framiere Exp $
 */
public class UserCodeParser {

    private static final String START = "// ";
    private static final String BLOCK_BEGIN = "+ ";
    private static final String BLOCK_END = "- ";

    private String LINE_SEP = System.getProperty("line.separator");
    private Hashtable codeHash;
    private String filename;
    private boolean isNew;

    /**
     * Constructs a new parser, and tells the parser to try to load the
     * given filename.
     * <br>
     * If the file does not exist, the parser will simply initialize a
     * new hashtable internally, and the get methods on the parser will
     * return new comment blocks (with nothing in between them).
     *
     * @param filename Full path to the existing file to parse.
     * @throws Exception if the existing file exists, but cannot be read,
     *         or if there is an error while reading the file
     */
    public UserCodeParser(String filename) throws Exception {
        parse(filename);
    }

    /**
     * Returns the filename associated with this parser.
     */
    public String getFilename() { return filename; }

    /**
     * Returns true if the file to parse did not exist.
     */
    public boolean isNew() { return isNew; }

    /**
     * Parses the file passed in.
     */
    public void parse(String filename) throws Exception {
        codeHash = new Hashtable();
        boolean inBlock = false;
        String blockName = null;
        StringBuffer code = new StringBuffer();
        isNew = true;

        File file = new File(filename);
        if(file.exists()) {
            isNew = false;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while(line != null) {
                if(inBlock) code.append(line).append(LINE_SEP);

                if(line.indexOf(START) != -1) {
                    if(inBlock) {
                        if(line.equals(START + blockName + BLOCK_END)) {

                            // Done parsing block.  Store it
                            codeHash.put(blockName, code.toString());
                            inBlock = false;
                        }
                    }
                    else {
                        blockName = parseName(line);
                        if(!blockName.equals("")) {
                            inBlock = true;
                            code.setLength(0);
                            code.append(line).append(LINE_SEP);
                        }
                    }
                }

                line = reader.readLine();
            }
            reader.close();
        }
    }

    private String parseName(String line) {
        int startPos = line.indexOf(START);
        if(startPos == -1) return "";
        startPos += START.length();

        if(startPos >= (line.length() + 1)) return "";

        int endPos = line.indexOf(BLOCK_BEGIN, startPos);
        if(endPos == -1) return "";
        else  {
            String name = line.substring(startPos, endPos);
            return name;
        }
    }

    /**
     * Returns true if there is a block with the given name in the
     * existing file that was parsed, otherwise returns false.
     *
     * @param name Name of the code block to check for.
     */
    public boolean hasBlock(String name) {
        return codeHash.get(name) != null;
    }

    /**
     * Returns the comment+code block associated with this name.
     * <br>
     * If the name is not found in the parsed file, this returns a new
     * comment block with no code inside it.
     *
     * @param name Name of the code block to return.
     */
    public String getBlock(String name) {
        String code = null;
        if (name != null) code = (String)codeHash.get(name);
        if(code == null) {
            code = generateNewBlock(name);
            codeHash.put(name, code);
        }
        return code;
    }

    /**
     * Returns an array of the block names parsed in the existing file.
     * <br>
     * If the file did not exist, or if there are no blocks in the file,
     * a zero length array is returned.
     */
    public String[] getBlockNames() {
        String list[] = new String[codeHash.size()];
        int i = 0;
        for(Enumeration e = codeHash.keys(); e.hasMoreElements(); ) {
            list[i++] = (String)e.nextElement();
        }

        return list;
    }

    /**
     * Generates a new code block with this name.
     */
    private String generateNewBlock(String name) {
        StringBuffer str = new StringBuffer(512);
        str.append(START);
        str.append(name);
        str.append(BLOCK_BEGIN);
        str.append(LINE_SEP).append(LINE_SEP);
        str.append(START);
        str.append(name);
        str.append(BLOCK_END);

        return str.toString();
    }
}
