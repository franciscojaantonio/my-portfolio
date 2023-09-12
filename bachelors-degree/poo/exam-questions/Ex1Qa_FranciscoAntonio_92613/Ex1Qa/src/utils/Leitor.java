package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import questao.a.Musica;
import questao.a.MusicaComVariosAutores;

public class Leitor {

	public static List<Musica> lerMusicas(String nomeFicheiro) {

		List<Musica> musicas = new ArrayList<>();

		try(Scanner s = new Scanner(new File(nomeFicheiro))) {	

			while (s.hasNextLine()) {
				String tokens[] = s.nextLine().split(";");
				String titulo = tokens[0];
				String duracao = tokens[1];

				String autores[] = s.nextLine().split(";");
				if (autores.length == 1) 
					musicas.add(new Musica(titulo, autores[0], new Time(duracao)));
				if (autores.length > 1) 
					musicas.add(new MusicaComVariosAutores(titulo, autores, new Time(duracao)));			
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Erro na abertura do ficheiro");
		}
		return musicas;
	}
	
	
}
