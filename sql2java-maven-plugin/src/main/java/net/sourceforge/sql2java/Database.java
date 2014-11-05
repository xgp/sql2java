//$Id: Database.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java;

import java.sql.*;
import java.util.*;
import java.io.*;
import java.util.Vector;
import java.util.Hashtable;

public class Database
{
    private String tableTypes[];
    private Connection pConnection;
    private DatabaseMetaData meta;
    private Vector tables;
    private Hashtable tableHash;
    private String driver, url, username, password, catalog, schema, tablenamepattern;
    private boolean retrieveRemarks = true;

    public void setOracleRetrieveRemarks(boolean retrieveRemarks) { this.retrieveRemarks = retrieveRemarks;}
    public void setDriver(String driver) { this.driver = driver; }
    public void setUrl(String url) { this.url = url; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setCatalog(String catalog) { this.catalog = catalog; }
    public void setTableNamePattern(String tablenamepattern) { this.tablenamepattern = tablenamepattern; }
    public void setTableTypes(String[] tt) { this.tableTypes = tt; }

    public boolean getOracleRetrieveRemarks() { return this.retrieveRemarks; }
    public String getDriver() { return driver; }
    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCatalog() { return catalog; }
    public String getSchema() { return schema; }
    public String getTableNamePattern() { return tablenamepattern; }
    public String[] getTableTypes() { return tableTypes; }

    public void setSchema(String schema)
    {
        if ("null".equalsIgnoreCase(schema))
            this.schema = null;
        else
            this.schema = schema;
    }

    public void cleanup() {
	if (pConnection != null) {
	    try {
		//HACK
		if (url.contains("jdbc:hsqldb:file")) {
		    Statement statement = pConnection.createStatement();
		    statement.executeUpdate("SHUTDOWN");
		    statement.close();
		}
	    } catch (SQLException ignore) {}
	    try {
		pConnection.close();
	    } catch (SQLException ignore) {}
	}
    }
    
    /**
     * Return an array of tables having foreign key pointing to the
     * passed table.
     */
    public Table[] getRelationTable(Table table)
    {
        Vector vector = new Vector();

        for (int iIndex = 0; iIndex < tables.size(); iIndex ++)
	    {
		Table tempTable = (Table)tables.get(iIndex);

		// skip itself
		if (table.equals(tempTable))
		    continue;

		// check only for relation table
		if (tempTable.isRelationTable())
		    {
			if (tempTable.relationConnectsTo(table))
			    {
				if (!vector.contains(tempTable))
				    vector.add(tempTable);
			    }
		    }
	    }
        return (Table[])vector.toArray(new Table[0]);
    }

    public void load() throws SQLException, ClassNotFoundException
    {
        // Connect to the database
        Class.forName(driver);


        System.out.println("Connecting to " + username + " on " + url + " ...");
        pConnection = DriverManager.getConnection(url, username, password);
        System.out.println("    Connected.");
	//         if (pConnection instanceof oracle.jdbc.driver.OracleConnection)
	//             ((oracle.jdbc.driver.OracleConnection)pConnection).setRemarksReporting(getOracleRetrieveRemarks());

        meta = pConnection.getMetaData();
        System.out.println("    Database server :" + meta.getDatabaseProductName() + ".");
        tables = new Vector();
        tableHash = new Hashtable();

        loadTables();
        loadColumns();
        loadPrimaryKeys();
	
	// loadImportedKeys();
        // loadManyToMany();
	//		loadIndexes(); // experimental
    }

    public Table[] getTables()
    {
        return (Table[])tables.toArray(new Table[0]);
    }

    private void addTable(Table t) {
        tables.addElement(t);
        tableHash.put(t.getName(), t);
    }

    public Table getTable(String name) {
        return (Table)tableHash.get(name);
    }

    /**
     * Load all the tables for this schema.
     */
    private void loadTables() throws SQLException
    {
        System.out.println("Loading table list according to pattern " + tablenamepattern + " ...");

        // tablenamepattern is now a comma-separated list of patterns
        java.util.StringTokenizer st = new java.util.StringTokenizer(tablenamepattern, ",; \t");
        while(st.hasMoreTokens()) {
            String pattern = ((String)st.nextToken()).trim();
            ResultSet resultSet =  meta.getTables(catalog, schema, pattern, tableTypes);
            while(resultSet.next())
		{
		    Table table = new Table();
		    table.setDatabase(this);
		    table.setCatalog(resultSet.getString("TABLE_CAT"));

		    String sch = resultSet.getString("TABLE_SCHEM");
		    System.out.println(sch);
		    //                table.setSchema(resultSet.getString("TABLE_SCHEM"));
		    table.setSchema(sch);
		
		    table.setName(resultSet.getString("TABLE_NAME"));
		    table.setType(resultSet.getString("TABLE_TYPE"));
		    table.setRemarks(resultSet.getString("REMARKS"));
		    addTable(table);
		    System.out.println("    table " + table.getName() + " found");
		}
            resultSet.close();
        }
    }

    /**
     * For each table, load all the columns.
     */
    private void loadColumns() throws SQLException
    {
        Table tables[] = getTables();

        System.out.println("Loading columns ...");
        boolean b = false;
        for(int i = 0; i < tables.length; i++)
	    {
		Table table = tables[i];
		ResultSet resultSet =  meta.getColumns(catalog, schema, table.getName(), "%");
		Column c = null;

		while(resultSet.next())
		    {
			c = new Column();
			c.setDatabase(this);
			c.setCatalog(resultSet.getString("TABLE_CAT"));
			c.setSchema(resultSet.getString("TABLE_SCHEM"));
			c.setTableName(resultSet.getString("TABLE_NAME"));
			c.setName(resultSet.getString("COLUMN_NAME"));
			c.setType(resultSet.getShort("DATA_TYPE"));
			c.setSize(resultSet.getInt("COLUMN_SIZE"));
			c.setDecimalDigits(resultSet.getInt("DECIMAL_DIGITS"));
			c.setRadix(resultSet.getInt("NUM_PREC_RADIX"));
			c.setNullable(resultSet.getInt("NULLABLE"));
			c.setRemarks(resultSet.getString("REMARKS"));
			c.setDefaultValue(resultSet.getString("COLUMN_DEF"));
			c.setOrdinalPosition(resultSet.getInt("ORDINAL_POSITION"));
			table.addColumn(c);
		    }
		System.out.println("    " + table.getName() + " found " + table.countColumns() + " columns");

		resultSet.close();
	    }
    }

    /**
     * For each table, load the primary keys.
     */
    private void loadPrimaryKeys() throws SQLException
    {
        System.out.println("Loading primary keys ...");
        Table tables[] = getTables();

        for(int i = 0; i < tables.length; i++)
	    {
		Table table = tables[i];
		ResultSet resultSet = meta.getPrimaryKeys(catalog, schema, table.getName());

		while(resultSet.next())
		    {
			Column col = table.getColumn(resultSet.getString("COLUMN_NAME"));
			table.addPrimaryKey(col);
			System.out.println("    " + col.getFullName() + " found");
		    }

		resultSet.close();
	    }
    }

    /**
     * For each table, load the imported key.
     * <br>
     * An imported key is the other's table column clone. Its
     * ForeignKeyColName corresponds to the table's column name that
     * points to the other's table.
     */
    private void loadImportedKeys() throws SQLException
    {
        System.out.println("Loading imported keys ...");
        Table tables[] = getTables();

        for(int i = 0; i < tables.length; i++)
	    {
		Table table = tables[i];
		ResultSet resultSet =  meta.getImportedKeys(catalog, schema, table.getName());
		while(resultSet.next())
		    {
			String tabName = resultSet.getString("FKTABLE_NAME");
			String colName  = resultSet.getString("FKCOLUMN_NAME");

			String foreignTabName= resultSet.getString("PKTABLE_NAME");
			String foreignColName= resultSet.getString("PKCOLUMN_NAME");

			Column col = getTable(tabName).getColumn(colName);
			Column foreignCol = getTable(foreignTabName).getColumn(foreignColName);

			col.addForeignKey(foreignCol);
			foreignCol.addImportedKey(col);
			//getTable(foreignTabName).addImportedKey(col);

			System.out.println("    " +  col.getFullName() + " -> " + foreignCol.getFullName() + " found ");
		    }

		resultSet.close();
	    }
    }

    //
    // could avoid db call.
    // In each current table an entry is:
    //    other table column (that points to current table) | current pk column
    //     [other table has nb col. == pk length]
    private void loadManyToMany() throws SQLException
    {
        System.out.println("Loading many to many relationships...");
        Table tables[] = getTables();

        for(int i = 0; i < tables.length; i++)
	    {
		Table table = tables[i];

		//            if(table.getColumns().length == table.getPrimaryKeys().length)
		{
		    ResultSet resultSet =  meta.getImportedKeys(catalog, schema, table.getName());

		    while(resultSet.next())
			{
			    String tabName = resultSet.getString("PKTABLE_NAME");
			    String colName = resultSet.getString("PKCOLUMN_NAME");
			    System.out.println("    many to many " + tabName + " " + colName);

			    Table pkTable = getTable(tabName);
			    Column fkCol = table.getColumn(resultSet.getString("FKCOLUMN_NAME"));

			    if(pkTable != null)
				{
				    Column pkCol = pkTable.getColumn(colName);

				    if(pkCol != null && fkCol != null)
					{
					    pkTable.addManyToManyKey(fkCol, pkCol);
					}
				}
			}

		    resultSet.close();
		}
	    }
    }

    /**
     * For each table, load the indexes.
     */
    private void loadIndexes() throws SQLException
    {
	System.out.println("Loading indexes ...");
	Table tables[] = getTables();

	for(int i = 0; i < tables.length; i++)
	    {
		Table table = tables[i];
		ResultSet resultSet =  meta.getIndexInfo(catalog,
							 schema,
							 table.getName(),
							 true,
							 true);
		while(resultSet.next())
		    {
			String colName = resultSet.getString("COLUMN_NAME");
			String indName = resultSet.getString("INDEX_NAME");

			if (colName != null && indName != null) {
			    Column col = table.getColumn(colName);
			    if (!col.isPrimaryKey())
				System.out.println("  Found interesting index " + indName + " on " +
						   colName + " for table " +  table.getName());
			}
		    }

		resultSet.close();
	    }
    }

    public String[] getAllPackages()
    {
        Vector vector = new Vector();
        for (int iIndex = 0; iIndex < tables.size(); iIndex ++)
	    {
		Table table = (Table)tables.get(iIndex);
	        String packages[] = table.getLinkedPackages();
		for (int i = 0; i < packages.length; i++)
		    {
			if (vector.contains(packages[i]) == false)
			    {
				vector.add(packages[i]);
			    }
		    }
	    }
        return (String[])vector.toArray(new String[0]);
    }

}
