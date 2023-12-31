# Generated by Django 4.0.3 on 2022-04-30 23:51

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('social', '0004_rename_friendship_amizade'),
    ]

    operations = [
        migrations.AddField(
            model_name='publicacao',
            name='gostos',
            field=models.IntegerField(default=0),
        ),
        migrations.AddField(
            model_name='publicacao',
            name='nao_gostos',
            field=models.IntegerField(default=0),
        ),
        migrations.CreateModel(
            name='Comentario',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('datetime', models.DateTimeField()),
                ('text', models.CharField(max_length=500)),
                ('publicacao', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='social.publicacao')),
                ('user', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='social.utilizador')),
            ],
        ),
    ]
