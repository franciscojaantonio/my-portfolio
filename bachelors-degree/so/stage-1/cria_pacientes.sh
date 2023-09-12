#!/bin/bash
pacientes=$(cat /etc/passwd | grep '^a[0-9]' | head  -10 | sed 's/ /_/g' | sed 's/[:,]/ /g')
saldo_inicial=100

rm -f pacientes.txt
touch pacientes.txt

while read paciente; do  
    number=$(echo $paciente | awk '{print $1}' | cut -d'a' -f2)
    name=$(echo $paciente | awk '{print $5}' | sed 's/_/ /g')
    echo ${number}';'${name}';'';'';''a'${number}'@iscte-iul.pt'';'$saldo_inicial >> pacientes.txt 
done <<< "$pacientes"

echo -e "Lista de pacientes criada!\n"





