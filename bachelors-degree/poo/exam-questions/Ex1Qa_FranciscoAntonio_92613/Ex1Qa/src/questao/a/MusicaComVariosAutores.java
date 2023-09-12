package questao.a;

import java.util.ArrayList;

import utils.Time;

// TODO
// Acrescentar o que for necessario, incluindo atributos e codigo no construtor
//
public class MusicaComVariosAutores extends Musica {
	private ArrayList<String> autores;
	
	public MusicaComVariosAutores(String titulo, String[] vAutores, Time duracao) {
		super(titulo, vAutores[0], duracao);
		this.autores = new ArrayList<>();
		for (String s: vAutores)
			autores.add(s);
			
		
	}
	
	public ArrayList<String> getAutores() {
		return autores;
	}

	public String getAutor() {
		return autores.get(0)+" et al.";		
	}	
}
