

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pt.iul.ista.poo.gui.ImageMatrixGUI;
import pt.iul.ista.poo.gui.ImageTile;

public class Main {

	// O objetivo � apanhar os vegetais, e a galinha e p�r tudo no cesto.
	// H� um peso m�ximo que se pode carregar (2kg)
	// o peso de cada item � diferente (galinha 1, tomate 0.25 e couve 0.5)
	// Quando apanha a galinha n�o consegue apanhar mais vegetais, mesmo que possa carregar mais peso, 
	// Pode despejar tudo (inclusive a galinha) no cesto e volta a poder apanhar
	// O tomate pode ser colhido 3 vezes, s� depois disso desaparece
	// Nenhum dos objetos de jogo � intranspon�vel
	// A galinha mexe-se aleatoriamente uma posi��o numa dire��o a cada 2 turnos
	// O jogo termina quando todos os vegetais e a galinah forem recolhidos
	
	public static void main(String[] args) {
		ImageMatrixGUI.setSize(Colheita.WIDTH, Colheita.HEIGHT);
		try {
			Colheita s = Colheita.getInstance();
			ImageMatrixGUI.getInstance().registerObserver(s);
			ImageMatrixGUI.getInstance().go();
		} catch (IllegalStateException e) {
			System.err.println("Erro na leitura do ficheiro");
		}

	}

}
