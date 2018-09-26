package com.ef.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pratik
 *
 */
public final class DatabaseFactory {

    private static DatabaseFactory factory;
    private static final ReentrantLock lock = new ReentrantLock(true);
    private String username;
    private String url;
    private String password;

    private DatabaseFactory(String url, String username, String password) {
	try {
	    Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException e) {
	    throw new IllegalArgumentException(e);
	}
	this.url = url;
	this.username = username;
	this.password = password;
    }

    public static final void initFactory(Properties properties) {
	initFactory(properties.getProperty("db.url"), properties.getProperty("db.user"),
		properties.getProperty("db.password"));
    }

    public static final void initFactory(String url, String username, String password) {
	if (factory == null) {
	    try {
		lock.lock();
		initFactoryInstance(url, username, password);
	    } finally {
		lock.unlock();
	    }
	}
    }

    public static Connection getConnection() {
	try {
	    if (factory != null) {
		Connection connection = DriverManager.getConnection(factory.getUrl(), factory.getUsername(),
			factory.getPassword());
		connection.setAutoCommit(false);
		return connection;
	    }
	    return null;
	} catch (SQLException e) {
	    throw new IllegalArgumentException("Connection not found", e);
	}
    }

    private static final void initFactoryInstance(String url, String username, String password) {
	if (factory == null) {
	    factory = new DatabaseFactory(url, username, password);
	}
    }

    public String getUsername() {
	return username;
    }

    public String getUrl() {
	return url;
    }

    public String getPassword() {
	return password;
    }

}
