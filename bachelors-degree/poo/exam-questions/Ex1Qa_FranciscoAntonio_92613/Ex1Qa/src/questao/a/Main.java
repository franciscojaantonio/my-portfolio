package questao.a;

import java.util.ArrayList;
import java.util.List;

import utils.Leitor;
import utils.Time;

// Nao pode alterar a classe Musica.
// 
// Em todas as alineas necessita de testar as funcionalidades pedidas, mostrando claramente
// os resultados. Pode ser util usar o metodo lerMusicas() que esta' na classe Leitor e o 
// ficheiro musicas.txt
//
//
// a) Crie a classe MusicaComVariosAutores de modo a que extenda a classe Musica 
// e permita guardar o nome de todos os autores. Quando for chamado o metodo getAutor() 
// este deve devolver o nome do primeiro autor seguido de "et al." (significa "e outros")
//
// *****************************************************************************************
//
// b) Crie o metodo musicasDoAutor(...), cujo objetivo e' devolver todas as musicas que   
// contenham o autor passado em argumento, quer as musicas tenham um ou varios autores.
//
// Não deve usar um inspetor que devolva a lista de autores da classe MusicaComVariosAutores
// 
// *****************************************************************************************
//	
// c) Crie e use um comparador que permita ordenar musicas por ordem crescente de duracao, 
// e, para musicas com a mesma duracao, o desempate deve ser por ordem alfabetica do titulo. 
// Ordene as musicas de forma a testar o comparador.
// 
// *****************************************************************************************
// 
// d) Crie o metodo filtrarMusicas(...), cujo objetivo e' remover de uma lista todas as 
// musicas de acordo com um objeto FiltroDeMusicas que é passado no argumento. Teste de  
// forma a retirar da lista todas as musicas que tenham uma duracao superior a 6 minutos.
// 
// Pode usar expressoes lambda para testar.
//

public class Main {

	public static void main(String[] args) {
		String [] autores = {"Ze", "Manuel", "Tiago"};
		MusicaComVariosAutores m = new MusicaComVariosAutores ("Adeus", autores , new Time("03:30"));
		System.out.println(m.getAutor());
		System.out.println(musicasDoAutor("Serafim"));
		List<Musica> musicas = Leitor.lerMusicas("musicas.txt");
		musicas.sort(new ComparadorTempo());
		System.out.println(musicas);
		filtrarMusicas(musicas, new RemoveMoreThan6m());
		System.out.println(musicas);

		// TODO
		// Usar o main para testar todas as alineas
	}
	
	// TODO
	// Acrescentar os metodos pedidos
	public static List<Musica> musicasDoAutor(String autor) {
		List<Musica> musicas = Leitor.lerMusicas("musicas.txt");
		List<Musica> mda = new ArrayList<>();
		for (Musica m : musicas) {
			if (m instanceof MusicaComVariosAutores) {
				if (((MusicaComVariosAutores) m).getAutores().contains(autor)) 
					mda.add(m);
			}			
			else {
				if (m.getAutor().contains(autor))
					mda.add(m);
			}
		}
		return mda;
	}
	
	public static void filtrarMusicas(List<Musica> musica, FiltroDeMusicas filtro) {
		musica.removeIf(m -> (filtro.excluir(m)));				
	}		
}
				
			
				
		
	

	
