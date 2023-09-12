<?php

$uname = $_POST['uname'];
$psw = $_POST['psw'];
session_start();
$_SESSION['uname']=$uname;
$_SESSION['psw']=$psw;

function writeError($error_msg){
        include("index_top.php");
        echo "<br>".$error_msg;
        include("index_bot.php");
}
try{
    $conn = @mysqli_connect("localhost",$uname,$psw,"pisid");
    if(!$conn){
        writeError("Nome e/ou palavra passe incorretos!");
    }
    else{        
        $query = "SELECT IDUtilizador FROM utilizador WHERE Nome='$uname' AND IDTipoUtilizador = 1 LIMIT 1";
        $result = mysqli_query($conn,$query);
        $rows = mysqli_num_rows($result);
        if($rows > 0){
            $_SESSION['uid'] = mysqli_fetch_row($result)[0];
            header("location:home.php");
        }else{
            writeError("O Utilizador não está registado como investigador!");
        }

        mysqli_free_result($result);
        mysqli_close($conn);
    }

    }
catch (Exception $e) { echo $e->getMessage();}