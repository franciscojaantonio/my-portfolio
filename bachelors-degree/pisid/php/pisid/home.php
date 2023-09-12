<?php
session_start();
date_default_timezone_set('Europe/Lisbon');
$conn = @mysqli_connect("localhost",$_SESSION['uname'],$_SESSION['psw'],"pisid");
if($conn) {
     $query = "SELECT IDCultura,IDZona,Nome,Descricao,LastUpdate FROM cultura WHERE IDUtilizador = " . $_SESSION['uid'];
     $result = mysqli_query($conn,$query);
?>
<!DOCTYPE html>
<html>
<head>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
        integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"
        integrity="sha384-ka7Sk0Gln4gmtz2MlQnikT1wXgYsOg+OMhuP+IlRH9sENBO0LRn5q+8nbTov4+1p" crossorigin="anonymous">
    </script>
    <link rel="icon" href="sign_in_files/flask.png">
    <title>As suas culturas</title>
</head>

<body class="container-fluid" style="background-color: #f5f5f5;">
    <section class="row" style="padding-top: 25px; padding-bottom: 25px;">
        <section class="col-sm-1"></section>
        <section class="col-sm-10">
        <div class="card">
            <div class="card-body d-flex justify-content-between">
                <h5 style="margin-top: auto; margin-bottom: auto">Nome: <?php echo($_SESSION['uname']); ?></h5>
                <a class="btn btn-secondary btn-lg" href="index.php">Logout</a>
            </div>
        </div>
        
            
            <div style="padding-bottom: 25px;"></div>
            <div class="card" style="padding: 30px 30px 30px 30px">
                <div class="row row-cols-1 row-cols-md-5 g-4">
                <?php
                $curtime = new DateTime();
                //echo $curtime->format('Y-m-d H:i:s');
                    while ($row = $result->fetch_row()) {
                     $culTime = new DateTime($row[4]);
                     //echo $curtime->format('Y-m-d H:i:s') . " " . $culTime->format('Y-m-d H:i:s');

                     $timediff = $curtime->diff($culTime);
                     //->format('%Y years %m months %d days %H hours %i minutes %s seconds');
                     switch(TRUE){
                        case $timediff->y > 0:
                            $format = "%Y anos e %m meses"; break;
                        case $timediff->m > 0:
                            $format = "%m meses e %d dias"; break;
                        case $timediff->d > 0:
                            $format = "%d dias e %H horas"; break;
                        case $timediff->h > 0:
                            $format = "%H horas e %i minutos"; break;
                        case $timediff->i > 0:
                            $format = "%i minutos e %s segundos"; break;
                        default :
                            $format = "%s segundos";
                     }

                     ?>
                    <div class="text-start col">
                        <a href = <?php echo "parametros.php?cul=" . $row[0] ; ?> style="text-decoration: none; color: black;">
                            <div class="card h-100">
                                <img src=<?php echo "lab_images/" . $row[0]%11  . ".png"; ?> class="card-img-top" alt="...">
                               <?php
                               echo "<div class='card-body'> <h5 class='card-title'> Nome: " . $row[2] ."</h5>
                               <p class='card-text'> Zona: Z" . $row[1] . "</p>
                               <p class='card-text'> Descrição: " . $row[3] . "</p>
                                </div>
                                <div class='card-footer'>
                                    <small class='text-muted'>Ultima alteração: " . $timediff->format($format) . " </small>
                                </div>" ?>
                            </div>
                        </a>
                    </div>
                    <?php }
                     mysqli_free_result($result);
                     mysqli_close($conn);
                     ?>
                </div>
            </div>
        </section>
        <section class="col-sm-1">

        </section>
    </section>
</body>

</html>
<?php
} else { echo "algo deu errado";}
?>