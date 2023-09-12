<?php
	$url="127.0.0.1";
	$database="pisid"; // Alterar nome da BD se necessario
    $conn = mysqli_connect($url,"root","abc",$database);	
	// Alterar nome da tabela Alerta e nome do campo Hora se necessario
	$sql = "SELECT z.Nome as Zona, s.Nome as Sensor, m.Data as Hora, m.Leitura, se.Nome as TipoAlerta, c.Nome as Cultura, a.Mensagem, c.IDUtilizador, a.IDCultura, a.Data as HoraEscrita
	from alerta as a, utilizador as u, cultura as c, medicao as m, zona as z, sensor as s, severidade as se
	where a.IDMedicao = m.IDMedicao AND se.IDSeveridade = a.IDSeveridade AND a.IDCultura = c.IDCultura
	AND u.IDUtilizador = c.IDUtilizador AND z.IDZona = m.IDZona AND s.IDSensor = m.IDSensor AND u.Nome = '"
	. $_POST['username'] ."' AND DATE(m.Data) = '" . $_POST['date'] . "';";	
	
	/*$sql = "SELECT Alerta.Zona, Alerta.Sensor, Alerta.Hora, Alerta.Leitura, 
	Alerta.TipoAlerta, Alerta.Cultura, Alerta.Mensagem, Alerta.IDUtilizador, Alerta.IDCultura, Alerta.HoraEscrita 
	from Alerta, Utilizador where Utilizador.IDUtilizador = Alerta.IDUtilizador AND Utilizador.Email = '"
	. $_POST['username'] ."' AND DATE(Alerta.Hora) = '" . $_POST['date'] . "';";	
	*/
	$result = mysqli_query($conn, $sql);
	$response["avisos"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Alterar nome dos campos da tabela se necessario
				$ad["Zona"] = $r['Zona'];
				$ad["Sensor"] = $r['Sensor'];
				$ad["Hora"] = $r['Hora'];
				$ad["Leitura"] = $r['Leitura'];
				$ad["TipoAlerta"] = $r['TipoAlerta'];
				$ad["Cultura"] = $r['Cultura'];
				$ad["Mensagem"] = $r['Mensagem'];
				$ad["IDUtilizador"] = $r['IDUtilizador'];
				$ad["IDCultura"] = $r['IDCultura'];
				$ad["HoraEscrita"] = $r['HoraEscrita'];
				array_push($response["avisos"], $ad);
			}
		}
	}
	$json = json_encode($response["avisos"]);
	echo $json;
	mysqli_close ($conn);
?>