% Desafio A - Torres de Hanói:
% Mover N discos da torre TA para a torre TD
% usando a torre Taux como auxliar.
% Recursivamente mover os N-1 discos para uma torre auxiliar, de maneira
% a poder colocar o disco da base na torre destino. Resta depois mover
% os N-1 discos da torre auxiliar para a destino e fica completo o
% deslocamento.

print_instruction(TA, TD):-
    write("Move top disk from "),
    write(TA),
    write(" to "),
    write(TD), nl.

move(1, TA, TD, _):-
    print_instruction(TA, TD).

move(N, TA, TD, Taux):-
    N>1,
    R is N-1,
    move(R, TA, Taux, TD),
    move(1, TA, TD, _),
    move(R, Taux, TD, TA).