<?php
	//$url="127.0.0.1";
	$url="localhost";
	$database="pisid"; // Alterar nome da BD se necessario
    $conn = mysqli_connect($url,$_POST['username'],$_POST['password'],$database);
	$query_select = "SELECT current_role()";
	$result_query_select = mysqli_query($conn, $query_select); 
	$conn->next_result();
	$administrador = mysqli_fetch_assoc($result_query_select)['current_role()'];
	if($administrador == "Investigador") { // Alterar role se necessario
		$response["valid"] = array();
		$json = json_encode($response["valid"]);
		echo $json;
	}
	$result_query_select->close();
	mysqli_close ($conn);
?>