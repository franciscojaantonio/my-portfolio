% Desafio B - Caminho entre dois nós:
% edge(X,Y,D) significa que existe um arco do nó X para o nó Y
% com um comprimento de D km

edge((0,0),(0,1),1).
edge((0,0),(2,0),6).
edge((2,1),(2,0),1).
edge((0,1),(2,1),3).
edge((0,1),(1,2),1).
edge((1,2),(2,1),1).

% A):
% path/4, tal que path(X,Y,P,C) siginifica que P é um caminho possível
% entre o nó X e o nó Y com o comprimento C.


path(X,Y,P,C) :-
    find_path(X,Y,[X],Path,0,C),
    reverse(Path,P).

find_path(X,Y,P,[Y|P],C1,CF) :-
    neighbours(X,Y),
    edge(X,Y,Distance),
    CF is C1+Distance.

find_path(X,Y,Z,Path,C1,C) :-
    neighbours(X,Next),
    \+(Next=Y),
    \+member(Next,Z),
    edge(X,Next,Distance),
    C2 is C1+Distance,
    find_path(Next,Y,[Next|Z],Path,C2,C).

neighbours(X,Y):-
    edge(X,Y,_).

% B):
% shortest_path/4, tal que shortest_path(A,B,P,C) significa que P é o
% caminho mais curto entre o nó A e o nó B com o comprimento C.

shortest_path(A,B,P,C):-
    possible_paths(A,B,_,_,L),
    find_shortest_path(L, C),
    path(A,B,P,C).

possible_paths(A,B,P,C,L):-
    findall((P, C),path(A,B,P,C),L).

find_shortest_path([H|T], Res):-
    (_,C) = H,
    find_shortest_path(T, C, Res).

find_shortest_path([], Res, Res).

find_shortest_path([H|T], Min, Res):-
    (_,C) = H,
    NewMin is min(C, Min),
    find_shortest_path(T, NewMin, Res).
