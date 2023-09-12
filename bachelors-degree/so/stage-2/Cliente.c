#include "clientFunctions.h"

// variaveis globais
int tipo;
char descricao[100];
int pid_servidor, pid_consulta;
char comecou='f'; // para saber se a consulta teve início (t/f)
Consulta consulta;

// main
int main() {   
    printf("\nBem vindo ao Cliente Cliniq-IUL!\n");
    registar_sinais();
    
    pid_consulta=getpid();
    // C1
    printf("Tipo de Consulta (1-Normal, 2-COVID19, 3-Urgente): ");
    scanf("%d", &tipo);
    if (tipo != 1 && tipo != 2 && tipo != 3){
        fprintf(stderr, "Opção tipo inválida!\n\n");
        exit(0);
    }
    printf("Descrição da Consulta: ");
    scanf(" %99[^\n]", &descricao);
    if(validar_descricao() < 0) {
        fprintf(stderr, "Erro: Descrição da consulta inválida!\n");
        exit(0);
    }
    // C2 C8
    if (verificar_disponibilidade() == 't'){
        escrever_dados_consulta();
        // C3
        inicio_consulta();
    }
    else 
        alarm(10);
    
    while(1) 
        pause();
    return 0;
}

// garantir que o cliente apenas apaga o ficheiro se este corresponder à sua consulta
int verificar_pid_consulta(){
    if ( access("PedidoConsulta.txt", F_OK ) != 0 )
        return -1;
    char p[50];
    FILE* file = fopen("PedidoConsulta.txt", "r");
    if (file == NULL){
        fprintf(stderr, "Erro: Não foi possível abrir PedidoConsulta.txt para leitura!\n\n");
        exit(0);
    } else {
        for(int i=0; i<3; i++)
            fgets(p, 100, file);
    }
    fclose(file);
    if(atoi(p) == consulta.pid_consulta)
        return 1;
    return -1;
}

// remover um ficheiro dado o seu nome 
void remover_ficheiro(char *filename){
    int rm = remove(filename);
    if (rm != 0) 
        fprintf(stderr, "Erro: Não foi possível remover %s!\n\n", filename);
}

// Devolve uma Consulta com os campos devidamente preenchidos 
Consulta nova_consulta() {
    Consulta c;
    c.tipo = tipo;
    strcpy(c.descricao, descricao);
    c.pid_consulta = pid_consulta;
    return c;
}

// Auxiliar C2
void escrever_dados_consulta(){
    consulta = nova_consulta();
    FILE* file = fopen("PedidoConsulta.txt", "w");
    if (file == NULL){
        fprintf(stderr, "Erro: Não foi possível escrever no ficheiro PedidoConsulta.txt!\n\n");
        exit(0);
    } 
    else { 
        fprintf(file, "%d\n%s\n%d", consulta.tipo, consulta.descricao, consulta.pid_consulta); 
    }
    fclose(file);
}

// Auxiliar C8
char verificar_disponibilidade(){
    char v='t';
    if ( access("PedidoConsulta.txt", F_OK ) == 0 ) {
        fprintf(stderr, "\nErro: Não foi possível submeter o seu pedido!\n");
        printf("Será feito um novo pedido automaticamente dentro de 10 segundos...\n");
        v='f';
    }
    else
        printf("Se necessário aguarde um pouco, obrigado.\n");
    return v;
} 

// C3
void inicio_consulta(){
    char temp[50];
    FILE* file = fopen("SrvConsultas.pid", "r");
    if (file == NULL) {
        printf("Não foi possível conectar ao servidor!\n\n");
        if(verificar_pid_consulta() > 0 && comecou == 'f'){
            remover_ficheiro("PedidoConsulta.txt");
            exit(0);
        }
    }
    else {
        fgets(temp, 50, file);
        pid_servidor=atoi(temp);
    }
    fclose(file);
    kill(pid_servidor, SIGUSR1);
}

// C4
void handler_sighup(int signal){
    printf("\nConsulta iniciada para o processo %d\n", pid_consulta);
    comecou='t';
    if(verificar_pid_consulta() > 0)
        remover_ficheiro("PedidoConsulta.txt");
    pause();
}

// C5
void handler_sigterm(int signal){
    if (comecou == 't')
        printf("Consulta concluída para o processo %d\n", pid_consulta);
    else
        fprintf(stderr, "Erro: A consulta para o processo %d não teve início!\n\n", pid_consulta);
    exit(0);        
}

// C6
void handler_sigusr2(int signal){
    printf("Consulta não é possível para o processo %d\n\n", pid_consulta);
    if(verificar_pid_consulta() > 0)
        remover_ficheiro("PedidoConsulta.txt");
    exit(0);
}

// C7
void handler_sigint(int signal){
    printf("\nPaciente cancelou o pedido!\n\n");
    if(verificar_pid_consulta() > 0)
        remover_ficheiro("PedidoConsulta.txt");
    exit(0);
}

// Auxiliar C8
void handler_sigalrm(int signal){
    printf("O seu pedido original foi novamente enviado\n");
    if (verificar_disponibilidade() == 't'){
        escrever_dados_consulta();
        inicio_consulta();
    }
    else 
        alarm(10);
}

// Trata os sinais 
void registar_sinais(){
    signal(SIGHUP, handler_sighup);
    signal(SIGTERM, handler_sigterm);
    signal(SIGUSR2, handler_sigusr2);
    signal(SIGINT, handler_sigint);
    signal(SIGALRM, handler_sigalrm);
}

// validar se a descricao é válida
int validar_descricao(){
    if(isdigit(descricao[0]) || descricao[0] == ' ' || strlen(descricao) < 2)
        return -1;
    for(int i=1; i<strlen(descricao) - 1; i++){
        if(isdigit(descricao[i]))
            return -1;
    }
    if(isdigit(descricao[strlen(descricao) - 1]) || descricao[strlen(descricao) - 1] == ' ')
        return -1;
    return 1;
}