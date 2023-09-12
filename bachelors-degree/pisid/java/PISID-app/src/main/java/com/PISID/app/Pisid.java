package com.PISID.app;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.Document;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.MongoTimeoutException;
import com.mongodb.client.*;
import com.mongodb.internal.connection.MongoWriteConcernWithResponseException;

public class Pisid {

	Pisid _this = this;

	//db mongo cloud
	public MongoConnection cloud_mongo_conn;
	//public static String cloud_db_name = "sid2022";
	public String cloud_mongo_db_name = "sensores";
	//colecao cloud
	//public String cloud_mongo_coll_medicoes = "medicoes";
	public String cloud_mongo_coll_medicoes = "medicoesCloud";
	//logar como user cloud
	public String cloud_mongo_username = "aluno";
	public String cloud_mongo_password = "aluno";

	//db mongo local
	public MongoConnection local_mongo_conn;
	public String local_mongo_db_name = "sensores";
	// collecoes locais
	public String local_mongo_coll_medicoes = "mongoMedicoes";
	public String local_mongo_coll_antigas = "mongoMedicoesAntigas";
	public String local_mongo_coll_errors = "mongoMedicoesError";
	//logar como user local
	public String local_mongo_username = "java";
	public String local_mongo_password = "java";

	//mongo - mongo aux
	public int num_medicoes_migradas = 0;
	public List<String> medicao_fields = Arrays.asList("_id", "Zona", "Sensor", "Data", "Medicao");
	public Set<String> medicao_set = new HashSet<>(medicao_fields);
	public static DateTimeFormatter dataFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	//db sql local
	public SqlConnection local_sql_conn;
	// collecoes
	public String local_sql_db_name = "pisid";
	//logar como user local
	public static String local_sql_username = "java";
	public static String local_sql_password = "java";

	//db sql cloud
	public SqlConnection cloud_sql_conn;
	// collecoes
	public String cloud_sql_db_name = "sid2022";
	//logar como user local
	public static String cloud_sql_username = "aluno";
	public static String cloud_sql_password = "aluno";

	public Map<String,Triplet<Integer,Double,Double>> sensorMap;
	public Map<String,Integer> tipoSensorMap;
	public Map<String,Integer> zonaMap;
	public boolean wfs = false;
	public boolean st = false;

	public static void main(String[] args){

		Pisid p = new Pisid();

		p.startProgram();
	}

	private void createConnections() {

		local_mongo_conn =
				new MongoConnection(
						//"mongodb://"+local_mongo_username+":"+local_mongo_password+"@localhost",
						"mongodb://"+local_mongo_username+":"+local_mongo_password+"@localhost:27019,localhost:25019,localhost:23019/?replicaSet=rs0",
						local_mongo_db_name);
		cloud_mongo_conn =
				new MongoConnection(
						//"mongodb://"+cloud_mongo_username+":"+cloud_mongo_password+"@194.210.86.10:27017/?authSource=admin",
						"mongodb://"+local_mongo_username+":"+local_mongo_password+"@localhost",
						//"mongodb://"+local_mongo_username+":"+local_mongo_password+"@localhost:27019,localhost:25019,localhost:23019/?replicaSet=rs0",
						cloud_mongo_db_name);
		local_sql_conn =
				new SqlConnection(
						"jdbc:mysql://localhost/"+local_sql_db_name,
						local_sql_username,
						local_sql_password
				);
		cloud_sql_conn = new SqlConnection(
				"jdbc:mysql://194.210.86.10/"+cloud_sql_db_name,
				cloud_sql_username,
				cloud_sql_password
		);
	}

	private void waitForServers() throws InterruptedException {

		if(!wfs) {
			wfs=true;
			System.out.println("============================");
			System.out.println(Thread.currentThread() + " > sincronizando os servidores ...");

			while(!local_mongo_conn.isConnected() || !cloud_mongo_conn.isConnected() ||
					!local_sql_conn.isConnected() || !cloud_sql_conn.isConnected()) {

				System.out.println("...");
				local_mongo_conn.doConnections();
				cloud_mongo_conn.doConnections();
				local_sql_conn.doConnections();
				cloud_sql_conn.doConnections();

				local_mongo_conn.join();
				cloud_mongo_conn.join();
				local_sql_conn.join();
				cloud_sql_conn.join();

			}
			System.out.println(Thread.currentThread() + " > sincronizou os servidores");
			System.out.println("============================");

			wfs=false;
		}
	}

