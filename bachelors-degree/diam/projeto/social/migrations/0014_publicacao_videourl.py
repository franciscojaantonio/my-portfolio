# Generated by Django 4.0.3 on 2022-05-11 01:06

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('social', '0013_alter_notificacao_date'),
    ]

    operations = [
        migrations.AddField(
            model_name='publicacao',
            name='videoUrl',
            field=models.CharField(default='none', max_length=200),
            preserve_default=False,
        ),
    ]
