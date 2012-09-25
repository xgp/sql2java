package net.sourceforge.sql2java.maven;

import net.sourceforge.sql2java.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Goal which uses a sql2java to generate Java files from a SQL database.
 * @goal sql2java
 * @requiresDirectInvocation true
 * @author garth
 */
public class Sql2JavaMojo extends AbstractMojo
{

    /**
     * The working directory where the generated Java source files are created.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/sql2java"
     * @required
     */
    private File outputDirectory;

    protected File getOutputDirectory()
    {
        return outputDirectory;
    }

    /**
     * The location of the properties file.
     *
     * @parameter default-value="${project.basedir}/src/main/resources/sql2java.properties"
     * @required
     */
    private File propertiesFile;

    protected File getPropertiesFile()
    {
        return propertiesFile;
    }

    public void execute() throws MojoExecutionException {
        File f = outputDirectory;
        if ( !f.exists() ) {
            f.mkdirs();
        }

	if ( !propertiesFile.exists() ) {
	    throw new MojoExecutionException(propertiesFile.getName() + " doesn't exist!");
	}

        //properties already here ?
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(propertiesFile));
            Database db = new Database();
            db.setDriver(getProperty(prop, "jdbc.driver"));
            db.setUrl(getProperty(prop, "jdbc.url"));
            db.setUsername(getProperty(prop, "jdbc.username"));
            db.setPassword(getProperty(prop, "jdbc.password"));
            db.setCatalog(getProperty(prop, "jdbc.catalog"));
            db.setSchema(getProperty(prop, "jdbc.schema"));
            db.setTableNamePattern(getProperty(prop, "jdbc.tablenamepattern"));
	    
            if ("false".equalsIgnoreCase(getProperty(prop, "jdbc.oracle.retrieve.remarks")))
                db.setOracleRetrieveRemarks(false);
            else
                db.setOracleRetrieveRemarks(true);
	    
            String tt = getProperty(prop, "jdbc.tabletypes", "TABLE");
            StringTokenizer st = new StringTokenizer(tt, ",; \t");
            ArrayList al = new ArrayList();
	    
            while(st.hasMoreTokens()) {
                al.add(((String)st.nextToken()).trim());
            }
	    
            db.setTableTypes((String[])al.toArray(new String[al.size()]));
	    
            db.load();
	    
            CodeWriter writer = new CodeWriter(db, prop);
            // override destdir if given
	    writer.setDestinationFolder(outputDirectory.getPath());
            writer.process();
        } catch(Exception e) {
	    throw new MojoExecutionException("Error executing plugin", e);
        }

    }

    public static String getProperty(Properties prop, String key)
    {
        String s = prop.getProperty(key);
        return s!=null?s.trim():s;
    }

    /**
     * helper method with default values
     */
    public static String getProperty(Properties prop, String key, String default_val)
    {
        String s = getProperty(prop, key);
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
