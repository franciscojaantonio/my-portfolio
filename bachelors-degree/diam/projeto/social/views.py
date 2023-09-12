from itertools import chain

from django.db import IntegrityError
from pytube import extract
from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.decorators import login_required
from django.contrib.auth.models import User
from django.core.exceptions import ObjectDoesNotExist
from django.core.files.storage import FileSystemStorage
from django.db.models import Q
from django.shortcuts import render, get_object_or_404
from django.http import HttpResponseRedirect, JsonResponse
from datetime import datetime, timedelta

from django.utils import timezone
from pytube.exceptions import RegexMatchError

from .models import Utilizador, Publicacao, Amizade, Notificacao, Comentario, Sala, Mensagem, Interacao, Ticket, Evento
from django.urls import reverse, reverse_lazy


def loginpage(request):
    return render(request, 'social/login.html')


def fazerlogin(request):
    context = {}
    if request.method == 'POST':
        username = request.POST['username']
        password = request.POST['password']
        if username and password:
            user = authenticate(username=username, password=password)
            if user is not None:
                login(request, user)
                return HttpResponseRedirect(reverse('social:mainpage'))
            else:
                context = {'msg_invalid_input': "Login inválido! Por favor, tente novamente"}
        else:
            context = {'msg_invalid_input': "Login inválido! É necessário inserir username e password"}
    return render(request, 'social/login.html', context)


def registarutilizador(request):
    image = None
    context = {}
    default_image_url = "media/profile_images/no_image.png"
    if request.method == "POST":
        try:
            image = request.FILES['myfile']
            image_exists = True
        except KeyError:
            image_exists = False
        username = request.POST.get("username")
        password = request.POST.get("password")
        fname = request.POST.get("fname")
        lname = request.POST.get("lname")
        email = request.POST.get("email")
        tel = request.POST.get("tel")
        dataaniv = request.POST.get("dataaniv")
        gender = request.POST.get("gender")
        if valid_input([username, password, fname, lname, email, dataaniv, gender], [2, 2, 2, 2, 2]):
            data = datetime.strptime(dataaniv, '%Y-%m-%d')
            user = User.objects.create_user(username=username, email=email, password=password, first_name=fname, last_name=lname)
            if image_exists:
                fs = FileSystemStorage()
                filename = fs.save("profile_images/" + username + "." + image.name.split(".")[1], image)
                utilizador = Utilizador(user=user, tel=tel, birthday=data, gender=gender, imageUrl="media/" + filename)
            else:
                utilizador = Utilizador(user=user, tel=tel, birthday=data, gender=gender, imageUrl=default_image_url)
            try:
                utilizador.save()
                Evento.objects.create(tipo="Aniversário", user=utilizador, descricao="Aniversário de " + str(utilizador),
                                      datetime=datetime.combine(data.replace(year=timezone.now().year), datetime.min.time()), is_aniversario=True)
                context = {'msg_successful_registration': "Registado com sucesso!"}
            except IntegrityError:
                context = {'msg_invalid_registration': 'Registo inválido! Este username já se encontra registado'}
            try:
                del request.FILES['myfile']
            except KeyError:
                pass
        else:
            context = {'msg_invalid_registration': "Registo inválido! Por favor, tente novamente"}
    return render(request, 'social/login.html', context)


def delete_jump_to(request):
    try:
        del request.session['jump_to']
    except KeyError:
        pass


def valid_input(lista_inputs, lista_length):
    for i in range(len(lista_length)):
        if len(str(lista_inputs[i]).replace(" ", "")) < lista_length[i]:
            return False
    if lista_inputs[5] is None:
        return False
    if lista_inputs[6] != "Masculino" and lista_inputs[5] != "Feminino" and lista_inputs[5] != "Outro":
        return False
    return True


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def fazerlogout(request):
    logout(request)
    return HttpResponseRedirect(reverse('social:loginpage'))


def obter_publicacoes_filtradas(request, sort, time):
    options_sort = ['-date', 'date', '-gostos', '-nao_gostos', '-num_coms']
    options_time = [1, 3, 7, 30, -1]
    sort_string = options_sort[sort - 1]
    time_days = options_time[time - 1]
    if time_days > 0:
        return Publicacao.objects.filter(date__gt=timezone.now()-timedelta(days=time_days)).order_by(sort_string)
    return Publicacao.objects.order_by(sort_string)


