package com.test;

import java.io.File;
import java.util.Date;
import javax.sql.DataSource;
import net.sourceforge.sql2java.lib.*;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.*; 

public class TransactionTest {

    @Test public void twoTableCommitTransactionTest() throws Exception {
	JDBCDataSource ds = new JDBCDataSource();
	ds.setDatabase("jdbc:hsqldb:file:target/databases/test");
	ds.setUser("SA");
	ds.setPassword("");

	PublicDatabase db = new PublicDatabase(ds);
	//clean up
	db.deleteByWhere(Phone.class, "WHERE PHONE_NUMBER='+14105551212'");
	db.deleteByWhere(Person.class, "WHERE USERNAME='hansolo'");

	try {
	    db.beginTransaction(Txn.Isolation.REPEATABLE_READ);
	    Person s0 = db.createBean(Person.class);
	    s0.setUsername("hansolo");
	    s0.setFirstName("Harrison");
	    s0.setLastName("Ford");
	    s0.setCreateDate(new Date());
	    s0 = db.save(s0);
	    Phone m0 = db.createBean(Phone.class);
	    m0.setPersonId(s0.getId());
	    m0.setPhoneType(1);
	    m0.setPhoneNumber("+14105551212");
	    m0.setCreateDate(new Date());
	    m0 = db.save(m0);
	    db.commitTransaction();
	} finally {
	    db.endTransaction();
	}
	
	Person s1 = db.loadUniqueByWhere(Person.class, "WHERE USERNAME='hansolo'");
	Assert.assertNotNull(s1);

	Phone m1 = db.loadUniqueByWhere(Phone.class, "WHERE PHONE_NUMBER='+14105551212'");
	Assert.assertNotNull(m1);
    }


    @Test public void twoTableRollbackTransactionTest() throws Exception {
	JDBCDataSource ds = new JDBCDataSource();
	ds.setDatabase("jdbc:hsqldb:file:target/databases/test");
	ds.setUser("SA");
	ds.setPassword("");

	PublicDatabase db = new PublicDatabase(ds);
	//clean up
	db.deleteByWhere(Phone.class, "WHERE PHONE_NUMBER='+14105551212'");
	db.deleteByWhere(Person.class, "WHERE USERNAME='hansolo'");

	try {
	    db.beginTransaction(Txn.Isolation.REPEATABLE_READ);
	    Person s0 = db.createBean(Person.class);
	    s0.setUsername("hansolo");
	    s0.setFirstName("Harrison");
	    s0.setLastName("Ford");
	    s0.setCreateDate(new Date());
	    s0 = db.save(s0);
	    Phone m0 = db.createBean(Phone.class);
	    m0.setPersonId(s0.getId());
	    m0.setPhoneType(1);
	    m0.setPhoneNumber("+14105551212");
	    m0.setCreateDate(new Date());
	    m0 = db.save(m0);
	    db.rollbackTransaction();
	} finally {
	    db.endTransaction();
	}
	
	Person s1 = db.loadUniqueByWhere(Person.class, "WHERE USERNAME='hansolo'");
	Assert.assertNull(s1);

	Phone m1 = db.loadUniqueByWhere(Phone.class, "WHERE PHONE_NUMBER='+14105551212'");
	Assert.assertNull(m1);
    }
    
}
