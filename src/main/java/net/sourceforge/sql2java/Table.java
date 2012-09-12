//$Id: Table.java,v 1.1 2005/10/12 18:44:24 framiere Exp $

package net.sourceforge.sql2java;

import java.util.*;

public class Table
{

    private Hashtable colHash = new Hashtable();
    private Vector cols = new Vector();
    private Vector priKey = new Vector();
    private Hashtable manyToManyHash = new Hashtable();
    private String catalog, schema, name, type, remarks;
    private Database db;

    private Vector foreignKeys = new Vector();
    private Vector importedKeys = new Vector();

    public boolean isRelationTable()
    {
        return importedKeys.size() > 1;
    }

    /**
     * Tells whether if one of this table's columns (imported key)
     * points to one of the otherTable's pk.
     */
    public boolean relationConnectsTo(Table otherTable)
    {
        if (this.equals(otherTable))
        {
            return false;
        }

        for (int i = 0; i < foreignKeys.size(); i++)
        {
            Column c = (Column) importedKeys.get(i);
            if (c.getTableName().equals(otherTable.getName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return, beside the passed table, the tables this table points to.
     */
    public Table[] linkedTables(Database pDatabase, Table pTable)
    {
        Vector vector = new Vector();

        for (int iIndex = 0; iIndex < importedKeys.size(); iIndex++)
        {
            Column pColumn = (Column) importedKeys.get(iIndex);
            if (pColumn.getTableName().equals(pTable.getName()) == false)
            {
                Table pTableToAdd = pDatabase.getTable(pColumn.getTableName());
                if (vector.contains(pTableToAdd) == false)
                    vector.add(pTableToAdd);
            }
        }
        return (Table[])vector.toArray(new Table[0]);
    }

    /**
     * Return the imported key pointing to the passed table.
     */
    public Column getForeignKeyFor(Table pTable)
    {
        Vector vector = new Vector();
        for (int iIndex = 0; iIndex < importedKeys.size(); iIndex++)
        {
            Column pColumn = (Column) importedKeys.get(iIndex);
            if (pColumn.getTableName().equals(pTable.getName()))
                return pColumn;
        }
        return null;
    }

    public void setDatabase(Database db)
    {
        this.db = db;
    }
    public void setCatalog(String catalog)
    {
        this.catalog = catalog;
    }
    public void setSchema(String schema)
    {
        this.schema = schema;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setType(String type)
    {
        this.type = type;
    }
    public void setRemarks(String remarks)
    {
        if (remarks!=null) {
            this.remarks = remarks.replaceAll("/\\*", "SLASH*").replaceAll("\\*/", "*SLASH");
        }
//        else setRemarks("No remarks in this table /* escape me */ /** me too ****/ / * not me * /");
    }

    public String getCatalog()
    {
        return catalog;
    }
    public String getSchema()
    {
        return schema;
    }
    public String getName()
    {
        return name;
    }
    public String getType()
    {
        return type;
    }
    public String getRemarks()
    {
        return remarks==null?"":remarks;
    }

    public Column[] getColumns()
    {
        return (Column[])cols.toArray(new Column[0]);
    }

    public Column getColumn(String name)
    {
        return (Column) colHash.get(name.toLowerCase());
    }

    public void addColumn(Column column)
    {
        colHash.put(column.getName().toLowerCase(), column);
        cols.addElement(column);
    }

    public void removeColumn(Column column)
    {
        cols.removeElement(column);
        colHash.remove(column.getName().toLowerCase());
    }

    public Column[] getPrimaryKeys()
    {
        return (Column[])priKey.toArray(new Column[0]);
    }

    public void addPrimaryKey(Column column)
    {
        priKey.addElement(column);
        column.isPrimaryKey(true);
    }

    public Column[] getImportedKeys()
    {
        return (Column[])importedKeys.toArray(new Column[0]);
    }

    public void addImportedKey(Column column)
    {
        if (importedKeys.contains(column) == false)
            importedKeys.addElement(column);
    }

    /**
     * Returns a 2-D array of the keys in this table that form a many
     * to many relationship.
     * <br>
     * The size of the first dimension is based on the number of
     * unique tables that are being managed. The second dimension
     * is always 2 elements. The first element is the column in the
     * relationship table itself. The second column is the primary
     * key column in the target table.
     */
    public Column[][] getManyToManyKeys()
    {

        //         // it matters only if we have 2 entries.
        //         if (manyToManyHash.size()<=1) {
        //             return new Column[0][0];
        //         }

        Column list[][] = new Column[manyToManyHash.size()][2];
        int i = 0;
        for (Enumeration e = manyToManyHash.keys(); e.hasMoreElements(); i++)
        {
            Column fk = (Column) e.nextElement();
            Column pk = (Column) manyToManyHash.get(fk);
            list[i][0] = fk;
            list[i][1] = pk;
        }
        return list;
    }

    public void addManyToManyKey(Column fk, Column pk)
    {
        manyToManyHash.put(fk, pk);
    }

    public int countColumns()
    {
        return cols.size();
    }

    public int countPrimaryKeys()
    {
        return priKey.size();
    }

    public boolean hasPrimaryKey()
    {
        return countPrimaryKeys() > 0;
    }

    public int countImportedKeys()
    {
        return importedKeys.size();
    }

    public boolean hasImportedKeys()
    {
        return countImportedKeys() > 0;
    }

    public int countForeignKeys()
    {
        return foreignKeys.size();
    }

    public boolean hasForeignKeys()
    {
        return countForeignKeys() > 0;
    }

    public void addForeignKey(Column col)
    {
        if (foreignKeys.contains(col) == false)
            foreignKeys.add(col);
    }

    public Column[] getForeignKeys()
    {
        return (Column[])foreignKeys.toArray(new Column[0]);
    }

    public boolean isForeignKey(Column col)
    {
        return foreignKeys.contains(col);
    }

    public Table[] getLinkedTables()
    {
        Vector vector = new Vector();

        for (int iIndex = 0; iIndex < importedKeys.size(); iIndex++)
        {
            Column column = (Column) importedKeys.get(iIndex);
            if (column.getTableName().equals(getName()) == false)
            {
                Table pTableToAdd = column.getTable();
                if (vector.contains(pTableToAdd) == false)
                    vector.add(pTableToAdd);
            }
        }

        for (int iIndex = 0; iIndex < foreignKeys.size(); iIndex++)
        {
            Column column = (Column) foreignKeys.get(iIndex);
            column = column.getForeignColumn();
            if (column.getTableName().equals(getName()) == false)
            {
                Table pTableToAdd = column.getTable();
                if (vector.contains(pTableToAdd) == false)
                    vector.add(pTableToAdd);
            }
        }
        return (Table[])vector.toArray(new Table[0]);
    }

    private Table[] getImportedTablesFromVector(Vector keys)
    {
        Vector vector = new Vector();
        for (int iIndex = 0; iIndex < keys.size(); iIndex++)
        {
            Column column = (Column) keys.get(iIndex);
            if (column.getTableName().equals(getName()) == false)
            {
                Table pTableToAdd = column.getTable();
                if (vector.contains(pTableToAdd) == false)
                    vector.add(pTableToAdd);
            }
        }
        return (Table[])vector.toArray(new Table[0]);
    }

    public Table[] getImportedTables()
    {
        return getImportedTablesFromVector(importedKeys);
    }

    public Table[] getForeignTables()
    {
        return getImportedTablesFromVector(foreignKeys);
    }

    public String[] getLinkedPackages()
    {
        Vector vector = new Vector();
        Table[] linkedTables = getLinkedTables();
        for (int iIndex = 0; iIndex < linkedTables.length; iIndex++)
        {
            if (vector.contains(linkedTables[iIndex].getPackage()) == false)
                vector.add(linkedTables[iIndex].getPackage());
        }
        return (String[])vector.toArray(new String[0]);
    }

    public String getPackage()
    {
        String basePackage = CodeWriter.getProperty("mgrwriter.package");
        // iterate in the subpackage.X.names
        // starting at 1
        int iterating = 1;
        while(true)
        {
            String tablesProperty = "subpackage." + iterating + ".tables";
            String packageNameProperty = "subpackage." + iterating + ".name";
            String tables[] = CodeWriter.getPropertyExploded(tablesProperty);
            for (int i = 0; i < tables.length; i ++)
            {
                if (getName().equalsIgnoreCase(tables[i]))
                {
                    String packageName = CodeWriter.getProperty(packageNameProperty);
                    if (packageName == null)
                        return basePackage;
                    return basePackage + "." + packageName;
                }
            }
            iterating ++;

            // no tables found ?
            // ok stop iterating
            if (tables.length == 0)
                break;
        }
        return basePackage;
    }

    public String getPackagePath()
    {
        return getPackage().replace('.', '/');
    }

    public Column[] getColumnsFor(String type)
    {
        Vector vector = new Vector();
        for (int i = 0; i < cols.size(); i++)
        {
            Column c = (Column)cols.get(i);
            if (c.columnFor(type))
                vector.add(c);
        }
        return (Column[])vector.toArray(new Column[0]);
    }

}
