package com.PISID.app;

import java.sql.SQLException;
import java.util.List;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoTimeoutException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

	 public class MongoConnection extends Thread{
	    	
	    	public MongoClient client;
		    public MongoDatabase database;
		    public String db_name;
	    	
	    	public String connectionString;
	 	
	    	public MongoConnection(String connectionString, String db_name){	
	    		this.connectionString = connectionString;
	    		this.db_name = db_name; 
	    		//MongoClientSettings settings = MongoClientSettings.builder(); 
	    	}
	    	
	    	@Override
	    	public void run() {
	    		try {
					doConnections();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    	
	    	private void tryAgain(String error_msg) throws InterruptedException {
	    		System.err.println(">"+error_msg);
	    		System.err.println(">Tentando outravez ...");
	    		sleep(1000);
	    	}
			
	    	public boolean doConnections() throws InterruptedException {
	    			try {
	    			client = MongoClients.create(
	    					MongoClientSettings.builder().
	    					applyConnectionString(
	    							new ConnectionString(connectionString)).
	    					build());
			    	
			    	database =  client.getDatabase(db_name);
			    	client.startSession();
			    	System.out.println("Coneccao bem sucedida: " + connectionString);
			    	return true;
			    	}
			    	catch(Exception e) {
			    		tryAgain("Nao foi possivel conectar ao servidor mongo: "+connectionString);
			    		return doConnections(); 
			    	}
	    	}
	    	
	    	public int getNumDocs(List<String> colls_name) throws InterruptedException, MongoTimeoutException {
	    			int toReturn = 0;
	    			for(String str : colls_name)
	    				toReturn += database.getCollection(str).countDocuments();
	    			return toReturn; }
	    	
	    	public MongoCollection<Document> getColl(String coll_name) throws InterruptedException, MongoTimeoutException{
	    			MongoCollection<Document> coll = database.getCollection(coll_name);
	    			return coll; }
	    	
	    	public boolean isConnected() {
	    		try {
	    		return client != null && client.listDatabaseNames().iterator().hasNext();
	    		}
	    		catch (MongoTimeoutException  e) { return false;}
	    	}
	    }
