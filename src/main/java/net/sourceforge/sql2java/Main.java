//$Id: Main.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java;

import java.util.*;
import java.io.*;
import java.sql.SQLException;

public class Main {

    /**
     * properties given in the command line
     */
    private static Properties prop;

    /**
     * main entry point
     */
    public static void main(String argv[])
    {
        main(argv, null);
    }

    /**
     * main entry point
     */
    public static void main(String argv[], Map overideFileProperties)
    {
        // Check for required argument
        if(argv == null || argv.length < 1) {
            System.err.println("Usage: java net.sourceforge.sql2java.Main <properties filename>");
            System.exit(1);
        }

        //properties already here ?
        prop = new Properties();
        try
        {
            prop.load(new FileInputStream(argv[0]));
            Database db = new Database();
            db.setDriver(getProperty("jdbc.driver"));
            db.setUrl(getProperty("jdbc.url"));
            db.setUsername(getProperty("jdbc.username"));
            db.setPassword(getProperty("jdbc.password"));
            db.setCatalog(getProperty("jdbc.catalog"));
            db.setSchema(getProperty("jdbc.schema"));
            db.setTableNamePattern(getProperty("jdbc.tablenamepattern"));

            if (overideFileProperties != null)
                prop.putAll(overideFileProperties);

            if ("false".equalsIgnoreCase(getProperty("jdbc.oracle.retrieve.remarks")))
                db.setOracleRetrieveRemarks(false);
            else
                db.setOracleRetrieveRemarks(true);

            String tt = getProperty("jdbc.tabletypes", "TABLE");
            StringTokenizer st = new StringTokenizer(tt, ",; \t");
            ArrayList al = new ArrayList();

            while(st.hasMoreTokens()) {
                al.add(((String)st.nextToken()).trim());
            }

            db.setTableTypes((String[])al.toArray(new String[al.size()]));

            db.load();

            CodeWriter writer = new CodeWriter(db, prop);
            // override destdir if given
            if (argv.length > 1)
                writer.setDestinationFolder(argv[1]);
            writer.process();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static String getProperty(String key)
    {
        String s = prop.getProperty(key);
        return s!=null?s.trim():s;
    }

    /**
     * helper method with default values
     */
    public static String getProperty(String key, String default_val)
    {
        String s = getProperty(key);
        if (s == null)
            return default_val;
        return s;
    }

    /**
     * is the given code in the string array ?
     */
    public static boolean isInArray(String[] ar, String code)
    {
        if (ar == null)
            return false;
        for (int i = 0; i < ar.length; i ++)
        {
            if (code.equalsIgnoreCase(ar[i]))
                return true;
        }
        return false;
    }

}
