#ifndef __SHAREDMEMORY_H__
#define __SHAREDMEMORY_H__

typedef struct {
    Consulta lista_consultas[10];
    int tipos[4];
} SharedMemory;

#endif