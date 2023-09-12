from django import template
from django.core.exceptions import ObjectDoesNotExist
from ..models import *

register = template.Library()


@register.simple_tag
def obter_interacao(user_id, pub_id):
    try:
        user = Utilizador.objects.get(id=user_id)
        pub = Publicacao.objects.get(id=pub_id)
        interacao = Interacao.objects.get(user=user, publicacao=pub)
        if interacao.like:
            return True
        elif not interacao.like:
            return False
    except ObjectDoesNotExist:
        return None
