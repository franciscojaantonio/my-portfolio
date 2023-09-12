<?php
session_start();
$conn = @mysqli_connect("localhost",$_SESSION['uname'],$_SESSION['psw'],"pisid");
if($conn && isset($_GET["cul"]) ) {
     $_SESSION['cid'] = $_GET["cul"];
     $query = "SELECT c.IDZona,c.Nome,c.Descricao,pc.TemperaturaMinima, pc.TemperaturaMaxima,  pc.HumidadeMinima,
	 pc.HumidadeMaxima, pc.LuzMinima, pc.LuzMaxima
      FROM cultura as c INNER JOIN parametrocultura as pc ON c.IDCultura = pc.IDCultura
                            WHERE c.IDCultura = " . $_GET["cul"] .
                            " AND c.IDUtilizador= " . $_SESSION['uid'] . " LIMIT 1";
      $result = mysqli_query($conn,$query);
    if(!$result){echo "Essa cultura nao existe ou nao pertence ao investigador "; exit();}

     $row = $result->fetch_row();
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
  <title>Detalhes da cultura</title>
</head>

<body class="container-fluid" style="background-color: #f5f5f5;">
  <section class="row" style="padding-top: 25px; padding-bottom: 25px;">
    <section class="col-sm-3"></section>

    <section class="col-sm-6" style="text-align: right;">
    <div class="card mx-auto" style="width: 70%">
            <div class="card-body d-flex justify-content-between">
                <h5 style="margin-top: auto; margin-bottom: auto">Nome: <?php echo($_SESSION['uname']); ?></h5>
                <a class="btn btn-secondary" href="index.php">Logout</a>
            </div>
        </div>

      <div style="padding-bottom: 25px;"></div>

      <div class="card text-start mx-auto" style="width: 70%">

        <div class="bg-image hover-overlay ripple rounded-0 text-center bg-light" data-mdb-ripple-color="light">
          <img style="max-height: 200px" class="img-fluid"
            src=<?php echo "lab_images/" . $_SESSION['cid']%11 . ".png"; ?> alt="Card image cap" />
        </div>
        <form action="guardar.php" method="post">
          <div class="card-body">
            <h5 class="card-title"><?php echo $row[1]; ?></h5>
            <p class="card-text"><?php echo $row[2]; ?></p>
          </div>


          <ul class="list-group list-group-flush">
            <!--<li class="list-group-item">
                <div class="input-group mb-3">
                <div class="input-group-prepend">
                  <label class="input-group-text" for="inputGroupSelect01">Estado</label>
                </div>
                <select name="estado" class="custom-select" id="inputGroupSelect01">
                  <option selected>Estado atual</option>
                  <option value="1">Estado A</option>
                  <option value="2">Estado B</option>
                  <option value="3">Estado C</option>
                </select>
              </div>
            </li>-->
            <li class="list-group-item">
              <h5> <?php echo "Zona: Z" . $row[0]; ?> </h5>
            </li>
            <li class="list-group-item">
              <h5>Limites da Cultura</h5>
              <ul class="list-group list-group-flush">
                <li class="list-group-item">
                  <h5>Luz<p class="float-end">
                      De <input style = "width:30%;" type="number" step="0.01" name="luzMin" value= <?php echo $row[7]; ?> >
                      a <input style = "width:30%;" type="number" step="0.01" name="luzMax"  value= <?php echo $row[8]; ?> >
                    </p>
                  </h5>

                </li>
                <li class="list-group-item">
                  <h5>Humidade<p class="float-end">
                      De <input style = "width:30%;" type="number" step="0.01"  name="humMin" value = <?php echo $row[5]; ?> >
                      a <input  style = "width:30%;"type="number" step="0.01" name="humMax" value = <?php echo $row[6]; ?> >
                    </p>
                  </h5>
                </li>
                <li class="list-group-item">
                  <h5>Temperatura<p class="float-end">
                      De <input style = "width:30%;"  type="number" step="0.01" name="temMin" value = <?php echo $row[3]; ?> >
                      a <input style = "width:30%;"  type="number" step="0.01" name="temMax" value = <?php echo $row[4]; ?> >
                    </p>
                  </h5>
                </li>

              </ul>
            </li>
          </ul>

          <div class="card-footer">

            <a style="margin-bottom: 10px;" href="home.php" class="btn btn-secondary float-start">Voltar</a>
            <input style="margin-bottom: 10px;" type="submit" class="btn btn-primary float-end"
              value="Guardar alterações"></a>
            <?php if(isset($_GET['error'])) { echo "<p style='text-align: center;color:red;'>".$_GET['error']."</p>";} ?>
          </div>
        </form>
      </div>
    </section>
  </section>
  </body>

</html>
<?php
mysqli_free_result($result);
mysqli_close($conn);
} else { echo "algo deu errado";}
?>