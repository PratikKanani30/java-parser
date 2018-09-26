package com.ef.dao;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import com.ef.LogData;

/**
 * @author Pratik
 *
 */
public class LogDao implements Closeable {

    private static final String INSERT_STATEMENT = "INSERT INTO REQUEST_DATA (REQ_DATE, REQ_IP, REQ_REQ,REQ_STATUS,REQ_USER_AGENT) VALUES (?, ?, ?, ?, ?)";
    private final Connection connection;
    private PreparedStatement statement;

    public LogDao() {
	connection = DatabaseFactory.getConnection();

    }

    public void addToSave(LogData data) {
	try {
	    if (connection != null) {
		if (statement == null) {
		    statement = connection.prepareStatement(INSERT_STATEMENT);
		}
		Calendar calc = Calendar.getInstance();
		calc.setTime(data.getDate());
		statement.setTimestamp(1, new Timestamp(calc.getTimeInMillis()));
		statement.setString(2, data.getIp());
		statement.setString(3, data.getRequest());
		statement.setString(4, data.getStatus());
		statement.setString(5, data.getUserAgent());
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