def filtro_valido(sort, time):
    aux = [1, 2, 3, 4, 5]
    if sort in aux and time in aux:
        return True
    return False


@login_required(login_url=reverse_lazy('social:loginpage'))
def mainpage(request):
    filtro = False
    pubs_list = []
    comentarios = []
    sort, time = 0, 0
    if request.method == 'POST':
        try:
            sort = int(request.POST.get('sort'))
            time = int(request.POST.get('time'))
            del request.session['jump_to']
        except (ValueError, KeyError):
            pass
    else:
        try:
            sort = int(request.session['sort'])
            time = int(request.session['time'])
        except (KeyError, ValueError):
            pass
    if filtro_valido(sort, time):
        request.session['sort'] = sort
        request.session['time'] = time
        filtro = True
        pubs = obter_publicacoes_filtradas(request, sort, time)
    else:
        pubs = Publicacao.objects.order_by('-date')
    if not request.user.is_superuser:
        for pub in pubs:
            if pub.user in get_friends_list_with_confirm(request, True) or pub.user == request.user.utilizador or pub.user.user.is_superuser:
                pubs_list.append(pub)
                pub_comentarios = Comentario.objects.filter(publicacao=pub).order_by('-datetime')
                comentarios = list(chain(comentarios, pub_comentarios))
    else:
        for pub in pubs:
            pubs_list.append(pub)
            pub_comentarios = Comentario.objects.filter(publicacao=pub).order_by('-datetime')
            comentarios = list(chain(comentarios, pub_comentarios))
    interacoes = Interacao.objects.filter(user=request.user.utilizador)
    context = {'pubs_list': pubs_list, 'notifications': obter_notificacoes(request), 'comentarios': comentarios, 'interacoes': interacoes, 'filtro': filtro}
    return render(request, 'social/main_page.html', context)


@login_required(login_url=reverse_lazy('social:loginpage'))
def reset_filter(request):
    aux = ['time', 'sort', 'jump_to']
    for key in aux:
        try:
            del request.session[key]
        except KeyError:
            pass
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:fazerlogin'))
def criar_publicacao(request):
    if request.method == 'POST':
        image = None
        title = request.POST.get("title")
        textarea = request.POST.get("textarea")
        url = request.POST.get("myvideo")
        try:
            url = extract.video_id(url)
        except RegexMatchError:
            url = "none"
        user = request.user.utilizador
        if title and textarea:
            new_pub = Publicacao.objects.create(user=user, videoUrl=url, date=timezone.now(), title=title, text=textarea)
            delete_jump_to(request)
            try:
                image = request.FILES['myfile']
                image_exists = True
            except KeyError:
                image_exists = False
            if image_exists and url == 'none':
                fs = FileSystemStorage()
                filename = fs.save("pub_images/pub#" + str(new_pub.id) + "." + image.name.split(".")[1], image)
                new_pub.imageUrl = "media/" + filename
                new_pub.save()
                user.pubs += 1
                user.save()
                Notificacao.objects.create(user=request.user.utilizador, date=timezone.now(), title="Nova publicação",
                                           text="A sua nova publicação já está visível para si e para os seus amigos")
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:fazerlogin'))
def encontrar_amigos(request):
    # Utilizadores que ainda não têm nenhuma relação
    nao_amigos = []
    users = Utilizador.objects.all().exclude(id=request.user.utilizador.id)
    for user in users:
        if user not in get_amigos_pendentes_e_confirmados(request):
            if not user.user.is_superuser:
                nao_amigos.append(user)

    # Amizades que ainda não foram confirmadas
    amigos_pendentes = get_friends_list_with_confirm(request, False)

    # Amizades que posso aceitar
    para_aceitar = []
    amizades_para_aceitar = Amizade.objects.filter(user2=request.user.utilizador, confirmed=False).order_by('date')
    for amizade in amizades_para_aceitar:
        para_aceitar.append(amizade.user1)
    context = {'users_list': nao_amigos, 'amigos_pendentes': amigos_pendentes, 'notifications': obter_notificacoes(request), 'para_aceitar': para_aceitar}
    return render(request, 'social/find_friends.html', context)


