# Generated by Django 4.0.3 on 2022-05-01 00:07

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('social', '0006_publicacao_num_pubs'),
    ]

    operations = [
        migrations.RenameField(
            model_name='publicacao',
            old_name='num_pubs',
            new_name='num_coms',
        ),
    ]