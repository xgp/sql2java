package net.sourceforge.sql2java.lib;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class Txn {

    public enum Isolation {
	DEFAULT(-1),
	READ_COMMITTED(java.sql.Connection.TRANSACTION_READ_COMMITTED),
	READ_UNCOMMITTED(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED),
	REPEATABLE_READ(java.sql.Connection.TRANSACTION_REPEATABLE_READ),
	SERIALIZABLE(java.sql.Connection.TRANSACTION_SERIALIZABLE);
	Isolation(int i) {
	    this.level = i;
	}
	final int level;
	public int getLevel() { return this.level; }
    }

    private final Isolation isolation;
    private DataSource dataSource;
    private boolean active;
    private Connection connection;

    public Txn() {
	this.isolation = Isolation.DEFAULT;
	this.active = false;
    }

    public Txn(Isolation isolation) {
	this.isolation = isolation;
	this.active = false;
    }

    public Txn(DataSource dataSource, Isolation isolation) {
	this.dataSource = dataSource;
	this.isolation = isolation;
	this.active = true;
    }

    public void setDataSource(DataSource dataSource) {
	if (this.dataSource != null) throw new IllegalStateException();
	this.dataSource = dataSource; 
	this.active = true;
    }

    public boolean isDefault() {
	return (isolation == Isolation.DEFAULT);
    }

    public boolean isActive() {
	return active;
    }

    public void commit() throws SQLException {
	if (connection == null || !active) throw new IllegalStateException();
	try {
	    connection.commit();
	} finally {
	    active = false;
	    connection.close();
	}
    }

    public void rollback() throws SQLException {
	if (connection == null || !active) throw new IllegalStateException();
	try {
	    connection.rollback();
	} finally {
	    active = false;
	    connection.close();
	}
    }

    public Connection getConnection() throws SQLException {
	if (dataSource == null || !active) throw new IllegalStateException();
	if (connection != null) return connection;
	connection = dataSource.getConnection();
	connection.setAutoCommit(false);
	connection.setTransactionIsolation(isolation.getLevel());
	return connection;
    }

}