def get_amigo_from_amizade(request, amizade):
    if amizade.user1 == request.user.utilizador:
        return amizade.user2
    elif amizade.user2 == request.user.utilizador:
        return amizade.user1
    return None


def get_friends_list_with_confirm(request, confirmed):
    friends = []
    friends_list = Amizade.objects.filter(Q(confirmed=confirmed),
                                          (Q(user1=request.user.utilizador) |
                                           Q(user2=request.user.utilizador))).order_by('-date')
    for amizade in friends_list:
        if amizade.user1 == request.user.utilizador:
            friends.append(amizade.user2)
        elif amizade.user2 == request.user.utilizador:
            friends.append(amizade.user1)
    return friends


def get_friends_list_with_confirm_by_id(user_id, confirmed):
    user = get_object_or_404(Utilizador, pk=user_id)
    friends = []
    friends_list = Amizade.objects.filter(Q(confirmed=confirmed), (Q(user1=user) | Q(user2=user))).order_by('-date')
    for amizade in friends_list:
        if amizade.user1 == user:
            friends.append(amizade.user2)
        elif amizade.user2 == user:
            friends.append(amizade.user1)
    return friends


def get_amigos_pendentes_e_confirmados(request):
    friends = []
    friends_list = Amizade.objects.filter(Q(user1=request.user.utilizador) | Q(user2=request.user.utilizador)).order_by('date')
    for amizade in friends_list:
        if amizade.user1 == request.user.utilizador:
            friends.append(amizade.user2)
        elif amizade.user2 == request.user.utilizador:
            friends.append(amizade.user1)
    return friends


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def ver_perfil(request):
    friends = get_friends_list_with_confirm(request, True)
    context = {'friends_list': friends, 'notifications': obter_notificacoes(request)}
    return render(request, 'social/ver_perfil.html', context)


@login_required(login_url=reverse_lazy('social:fazerlogin'))
def pedir_amizade(request, user_id):
    if not amizade_existe(request, user_id):
        user2 = get_object_or_404(Utilizador, pk=user_id)
        pedido = Amizade.objects.create(user1=request.user.utilizador, user2=user2)
        pedido.save()
        Notificacao.objects.create(user=user2, date=timezone.now(), title="Pedido de amizade",
                                   text=str(request.user.utilizador) + " quer ser seu amigo")
    return HttpResponseRedirect(reverse('social:encontrar_amigos'))


def amizade_existe(request, user_id):
    user2 = get_object_or_404(Utilizador, pk=user_id)
    pedido1 = Amizade.objects.filter(user1=request.user.utilizador, user2=user2)
    pedido2 = Amizade.objects.filter(user1=user2, user2=request.user.utilizador)
    if pedido1.count() == 0 and pedido2.count() == 0:
        return False
    return True


def remover_pedido_friend(request, user_id, confirmed):
    user2 = get_object_or_404(Utilizador, pk=user_id)
    pedido = Amizade.objects.filter(user1=request.user.utilizador, user2=user2)
    if pedido.count() < 1:
        pedido = Amizade.objects.filter(user1=user2, user2=request.user.utilizador)
    if pedido.count() > 0:
        if pedido.first().confirmed is confirmed:
            pedido.delete()


@login_required(login_url=reverse_lazy('social:fazerlogin'))
def remover_pedido(request, user_id):
    remover_pedido_friend(request, user_id, False)
    return HttpResponseRedirect(reverse('social:encontrar_amigos'))


@login_required(login_url=reverse_lazy('social:fazerlogin'))
def remover_amizade(request, user_id):
    remover_pedido_friend(request, user_id, True)
    user2 = get_object_or_404(Utilizador, pk=user_id)
    user = request.user.utilizador
    user.amigos -= 1
    user2.amigos -= 1
    user.save()
    user2.save()
    Notificacao.objects.create(user=user2, date=timezone.now(), title="Fim de Amizade",
                               text=str(request.user.utilizador) + " deixou de ser seu amigo")
    Notificacao.objects.create(user=request.user.utilizador, date=timezone.now(), title="Fim de Amizade",
                               text=str(user2) + " deixou de ser seu amigo")
    return HttpResponseRedirect(reverse('social:ver_perfil'))


