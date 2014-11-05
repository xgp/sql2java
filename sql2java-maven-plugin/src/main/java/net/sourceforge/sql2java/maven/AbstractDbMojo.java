package net.sourceforge.sql2java.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Common abstract Mojo for plugins with db props.
 * @author garth
 */
public abstract class AbstractDbMojo extends AbstractMojo {

    /**
     * JDBC driver class name.
     */
    @Parameter(property = "driver", defaultValue = "org.hsqldb.jdbcDriver", required = true)
    protected String driver;

    /**
     * Database connection string.
     */
    @Parameter(property = "url", defaultValue = "jdbc:hsqldb:file:${project.build.directory}/databases/test", required = true)
    protected String url;
    
    /**
     * Database connection user name.
     */
    @Parameter(property = "user", defaultValue = "SA", required = true)
    protected String user;
    
    /**
     * Database connection user password.
     */
    @Parameter(property = "password", defaultValue = "")
    protected String password;

    /**
     * Database catalog
     */
    @Parameter(property = "catalog")
    protected String catalog;
    
    /**
     * Database schema
     */
    @Parameter(property = "schema", defaultValue = "PUBLIC")
    protected String schema;

}
