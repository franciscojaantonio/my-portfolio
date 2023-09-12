#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include "consulta.h"

int verificar_pid_consulta();
void remover_ficheiro(char *filename);
Consulta nova_consulta();
void escrever_dados_consulta();
char verificar_disponibilidade();
void inicio_consulta();
void handler_sighup(int signal);
void handler_sigterm(int signal);
void handler_sigusr2(int signal);
void handler_sigint(int signal);
void handler_sigalrm(int signal);
void registar_sinais();
int validar_descricao();