@login_required(login_url=reverse_lazy('social:fazerlogin'))
def aceitar_pedido(request, user_id):
    user = request.user.utilizador
    user1 = get_object_or_404(Utilizador, pk=user_id)
    pedido = Amizade.objects.get(user1=user1, user2=request.user.utilizador)
    pedido.confirmed = True
    pedido.date = timezone.now()
    pedido.save()
    user.amigos += 1
    user1.amigos += 1
    user.save()
    user1.save()
    Notificacao.objects.create(user=user1, date=timezone.now(), title="Confirmação de amizade",
                               text="Tu e " + str(request.user.utilizador) + " são agora amigos")
    Notificacao.objects.create(user=request.user.utilizador, date=timezone.now(), title="Confirmação de amizade",
                               text="Tu e " + str(user1) + " são agora amigos")
    return HttpResponseRedirect(reverse('social:encontrar_amigos'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def remover_notificacao(request, noti_id):
    notificao = get_object_or_404(Notificacao, pk=noti_id)
    notificao.delete()
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def remover_todas_notificacoes(request):
    notificacoes = Notificacao.objects.filter(user=request.user.utilizador)
    for noti in notificacoes:
        noti.delete()
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def criar_comentario(request, pub_id):
    if request.method == 'POST':
        text = request.POST.get("textarea")
        pub = get_object_or_404(Publicacao, pk=pub_id)
        if text and pub:
            Comentario.objects.create(user=request.user.utilizador, publicacao=pub, datetime=timezone.now(), text=text)
            pub.num_coms += 1
            pub.save()
            request.session['jump_to'] = 'pub#' + str(pub_id)
            Notificacao.objects.create(user=pub.user, date=timezone.now(),
                                       title="Novo comentário", text=str(request.user.utilizador) + " adicionou um comentário à sua publicação")
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def sala_mensagens(request):
    if request.user.is_superuser:
        contactos = Utilizador.objects.all().exclude(id=request.user.utilizador.id)
    else:
        contactos = get_friends_list_with_confirm(request, True)
    if len(contactos) < 1:
        return HttpResponseRedirect(reverse('social:mainpage'))
    if request.method == 'POST':
        contacto_id = request.POST.get("value")
        if contacto_id:
            request.session['contacto'] = contacto_id
    try:
        contacto = get_object_or_404(Utilizador, pk=request.session['contacto'])
    except KeyError:
        contacto = contactos[0]
        request.session['contacto'] = contactos[0].id
    sala = selecionar_sala_entre_users(request, contacto)
    context = {'contactos': contactos, 'contacto': contacto, 'sala': sala, 'proprio': request.user.utilizador}
    return render(request, 'social/mensagens.html', context)


def criar_mensagem(request, sala_id):
    try:
        if request.method == 'POST':
            sala = Sala.objects.get(id=sala_id)
            textarea = request.POST['text']
            if str(textarea).replace(" ", ""):
                Mensagem.objects.create(user=request.user.utilizador, text=textarea, sala=sala, datetime=timezone.now())
                return JsonResponse({'status': 'OK'})
    except ObjectDoesNotExist:
        pass
    return JsonResponse({'status': 'ERROR'})


def selecionar_sala_entre_users(request, user2):
    user = request.user.utilizador
    try:
        sala = Sala.objects.get(user1=user, user2=user2)
    except ObjectDoesNotExist:
        try:
            sala = Sala.objects.get(user1=user2, user2=user)
        except ObjectDoesNotExist:
            sala = Sala.objects.create(user1=user, user2=user2, datetime_creation=timezone.now())
    return sala


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def dar_gosto(request, pub_id):
    pub = get_object_or_404(Publicacao, pk=pub_id)
    try:
        interacao = Interacao.objects.get(user=request.user.utilizador, publicacao=pub)
        if interacao.like:
            interacao.delete()
            pub.gostos -= 1
        elif not interacao.like:
            interacao.like = True
            pub.nao_gostos -= 1
            pub.gostos += 1
            Notificacao.objects.create(user=pub.user, date=timezone.now(), title="Novo Gosto",
                                       text=str(request.user.utilizador) + " gostou da sua publicação")
            interacao.save()
    except ObjectDoesNotExist:
        Interacao.objects.create(user=request.user.utilizador, publicacao=pub, like=True)
        pub.gostos += 1
        Notificacao.objects.create(user=pub.user, date=timezone.now(), title="Novo Gosto",
                                   text=str(request.user.utilizador) + " gostou da sua publicação")
    pub.save()
    request.session['jump_to'] = 'pub#' + str(pub_id)
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def dar_nao_gosto(request, pub_id):
    pub = get_object_or_404(Publicacao, pk=pub_id)
    try:
        interacao = Interacao.objects.get(user=request.user.utilizador, publicacao=pub)
        if not interacao.like:
            interacao.delete()
            pub.nao_gostos -= 1
        elif interacao.like:
            interacao.like = False
            pub.nao_gostos += 1
            pub.gostos -= 1
            Notificacao.objects.create(user=pub.user, date=timezone.now(), title="Novo Não Gosto",
                                       text=str(request.user.utilizador) + " não gostou da sua publicação")
            interacao.save()
    except ObjectDoesNotExist:
        Interacao.objects.create(user=request.user.utilizador, publicacao=pub, like=False)
        pub.nao_gostos += 1
        Notificacao.objects.create(user=pub.user, date=timezone.now(), title="Novo Não Gosto",
                                   text=str(request.user.utilizador) + " não gostou da sua publicação")
    pub.save()
    request.session['jump_to'] = 'pub#' + str(pub_id)
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def alterar_dados(request):
    user = request.user.utilizador
    if request.method == 'POST':
        text = request.POST.get("textarea")
        if str(text).replace(" ", ""):
            user.biografia = text
            user.save()
    return HttpResponseRedirect(reverse('social:ver_perfil'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def alterar_imagem_perfil(request):
    image = None
    try:
        image = request.FILES['new_image']
        image_exists = True
    except KeyError:
        image_exists = False
    if image_exists:
        fs = FileSystemStorage()
        path = request.user.utilizador.imageUrl.replace("media/", "")
        if fs.exists(path):
            fs.delete(path)
        fs.save(path, image)
    return HttpResponseRedirect(reverse('social:ver_perfil'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def ver_perfil_utilizador(request, user_id):
    user = get_object_or_404(Utilizador, pk=user_id)
    amigo = amizade_existe(request, user_id)
    amigos = get_friends_list_with_confirm_by_id(user.id, True)
    context = {'utilizador': user, 'notifications': obter_notificacoes(request), 'amigo': amigo, 'friends_list': amigos}
    return render(request, 'social/ver_perfil_utilizador.html', context)


def obter_mensagens(request, sala_id):
    sala = get_object_or_404(Sala, pk=sala_id)
    mensagens = Mensagem.objects.filter(sala=sala).order_by('datetime')
    if mensagens.count() > 0:
        return JsonResponse({'mensagens': list(mensagens.values())})
    else:
        return JsonResponse({'mensagens': list(mensagens.values())}, status=404)


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def definir_privacidade(request):
    user = request.user.utilizador
    user.private = not user.private
    user.save()
    return HttpResponseRedirect(reverse('social:ver_perfil'))


@login_required(login_url=reverse_lazy('social:ocorreu_erro'))
def enviarticket(request):
    user = request.user.utilizador
    title = request.POST.get("title")
    text = request.POST.get("assunto")
    if title and text:
        Ticket.objects.create(user=user, title=title, assunto=text, datetime=timezone.now())
    return HttpResponseRedirect(reverse('social:reportbug'))


@login_required(login_url=reverse_lazy('social:error404'))
def reportarbug(request):
    ticket_list = Ticket.objects.all()
    ticket_list_ativos = Ticket.objects.filter(closed=False)
    context = {'notifications': obter_notificacoes(request), 'ticket_list': ticket_list, 'ticket_list_ativos': ticket_list_ativos}
    return render(request, 'social/reportbug.html', context)


@login_required(login_url=reverse_lazy('social:error404'))
def ticketresolve(request, ticket_id):
    ticket = get_object_or_404(Ticket, pk=ticket_id)
    context = {'notifications': obter_notificacoes(request), 'ticket': ticket}
    if request.method == 'POST':
        resposta = request.POST.get("resposta")
        closed = request.POST.get("closed")
        if closed:
            ticket.closed = True
        ticket.resposta = resposta
        ticket.save()
        Notificacao.objects.create(user=ticket.user, date=timezone.now(), title="Resposta Ticket",
                                   text="Tem uma nova resposta ao seu Ticket")
        return HttpResponseRedirect(reverse('social:reportbug'))
    return render(request, 'social/ticketresolve.html', context)


@login_required(login_url=reverse_lazy('social:error404'))
def eliminar_publicacao(request, pub_id):
    try:
        publicacao = Publicacao.objects.get(id=pub_id)
        if request.user.is_superuser or request.user.utilizador == publicacao.user:
            if publicacao.videoUrl == 'none':
                fs = FileSystemStorage()
                path = publicacao.imageUrl.replace("media/", "")
                if fs.exists(path):
                    fs.delete(path)
            publicacao.delete()
    except ObjectDoesNotExist:
        pass
    return HttpResponseRedirect(reverse('social:mainpage'))


@login_required(login_url=reverse_lazy('social:error404'))
def eliminar_comentario(request, com_id):
    try:
        comentario = Comentario.objects.get(id=com_id)
        if request.user.is_superuser or request.user.utilizador == comentario.user:
            pub = comentario.publicacao
            pub.num_coms -= 1
            pub.save()
            comentario.delete()
    except ObjectDoesNotExist:
        pass
    return HttpResponseRedirect(reverse('social:mainpage'))


def atualizar_scroll(request, pub_id):
    request.session['jump_to'] = 'pub#' + str(pub_id)
    return JsonResponse({'output': 'Done'})


def remover_eventos_antigos():
    events = Evento.objects.filter(datetime__lt=timezone.now())
    for event in events:
        if event.is_aniversario:
            criar_evento_aniversario(event)
        event.delete()


def criar_evento_aniversario(event):
    new_date = event.datetime.replace(year=event.datetime.year + 1)
    Evento.objects.create(tipo="Aniversário", user=event.user, descricao="Aniversário de " + str(event.user), datetime=new_date, is_aniversario=True)


@login_required(login_url=reverse_lazy('social:error404'))
def eventos(request):
    events_list = []
    remover_eventos_antigos()
    events = Evento.objects.all().order_by("datetime")
    if not request.user.is_superuser:
        for event in events:
            if event.user in get_friends_list_with_confirm(request, True) or event.user == request.user.utilizador or event.user.user.is_superuser:
                events_list.append(event)
    else:
        events_list = list(events)
    context = {'notifications': obter_notificacoes(request), 'eventos': events_list}
    return render(request, 'social/eventos.html', context)


@login_required(login_url=reverse_lazy('social:error404'))
def criar_evento(request):
    if request.method == 'POST':
        tipo = request.POST.get('tipo')
        descricao = request.POST.get('descricao')
        time = request.POST.get('data')
        if tipo and descricao and time:
            Evento.objects.create(tipo=tipo, user=request.user.utilizador, descricao=descricao, datetime=time)
    return HttpResponseRedirect(reverse('social:eventos'))


@login_required(login_url=reverse_lazy('social:error404'))
def eliminar_evento(request, event_id):
    try:
        event = Evento.objects.get(id=event_id)
        if request.user.is_superuser or request.user.utilizador == event.user:
            event.delete()
    except ObjectDoesNotExist:
        pass
    return HttpResponseRedirect(reverse('social:eventos'))


def obter_notificacoes(request):
    return Notificacao.objects.filter(user=request.user.utilizador).order_by("-date")


@login_required(login_url=reverse_lazy('social:error404'))
def sobre(request):
    return render(request, 'social/sobre.html')


def ocorreu_erro(request):
    return render(request, 'social/error.html')
