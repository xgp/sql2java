//$Id: CodeWriter.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java;

//TODO: Add exception handling in this class for the VelocityEngine.

import java.io.*;
import java.util.*;
import java.sql.Types;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.FieldMethodizer;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;


// this class is a mess, would need some brushing, however the generated code is clean and
// that's what really matters
public class CodeWriter
{
    static protected Properties props;

    public static String MGR_CLASS="Manager";

    protected static String dateClassName;
    protected static String timeClassName;
    protected static String timestampClassName;

    protected Database db;
    protected Hashtable includeHash, excludeHash;

    protected String basePackage;
    protected String destDir;
    protected String optimisticLockType;
    protected String optimisticLockColumn;
    public String classPrefix;

    protected VelocityContext vc;

    public Table table;
    protected VelocityContext current_vc;

    ///////////////////////////////////////////////////////
    // CODE WRITER INIT
    ///////////////////////////////////////////////////////

    /** The Default Constructor
     * @author Kelvin Nishikawa
     * @param props Properties for configuring this instance
     */
    public CodeWriter (Database db, Properties props) {
    	try {
            this.db  = db;
	    this.props = props;

            dateClassName = props.getProperty("jdbc2java.date", "java.sql.Date");
            timeClassName = props.getProperty("jdbc2java.time", "java.sql.Time");
            timestampClassName = props.getProperty("jdbc2java.timestamp", "java.sql.Timestamp");

            // Set properties
            basePackage = props.getProperty("mgrwriter.package");
            classPrefix = props.getProperty("mgrwriter.classprefix");
            setDestinationFolder(props.getProperty("mgrwriter.destdir"));

            excludeHash = setHash(props.getProperty("mgrwriter.exclude"));
            if (excludeHash.size() != 0)
                System.out.println("Excluding the following tables: " +  props.getProperty("mgrwriter.exclude"));
            includeHash = setHash(props.getProperty("mgrwriter.include"));
            if (includeHash.size() != 0)
                System.out.println("Including only the following tables: " +  props.getProperty("mgrwriter.include"));

            optimisticLockType = props.getProperty("optimisticlock.type", "none");
            optimisticLockColumn = props.getProperty("optimisticlock.column");

            if(basePackage == null) throw new Exception("Missing property: mgrwriter.package");
    	} catch (Exception e) {
	    //knishikawa - maybe this needs better exception handling for the Velocity inits
	    System.err.println("Threw an exception in the CodeWriter constructor:" + e.getMessage());
	    e.printStackTrace();
    	}
    }


    public void setDestinationFolder(String destDir) throws Exception
    {
        this.destDir = destDir;
        if(destDir == null) throw new Exception("Missing property: mgrwriter.destdir");

        File dir = new File(destDir);
        try {
            dir.mkdirs();
        } catch (Exception e) {
            // ignore
        }

        if(!dir.isDirectory() || !dir.canWrite()) throw new Exception("Cannot write to: " + destDir);
    }


    private Hashtable setHash(String str)
    {
        if (str == null || str.trim().equals("")) {
            return new Hashtable();
        } else {
	    Hashtable hash = new Hashtable();
	    StringTokenizer st = new StringTokenizer(str);
	    while(st.hasMoreTokens()) {
		String val = st.nextToken().toLowerCase();
		hash.put(val, val);
	    }
	    return hash;
	}
    }

    public boolean checkTable(Table table) throws Exception
    {
        System.out.println("    checking table " + table.getName() + " ...");
        boolean error = false;
        Column primaryKeys[] = table.getPrimaryKeys();
        if (table.getColumns().length == 0) {
	    System.err.println("        WARN : no column found !");
	    error = false;
	}
        if (primaryKeys.length == 0) {
	    System.err.println("        WARN : No primary key is defined on table " + table.getName());
	    System.err.println("            Tables without primary key are not fully supported");
	    error = false;
	} else {
	    if (primaryKeys.length > 1) {
		System.err.print("        WARN : Composite primary key ");
		for (int ii = 0; ii < primaryKeys.length; ii++)
		    System.err.print(primaryKeys[ii].getFullName() + ", ");
		System.err.println();
		System.err.println("            Tables with composite primary key are not fully supported");
	    } else {
		Column pk = primaryKeys[0];
		String pkName = pk.getName();
		String normalKey = table.getName() + "_id";
		if (pkName.equalsIgnoreCase(normalKey) == false) {
		    System.err.println("          WARN : primary key should of form <TABLE_NAME>_ID");
		    System.err.println("              found " + pkName + " expected " + normalKey);
		}
		if (pk.isColumnNumeric() == false) {
		    System.err.println("          WARN : primary key should be an integer ");
		    System.err.println("              found " + pk.getJavaType());
		}
	    }
	}
        return error;
    }

