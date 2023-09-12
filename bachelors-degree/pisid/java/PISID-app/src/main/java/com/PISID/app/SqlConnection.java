package com.PISID.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection extends Thread{

	public Connection mysqlConnector;
	private String connectionString;
	private String mysql_username;
	private String mysql_password;
    public int timeout;
	
	public SqlConnection(String connectionString,String mysql_username,String mysql_password){	
		this.connectionString = connectionString;
		this.mysql_username = mysql_username;
		this.mysql_password = mysql_password;
		timeout = 5000;
	}
	
	private void tryAgain(String error_msg) {
		System.err.println(">"+error_msg);
		System.err.println(">Tentando outravez em: " + timeout +" millisegundos");
		try {
			sleep(timeout);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
  	@Override
	public void run() {
			doConnections();
	}
	
	public void doConnections() {
		try {
			//Connection mysqlConnector = DriverManager.getConnection("jdbc:mysql://localhost:3306/"+mysql_db_name,mysql_username,mysql_password);
			mysqlConnector = DriverManager.getConnection(connectionString,mysql_username,mysql_password);
			if(mysqlConnector == null || !mysqlConnector.isValid(0))
				throw new SQLException();
			System.out.println("Coneccao bem sucedida: " + connectionString);
		}catch(SQLException ex) {
			tryAgain("servidor mysql não é valido: "+ connectionString +"/n impossivel fazer coneccao");
			doConnections();
		}
		
	}
	
	public boolean isConnected() {
		try {
			return mysqlConnector != null && mysqlConnector.isValid(0);
		} catch (SQLException e) {
			return false;
		}
	}
	
}

