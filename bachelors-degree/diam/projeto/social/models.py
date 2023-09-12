from django.contrib.auth.models import User
from django.db import models


class Utilizador(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE)
    birthday = models.DateField()
    biografia = models.CharField(max_length=400, blank=True, null=True)
    gender = models.CharField(max_length=20)
    amigos = models.IntegerField(default=0)
    imageUrl = models.CharField(max_length=200, default="media/no_image.png")
    tel = models.CharField(max_length=9)
    pubs = models.IntegerField(default=0)
    private = models.BooleanField(default=False)

    def __str__(self):
        return self.user.first_name + " " + self.user.last_name


class Publicacao(models.Model):
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    date = models.DateTimeField()
    imageUrl = models.CharField(max_length=200, blank=True, null=True)
    videoUrl = models.CharField(max_length=200)
    title = models.CharField(max_length=100, blank=True, null=True)
    text = models.CharField(max_length=750)
    num_coms = models.IntegerField(default=0)
    gostos = models.IntegerField(default=0)
    nao_gostos = models.IntegerField(default=0)


class Amizade(models.Model):
    user1 = models.ForeignKey(Utilizador, on_delete=models.CASCADE, related_name='user1')
    user2 = models.ForeignKey(Utilizador, on_delete=models.CASCADE, related_name='user2')
    confirmed = models.BooleanField(default=False)
    date = models.DateTimeField(null=True, blank=True)

    def __str__(self):
        return self.user1.user.first_name + " é amigo de " + self.user2.user.first_name


class Notificacao(models.Model):
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    date = models.DateTimeField()
    vista = models.BooleanField(default=False)
    title = models.CharField(max_length=20)
    text = models.CharField(max_length=200)


class Comentario(models.Model):
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    publicacao = models.ForeignKey(Publicacao, on_delete=models.CASCADE)
    datetime = models.DateTimeField()
    text = models.CharField(max_length=500)


class Sala(models.Model):
    user1 = models.ForeignKey(Utilizador, on_delete=models.CASCADE, related_name='user1_sala')
    user2 = models.ForeignKey(Utilizador, on_delete=models.CASCADE, related_name='user2_sala')
    datetime_creation = models.DateTimeField()


class Mensagem(models.Model):
    text = models.CharField(max_length=400)
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    sala = models.ForeignKey(Sala, on_delete=models.CASCADE)
    datetime = models.DateTimeField()


class Interacao(models.Model):
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    publicacao = models.ForeignKey(Publicacao, on_delete=models.CASCADE)
    like = models.BooleanField(default=True)


class Ticket(models.Model):
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    title = models.CharField(max_length=100, blank=True, null=True)
    assunto = models.CharField(max_length=500)
    datetime = models.DateTimeField()
    closed = models.BooleanField(default=False)
    resposta = models.CharField(max_length=500, blank=True, null=True)


class Evento(models.Model):
    user = models.ForeignKey(Utilizador, on_delete=models.CASCADE)
    tipo = models.CharField(max_length=50)
    descricao = models.CharField(max_length=200)
    datetime = models.DateTimeField()
    is_aniversario = models.BooleanField(default=False)
