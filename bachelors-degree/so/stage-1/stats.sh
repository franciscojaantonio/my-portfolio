#!/bin/bash
if (( $# != 2 )); then
    echo -e "\nOperação cancelada por argumentos inválidos!\n"
    exit
elif ! echo "$1" | grep -q -P "^([a-zA-zÁ-ú][a-zA-zÁ-ú]*[.']?[ ]?)*[a-zA-zÁ-ú]$" || ! (( ${#1} >= 3 && ${#1} <= 50 )); then
    echo -e "\nOperação cancelada!\nNão inseriu uma localidade válida.\n"
    exit                
elif ! [[ "$2" =~ ^[0-9]+$ ]]; then
    echo -e "\nOperação cancelada!\nNão inseriu um valor de saldo válido.\n" 
    exit               
fi

if ! [ -f medicos.txt ] || ! [ -f pacientes.txt ]; then
    echo -e "\nNão existem médicos ou pacientes registados!"
    exit
fi

localidade=$(cat pacientes.txt | awk -v loc="$1" -v n=0 'BEGIN { FS = ";" } { if ( tolower(loc) == tolower($3) ) n=n+1; } END {print n}')
saldo=$(cat medicos.txt | awk -v min="$2" 'BEGIN { FS = ";" } { if ( $7 > min ) print $7; }' | wc -w)

echo -e "\nNúmero de pacientes inscritos residentes em $1: $localidade"
echo -e "Número de médicos inscritos com saldo superior a ${2}€: $saldo\n"