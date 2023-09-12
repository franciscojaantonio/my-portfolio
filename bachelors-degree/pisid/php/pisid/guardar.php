<?php
session_start();
$conn = @mysqli_connect("localhost",$_SESSION['uname'],$_SESSION['psw'],"pisid");

function writeError($error_msg){
    //echo $error_msg."<br>";
    //$error_msg= str_replace("\n", "+", $error_msg);
    //echo $error_msg;
    header("location:parametros.php?error=".$error_msg."&cul=".$_SESSION['cid']);
}

if($conn && isset($_SESSION['cid'])) {

    if(isset($_POST['temMin']) && isset($_POST['temMax']) &&
       isset($_POST['humMin']) && isset($_POST['humMax']) &&
       isset($_POST['luzMin']) && isset($_POST['luzMax']) &&
       !empty($_POST['temMin']) && !empty($_POST['temMax']) &&
       !empty($_POST['humMin']) && !empty($_POST['humMax']) &&
       !empty($_POST['luzMin']) && !empty($_POST['luzMax'])) {

       if($_POST['temMin'] <= $_POST['temMax'] &&
       $_POST['humMin'] <= $_POST['humMax'] &&
       $_POST['luzMin'] <= $_POST['luzMax'] ) {

    $query =
        "UPDATE parametrocultura SET 
		TemperaturaMinima =". $_POST['temMin']." , TemperaturaMaxima =". $_POST['temMax']." , HumidadeMinima =". $_POST['humMin']." ,
        HumidadeMaxima =". $_POST['humMax'].", LuzMinima =". $_POST['luzMin']." , LuzMaxima =". $_POST['luzMax']."
        WHERE IDCultura = ".$_SESSION['cid'];


        if ($conn->query($query) === TRUE) {
		   header("location:parametros.php?cul=".$_SESSION['cid']);
        } else {
            writeError("Erro na query ");
        }

        $conn->close();


        } else { writeError("Campos Mininos > Campos Maximos!");}
    }
    else { writeError("Campos Invalidos!"); }
}
else {
echo "algo deu errado"; }