    public void checkDatabase() throws Exception
    {
        System.out.println("Checking database tables");
        boolean error = false;
        Table tables[] = db.getTables();
        for (int i = 0; i < tables.length; i++) {
	    if (includeHash.size() != 0) {
		if (includeHash.get(tables[i].getName().toLowerCase()) != null) {
		    if (excludeHash.get(tables[i].getName().toLowerCase()) == null) {
			boolean b = checkTable(tables[i]);
			if (b == true)
			    error = true;
		    }
		}
	    } else {
		if (excludeHash.size() != 0) {
		    if (excludeHash.get(tables[i].getName().toLowerCase()) == null) {
			boolean b = checkTable(tables[i]);
			if (b == true) error = true;
		    }
		} else {
		    boolean b = checkTable(tables[i]);
		    if (b == true) error = true;
		}
	    }
	}
        if (error == true) {
	    System.err.println("    Failed : at least one of the mandatory rule for sql2java is followed by your schema.");
	    System.err.println("    Please check the documentation for more information");
	    System.exit(-1);
	}
        System.out.println("    Passed.");
    }

    ///////////////////////////////////////////////////////
    // CODE WRITER CORE
    ///////////////////////////////////////////////////////

    /** The entry point for generating code. */
    public synchronized void process() throws Exception
    {
        if ("true".equalsIgnoreCase(props.getProperty("check.database")))
            checkDatabase();

        if ("true".equalsIgnoreCase(props.getProperty("check.only.database")))
            return;

        // Init Velocity
        Properties vprops = new Properties();
	vprops.put("runtime.log", "target/velocity.log");
	vprops.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
	vprops.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
	// for file and class
	// vprops.put(RuntimeConstants.RESOURCE_LOADER, "file, classpath");
	// vprops.put("file.resource.loader.class", FileResourceLoader.class.getName());
	// vprops.put("file.resource.loader.path", getProperty("mgrwriter.templates.loadingpath",".") );
	// vprops.put("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
	
        Velocity.init(vprops);
        vc = new VelocityContext();
        vc.put("CodeWriter", new FieldMethodizer( this ));
        vc.put("codewriter", this );
        vc.put("pkg", basePackage);
        vc.put("pkgPath", basePackage.replace('.', '/'));
        vc.put("strUtil", StringUtilities.getInstance());
        current_vc = new VelocityContext(vc);

        System.out.println("Generation in folder " + destDir + " ...");
        String[] schema_templates = getPropertyExploded("mgrwriter.templates.perschema");
        for(int i=0; i<schema_templates.length; i++) {
            writeComponent(schema_templates[i]);
        }
        if ("true".equalsIgnoreCase(props.getProperty("write.only.per.schema.templates")))
            return;

        // Generate core and manager classes for all tables
        Table tables[] = db.getTables();
        for(int i = 0; i < tables.length; i++) {
            // See if this is in our exclude or include list
            if(includeHash.size() != 0) {
                if(includeHash.get(tables[i].getName().toLowerCase()) != null) {
                    if (excludeHash.get(tables[i].getName().toLowerCase()) == null){
                    	writeTable(tables[i]);
                    }
                }
            } else if(excludeHash.size() != 0) {
                if(excludeHash.get(tables[i].getName().toLowerCase()) == null) {
		    if(includeHash.get(tables[i].getName().toLowerCase()) != null) {
    	                writeTable(tables[i]);
    	            }
                }
            } else  {
                writeTable(tables[i]);
            }
        }
    }

