package net.sourceforge.sql2java.maven;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Arrays;
import net.sourceforge.sql2java.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.hsqldb.cmdline.SqlFile;

/**
 * Goal which uses hsqldb's SqlFile to source scripts to a db.
 * @author garth
 */
@Mojo(name="sqlfile", requiresDirectInvocation=true)
public class SqlFileMojo extends AbstractDbMojo {

    /**
     * Wipe all data and table definitions if they already exist?
     */
    @Parameter(property="deleteData", defaultValue="false")
    private boolean deleteData;

    /**
     * The script directory.
     */
    @Parameter(property="scriptDirectory", defaultValue="${project.basedir}/src/main/sql", required=true)
    private File scriptDirectory;

    protected File getScriptDirectory() {
        return scriptDirectory;
    }

    public void execute() throws MojoExecutionException {
	Connection connection = null;
	try {
	    Class.forName(driver);
	    connection = DriverManager.getConnection(url, user, password);

	    //deleteData?
	    //hsql:DROP SCHEMA PUBLIC CASCADE
	    
	    File[] files = scriptDirectory.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
			return name.toLowerCase().endsWith(".sql");
		    }
		});
	    Arrays.sort(files);
	    for (File file : files) {    
		getLog().info(String.format("Processing %s", file));
		SqlFile sqlFile = new SqlFile(file, null, false);
		sqlFile.setConnection(connection);
		sqlFile.execute();
	    }
	} catch (Exception e) {
	    getLog().error("", e);
	} finally {
	    if (connection != null) {
		try {
		    //HACK
		    if (url.contains("jdbc:hsqldb:file")) {
			Statement statement = connection.createStatement();
			statement.executeUpdate("SHUTDOWN");
			statement.close();
		    }
		} catch (SQLException ignore) {}
		try {
		    connection.close();
		} catch (SQLException ignore) {}
	    }
	}
    }

}
