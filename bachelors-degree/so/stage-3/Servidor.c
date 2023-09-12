#include "definesservidor.h"

// variaveis globais
int pid_servidor;
SharedMemory* sh; // Apontador memória partilhada
int sem_id, status_sem, val, mem_id, msg_id, status_mensagem; // Id's  

int main() {

    // Cria um semáforo para sincronização
    sem_id = semget( IPC_KEY, 1, IPC_CREAT | 0666 );
    exit_on_error(sem_id, "semget");
    
    // Status do semáforo
    status_sem = semctl(sem_id, 0, SETVAL, 0);
    val = semctl(sem_id, 0, GETVAL);
    exit_on_error(status_sem, "semctl(SETVAL)");
    printf("Status semáforo: %d\n", val);
    
    // S1 Cria memória partilhada / inicializa valores (se necessário) / define apontador
    mem_id = shmget( IPC_KEY, sizeof(SharedMemory) , IPC_CREAT | IPC_EXCL | 0666 );
    if(mem_id == -1){
        if(errno == EEXIST) { // Se a memória partilhada existir
            mem_id = shmget( IPC_KEY, sizeof(SharedMemory) , 0 );
            sh = (SharedMemory *) shmat(mem_id, NULL, 0);
            exit_on_null(sh, "shmat");
            printf("Estou a usar a memória partilhada id=%d\n", mem_id);
        }
    }
    else { // Se a memória partilhada não existir
        printf("Criei a memória partilhada id=%d\n", mem_id);
        sh = (SharedMemory *) shmat(mem_id, NULL, 0);
        exit_on_null(sh, "shmat");
        // Inicializa valores tipo de Consulta
        for(int i=0; i < N; i++)
            sh->lista_consultas[i].tipo=-1;
        // Inicializa contadores de tipos de consulta
        for(int i=0; i < TC; i++)
            sh->tipos[i]=0;
    }
    
    // Cria fila de mensagens
    msg_id = msgget( IPC_KEY, IPC_CREAT | 0666 );
    exit_on_error(msg_id, "Erro no msgget");

    // Registar sinais
    signal(SIGALRM, handler_alarm);
    signal(SIGINT, handler_int);
    signal(SIGCHLD, SIG_IGN); // S5 Remove os processos zombie

    // Coloca o semáforo a 1
    sem_up();

    // S2 Espera por mensagens do Cliente
    while (1)
        ler_mensagens();       
}

// Lê as mensagens do Cliente
void ler_mensagens() {
    Mensagem m;
    status_mensagem = msgrcv(msg_id, &m, sizeof(m.consulta), MSG_TYP, 0);
    exit_on_error(status_mensagem, "Erro ao receber a mensagem!");
    interpretar_mensagens(m.consulta.status, m);
}

// S3 S3.1 S3.2 Interpreta as mensagens com base no seu status de consulta
void interpretar_mensagens(int x, Mensagem m){
    switch(x) {
        case 1:
            printf("Chegou novo pedido de consulta do tipo: %d, descrição: %s e PID: %d\n", m.consulta.tipo, m.consulta.descricao, m.consulta.pid_consulta);
            iniciar_consulta(m);
            break;
        default:
            printf("Erro! Não foi possível interpretar a mensagem recebida.\n");
    }
}

// Envia mensagem ao cliente com a respetiva resposta
void responder_cliente(int x, Mensagem m){
    m.m_type=m.consulta.pid_consulta;
    m.consulta.status=x;
    status_mensagem = msgsnd(msg_id, &m, sizeof(m.consulta), 0);
    exit_on_error(status_mensagem, "Erro ao enviar mensagem ao cliente!");
}

// Adiciona a consulta à lista_consultas na memória partilhada
void adicionar_consulta(Mensagem m, int sala){
    Consulta c;
    c.tipo=m.consulta.tipo;
    strcpy(c.descricao, m.consulta.descricao);
    c.pid_consulta=m.consulta.pid_consulta;
    c.status=m.consulta.status;
    sh->lista_consultas[sala]=c;
}

