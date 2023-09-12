<?php
	$url="127.0.0.1";
	$database="pisid"; // Alterar nome da BD se necessario
    $conn = mysqli_connect($url,$_POST['username'],$_POST['password'],$database);
	// Alterar nome da tabela Medicao, nome dos campos Hora e Leitura, e a sigla do tipo de sensor de temperatura ("TEM") se necessario
	$sql = "SELECT Data, Leitura 
	from medicao 
	INNER JOIN sensor 
	ON sensor.IDSensor = medicao.IDSensor 
	where medicao.LeituraCorreta = 1 AND sensor.IDTipoSensor = 3 AND Data >= now() - interval 3 minute ORDER BY Data ASC";
	$result = mysqli_query($conn, $sql);
	$response["medicoes"] = array();
	if ($result){
		if (mysqli_num_rows($result)>0){
			while($r=mysqli_fetch_assoc($result)){
				$ad = array();
				// Alterar nome dos campos se necessario
				$ad["Hora"] = $r['Data'];
				$ad["Leitura"] = $r['Leitura'];
				array_push($response["medicoes"], $ad);
			}
		}	
	}
	$json = json_encode($response["medicoes"]);
	echo $json;
	mysqli_close ($conn);
?>