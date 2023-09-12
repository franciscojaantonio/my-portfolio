package questao.a;

import utils.Time;

// NAO ALTERAR
//

public class Musica {

	private String titulo; 
	private String autor;
	private Time duracao;
	
	public Musica(String titulo, String autor, Time duracao) {
		this.titulo = titulo;
		this.autor = autor;
		this.duracao = duracao;
	}
	
	public String getTitulo() {
		return titulo;
	}
	
	public String getAutor() {
		return autor;
	}
	
	public Time getDuracao() {
		return duracao;
	}
	
	public boolean contemAutor(String nome) {
		return nome.equals(autor);
	}
	
	@Override
	public String toString() {
		return titulo + ", " + duracao + ", " + getAutor();
	}
}