// S3.3.2 Servidor dedicado que trata das consultas
void servidor_dedicado(Mensagem m){
    // Verifica existência de vagas
    int sala = verificar_vaga();
    if (sala < 0) {
        responder_cliente(RECUSADA, m);
        exit(0);
    }    
    // Retira o acesso às operações 
    sem_down();

    adicionar_consulta(m, sala);
    printf("Consulta agendada para a sala %d\n", sala);
    update_contadores(m.consulta.tipo);
    
    // Inicia uma consulta de DURACAO segundos
    responder_cliente(INICIADA, m);
    alarm(DURACAO);
    sem_up();

    // S3.3.3 Preparar para receber mensagem ou para cancelar
    status_mensagem = msgrcv(msg_id, &m, sizeof(m.consulta), m.consulta.pid_consulta, 0);
    if(status_mensagem == -1) {
        if(errno == EINTR) {
            printf("Consulta terminada na sala %d\n", sala);
            responder_cliente(TERMINADA, m);
            sem_down();
            apaga_consulta(m.consulta.pid_consulta, N);
            sem_up();
        }
    }
    else {
        if(m.consulta.status == CANCELADA) {
            printf("Consulta cancelada pelo utilizador %d\n", m.consulta.pid_consulta);
            sem_down();
            apaga_consulta(m.consulta.pid_consulta, N);
            sem_up();
        }
    }
}

// S3.3 Ativa o servidor dedicado que dá início à consulta
void iniciar_consulta(Mensagem m){
    pid_t parent = fork();
    if ( !parent ) {  // Servidor Dedicado gerido pelo filho
        servidor_dedicado(m);
        exit(0); 
    }// Pai segue para receber novos pedidos
}

// Atualizar os contadores na memória partilhada
void update_contadores(int x){
    if (x == 1)
        sh->tipos[1]++;
    else if (x == 2)
        sh->tipos[2]++;
    else if (x == 3)
        sh->tipos[3]++;
}

// S3.3.1 Verifica se existe vaga para consulta
int verificar_vaga(){
    int vaga = -1;
    int sala = -1;
    for(int i=0; i<N; i++){
        if (sh->lista_consultas[i].tipo < 0) {
            vaga = 1;
            sala = i;
            break;
        }
    }
    if (vaga != 1) {
        printf("Lista de consultas cheia!\n");
        sh->tipos[0]++;
    }
    return sala;
}

// Apaga uma consulta da memória
void apaga_consulta(int pid, int size) {
    for( int i = 0; i < size; i++ ) {
        if ( sh->lista_consultas[i].pid_consulta == pid ) {
            sh->lista_consultas[i].tipo = -1;
            break;
        }
    }
}

// Dispara o alarme apenas para informar o servidor dedicado do fim da consulta
void handler_alarm(int signal){
}

// S4 Trata o sinal SIGINT
void handler_int(int signal){
    printf("\nEstatísticas (Tipo Consuta):\n");  
    printf("Perdidas = %d\n", sh->tipos[0]);
    printf("(1) Normal = %d\n", sh->tipos[1]);
    printf("(2) Covid-19 = %d\n", sh->tipos[2]);
    printf("(3) Urgente = %d\n", sh->tipos[3]);
    sair();
}

// Fecha o servidor
void sair(){
    printf("\nAté Breve!\n");
    exit(0);
}

// Baixar o valor do semáforo
void sem_down(){
    struct sembuf DOWN = { .sem_op = -1 };
    status_sem = semop(sem_id, &DOWN, 1);
    exit_on_error(status_sem, "DOWN");
    val = semctl(sem_id, 0, GETVAL);
}

// Sobe o valor do semáforo
void sem_up(){
    struct sembuf UP = { .sem_op = +1 };
    status_sem = semop(sem_id, &UP, 1);
    exit_on_error(status_sem, "UP");
    val = semctl(sem_id, 0, GETVAL);
}