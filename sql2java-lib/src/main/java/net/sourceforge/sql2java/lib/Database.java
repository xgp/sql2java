package net.sourceforge.sql2java.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Database {

    private static final Logger log = LoggerFactory.getLogger(Database.class);

    protected Database(DataSource dataSource) {
	this.dataSource = dataSource;
	this.classMap = new HashMap<Class,DaoManager>();
    }

    private final DataSource dataSource;
    private final Map<Class,DaoManager> classMap;

    protected void registerManager(DaoManager manager) {
	Class beanClass = manager.createBean().getClass();
	log.trace("Added mapping for {} : {}", beanClass.getName(), manager.getClass().getName());
	classMap.put(beanClass, manager);
    }

    public <T extends DaoBean> DaoManager<T> managerForClass(Class<T> type) { 
	return (DaoManager<T>)getManagerForClass(type); 
    }

    public DaoManager getManagerForClass(Class type) {
     	DaoManager daoManager = classMap.get(type);
     	if (daoManager == null) throw new IllegalArgumentException("No DaoManager found for "+type.getClass().getName());
    	return daoManager;
    }

    // DAO conveniences

    public <T extends DaoBean> T createBean(Class<T> type) {
	return (T)getManagerForClass(type).createBean();
    }

    public String getTableName(Class<?> type) {
	return getManagerForClass(type).getTableName();
    }

    public <T extends DaoBean> T loadByPrimaryKey(Class<T> type, Integer id) throws SQLException {
	return (T)getManagerForClass(type).loadByPrimaryKey(id);
    }

    public int deleteByPrimaryKey(Class<?> type, Integer id) throws SQLException {
	return getManagerForClass(type).deleteByPrimaryKey(id);
    }

    public <T extends DaoBean> List<T> loadAll(Class<T> type) throws SQLException {
	return (List<T>)getManagerForClass(type).loadAll();
    }

    public <T extends DaoBean> List<T> loadAll(Class<T> type,int startRow, int numRows) throws SQLException {
	return (List<T>)getManagerForClass(type).loadAll(startRow, numRows);
    }

    public <T extends DaoBean> List<T> loadByWhere(Class<T> type, String where) throws SQLException {
	return (List<T>)getManagerForClass(type).loadByWhere(where);
    }

    public <T extends DaoBean> List<T> loadByWhere(Class<T> type, String where, Object... fields) throws SQLException {
	return (List<T>)getManagerForClass(type).loadByWhere(where, fields);
    }

    public <T extends DaoBean> List<T> loadByWhere(Class<T> type, String where, int startRow, int numRows) throws SQLException {
	return (List<T>)getManagerForClass(type).loadByWhere(where, startRow, numRows);
    }

    public <T extends DaoBean> T loadUniqueByWhere(Class<T> type, String where) throws SQLException {
	return (T)getManagerForClass(type).loadUniqueByWhere(where);
    }

    public <T extends DaoBean> T loadUniqueByWhere(Class<T> type, String where, Object... fields) throws SQLException {
	return (T)getManagerForClass(type).loadUniqueByWhere(where, fields);
    }

    public int deleteAll(Class<?> type) throws SQLException {
	return getManagerForClass(type).deleteAll();
    }

    public int deleteByWhere(Class<?> type, String where) throws SQLException {
	return getManagerForClass(type).deleteByWhere(where);
    }

    public int deleteByWhere(Class<?> type, String where, Object... fields) throws SQLException {
	return getManagerForClass(type).deleteByWhere(where, fields);
    }

    public <T extends DaoBean> T save(T bean) throws SQLException {
	return (T)getManagerForClass(bean.getClass()).save(bean);
    }

    public <T extends DaoBean> T insert(T bean) throws SQLException {
	return (T)getManagerForClass(bean.getClass()).insert(bean);
    }

    public <T extends DaoBean> T insert(T bean, boolean orUpdate) throws SQLException {
	return (T)getManagerForClass(bean.getClass()).insert(bean, orUpdate);
    }

    public <T extends DaoBean> T insert(T bean, boolean orUpdate, boolean delayed) throws SQLException {
	return (T)getManagerForClass(bean.getClass()).insert(bean, orUpdate, delayed);
    }

    public <T extends DaoBean> T update(T bean) throws SQLException {
	return (T)getManagerForClass(bean.getClass()).update(bean);
    }

    public <T extends DaoBean> List<T> save(List<T> beans) throws SQLException {
	if (beans == null || beans.size() < 1) return beans;
	Class type = beans.get(0).getClass();
	return (List<T>)getManagerForClass(type).save(beans);
    }

    public <T extends DaoBean> List<T> insert(List<T> beans) throws SQLException {
	if (beans == null || beans.size() < 1) return beans;
	Class type = beans.get(0).getClass();
	return (List<T>)getManagerForClass(type).insert(beans);
    }

    public <T extends DaoBean> List<T> update(List<T> beans) throws SQLException {
	if (beans == null || beans.size() < 1) return beans;
	Class type = beans.get(0).getClass();
	return (List<T>)getManagerForClass(type).update(beans);
    }

    public <T extends DaoBean> T loadUniqueUsingTemplate(T bean) throws SQLException {
	return (T)getManagerForClass(bean.getClass()).loadUniqueUsingTemplate(bean);
    }

    public <T extends DaoBean> List<T> loadUsingTemplate(Class<T> type, T bean) throws SQLException {
	return (List<T>)getManagerForClass(type).loadUsingTemplate(bean);
    }

    public <T extends DaoBean> List<T> loadUsingTemplate(T bean, int startRow, int numRows) throws SQLException {
	return (List<T>)getManagerForClass(bean.getClass()).loadUsingTemplate(bean, startRow, numRows);
    }

    public <T extends DaoBean> List<T> loadUsingTemplate(T bean, int startRow, int numRows, int searchType) throws SQLException {
	return (List<T>)getManagerForClass(bean.getClass()).loadUsingTemplate(bean, startRow, numRows, searchType);
    }

    public <T extends DaoBean> boolean delete(T bean) throws SQLException {
	if (bean instanceof DaoBean) {
	    if (((DaoBean)bean).isNew()) return false;
	    else return (getManagerForClass(bean.getClass()).deleteUsingTemplate(bean) > 0);
	} else {
	    throw new IllegalArgumentException("Not a DaoBean "+bean.getClass().getName());
	}
    }

    public <T extends DaoBean> int deleteUsingTemplate(T bean) throws SQLException {
	return getManagerForClass(bean.getClass()).deleteUsingTemplate(bean);
    }

    public int countAll(Class<?> type) throws SQLException {
	return getManagerForClass(type).countAll();
    }

    public int countWhere(Class<?> type, String where) throws SQLException {
	return getManagerForClass(type).countWhere(where);
    }

    public int countWhere(Class<?> type, String where, Object... fields) throws SQLException {
	return getManagerForClass(type).countWhere(where, fields);
    }

    public <T extends DaoBean> int countUsingTemplate(T bean) throws SQLException {
	return getManagerForClass(bean.getClass()).countUsingTemplate(bean);
    }

    public <T extends DaoBean> int countUsingTemplate(T bean, int startRow, int numRows) throws SQLException {
	return getManagerForClass(bean.getClass()).countUsingTemplate(bean, startRow, numRows);
    }

    public <T extends DaoBean> int countUsingTemplate(T bean, int startRow, int numRows, int searchType) throws SQLException {
	return getManagerForClass(bean.getClass()).countUsingTemplate(bean, startRow, numRows, searchType);
    }

    public <T extends DaoBean> List<T> decodeResultSet(Class<T> type, ResultSet rs, int startRow, int numRows) throws SQLException {
	return (List<T>)getManagerForClass(type).decodeResultSet(rs, startRow, numRows);
    }

    public <T extends DaoBean> T decodeRow(Class<T> type, ResultSet rs) throws SQLException {
	return (T)getManagerForClass(type).decodeRow(rs);
    }

    public <T extends DaoBean> T metaDataDecodeRow(Class<T> type, ResultSet rs) throws SQLException {
	return (T)getManagerForClass(type).metaDataDecodeRow(rs);
    }

    public <T extends DaoBean> List<T> loadByPreparedStatement(Class<T> type, PreparedStatement ps) throws SQLException {
	return (List<T>)getManagerForClass(type).loadByPreparedStatement(ps);
    }

    public <T extends DaoBean> List<T> loadByPreparedStatement(Class<T> type, PreparedStatement ps, int startRow, int numRows) throws SQLException {
	return (List<T>)getManagerForClass(type).loadByPreparedStatement(ps, startRow, numRows);
    }

    // Transactions
    private static final ThreadLocal<Txn> transaction =
	new ThreadLocal<Txn>() {
	@Override protected Txn initialValue() {
	    return null;
	}
    };

    static public Txn beginTransaction() {
	if (transaction.get() == null) {
	    transaction.set(new Txn());
	} else {
	    throw new IllegalStateException("Transaction already begun.");
	}
	return transaction.get();
    }

    static public Txn beginTransaction(Txn.Isolation isolation) {
 	if (transaction.get() == null) {
	    transaction.set(new Txn(isolation));
	} else {
	    throw new IllegalStateException("Transaction already begun.");
	}
	return transaction.get();
    }

    static public Txn currentTransaction() {
	return transaction.get();
    }

    static public void commitTransaction() {
	if (transaction.get() == null) {
	    throw new IllegalStateException("No transaction to commit.");
	} else {
	    try {
		transaction.get().commit();
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
    }

    static public void endTransaction() {
	if (transaction.get() == null) {
	    throw new IllegalStateException("No transaction to commit.");
	} else if (transaction.get().isActive()) {
	    rollbackTransaction();
	}
	transaction.remove();
    }

    static public void rollbackTransaction() {
	if (transaction.get() == null) {
	    throw new IllegalStateException("No transaction to rollback.");
	} else {
	    try {
		transaction.get().rollback();
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
    }

}