	private void startProgram() {

		createConnections();


		try {

			local_mongo_conn.start();
			cloud_mongo_conn.start();
			local_sql_conn.start();
			cloud_sql_conn.start();

			local_mongo_conn.join();
			cloud_mongo_conn.join();
			local_sql_conn.join();
			cloud_sql_conn.join();

			num_medicoes_migradas = local_mongo_conn.getNumDocs(Arrays.asList(
					local_mongo_coll_medicoes,local_mongo_coll_antigas,local_mongo_coll_errors));

			new Thread(() -> {
				try {startMigrationSqlSql(100000);}
				catch (InterruptedException e){e.printStackTrace();}
			}).start();

			synchronized(this) {
				System.out.println(Thread.currentThread() + " > Main thread esperando tabelas sincronizar ...");
				wait();
			}


			new Thread(() -> {
				try {startMigrationMongoMongo(5000);}
				catch (InterruptedException e) {e.printStackTrace();}
			}).start();

			new Thread(() -> {
				try {startMigrationMongoSql(5000);}
				catch (InterruptedException | SQLException e) {e.printStackTrace();}
			}).start();
			/*
							new Thread(() -> {
					try {startMigrationMongoMQTT(60000);} 
						catch (InterruptedException e) {e.printStackTrace();}
					}).start();
				*/

			while(true) {
				synchronized(this) {
					System.out.println(Thread.currentThread() + " > Main thread esperando sinal para sincronizar os servidores ...");
					wait();
				}
				waitForServers();
				synchronized(this) {
					notifyAll();
					System.out.println(Thread.currentThread() + " > Main thread sincronizou os servidores");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void startMigrationSqlSql(int milliseconds_between_migrations) throws InterruptedException {

		String insertZona =
				"insert into zona (IDZona, Nome, TemperaturaMedia , HumidadeMedia , LuzMedia) values (NULL, ?,?,?,?)";

		String updateZona =
				"UPDATE zona "
						+ "SET TemperaturaMedia = ?, HumidadeMedia = ?, LuzMedia = ? "
						+ "WHERE Nome = ? ";

		String insertSensor =
				"insert into sensor (IDSensor, Nome, IDTipoSensor , IDZona , LimiteInferior, LimiteSuperior) values (NULL,?,?,?,?,?)";

		String updateSensor =
				"UPDATE sensor "
						+ "SET LimiteInferior = ?, LimiteSuperior = ? "
						+ "WHERE Nome = ? ";

		tipoSensorMap = new HashMap<>();
		tipoSensorMap.put("H",1);
		tipoSensorMap.put("L",2);
		tipoSensorMap.put("T",3);

		zonaMap = new HashMap<>();
		zonaMap.put("Z1",1);
		zonaMap.put("Z2",2);

		sensorMap = new HashMap<>();
		sensorMap.put("H1",Triplet.with(1,0.0,100.0));
		sensorMap.put("H2",Triplet.with(2,0.0,100.0));
		sensorMap.put("L1",Triplet.with(3,0.0,100.0));
		sensorMap.put("L2",Triplet.with(4,0.0,100.0));
		sensorMap.put("T1",Triplet.with(5,0.0,100.0));
		sensorMap.put("T2",Triplet.with(6,0.0,100.0));

		try {
			while(true) {
				try  {
					Statement ps = cloud_sql_conn.mysqlConnector.createStatement();
					Statement lps = local_sql_conn.mysqlConnector.createStatement();

					PreparedStatement psInsertZona = local_sql_conn.mysqlConnector.prepareStatement(insertZona);
					PreparedStatement psUpdateZona = local_sql_conn.mysqlConnector.prepareStatement(updateZona);
					PreparedStatement psInsertSensor = local_sql_conn.mysqlConnector.prepareStatement(insertSensor);
					PreparedStatement psUpdateSensor = local_sql_conn.mysqlConnector.prepareStatement(updateSensor);

					transferDataSqlSql(ps,lps,psInsertZona,psUpdateZona,psInsertSensor,psUpdateSensor);

					ps.close();
					lps.close();
					psInsertZona.close();
					psUpdateZona.close();
					psInsertSensor.close();
					psUpdateSensor.close();


					Thread.sleep(milliseconds_between_migrations);
				}
				catch (CommunicationsException e) {
					signalMain();
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void transferDataSqlSql(Statement ps, Statement lps, PreparedStatement psInsertZona,
									PreparedStatement psUpdateZona, PreparedStatement psInsertSensor, PreparedStatement psUpdateSensor)
			throws CommunicationsException, SQLException {

		ResultSet rsZona =ps.executeQuery("select * from zona");
		//toTable(rsZona);
		zonaMap = new HashMap<String,Integer>();

		while (rsZona.next()) {
			String nome = "Z"+rsZona.getString(1);
			Double t = rsZona.getDouble(2);
			Double h = rsZona.getDouble(3);
			Double l = rsZona.getDouble(4);

			//System.out.println(nome + " " + t + " " + h + " " + l );

			psUpdateZona.setDouble(1, t);
			psUpdateZona.setDouble(2, h);
			psUpdateZona.setDouble(3, l);
			psUpdateZona.setString(4, nome);

			psUpdateZona.executeUpdate();
			String query = "SELECT * FROM zona WHERE Nome = '"+nome+"'";
			//System.out.println(query);
			if(!lps.executeQuery(query).next()) {

				psInsertZona.setString(1, nome);
				psInsertZona.setDouble(2, t);
				psInsertZona.setDouble(3, h);
				psInsertZona.setDouble(4, l);

				System.out.println(psInsertZona.toString());
				psInsertZona.executeUpdate();
			}
			ResultSet res = lps.executeQuery("select IDZona from zona where Nome = '"+nome+"'");
			if(res.next())
				zonaMap.put(nome,res.getInt(1));

		}


		ResultSet rsSensor =ps.executeQuery("select * from sensor");
		//toTable(rsSensor);
		sensorMap = new HashMap<String,Triplet<Integer,Double,Double>>();

		while (rsSensor.next()) {
			String tipo = rsSensor.getString(2);
			String nome = tipo+rsSensor.getString(1);
			String zona = "Z"+rsSensor.getString(5);
			Double inf = rsSensor.getDouble(3);
			Double sup = rsSensor.getDouble(4);

			// System.out.println(nome + " " + tipo + " " + zona + " " + inf + " " + sup);

			psUpdateSensor.setDouble(1, inf);
			psUpdateSensor.setDouble(2, sup);
			psUpdateSensor.setString(3, nome);

			psUpdateSensor.executeUpdate();

			if(!lps.executeQuery("select * from sensor where Nome ='"+nome+"'").next()) {

				psInsertSensor.setString(1,nome);
				psInsertSensor.setInt(2,tipoSensorMap.get(tipo));
				psInsertSensor.setInt(3,zonaMap.get(zona));
				psInsertSensor.setDouble(4, inf);
				psInsertSensor.setDouble(5, sup);

				psInsertSensor.executeUpdate();
			}
			ResultSet res = lps.executeQuery("select IDSensor,LimiteInferior,LimiteSuperior from sensor where Nome = '"+nome+"'");
			if(res.next()) {
				sensorMap.put(nome,Triplet.with(res.getInt(1),res.getDouble(2),res.getDouble(3)));
			}

		}

		System.out.println(Thread.currentThread() + " > As tabelas sensor e zona foram atualizadas");
		if(!st)
			synchronized(this) {
				st = true;
				notify();
			}

	}

	private void startMigrationMongoSql(int milliseconds_between_migrations) throws InterruptedException, SQLException {
		String query =  "INSERT INTO medicao (IDSensor, IDZona, Data, Leitura, LeituraCorreta) VALUES (?,?,?,?,?)";

		//System.out.println(query);
		try {
			MongoCollection<Document> from = local_mongo_conn.getColl(local_mongo_coll_medicoes);
			MongoCollection<Document> to = local_mongo_conn.getColl(local_mongo_coll_antigas);
			PreparedStatement prepStat = local_sql_conn.mysqlConnector.prepareStatement(query);

			while (true) {

				try {
					Thread.sleep(milliseconds_between_migrations);
					long startTime = System.currentTimeMillis();
					transferDataMongoSql(from, to, prepStat);
					long endTime = System.currentTimeMillis();
					System.out.println("Demorei " + (endTime-startTime) + " millisegundos");
				}
				catch (CommunicationsException | MongoTimeoutException e) {
					signalMain();
					from = local_mongo_conn.getColl(local_mongo_coll_medicoes);
					to = local_mongo_conn.getColl(local_mongo_coll_antigas);
					prepStat = local_sql_conn.mysqlConnector.prepareStatement(query);
				}
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void transferDataMongoSql(MongoCollection<Document> from, MongoCollection<Document> to, PreparedStatement prepStat)
			throws MongoTimeoutException,CommunicationsException, SQLException, InterruptedException{
		long numDoc = from.countDocuments();
		if(numDoc > 0) {
			MongoCursor<Document> cursor = from.find().iterator();
			System.out.println("Comecei a transferir "+numDoc+" Documentos para o sql local!");

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				Double leitura = Double.parseDouble(doc.get("Medicao").toString().replace(",","."));
				Triplet<Integer, Double, Double> sensorInfo = sensorMap.get((String) doc.get("Sensor"));
				Boolean leituraCorreta;
				while(sensorInfo == null) {
					System.out.println("Nome sensor desconhecido:" + (String) doc.get("Sensor") + " esperando sincronizacao");
					Thread.sleep(5000);
					sensorInfo =  sensorMap.get((String) doc.get("Sensor"));
				}

				leituraCorreta = leitura > sensorInfo.getValue1() && leitura < sensorInfo.getValue2();


				java.sql.Timestamp data = new java.sql.Timestamp(
						LocalDateTime.parse((CharSequence) doc.get("Data"),dataFormat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
				prepStat.setInt (1,sensorInfo.getValue0());
				prepStat.setInt (2,zonaMap.get(doc.get("Zona")));
				prepStat.setTimestamp(3, data);
				prepStat.setDouble (4,leitura);
				prepStat.setBoolean(5, leituraCorreta);

				//System.out.println(prepStat.toString());
				prepStat.executeUpdate();
				from.deleteOne(doc);
				to.insertOne(doc);

			}

			System.out.println("Acabei de transferir "+numDoc+" Documentos para o sql local!");
		}
		else System.out.println("Nao existem mais medicoes no mongo local!");
	}

	void startMigrationMongoMongo(int milliseconds_between_migrations) throws InterruptedException {

		MongoCollection<Document> from = cloud_mongo_conn.getColl(cloud_mongo_coll_medicoes);
		MongoCollection<Document> toClean =local_mongo_conn.getColl(local_mongo_coll_medicoes);
		MongoCollection<Document> toErr = local_mongo_conn.getColl(local_mongo_coll_errors);
		while(true)
			try {
				Thread.sleep(milliseconds_between_migrations);
				transferDataMongoMongo(from,toClean,toErr);
			}
			catch(MongoTimeoutException | MongoWriteConcernWithResponseException e) {
				signalMain();
			}
	}

	void transferDataMongoMongo(MongoCollection<Document> from, MongoCollection<Document> toClean, MongoCollection<Document> toErr)
			throws MongoTimeoutException, MongoWriteConcernWithResponseException {

		int numFrom = (int) from.countDocuments();
		int num = numFrom - num_medicoes_migradas;
		if(num > 0) {
			FindIterable<Document> fromIterable = from.find().limit(num).skip(num_medicoes_migradas);
			//System.out.println(" Migrando "+num+" documentos para o mongo local");
			Pair<List<Document>,List<Document>> lists = getAndValidateDocs(fromIterable);
			//System.out.println(lists.getValue0().toString());

			if (lists.getValue0().size() > 0)
				toClean.insertMany(lists.getValue0());
			if (lists.getValue1().size() > 0)
				toErr.insertMany(lists.getValue1());

			num_medicoes_migradas += num;

			System.out.println("Foram migrados "+num+" documentos para o mongo local");
		}
		else {
			System.out.println("Todos os documentos foram migrados");
		}
	}

	Pair<List<Document>,List<Document>> getAndValidateDocs(FindIterable<Document> coll) {

		List<Document> cleanList = new ArrayList<>();
		List<Document> errorList = new ArrayList<>();
		for(Document doc :coll)  {
			//System.out.println(doc.toJson());
			// Verificar sintaxe
			if(checkDoc(doc))
				cleanList.add(doc);
			else {
				System.out.println("Documento Invalido");
				System.out.println(doc.toJson());
				errorList.add(doc);
			}
		}

		return Pair.with(cleanList,errorList);
	}

	boolean checkDoc(Document doc) {

		try {
			if (!doc.keySet().equals(medicao_set)) return false;

			String zona = doc.get("Zona").toString();
			String sensor = doc.get("Sensor").toString();

			String data = doc.get("Data").toString().replace("Z","").replace("T"," ");
			double medicao = Double.parseDouble(doc.get("Medicao").toString().replace(",","."));
			LocalDateTime.parse(data, dataFormat);
			doc.put("Data", data);
			doc.put("Medicao", medicao);

			return zona.startsWith("Z") && "HTL".contains(String.valueOf(sensor.toCharArray()[0]).toUpperCase());
		} catch (Exception e) {
			//System.err.println("Formato Errado");
			return false;
		}

	}

	private void signalMain() throws InterruptedException {

		synchronized(this) {
			if(!wfs)
				notify();
			System.out.println(Thread.currentThread() + " > Esperando sincronizar os servidores ...");
			wait();
			System.out.println(Thread.currentThread() + " > Servidores sincronizados");
		}
	}

	private void addToMYSQL(JSONObject jsonObject) throws InterruptedException, JSONException, SQLException {
		String query =
				"INSERT INTO medicao "
						+ "(IDSensor,IDZona, Data, LeituraCorreta, Leitura) "
						+ "VALUES (?,?,?,?,?)";

		PreparedStatement prepStat = local_sql_conn.mysqlConnector.prepareStatement(query);

		Double leitura = Double.parseDouble(jsonObject.get("Medicao").toString().replace(",","."));
		Boolean leituraCorreta = leitura > sensorMap.get(jsonObject.get("Sensor")).getValue1() && leitura < sensorMap.get(jsonObject.get("Sensor")).getValue2();

		java.sql.Timestamp data = new java.sql.Timestamp(
				LocalDateTime.parse((CharSequence) jsonObject.get("Data"),dataFormat).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		prepStat.setInt (1,sensorMap.get(jsonObject.get("Sensor")).getValue0());
		prepStat.setInt (2,zonaMap.get(jsonObject.get("Zona")));
		prepStat.setTimestamp(3,data);
		prepStat.setBoolean (4,leituraCorreta);
		prepStat.setDouble (5,leitura);

		prepStat.executeUpdate();

		prepStat.close();

		//System.out.println("addMysql: " + System.currentTimeMillis());

	}

	private void startMigrationMongoMQTT(int milliseconds_between_migrations) throws InterruptedException {


		MongoCollection<Document> from = local_mongo_conn.getColl(local_mongo_coll_medicoes);
		MongoCollection<Document> to = local_mongo_conn.getColl(local_mongo_coll_antigas);
		ClienteMQTT clienteMQTT = new ClienteMQTT("tcp://broker.mqtt-dashboard.com:1883", null, null);
		clienteMQTT.iniciar();
		new Ouvinte(clienteMQTT, "SID2022/#", 0);
		while(true) {
			try {
				Thread.sleep(milliseconds_between_migrations);
				// System.out.println("Comecei aos "+System.currentTimeMillis());
				transferDataMongoMQTT(from,to,clienteMQTT);

			} catch (MongoTimeoutException e) {
				signalMain();
			}

		}
	}

	private void transferDataMongoMQTT(MongoCollection<Document> from, MongoCollection<Document> to, ClienteMQTT clienteMQTT){
		long numDoc = from.countDocuments();
		if(numDoc > 0) {
			MongoCursor<Document> cursor = from.find().iterator();
			System.out.println("Comecei a transferir "+numDoc+" Documentos para o MQTT!");
			while (cursor.hasNext()) {
				Document doc=cursor.next();
				clienteMQTT.publicar("SID2022/teste", doc.toJson().getBytes(), 0);
				from.deleteOne(doc);
				to.insertOne(doc);
			}
			System.out.println("Acabei de transferir "+numDoc+" Documentos para o MQTT!");
		}
		else System.out.println("Nao existem mais medicoes no mongo local!");

	}

	public class Ouvinte implements IMqttMessageListener {

		public Ouvinte(ClienteMQTT clienteMQTT, String topico, int qos) {
			clienteMQTT.subscribe(qos, this, topico);
		}

		@Override
		public void messageArrived(String topico, MqttMessage mm) throws InterruptedException {
			try {
				addToMYSQL(new JSONObject(new String(mm.getPayload())));
			} catch (CommunicationsException e) {
				signalMain();
				messageArrived(topico, mm);
			} catch (JSONException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}

	}

}
	    
	   
	
	
	


