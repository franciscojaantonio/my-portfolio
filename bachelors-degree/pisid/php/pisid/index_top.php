<!DOCTYPE html>
<!-- saved from url=(0051)https://getbootstrap.com/docs/4.0/examples/sign-in/ -->
<html lang="en">
  <head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <link rel="icon" href="sign_in_files/flask.png">
    <title>Portal de Culturas</title>
    <link rel="canonical" href="https://getbootstrap.com/docs/4.0/examples/sign-in/">
    <!-- Bootstrap core CSS -->
    <link href="./sign_in_files/bootstrap.min.css" rel="stylesheet">
    <link type="text/css" media="screen" href="./style.css" rel="stylesheet"/>
    <!-- Custom styles for this template -->
    <link href="./sign_in_files/signin.css" rel="stylesheet">
  </head>

  <body class="text-center">
    <form class="form-signin" action="validar.php" method="post">
      <img class="mb-4" src="sign_in_files/biology.png" alt="" width="100" height="100">
      <h1 class="h3 mb-3 font-weight-normal">Iniciar sessão</h1>
      <label for="uname" class="sr-only">Nome de utilizador</label>
      <input type="text" name="uname" class="form-control" placeholder="Nome de utilizador" required="" autofocus="">
      <label for="psw" class="sr-only">Password</label>
      <input type="password" name="psw" class="form-control" placeholder="Password" required="">
      <input class="btn btn-lg btn-primary btn-block" type="submit" value="Login">
