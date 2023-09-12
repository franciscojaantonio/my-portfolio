#ifndef __DEFINESCLIENTE_H__
#define __DEFINESCLIENTE_H__

#include <stdio.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/ipc.h>
#include <sys/msg.h>
#include <sys/shm.h>
#include <sys/sem.h>
#include "consulta.h"
#include "sharedMemory.h"
#include "mensagem.h"
#define N 10
#define PEDIDO 1
#define INICIADA 2
#define TERMINADA 3
#define RECUSADA 4
#define CANCELADA 5


#define exit_on_error(s,m) if (s<0) { perror(m); exit(1);}

#define IPC_KEY 0x0a92613 
#define MSG_TYP 1

Consulta nova_consulta();
Mensagem nova_mensagem();
void interpretar_mensagem(int x);
void sair();
void handler(int signal);
int msgsnd(int msqid, const void *msgp, size_t msgsz, int msgflg);
void ler_mensagens();
void sem_down();
void sem_up();

#endif