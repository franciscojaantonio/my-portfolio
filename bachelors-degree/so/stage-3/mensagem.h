#ifndef __MENSAGEM_H__
#define __MENSAGEM_H__

#include "consulta.h"

typedef struct {
    long m_type;
    Consulta consulta;
} Mensagem;

#endif