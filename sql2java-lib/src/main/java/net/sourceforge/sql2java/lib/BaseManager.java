package net.sourceforge.sql2java.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base data access manager.
 */
public abstract class BaseManager<T extends DaoBean> implements DaoManager<T>
{
    private static final Logger log = LoggerFactory.getLogger(BaseManager.class);

    /* set =QUERY for loadUsingTemplate */
    static public final int SEARCH_EXACT = 0;
    /* set %QUERY% for loadLikeTemplate */
    static public final int SEARCH_LIKE = 1;
    /* set %QUERY for loadLikeTemplate */
    static public final int SEARCH_STARTING_LIKE = 2;
    /* set QUERY% for loadLikeTemplate */
    static public final int SEARCH_ENDING_LIKE = 3;

    protected BaseManager(DataSource dataSource) {
	this.dataSource = dataSource;
    }

    protected DataSource dataSource;

    /**
     * Contains all the full fields of the table.
     */
    protected abstract String[] getFullFieldNames();

    /**
     * Contains all the fields of the table.
     */
    protected abstract String[] getFieldNames();

    /**
     * Field that contains the comma separated fields of the table.
     */
    protected abstract String getAllFullFields();

    /**
     * Field that contains the comma separated fields of the table.
     */
    protected abstract String getAllFields();

    /**
     * Loads an object from the table using its key field.
     *
     * @return a unique object
     */
    public T loadByPrimaryKey(Integer primaryKey) throws SQLException {
	throw new UnsupportedOperationException();
    }

    /**
     * Creates a new bean instance.
     *
     * @return the new bean
     */
    public abstract T createBean();

    /**
     * Table managed by this manager
     *
     * @return tablename
     */
    public abstract String getTableName();

    /**
     * Deletes rows according to its keys.
     *
     * @return the number of deleted rows
     */
    public int deleteByPrimaryKey(Integer id) throws SQLException {
	throw new UnsupportedOperationException();
    }

    /**
     * Loads all the rows from table.
     *
     * @return a List of beans
     */
    public List<T> loadAll() throws SQLException {
        return loadUsingTemplate(null);
    }

    /**	
     * Loads the given number of rows from table, given the start row.
     *
     * @param startRow the start row to be used (first row = 1, last row = -1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @return a List of beans
     */
    public List<T> loadAll(int startRow, int numRows) throws SQLException {
        return loadUsingTemplate(null, startRow, numRows);
    }

    /**
     * Retrieves a List of beans given a sql 'where' clause.
     *
     * @param where the sql 'where' clause
     * @return the resulting bean
     */
    public List<T> loadByWhere(String where) throws SQLException {
        return loadByWhere(where, 1, -1);
    }

    /**
     * Retrieves a List of beans given a sql where clause and startRow and numRows.
     * It is up to you to pass the 'WHERE' in your where clausis.
     *
     * @param where the sql 'where' clause
     * @param startRow the start row to be used (first row = 1, last row = -1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @return the resulting bean List
     */
    public List<T> loadByWhere(String where, int startRow, int numRows) throws SQLException {
        StringBuilder sql = new StringBuilder();
	sql.append("SELECT ").append(getAllFields()).append(" FROM ").append(getTableName()).append(" ").append(where);
        Connection c = null;
        Statement st = null;
        ResultSet rs =  null;
        if (log.isTraceEnabled()) log.trace("loadByWhere: {}", sql);
        try {
            c = getConnection();
            st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery(sql.toString());
            return decodeResultSet(rs, startRow, numRows);
        } finally {
            close(st, rs);
            releaseConnection(c);
        }
    }

    /**
     * Retrieves a List of beans given a sql where clause and array of fields values
     *
     * @param where the sql 'where' clause
     * @param fields object array of fields
     * @return the resulting bean List
     */
    public List<T> loadByWhere(String where, Object... fields) throws SQLException {
        StringBuilder sql = new StringBuilder();
	sql.append("SELECT ").append(getAllFields()).append(" FROM ").append(getTableName()).append(" ").append(where);
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs =  null;
        if (log.isTraceEnabled()) log.trace("loadByWhere: {}", sql);
        try {
            c = getConnection();
            ps = c.prepareStatement(sql.toString(),
                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                    ResultSet.CONCUR_READ_ONLY);
	    fillPreparedStatement(ps, fields);
            rs = ps.executeQuery();
            return decodeResultSet(rs, 1, -1);
        } finally {
            close(ps, rs);
            releaseConnection(c);
        }
    }

