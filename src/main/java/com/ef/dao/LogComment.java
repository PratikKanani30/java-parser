package com.ef.dao;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Pratik
 *
 */
public class LogComment implements Closeable {

    private static final String INSERT_STATEMENT = "INSERT INTO LOG_SUMMARY ( SUM_IP, SUM_INFO) VALUES (?, ?)";
    private final Connection connection;
    private PreparedStatement statement;

    public LogComment() {
	connection = DatabaseFactory.getConnection();

    }

    public void addToSave(String ip, String comment) {
	try {
	    if (connection != null) {
		if (statement == null) {
		    statement = connection.prepareStatement(INSERT_STATEMENT);
		}
		statement.setString(1, ip);
		statement.setString(2, comment);
		statement.addBatch();
	    }
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void saveBatch() {
	try {
	    if (statement != null) {
		statement.executeBatch();
	    }
	    commit();
	} catch (SQLException e) {
	    rollback();
	    throw new RuntimeException(e);
	}
    }

    private void commit() throws SQLException {
	if (connection != null) {
	    connection.commit();
	}
    }

    private void rollback() {
	try {
	    if (connection != null) {
		connection.rollback();
	    }
	} catch (SQLException e1) {
	    throw new RuntimeException(e1);
	}
    }

    @Override
    public void close() throws IOException {
	try {
	    closeStatement();
	    closeDbConnection();
	} catch (SQLException e) {
	    throw new IOException(e);
	}
    }

    private void closeStatement() throws SQLException {
	if (statement != null) {
	    statement.close();
	    statement = null;
	}
    }

    private void closeDbConnection() throws SQLException {
	if (connection != null) {
	    connection.close();
	}
    }

}
