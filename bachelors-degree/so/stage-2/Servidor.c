#include "serverFunctions.h"

// variaveis globais
Consulta lista_consultas[N];
int pid_servidor;
int tipos[4]; // 0-perdidas, 1-tipo1, 2-tipo2, 3-tipo3

// main 
int main(){
    atualizar_stats();
    // S1
    for(int i=0; i<N; i++)
        lista_consultas[i].tipo=-1;

    pid_servidor=getpid();
    
    signal (SIGUSR1, handler_sigusr1);
    signal (SIGINT, handler_sigint);
    
    printf("\nA Iníciar o servidor Cliniq-IUL PID[%d]...\n", pid_servidor);
    // S2
    FILE* file = fopen("SrvConsultas.pid", "w");
    if (file == NULL){
        fprintf(stderr, "Erro: Não foi possível escrever no ficheiro SrvConsultas.pid!\n\n");
        exit(0);
    }
    else
        fprintf(file, "%d", pid_servidor);
    fclose(file);
    
    while(1) 
        pause();
    return 0;
}

// S3
void handler_sigusr1(int signal){
    iniciar_consulta();
}

// S3.1
Consulta receber_informacao(){
    Consulta c;
    char informacao[3][100];
    if ( access("PedidoConsulta.txt", F_OK ) == 0 ){
        FILE* file = fopen("PedidoConsulta.txt", "r");
        if (file == NULL){
            fprintf(stderr, "Erro: Não foi possível abrir PedidoConsulta.txt para leitura!\n");
            handler_sigint(0);
        } else {
            for(int i=0; i<3; i++)
                fgets(informacao[i], 100, file);
            fclose(file);
            c.tipo = atoi(informacao[0]);
            strtok(informacao[1], "\n");
            strcpy(c.descricao, informacao[1]);
            c.pid_consulta = atoi(informacao[2]);  
        }
    }
    else
        c.tipo = -1;
    return c;
}


// S3.2
void iniciar_consulta(){
    Consulta c = receber_informacao();
    if (c.tipo > 0) {
        int sala = verificar_vaga(c);
        printf("\nChegou novo pedido de consulta do tipo %d, descrição %s e PID %d\n", c.tipo, c.descricao, c.pid_consulta);   
        if (sala >= 0) {
            lista_consultas[sala]=c;
            printf("Consulta agendada para a sala %d\n", sala);
            update_tipo_consulta(c);
            
            // S3.4.1 e S3.4.2
            pid_t parent = fork();
            if ( !parent ) {
                kill(c.pid_consulta, SIGHUP);
                sleep(10);
                printf("Consulta terminada na sala %d\n", sala);
                kill(c.pid_consulta, SIGTERM);
                exit(0); 
            }
            else {
                wait(NULL);
                apaga_consulta(c.pid_consulta, lista_consultas, N);
            }
        }
    }
}

// S3.3
int verificar_vaga(Consulta c){
    int vaga = -1;
    int sala = -1;
    for(int i=0; i<N; i++){
        if (lista_consultas[i].tipo < 0) {
            vaga = 1;
            sala = i;
            break;
        }
    }
    if (vaga != 1) {
        printf("Lista de consultas cheia!\n");
        kill(c.pid_consulta, SIGUSR2);
        tipos[0]++;
    }
    return sala;
}

// Auxiliar S3.4 - Procura a consulta pelo seu PID e depois remove-a (define o seu tipo como -1)
void apaga_consulta(int pid, Consulta lista[], int size) {
    for( int i = 0; i < size; i++ ) {
        if ( lista[i].pid_consulta == pid ) {
            lista[i].tipo = -1;
            break;
        }
    }
}

// Auxiliar S3.4 - Incrementa o contador de consultas do tipo correspondente
void update_tipo_consulta(Consulta c){
    if (c.tipo == 1)
        tipos[1]++;
    else if (c.tipo == 2)
        tipos[2]++;
    else if (c.tipo == 3)
        tipos[3]++;
}

// Auxiliar S4 - Escrever as stats do tipo consulta no ficheiro após receber o SIGINT
void escrever_stats(){
    FILE* file = fopen("StatsConsultas.dat", "wb");
    if (file == NULL){
        fprintf(stderr, "Erro: Não foi possível escrever no ficheiro StatsConsultas.dat!\n\n");
        exit(0);
    }
    else {
        for(int i=0; i<4; i++)
            fwrite(&tipos[i], sizeof(int), 1, file);
    }
    fclose(file);
}

// Auxiliar S4 - Atribuir ao array de tipos de consulta o seu valor respetivamente
void atualizar_stats(){
    if ( access("StatsConsultas.dat", F_OK ) != 0 ) {
        fprintf(stderr, "\nNão existe ficheiro StatsConsultas.dat!\n");
        printf("A criar o ficheiro...\n");
        escrever_stats(); 
    }
    FILE* file = fopen("StatsConsultas.dat", "rb");
    if (file == NULL){
        fprintf(stderr, "Erro: Não foi possível ler o ficheiro StatsConsultas.dat!\n\n");
        exit(0);
    }
    for(int i=0; i<4; i++)
        fread(&tipos[i], sizeof(int), 1, file);
    fclose(file);
}

// S4
void handler_sigint(int signal){
    int rm = remove("SrvConsultas.pid");
    if (rm != 0)
        fprintf(stderr, "Erro: Não foi possível remover SrvConsultas.pid!\n\n");
    else {
        printf("\nA registar os dados atualizados...\n");
        escrever_stats(); 
        printf("Até Breve!\n\n");
    }
    exit(0);
}