    /**
     * Retrieves the unique object given a sql 'where' clause.
     *
     * @param where The sql 'where' clause
     * @return The resulting bean
     */
    public T loadUniqueByWhere(String where) throws SQLException {
        List<T> os = loadByWhere(where);
        if (os != null && os.size() > 0) {
            return os.get(0);
	} else {
	    return null;
        }
    }

    /**
     * Retrieves the unique object given a sql 'where' clause and a array of field values
     *
     * @param where The sql 'where' clause
     * @param fields object array of fields
     * @return The resulting bean
     */
    public T loadUniqueByWhere(String where, Object... fields) throws SQLException {
        List<T> os = loadByWhere(where, fields);
        if (os != null && os.size() > 0) {
            return os.get(0);
	} else {
	    return null;
        }
    }

    /**
     * Deletes all rows from table.
     * @return the number of deleted rows.
     */
    public int deleteAll() throws SQLException {
        return deleteByWhere("");
    }

    /**
     * Deletes rows from the table using a 'where' clause.
     * It is up to you to pass the 'WHERE' in your where clausis.
     * <br>Attention, if 'WHERE' is omitted it will delete all records.
     *
     * @param where the sql 'where' clause
     * @return the number of deleted rows
     */
    public int deleteByWhere(String where) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        try {
            c = getConnection();
            StringBuilder sql = new StringBuilder("DELETE FROM ").append(getTableName()).append(" ").append(where);
            if (log.isTraceEnabled()) log.trace("deleteByWhere: {}", sql);
            ps = c.prepareStatement(sql.toString());
            return ps.executeUpdate();
        } finally {
            close(ps);
            releaseConnection(c);
        }
    }

    public int deleteByWhere(String where, Object... fields) throws SQLException {
	StringBuilder sql = new StringBuilder("DELETE FROM ").append(getTableName()).append(" ").append(where);
        Connection c = null;
        PreparedStatement ps = null;
	if (log.isTraceEnabled()) log.trace("deleteByWhere: {}", sql);
        try {
            c = getConnection();
	    ps = c.prepareStatement(sql.toString());
	    fillPreparedStatement(ps, fields);
            return ps.executeUpdate();
        } finally {
            close(ps);
            releaseConnection(c);
        }

    }

    /**
     * Saves the bean into the database.
     *
     * @param bean the bean to be saved
     */
    public T save(T bean) throws SQLException {
        if (bean.isNew())
            return insert(bean);
        else
            return update(bean);
    }

    /**
     * Insert the bean into the database.
     *
     * @param bean the bean to be saved
     */
    public T insert(T bean) throws SQLException {
	return insert(bean, false);
    }

    /**
     * Insert the bean into the database.
     *
     * @param bean the bean to be saved
     * @param orUpdate on duplicate key update
     */
    public T insert(T bean, boolean orUpdate) throws SQLException {
	return insert(bean, orUpdate, false);
    }

    /**
     * Insert the bean into the database.
     *
     * @param bean the bean to be saved
     * @param orUpdate on duplicate key update
     * @param delayed use INSERT DELAYED and don't get generated keys
     */
    public abstract T insert(T bean, boolean orUpdate, boolean delayed) throws SQLException;

    /**
     * Update the bean record in the database according to the changes.
     *
     * @param bean the bean to be updated
     */
    public abstract T update(T bean) throws SQLException;

    /**
     * Saves a List of beans into the database.
     *
     * @param beans to be saved
     * @return the saved bean List.
     */
    public List<T> save(List<T> beans) throws SQLException {
        for (T bean:beans) {
            save(bean);
        }
        return beans;
    }

    /**
     * Insert a List of beans into the database.
     *
     * @param beans to be inserted
     * @return the saved bean List.
     */
    public List<T> insert(List<T> beans) throws SQLException {
        return save(beans);
    }

    /**
     * Updates an List of beans into the database.
     *
     * @param beans to be inserted
     * @return the saved bean List.
     */
    public List<T> update(List<T> beans) throws SQLException {
        return save(beans);
    }

    /**
     * Loads a unique bean from a template one giving a c
     *
     * @param bean the bean to look for
     * @return the bean matching the template
     */
    public T loadUniqueUsingTemplate(T bean) throws SQLException {
         List<T> beans = loadUsingTemplate(bean);
         if (beans.size() == 0)
             return null;
         if (beans.size() > 1)
             throw new SQLException("More than one element !!");
         return beans.get(0);
    }

    /**
     * Loads a List of from a template one.
     *
     * @param bean the template to look for
     * @return all the matching the template
     */
    public List<T> loadUsingTemplate(T bean) throws SQLException {
        return loadUsingTemplate(bean, 1, -1);
    }

    /**
     * Loads a List of from a template one, given the start row and number of rows.
     *
     * @param bean the template to look for
     * @param startRow the start row to be used (first row = 1, last row=-1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @return all the matching the template
     */
    public List<T> loadUsingTemplate(T bean, int startRow, int numRows) throws SQLException {
        return loadUsingTemplate(bean, startRow, numRows, SEARCH_EXACT);
    }

    /**
     * Loads a List of beans from a template one, given the start row and number of rows.
     *
     * @param bean the bean template to look for
     * @param startRow the start row to be used (first row = 1, last row=-1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @param searchType exact ?  like ? starting like ?
     * @return all the beans matching the template
     */
    public List<T> loadUsingTemplate(T bean, int startRow, int numRows, int searchType) throws SQLException {
        if (log.isTraceEnabled()) log.trace("loadUsingTemplate startRow:{}, numRows:{}, searchType:{}", new Object[] { startRow, numRows, searchType });
        Connection c = null;
        PreparedStatement ps = null;
        StringBuilder sql = new StringBuilder("SELECT " + getAllFields() + " FROM " + getTableName() + " ");
        StringBuilder sqlWhere = new StringBuilder("");

        try {
            if (fillWhere(sqlWhere, bean, searchType) == 0) {
                if (log.isTraceEnabled()) log.trace("The bean to look is not initialized... loading all");
            } else {
                sql.append(" WHERE ").append(sqlWhere);
            }
            if (log.isTraceEnabled()) log.trace("loadUsingTemplate: {}", sql);
            c = getConnection();
            int scrollType = ResultSet.TYPE_SCROLL_INSENSITIVE;
            if (startRow != 1) scrollType = ResultSet.TYPE_SCROLL_SENSITIVE;
            ps = c.prepareStatement(sql.toString(),
                                    scrollType,
                                    ResultSet.CONCUR_READ_ONLY);
            fillPreparedStatement(ps, bean, searchType);
            return loadByPreparedStatement(ps, startRow, numRows);
        } finally {
            close(ps);
            releaseConnection(c);
            sql = null;
            sqlWhere = null;
        }
    }

    /**
     * Deletes rows using a bean template.
     *
     * @param bean the bean object(s) to be deleted
     * @return the number of deleted objects
     */
    public abstract int deleteUsingTemplate(T bean) throws SQLException;

    /**
     * Retrieves the number of rows of the table.
     *
     * @return the number of rows returned
     */
    public int countAll() throws SQLException {
        return countWhere("");
    }

    /**
     * Retrieves the number of rows of the table with a 'where' clause.
     * It is up to you to pass the 'WHERE' in your where clause.
     *
     * @param where the restriction clause
     * @return the number of rows returned
     */
    public int countWhere(String where) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS MCOUNT FROM ").append(getTableName()).append(" ").append(where);
        if (log.isTraceEnabled()) log.trace("countWhere: {}", sql);
        Connection c = null;
        Statement st = null;
        ResultSet rs =  null;
        try {
            int iReturn = -1;
            c = getConnection();
            st = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs =  st.executeQuery(sql.toString());
            if (rs.next()) {
                iReturn = rs.getInt("MCOUNT");
            }
            if (iReturn != -1) return iReturn;
        } finally {
            close(st, rs);
            releaseConnection(c);
        }
        throw new SQLException("Error in countWhere");
    }

    /**
     * Retrieves the number of rows of the table with a 'where' clause and a fields array.
     *
     * @param where the restriction clause
     * @param fields object array of fields
     * @return the number of rows returned
     */
    public int countWhere(String where, Object... fields) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS MCOUNT FROM ").append(getTableName()).append(" ").append(where);
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs =  null;
        if (log.isTraceEnabled()) log.trace("countWhere: {}", sql);
        try {
            int iReturn = -1;
            c = getConnection();
            ps = c.prepareStatement(sql.toString());
	    fillPreparedStatement(ps, fields);
            rs =  ps.executeQuery();
            if (rs.next()) {
                iReturn = rs.getInt("MCOUNT");
            }
            if (iReturn != -1) return iReturn;
        } finally {
            close(ps, rs);
            releaseConnection(c);
        }
        throw new SQLException("Error in countWhere");
    }

    /**
     * Count the number of elements of a specific bean
     *
     * @param bean the bean to look for ant count
     * @return the number of rows returned
     */
    public int countUsingTemplate(T bean) throws SQLException {
        return countUsingTemplate(bean, -1, -1);
    }

    /**
     * Count the number of elements of a specific bean, given the start row and number of rows.
     *
     * @param bean the template to look for and count
     * @param startRow the start row to be used (first row = 1, last row=-1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @return the number of rows returned
     */
    public int countUsingTemplate(T bean, int startRow, int numRows) throws SQLException {
        return countUsingTemplate(bean, startRow, numRows, SEARCH_EXACT);
    }

    /**
     * Count the number of elements of a specific bean given the start row and number of rows and the search type
     *
     * @param bean the template to look for
     * @param startRow the start row to be used (first row = 1, last row=-1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @param searchType exact ?  like ? starting like ?
     * @return the number of rows returned
     */
    public int countUsingTemplate(T bean, int startRow, int numRows, int searchType) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS MCOUNT FROM " + getTableName());
        StringBuilder sqlWhere = new StringBuilder("");
        try {
            if (fillWhere(sqlWhere, bean, SEARCH_EXACT) == 0) {
                if (log.isTraceEnabled()) log.trace("The bean to look is not initialized... counting all...");
            } else {
                sql.append(" WHERE ").append(sqlWhere);
            }
            if (log.isTraceEnabled()) log.trace("countUsingTemplate: {}", sql);
            c = getConnection();
            ps = c.prepareStatement(sql.toString(),
                                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                                    ResultSet.CONCUR_READ_ONLY);
            fillPreparedStatement(ps, bean, searchType);
            return countByPreparedStatement(ps);
        } finally {
            close(ps);
            releaseConnection(c);
            sql = null;
            sqlWhere = null;
        }
    }

    /**
     * Retrieves the number of rows of the table with a prepared statement.
     *
     * @param ps the PreparedStatement to be used
     * @return the number of rows returned
     */
    protected int countByPreparedStatement(PreparedStatement ps) throws SQLException {
        ResultSet rs =  null;
        try {
            int iReturn = -1;
            rs = ps.executeQuery();
            if (rs.next()) iReturn = rs.getInt("MCOUNT");
            if (iReturn != -1) return iReturn;
        } finally {
            close(rs);
        }
        throw new SQLException("Error in countByPreparedStatement");
    }

    /**
     * Decode a ResultSet in a List of objects
     *
     * @param rs the resultset to decode
     * @param startRow the start row to be used (first row = 1, last row = -1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @return the resulting bean List
     */
    public List<T> decodeResultSet(ResultSet rs, int startRow, int numRows) throws SQLException {
        List<T> v = new ArrayList();
        int count = 0;
	if (rs.absolute(startRow) && numRows!=0) {
	    do {
		v.add(decodeRow(rs));
		count++;
	    } while ( (count<numRows||numRows<0) && rs.next() );
	}
	return Collections.unmodifiableList(v);
    }

    /**
     * Transforms a ResultSet iterating on a bean.
     *
     * @param rs the ResultSet to be transformed
     * @return bean resulting bean
     */
    public abstract T decodeRow(ResultSet rs) throws SQLException;

    /**
     * Transforms a ResultSet iterating on a bean using the names of the columns
     *
     * @param rs the ResultSet to be transformed
     * @return bean resulting bean
     */
    public abstract T metaDataDecodeRow(ResultSet rs) throws SQLException;

    /**
     * Loads all the elements using a prepared statement.
     *
     * @param ps the PreparedStatement to be used
     * @return a List of beans
     */
    public List<T> loadByPreparedStatement(PreparedStatement ps) throws SQLException {
        return loadByPreparedStatement(ps, 1, -1);
    }

    /**
     * Loads all the elements using a prepared statement specifying the start row and the number of rows.
     *
     * @param ps the PreparedStatement to be used
     * @param startRow the start row to be used (first row = 1, last row = -1)
     * @param numRows the number of rows to be retrieved (all rows = a negative number)
     * @return a List of beans
     */
    public List<T> loadByPreparedStatement(PreparedStatement ps, int startRow, int numRows) throws SQLException {
        ResultSet rs =  null;
        try {
            rs = ps.executeQuery();
            return decodeResultSet(rs, startRow, numRows);
        } finally {
            close(rs);
        }
    }

    /**
     * Fills the given StringBuilder with the sql where clausis constructed using the bean and the search type
     * @param sqlWhere the stringbuffer that will be filled
     * @param bean the bean to use for creating the where clausis
     * @param searchType exact ?  like ? starting like ?
     * @return the number of clause is returned
     */
    protected abstract int fillWhere(StringBuilder sqlWhere, T bean, int searchType) throws SQLException;

    /**
     * Fill the given PreparedStatement with the bean values and a search type
     * @param ps the preparedStatement that will be filled
     * @param bean the bean to use for creating the where clausis
     * @param searchType exact ?  like ? starting like ?
     * @return the number of clause is returned
     */
    protected abstract int fillPreparedStatement(PreparedStatement ps, T bean, int searchType) throws SQLException;

    /**
     * Fill the given PreparedStatement with the given fields
     * @param ps the preparedStatement that will be filled
     * @param fields the Object array of fields to be filled
     * @return the number of fields filled
     */
    protected int fillPreparedStatement(PreparedStatement ps, Object... fields) throws SQLException {
	for (int i=0;i<fields.length;i++) {
	    if (fields[i] instanceof Boolean) setBoolean(ps, i+1, (Boolean)fields[i]);
	    else if (fields[i] instanceof Double) setDouble(ps, i+1, (Double)fields[i]);
	    else if (fields[i] instanceof Float) setFloat(ps, i+1, (Float)fields[i]);
	    else if (fields[i] instanceof Integer) setInteger(ps, i+1, (Integer)fields[i]);
	    else if (fields[i] instanceof Long) setLong(ps, i+1, (Long)fields[i]);
	    else if (fields[i] instanceof BigDecimal) ps.setBigDecimal(i+1, (BigDecimal)fields[i]);
	    else if (fields[i] instanceof String) ps.setString(i+1, (String)fields[i]);
	    else if (fields[i] instanceof java.sql.Date) ps.setDate(i+1, (java.sql.Date)fields[i]);
	    else if (fields[i] instanceof java.sql.Timestamp) ps.setTimestamp(i+1, (java.sql.Timestamp)fields[i]);
	    else ps.setObject(i+1, fields[i]); // Try setObject as a catch-all
	}
	return fields.length;
    }

    /////////////
    // UTILITIES
    /////////////

    /**
     * @return an auto commit connection
     */
    public Connection getConnection() throws SQLException {
	Txn transaction = Database.currentTransaction();
	if (transaction == null) {
	    return dataSource.getConnection();
	} else {
	    try {
		transaction.setDataSource(this.dataSource);
	    } catch (Exception ignore) {}
	    return transaction.getConnection();
	}
    }

    /**
     * Releases the database connection.
     */
    public void releaseConnection(Connection c)
    {
	if (c != null) {
	    Txn transaction = Database.currentTransaction();
	    if (transaction == null) {
		try {
		    c.close();
		} catch (Exception e) {
		    log.warn("Error releasing connection", e);
		}
	    }
	}
    }

    /**
     * Closes the passed Statement.
     */
    public void close(Statement s)
    {
        try {
            if (s != null) s.close();
        } catch (SQLException x) {
            log.warn("Could not close statement", x);
        }
    }

    /**
     * Closes the passed ResultSet.
     */
    public void close(ResultSet rs)
    {
        try {
            if (rs != null) rs.close();
        } catch (SQLException x) {
            log.warn("Could not close result set", x);
        }
    }

    /**
     * Closes the passed Statement and ResultSet.
     */
    public void close(Statement s, ResultSet rs)
    {
        close(rs);
        close(s);
    }

    ////////////////////////////////////////////////////
    // Helper methods for fetching numbers using IDs or names
    ////////////////////////////////////////////////////

    //Date/Timestamp
    //Byte
    //Blob/Clob

    /**
     * Retrieves an int value from the passed result set as an Integer object.
     */
    public static Integer getInteger(ResultSet rs, int pos) throws SQLException {
        int i = rs.getInt(pos);
        return rs.wasNull() ? (Integer)null : new Integer(i);
    }

    /**
     * Retrieves an int value from the passed result set as an Integer object.
     */
    public static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int i = rs.getInt(column);
        return rs.wasNull() ? (Integer)null : new Integer(i);
    }

    /**
     * Set an Integer object to the passed prepared statement as an int or as null.
     */
    public static void setInteger(PreparedStatement ps, int pos, Integer i) throws SQLException {
        if (i==null) ps.setNull(pos, Types.INTEGER);
        else ps.setInt(pos, i.intValue());
    }

    /**
     * Retrieves a float value from the passed result set as a Float object.
     */
    public static Float getFloat(ResultSet rs, int pos) throws SQLException {
        float f = rs.getFloat(pos);
        return rs.wasNull() ? (Float)null : new Float(f);
    }

    /**
     * Retrieves a float value from the passed result set as a Float object.
     */
    public static Float getFloat(ResultSet rs, String column) throws SQLException {
        float f = rs.getFloat(column);
        return rs.wasNull() ? (Float)null : new Float(f);
    }

    /**
     * Set a Float object to the passed prepared statement as a float or as null.
     */
    public static void  setFloat(PreparedStatement ps, int pos, Float f) throws SQLException {
        if (f==null) ps.setNull(pos, Types.FLOAT);
        else ps.setFloat(pos, f.floatValue());
    }

    /**
     * Retrieves a double value from the passed result set as a Double object.
     */
    public static Double getDouble(ResultSet rs, int pos) throws SQLException {
        double d = rs.getDouble(pos);
        return rs.wasNull() ? (Double)null : new Double(d);
    }

    /**
     * Retrieves a double value from the passed result set as a Double object.
     */
    public static Double getDouble(ResultSet rs, String column) throws SQLException {
        double d = rs.getDouble(column);
        return rs.wasNull() ? (Double)null : new Double(d);
    }

    /**
     * Set a Double object to the passed prepared statement as a double or as null.
     */
    public static void setDouble(PreparedStatement ps, int pos, Double d) throws SQLException {
        if (d==null) ps.setNull(pos, Types.DOUBLE);
        else ps.setDouble(pos, d.doubleValue());
    }

    /**
     * Retrieves a long value from the passed result set as a Long object.
     */
    public static Long getLong(ResultSet rs, int pos) throws SQLException {
        long l = rs.getLong(pos);
        return rs.wasNull() ? (Long)null : new Long(l);
    }

    /**
     * Retrieves a long value from the passed result set as a Long object.
     */
    public static Long getLong(ResultSet rs, String column) throws SQLException {
        long l = rs.getLong(column);
        return rs.wasNull() ? (Long)null : new Long(l);
    }

    /**
     * Set a Long object to the passed prepared statement as a long or as null.
     */
    public static void setLong(PreparedStatement ps, int pos, Long l) throws SQLException {
        if (l==null) ps.setNull(pos, Types.BIGINT);
        else ps.setLong(pos, l.longValue());
    }

    /**
     * Retrieves a boolean value from the passed result set as a Boolean object.
     */
    public static Boolean getBoolean(ResultSet rs, int pos) throws SQLException {
        boolean b = rs.getBoolean(pos);
        return rs.wasNull() ? (Boolean)null : new Boolean(b);
    }

    /**
     * Retrieves a boolean value from the passed result set as a Boolean object.
     */
    public static Boolean getBoolean(ResultSet rs, String column) throws SQLException {
        boolean b = rs.getBoolean(column);
        return rs.wasNull() ? (Boolean)null : new Boolean(b);
    }

    /**
     * Set a Boolean object to the passed prepared statement as a boolean or as null.
     */
    public static void setBoolean(PreparedStatement ps, int pos, Boolean b) throws SQLException {
        if (b==null) ps.setNull(pos, Types.BOOLEAN);
	else ps.setBoolean(pos, b.booleanValue());
    }

    /**
     * Retrieves a clob value from the passed result set as a String object.
     */
    protected String getStringFromClob(ResultSet rs, int pos) throws SQLException {
	return getStringFromClob(rs, getFieldNames()[pos]);
    }
    
    /**
     * Retrieves a clob value from the passed result set as a String object.
     */
    public static String getStringFromClob(ResultSet rs, String column) throws SQLException {
	Clob c = rs.getClob(column);
	StringBuilder sb = new StringBuilder();
	BufferedReader br = null;
	try {
	    br = new BufferedReader(c.getCharacterStream());
	    int b;
	    while (-1 != (b = br.read())) {
		sb.append((char)b);
	    }
	    return sb.toString();
	} catch (IOException e) {
	    log.warn("Could not convert CLOB to String", e);
	    throw new SQLException(e);
	} finally {
	    if (br != null) {
		try {
		    br.close();
		} catch (Exception ignore) {}
	    }
	}
    }

    /**
     * Set a String object to the passed prepared statement as a clob or as null.
     */
    public static void setClob(PreparedStatement ps, int pos, String s) throws SQLException {
	if (s == null) ps.setNull(pos, Types.CLOB);
	else ps.setClob(pos, new StringReader(s), s.length());
    }
    
    ////////////////////////////////////////////////////
    // Date helper methods
    ////////////////////////////////////////////////////
    
    /**
     * pattern for received date processing.
     */
    private static final String[] patterns = new String[]
    {
        "EEE, dd MMM yyyy HH:mm:ss '-'S '('z')'",
        "EEE, dd MMM yyyy HH:mm:ss '+'S '('z')'",
        "EEE, dd MMM yyyy HH:mm:ss '-'S",
        "EEE, dd MMM yyyy HH:mm:ss '+'S",
        "EEE, dd MMM yyyy HH:mm:ss z",
        "EEE, dd MMM yyyy HH:mm:ss Z",
        "EEE, dd MMM yyyy HH:mm:ss",
        "EEE, d MMM yyyy HH:mm:ss '-'S '('z')'",
        "EEE, d MMM yyyy HH:mm:ss '+'S '('z')'",
        "EEE, d MMM yyyy HH:mm:ss '-'S",
        "EEE, d MMM yyyy HH:mm:ss '+'S",
        "EEE, d MMM yyyy HH:mm:ss z",
        "EEE, d MMM yyyy HH:mm:ss Z",
        "EEE, d MMM yyyy HH:mm:ss",

        "EEE, dd MMM yy HH:mm:ss '-'S '('z')'",
        "EEE, dd MMM yy HH:mm:ss '+'S '('z')'",
        "EEE, dd MMM yy HH:mm:ss '-'S",
        "EEE, dd MMM yy HH:mm:ss '+'S",
        "EEE, dd MMM yy HH:mm:ss z",
        "EEE, dd MMM yy HH:mm:ss Z",
        "EEE, dd MMM yy HH:mm:ss",
        "EEE, d MMM yy HH:mm:ss '-'S '('z')'",
        "EEE, d MMM yy HH:mm:ss '+'S '('z')'",
        "EEE, d MMM yy HH:mm:ss '-'S",
        "EEE, d MMM yy HH:mm:ss '+'S",
        "EEE, d MMM yy HH:mm:ss z",
        "EEE, d MMM yy HH:mm:ss Z",
        "EEE, d MMM yy HH:mm:ss",

        "dd MMM yyyy HH:mm:ss '-'S",
        "dd MMM yyyy HH:mm:ss '+'S",
        "dd MMM yyyy HH:mm:ss '-'S '('z')'",
        "dd MMM yyyy HH:mm:ss '+'S '('z')'",
        "dd MMM yyyy HH:mm:ss z",
        "dd MMM yyyy HH:mm:ss Z",
        "dd MMM yyyy HH:mm:ss",

        "dd MMM yyy HH:mm:ss '-'S",
        "dd MMM yyy HH:mm:ss '+'S",
        "dd MMM yyy HH:mm:ss '-'S '('z')'",
        "dd MMM yyy HH:mm:ss '+'S '('z')'",
        "dd MMM yyy HH:mm:ss z",
        "dd MMM yyy HH:mm:ss Z",
        "dd MMM yyy HH:mm:ss",

        "yyyy.MM.dd HH:mm:ss z",
        "yyyy.MM.dd HH:mm:ss Z",
        "yyyy.MM.d HH:mm:ss z",
        "yyyy.MM.d HH:mm:ss Z",
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.d HH:mm:ss",

        "yy.MM.dd HH:mm:ss z",
        "yy.MM.dd HH:mm:ss Z",
        "yy.MM.d HH:mm:ss z",
        "yy.MM.d HH:mm:ss Z",
        "yy.MM.dd HH:mm:ss",
        "yy.MM.d HH:mm:ss",

        "yyyy MM dd HH:mm:ss",
        "yyyy MM d HH:mm:ss",
        "yyyy MM dd HH:mm:ss z",
        "yyyy MM dd HH:mm:ss Z",
        "yyyy MM d HH:mm:ss z",
        "yyyy MM d HH:mm:ss Z",

        "yy MM dd HH:mm:ss",
        "yy MM d HH:mm:ss",
        "yy MM dd HH:mm:ss z",
        "yy MM dd HH:mm:ss Z",
        "yy MM d HH:mm:ss z",
        "yy MM d HH:mm:ss Z",

        "yyyy-MM-dd HH:mm:ss z",
        "yyyy-MM-dd HH:mm:ss Z",
        "yyyy-MM-d HH:mm:ss z",
        "yyyy-MM-d HH:mm:ss Z",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-d HH:mm:ss",

        "yy-MM-dd HH:mm:ss z",
        "yy-MM-dd HH:mm:ss Z",
        "yy-MM-d HH:mm:ss z",
        "yy-MM-d HH:mm:ss Z",
        "yy-MM-dd HH:mm:ss",
        "yy-MM-d HH:mm:ss",

        "dd MMM yyyy",
        "d MMM yyyy",

        "dd.MMM.yyyy",
        "d.MMM.yyyy",

        "dd-MMM-yyyy",
        "d-MMM-yyyy",

        "dd MM yyyy",
        "d MM yyyy",

        "dd.MM.yyyy",
        "d.MM.yyyy",

        "dd-MM-yyyy",
        "d-MM-yyyy",

        "yyyy MM dd",
        "yyyy MM d",

        "yyyy.MM.dd",
        "yyyy.MM.d",

        "yyyy-MM-dd",
        "yyyy-MM-d",

        "dd MMM yy",
        "d MMM yy",

        "dd.MMM.yy",
        "d.MMM.yy",

        "dd-MMM-yy",
        "d-MMM-yy",

        "dd MM yy",
        "d MM yy",

        "dd.MM.yy",
        "d.MM.yy",

        "dd-MM-yy",
        "d-MM-yy",

        "yy MMM dd",
        "yy MMM d",

        "yy.MMM.dd",
        "yy.MMM.d",

        "yy-MMM-dd",
        "yy-MMM-d",

        "yy MMM dd",
        "yy MMM d",

        "yy.MMM.dd",
        "yy.MMM.d",

        "yy-MMM-dd",
        "yy-MMM-d",

        "EEE dd, MMM yyyy", // ex: Wed 19, Feb 2003

        "EEE dd, MMM yy" // ex: Wed 19, Feb 03
    };

    /**
     * get a date from a date string representation in one of the registered formats
     * @param strDate the date as string. If (null or empty) or correct pattern was not found
     * @return Date object
     */
    static public java.util.Date getDateFromString(String strDate)
    {
        if (strDate != null) strDate = strDate.trim();

        SimpleDateFormat pSimpleDateFormat = new SimpleDateFormat("");
        java.util.Date dReceivedDate = Calendar.getInstance().getTime();

        if (strDate != null && "".equals(strDate) == false) {
            for (int i=0; i<patterns.length; i++) {
                try {
                    pSimpleDateFormat.applyPattern(patterns[i]);
                    dReceivedDate = pSimpleDateFormat.parse(strDate);
                    if (dReceivedDate == null) continue;
                    return dReceivedDate;
                } catch (ParseException pe) {
                    // ignore this format try the next one
                }
            }
        }
        return dReceivedDate;
    }

    /**
     * Verify that the string represantes the date with one of the registered formats
     * @param strDate the date as string.
     * @return boolean "true" if the string represantes the date in one of the registed formats.
     */
    static public boolean isDate(String strDate)
    {
        if (strDate != null) strDate = strDate.trim();
        SimpleDateFormat pSimpleDateFormat = new SimpleDateFormat("");
        if (strDate != null && "".equals(strDate) == false) {
            for (int i=0; i<patterns.length; i++) {
                try {
                    pSimpleDateFormat.applyPattern(patterns[i]);
                    java.util.Date dReceivedDate = pSimpleDateFormat.parse(strDate);
                    if (dReceivedDate == null) continue;
                    return true;
                } catch (ParseException pe) {
                    // ignore as it is reported below
                }
            }
        }
        return false;
    }
}
