package net.sourceforge.sql2java.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;
import net.sourceforge.sql2java.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which uses sql2java to generate Java files from a SQL database.
 * @author garth
 */
@Mojo(name="sql2java", requiresDirectInvocation=true)
public class Sql2JavaMojo extends AbstractDbMojo {

    /**
     * The working directory where the generated Java source files are created.
     */
    @Parameter(property="outputDirectory", defaultValue="${project.build.directory}/generated-sources/sql2java", required=true)
    private File outputDirectory;

    protected File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * The location of the properties file.
     */
    @Parameter(property="propertiesFile", defaultValue="${project.basedir}/src/main/resources/sql2java.properties", required=true)
    private File propertiesFile;

    protected File getPropertiesFile() {
        return propertiesFile;
    }

    /**
     * Database schema alias
     */
    @Parameter(property="schemaAlias", defaultValue="test")
    private String schemaAlias;

    /**
     * Package name
     */
    @Parameter(property="packageName", required=true)
    private String packageName;

    /**
     * Class prefix
     */
    @Parameter(property="classPrefix", required=false)
    private String classPrefix;

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
	CodeWriter writer = null;
        try {
            prop.load(new FileInputStream(propertiesFile));
            Database db = new Database();
            db.setDriver(driver);
            db.setUrl(url);
            db.setUsername(user);
	    db.setPassword(password);
            db.setCatalog(catalog);
            db.setSchema(schema);
	    //db.setSchemaAlias(schemaAlias);
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
	    
	    //HACK
	    if (packageName != null) prop.setProperty("mgrwriter.package", packageName);
	    if (classPrefix != null) prop.setProperty("mgrwriter.classprefix", classPrefix);
	    writer = new CodeWriter(db, prop);
            // override destdir if given
	    writer.setUseLibrary("net.sourceforge.sql2java.lib");
	    writer.setDestinationFolder(outputDirectory.getPath());
            writer.process();
        } catch(Exception e) {
	    throw new MojoExecutionException("Error executing plugin", e);
        } finally {
	    if (writer != null) {
		writer.cleanup();
	    }
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
