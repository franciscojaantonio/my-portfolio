from django.urls import path
from . import views

# (. significa que importa views da mesma directoria)

app_name = 'social'
urlpatterns = [
    path("", views.loginpage, name="loginpage"),
    path('registarutilizador', views.registarutilizador, name='registarutilizador'),
    path('fazerlogin', views.fazerlogin, name='fazerlogin'),
    path('mainpage', views.mainpage, name='mainpage'),
    path('fazerlogout', views.fazerlogout, name='fazerlogout'),
    path('mainpage/criar_publicacao', views.criar_publicacao, name='criar_publicacao'),
    path('mainpage/sala_mensagens', views.sala_mensagens, name='sala_mensagens'),
    path('mainpage/sala_mensagens/obter_mensagens/<int:sala_id>', views.obter_mensagens, name='obter_mensagens'),
    path('mainpage/sala_mensagens/criar_mensagem/<int:sala_id>', views.criar_mensagem, name='criar_mensagem'),
    path('mainpage/encontrar_amigos', views.encontrar_amigos, name='encontrar_amigos'),
    path('mainpage/ver_perfil', views.ver_perfil, name='ver_perfil'),
    path('mainpage/criar_comentario/<int:pub_id>', views.criar_comentario, name='criar_comentario'),
    path('mainpage/remover_notificacao/<int:noti_id>', views.remover_notificacao, name='remover_notificao'),
    path('mainpage/remover_todas_notificacoes', views.remover_todas_notificacoes, name='remover_todas_notificoes'),
    path('mainpage/encontrar_amigos/pedir_amizade/<int:user_id>', views.pedir_amizade, name='pedir_amizade'),
    path('mainpage/encontrar_amigos/remover_pedido/<int:user_id>', views.remover_pedido, name='remover_pedido'),
    path('mainpage/encontrar_amigos/aceitar_pedido/<int:user_id>', views.aceitar_pedido, name='aceitar_pedido'),
    path('mainpage/ver_perfil/remover_amizade/<int:user_id>', views.remover_amizade, name='remover_amizade'),
    path('mainpage/ver_perfil/alterar_dados', views.alterar_dados, name='alterar_dados'),
    path('mainpage/ver_perfil/alterar_imagem_perfil', views.alterar_imagem_perfil, name='alterar_imagem_perfil'),
    path('mainpage/ver_perfil/definir_privacidade', views.definir_privacidade, name='definir_privacidade'),
    path('mainpage/dar_gosto/<int:pub_id>', views.dar_gosto, name='dar_gosto'),
    path('mainpage/dar_nao_gosto/<int:pub_id>', views.dar_nao_gosto, name='dar_nao_gosto'),
    path('mainpage/reset_filter', views.reset_filter, name='reset_filter'),
    path('mainpage/ver_perfil_utilizador/<int:user_id>', views.ver_perfil_utilizador, name='ver_perfil_utilizador'),
    path('mainpage/reportbug', views.reportarbug, name="reportbug"),
    path('mainpage/eliminar_publicacao/<int:pub_id>', views.eliminar_publicacao, name="eliminar_publicacao"),
    path('mainpage/eliminar_comentario/<int:com_id>', views.eliminar_comentario, name="eliminar_comentario"),
    path('mainpage/enviarticket', views.enviarticket, name="enviarticket"),
    path('mainpage/eventos', views.eventos, name="eventos"),
    path('mainpage/sobre', views.sobre, name="sobre"),
    path('ocorreu_erro', views.ocorreu_erro, name="ocorreu_erro"),
    path('mainpage/eventos/eliminar_evento/<int:event_id>', views.eliminar_evento, name="eliminar_evento"),
    path('mainpage/eventos/criar_evento', views.criar_evento, name="criar_evento"),
    path('mainpage/atualizar_scroll/<int:pub_id>', views.atualizar_scroll, name="atualizar_scroll"),
    path('mainpage/ticketresolve/<int:ticket_id>', views.ticketresolve, name='ticketresolve'),
]
