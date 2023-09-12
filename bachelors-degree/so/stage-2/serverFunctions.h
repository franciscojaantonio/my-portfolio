#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include "consulta.h"
#define N 10

void apaga_consulta(int pid, Consulta lista[], int size); 
int verificar_vaga(Consulta c);
Consulta receber_informacao();
void update_tipo_consulta(Consulta c);
void escrever_stats();
void atualizar_stats();
void iniciar_consulta();
void handler_sigint(int signal);
void handler_sigusr1(int signal);
