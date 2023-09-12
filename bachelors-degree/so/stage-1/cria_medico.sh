#!/bin/bash
if (( $# != 4 )); then
    echo -e "\nOperação cancelada por argumentos inválidos!\n"
    exit
elif ! echo "$1" | grep -q -P "^([a-zA-zÁ-ú][a-zA-zÁ-ú]*[.']?[ ]?)*[a-zA-zÁ-ú]$" || ! (( ${#1} >= 3 && ${#1} <= 100 )); then
    echo -e "\nOperação cancelada!\nO nome do médico não cumpre os requisitos.\n"
    exit                              
elif ! [[ "$2" =~ ^[0-9]+$ ]] || (( $2 == 0 )); then
    echo -e "\nOperação cancelada!\nO número de cédula profissional não cumpre os requisitos.\n"
    exit                            
elif ! echo "$3" | grep -q -P "^([a-zA-zÁ-ú][a-zA-zÁ-ú]*[.']?[ ]?)*[a-zA-zÁ-ú]$" || ! (( ${#3} >= 3 ${#3} <= 100 )); then
    echo -e "\nOperação cancelada!\nA especialidade inserida não cumpre os requisitos.\n"
    exit                
elif ! [[ "$4" = **@?*.?** ]] || (( ${#4} <= 3 )); then
    echo -e "\nOperação cancelada!\nO e-mail inserido não cumpre os requisitos.\n"
    exit                
fi

touch medicos.txt
registado=false

while read medico; do  
    number=$(echo $medico | awk 'BEGIN { FS = ";" } ; {print $2}')
    mail=$(echo $medico | awk 'BEGIN { FS = ";" } ; {print $4}')
    if (( $2 == $number )) || [ $mail = $4 ]; then
        registado=true
        break
    fi
done < medicos.txt

if [ "$registado" = false ]; then
    echo -e "\nMédico adicionado com sucesso!\n"
    echo ${1}';'${2}';'${3}';'$4';''0'';''0'';''0' >> medicos.txt
else
    echo -e "\nNão houve alterações!\nUm médico com este número de cédula profissional ou e-mail já se encontra registado.\n"
fi
echo "Lista de médicos registados:"
cat medicos.txt
echo


