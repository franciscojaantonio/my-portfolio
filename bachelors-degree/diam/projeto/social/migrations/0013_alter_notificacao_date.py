# Generated by Django 4.0.3 on 2022-05-10 20:03

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('social', '0012_utilizador_amigos'),
    ]

    operations = [
        migrations.AlterField(
            model_name='notificacao',
            name='date',
            field=models.DateTimeField(),
        ),
    ]
