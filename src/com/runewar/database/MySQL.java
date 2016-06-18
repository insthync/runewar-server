package com.runewar.database;

import java.sql.*;

public class MySQL {
	private Connection connect = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	private String host;
	private int port;
	private String username;
	private String password;
	private String dbname;
	public MySQL(String host, int port, String username, String password, String dbName) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.dbname = dbName;
        String connectionString = ("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password);
        
        try {
        	Class.forName("com.mysql.jdbc.Driver");
			connect = (Connection)DriverManager.getConnection(connectionString);
		} catch (SQLException ex) {
			System.err.println("Errors in constructor from MySQL...: " + ex.toString());
		} catch (ClassNotFoundException ex) {
			System.err.println("Errors in constructor from MySQL...: " + ex.toString());
		}
	}
	
	public MySQL clone() {
		return new MySQL(host, port, username, password, dbname);
	}

	public void Query(String queryString) {
		try {
			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (statement != null && !statement.isClosed())
				statement.close();
			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// Result set get the result of the SQL query
			resultSet = statement.executeQuery(queryString);
		} catch (SQLException ex) {
			System.err.println("Errors in method Query from MySQL...: " + ex.toString());
		}
	}
	
	public int QueryNumRows(String queryString) {
		try {
			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (statement != null && !statement.isClosed())
				statement.close();
			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// Result set get the result of the SQL query
			resultSet = statement.executeQuery(queryString);
			resultSet.last();
			return resultSet.getRow();
		} catch (SQLException ex) {
			System.err.println("Errors in method QueryNumRows from MySQL...: " + ex.toString());
			return 0;
		}
	}

    public void QueryNotExecute(String queryString) {
		try {
			if (resultSet != null && !resultSet.isClosed())
				resultSet.close();
			if (statement != null && !statement.isClosed())
				statement.close();
			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			// Result set get the result of the SQL query
			statement.executeUpdate(queryString);
		} catch (SQLException ex) {
			System.err.println("Errors in while QueryNotExecute from MySQL...: " + ex.toString());
		}
    }

	public ResultSet getResultSet() {
		return resultSet;
	}
	
	public Boolean isClosed() {
		try {
			return ((resultSet == null || resultSet.isClosed()) && (statement == null || statement.isClosed()) && (connect == null || connect.isClosed()));
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public void Close() {
		try {
			if (resultSet != null)
				resultSet.close();
			if (statement != null)
				statement.close();
			if (connect != null)
				connect.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
