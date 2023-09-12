#include "definescliente.h"

// variaveis globais
char comecou = 'f';
int tipo, pid_consulta, pid_servidor;
int status = PEDIDO;
char descricao[100];
int msg_id, mem_id, sem_id, status_sem, status_mensagem, val;
Mensagem mensagem;

int main() {
    
    // Bem-vindo
    printf("\nBem-vindo ao Cliente Cliniq-IUL!\n");

    // Obter identificador da Message Queue
    msg_id = msgget( IPC_KEY, 0 );
    exit_on_error(msg_id, "Erro no msgget!");

    // Obter PID
    pid_consulta=getpid();

    // Regista SIGINT
    signal(SIGINT, handler);
    
    // C1 Pede os dados necessários ao utilizador
    printf("Tipo de Consulta (1-Normal, 2-COVID19, 3-Urgente): ");
    scanf("%d", &tipo);
    if (tipo != 1 && tipo != 2 && tipo != 3){
        fprintf(stderr, "Opção tipo inválida!\n");
        exit(0);
    }
    printf("Descrição da Consulta: ");
    scanf(" %99[^\n]", &descricao);

    // C2 Criar mensagem
    mensagem = nova_mensagem(MSG_TYP);

    // Enviar mensagem
    status_mensagem = msgsnd(msg_id, &mensagem, sizeof(mensagem.consulta), 0);
    exit_on_error(status_mensagem, "Erro ao enviar a mensagem!");

    // Espera por mensagens do Servidor
    while(1)
        ler_mensagens();
}

// C3 Lê as mensagens do Servidor
void ler_mensagens(){
    status_mensagem = msgrcv(msg_id, &mensagem, sizeof(mensagem.consulta), pid_consulta, 0);
    exit_on_error(status_mensagem, "Erro ao receber mensagem do Servidor!");
    interpretar_mensagem(mensagem.consulta.status);
}

// C4 C5 C6 Interpreta a mensagem com base no seu valor status
void interpretar_mensagem(int x){
    switch(x) {
        case INICIADA:
            printf("Consulta iniciada para o processo %d\n", pid_consulta);
            comecou = 't';
            break;
        case TERMINADA:
            if( comecou != 't')
                printf("Erro! A consulta não foi iniciada e, portanto, não pode ser terminada para o processo %d\n", pid_consulta);
            else
                printf("Consulta concluída para o processo %d\n", pid_consulta);
            sair();
            break;
        case RECUSADA:
            printf("A Consulta foi recusada para o processo %d\n", pid_consulta);
            sair();
            break;
        default:
            printf("Erro! Não foi possível interpretar a mensagem recebida.\n");
    }
}

// C2 Cria uma mensagem
Mensagem nova_mensagem(int x) {
    Mensagem m;
    m.m_type = x;
    m.consulta.tipo = tipo;
    strcpy(m.consulta.descricao, descricao);
    m.consulta.pid_consulta = pid_consulta;
    m.consulta.status = status;
    return m;
}

// Fecha o cliente
void sair(){
    printf("\nAté Breve!\n");
    exit(0);
}

// C7 Trata o sinal SIGINT (Cancela o pedido)
void handler(int signal){
    printf("Paciente cancelou o pedido\n");
    status = CANCELADA;
    mensagem.m_type = pid_consulta;
    mensagem.consulta.status = status;
    status_mensagem = msgsnd(msg_id, &mensagem, sizeof(mensagem.consulta), 0);
    exit_on_error(status_mensagem, "Erro ao enviar mensagem para o servidor dedicado!");
    sair();
}