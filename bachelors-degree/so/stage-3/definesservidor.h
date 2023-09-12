#ifndef __DEFINESSERVIDOR_H__
#define __DEFINESSERVIDOR_H__

#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include <errno.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/shm.h>
#include <sys/sem.h>
#include "consulta.h"
#include "sharedMemory.h"
#include "mensagem.h"
#define N 10
#define TC 4
#define DURACAO 8
#define PEDIDO 1
#define INICIADA 2
#define TERMINADA 3
#define RECUSADA 4
#define CANCELADA 5

#define MSG_TYP 1
#define exit_on_error(s,m) if (s<0) {perror(m); exit(1);}
#define exit_on_null(s,m) if (s==NULL) { perror(m); exit(1); }

#define IPC_KEY 0x0a92613

void interpretar_mensagens();
void apaga_consulta(int pid, int size);
void iniciar_consulta();
int verificar_vaga();
void responder_cliente(int x, Mensagem men);
void adicionar_consulta(Mensagem m, int sala);
void update_contadores(int x);
void handler_alarm(int signal);
void handler_int(int signal);
void servidor_dedicado(Mensagem m);
void sair();
void ler_mensagens();
void sem_down();
void sem_up();

#endif