    private void writeTable(Table table) throws Exception
    {
        if (table.getColumns().length == 0) {
            return;
        }
        current_vc = new VelocityContext(vc);
        this.table = table;
        current_vc.put("table", table);

        String[] table_templates = getPropertyExploded("mgrwriter.templates.pertable");
        for(int i=0; i<table_templates.length; i++) {
            writeComponent(table_templates[i]);
        }
    }

    /** This method creates a file and generates the class; it is based on the original SQL2Java methods
     * The filename for the class is based on the value of the Velocity variable passed in.
     * @author Kelvin Nishikawa
     * @param templateName The template to parse and generate from
     * @param variableFileName A velocity variable on which to base the filename
     * @throws Exception (IOExceptions?)
     */
    public void writeComponent(String templateName) throws Exception {

    	//check the integrity of our velocity template
    	Template template = null;
    	try {
	    template = Velocity.getTemplate(templateName);
    	} catch (ResourceNotFoundException rnfe) {
	    System.err.println( "Aborted writing component:" + templateName
				+ (table!=null?(" for table:" + table.getName()):"")
				+ " because Velocity could not find the resource." );
	    return;
    	} catch (ParseErrorException pee) {
	    System.err.println( "Aborted writing component:" + templateName
				+ (table!=null?(" for table:" + table.getName()):"")
				+ " because there was a parse error in the resource.\n" + pee.getLocalizedMessage() );
	    return;
    	} catch (Exception e) {
	    System.err.println( "Aborted writing component:" + templateName
				+ (table!=null?(" for table:" + table.getName()):"")
				+ " there was an error initializing the template.\n" + e.getLocalizedMessage() );
	    return;
    	}

        // dummy process the template
        StringWriter sw = new StringWriter();
    	Velocity.mergeTemplate(templateName ,Velocity.ENCODING_DEFAULT, current_vc ,sw);

        //this logging should be moved somewhere else?
        File file = new File(current_fullfilename);
        (new File(file.getParent())).mkdirs();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(current_fullfilename)));
        writer.write(sw.toString());
        writer.flush();
        writer.close();
        System.out.println("    " + current_filename + " done.");
    }




    ////////////////////////////////////////////////////////////////
    // METHOD CALLED FROM TEMPLATES
    ////////////////////////////////////////////////////////////////

    String current_fullfilename = "";
    String current_filename = "";

    public void setCurrentFilename(String relpath_or_package, String fn) throws Exception {
        current_filename = relpath_or_package.replace('.', File.separatorChar) + File.separatorChar + fn;
        current_fullfilename = destDir + File.separatorChar +
	    relpath_or_package.replace('.', File.separatorChar) + File.separatorChar + fn;
        UserCodeParser uc = new UserCodeParser(current_fullfilename);
        current_vc.put("userCode", uc);
    }

    public void setCurrentJavaFilename(String relpath_or_package, String fn) throws Exception {
	setCurrentFilename(relpath_or_package, fn);
    }


    /** System.out.println() wrapper */
    public void log(String logStr ) {
    	System.out.println("        " + logStr );
    }

    public String getClassPrefix() {
        return classPrefix;
    }

    /** Public accessor for db */
    public Database getDb() {
    	return db;
    }

    /** Public db.getTables() */
    public List getTables() {
    	Table[] tabs = db.getTables();
    	List tables = new ArrayList(tabs.length);
    	for ( int i = 0; i < tabs.length; i++ ) tables.add(tabs[i]);
    	return tables;
    }

    /** Public db.getTables() */
    public Table getTable(String tableName) {
        return db.getTable(tableName);
    }

    /** Return table.getName() */
    public String tableName() {
    	if (table == null) return "";
    	return table.getName();
    }

    /* Ideally these methods would be in a helper class or a subclass/rewrite of the Table class*/
    /** Return table.getColumns() as a List */
    public List getColumns() {
    	Column [] cols = table.getColumns();
    	List columns = new ArrayList(cols.length);
    	for ( int i = 0; i < cols.length; i++ ) columns.add(cols[i]);
    	return columns;
    }

    /** Return table.getImportedKeys() as a List */
    public List getImportedKeys() {
    	Column [] cols = table.getImportedKeys();
    	List columns = new ArrayList(cols.length);
    	for ( int i = 0; i < cols.length; i++ ) columns.add(cols[i]);
    	return columns;
    }

    /** Return table.getForeignKeys() as a List */
    public List getForeignKeys() {
    	Column [] cols = table.getForeignKeys();
    	List columns = new ArrayList(cols.length);
    	for ( int i = 0; i < cols.length; i++ ) columns.add(cols[i]);
    	return columns;
    }

    /** Return table.getPrimaryKeys() as a List */
    public List getPrimaryKeys() {
    	Column [] cols = table.getPrimaryKeys();
    	List columns = new ArrayList(cols.length);
    	for ( int i = 0; i < cols.length; i++ ) columns.add(cols[i]);
    	return columns;
    }

    /** Returns true if the current table.getRemarks().length() > 0 */
    public boolean hasRemarks() {
    	String remarks = table.getRemarks();
    	if ( remarks != null ) if ( remarks.length() > 0 ) return true;
    	return false;
    }

    /** Returns the current table's remarks. */
    public String getRemarks() {
    	return table.getRemarks();
    }

    /** Returns a db.getRelationTable( table ) as a list */
    public List getRelationTable() {
    	Table [] rtabs = db.getRelationTable(table);
    	List tables = new ArrayList(rtabs.length);
    	for ( int i = 0; i < rtabs.length; i++ ) tables.add(rtabs[i]);
    	return tables;
    }

    /** Returns a table's linked tables as a list */
    public List getLinkedTables ( Table rTable ) {
    	Table[] ltabs = rTable.linkedTables(db, table);
    	List tables = new ArrayList(ltabs.length);
    	for ( int i = 0; i < ltabs.length; i++ ) tables.add(ltabs[i]);
    	return tables;
    }

    /** Wrapper for the static Column method */
    public boolean isPresentLock( Collection cols ) {
    	Column [] columns = new Column[cols.size()];
    	int i = 0;
    	Iterator iter = cols.iterator();
    	while ( iter.hasNext() ) columns[i++] = (Column)iter.next();
        return Column.isPresentLock(columns, optimisticLockType, optimisticLockColumn);
    }

    /** Find the column in the Collection that matches optimisticLockColumn */
    public Column getLockColumn(Collection cols) {
        Iterator iter = cols.iterator();
        while ( iter.hasNext() ) {
	    Column col = (Column)iter.next();
	    if ( col.getName().equalsIgnoreCase( optimisticLockColumn ) ) return col;
        }
        return null;
    }

    /** public accessor for table */
    public Table getTable() {
    	return table;
    }

    /** Check if a list contains an item that is equal() to a string */
    public boolean listContainsString( List list, String string ) {
    	Object obj = null;
    	for ( Iterator iter = list.iterator();
	      iter.hasNext();
	      obj = iter.next() ) {
	    if ( string.equals(obj) ) return true;
    	}
    	return false;
    }


    //////////////////////////////////////////////////////
    // PROPERTY UTILS
    //////////////////////////////////////////////////////
    /** Convenience property chop method
     * @author Kelvin Nishikawa
     * @param key the property to get from this.props
     * @return the associated value
     */
    static public String getProperty(String key)
    {
        String s = props.getProperty(key);
        return s!=null?s.trim():s;
    }

    /** Convenience property chop method
     * @author Kelvin Nishikawa
     * @param key the property to get from this.props
     * @param default_val the default value to return in case not found
     * @return the associated value
     */
    static public String getProperty(String key, String default_val) {
    	String s = props.getProperty(key,default_val);
        return s!=null?s.trim():s;
    }


    /**
     * Return as a String array the key's value.
     */
    static public String[] getPropertyExploded(String key)
    {
        String v = getProperty(key);
        if (v==null) {
            return new String[0];
        }
        ArrayList al = new ArrayList();

        StringTokenizer st = new StringTokenizer(v, " ,;\t");
        while (st.hasMoreTokens()) {
            al.add(st.nextToken().trim());
        }

        return (String[])al.toArray(new String[al.size()]);
    }

}
