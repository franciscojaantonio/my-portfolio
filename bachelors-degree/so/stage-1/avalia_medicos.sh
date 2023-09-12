#!/bin/bash
if ! [ -f medicos.txt ]; then
    echo 'Ação impossível, não existem médicos registados!'
    exit
fi

rm -f lista_negra_medicos.txt
touch lista_negra_medicos.txt

while read medico; do  
    rating=$(echo $medico | awk 'BEGIN { FS = ";" } ; {print $5}')
    consultas=$(echo $medico | awk 'BEGIN { FS = ";" } ; {print $6}')
    if (( $rating < 5 )) && (( $consultas > 6 )); then
        echo $medico >> lista_negra_medicos.txt
    fi
done < medicos.txt

bl=$(cat lista_negra_medicos.txt | wc -l)
if (( $bl > 0 )); then
    echo "Lista negra de médicos:"
    cat lista_negra_medicos.txt
else
    echo "Não existem médicos na lista negra!"
fi
echo 