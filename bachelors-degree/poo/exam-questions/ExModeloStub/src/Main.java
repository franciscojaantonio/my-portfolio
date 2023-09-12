

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pt.iul.ista.poo.gui.ImageMatrixGUI;
import pt.iul.ista.poo.gui.ImageTile;

public class Main {

	// O objetivo é apanhar os vegetais, e a galinha e pôr tudo no cesto.
	// Há um peso máximo que se pode carregar (2kg)
	// o peso de cada item é diferente (galinha 1, tomate 0.25 e couve 0.5)
	// Quando apanha a galinha não consegue apanhar mais vegetais, mesmo que possa carregar mais peso, 
	// Pode despejar tudo (inclusive a galinha) no cesto e volta a poder apanhar
	// O tomate pode ser colhido 3 vezes, só depois disso desaparece
	// Nenhum dos objetos de jogo é intransponível
	// A galinha mexe-se aleatoriamente uma posição numa direção a cada 2 turnos
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
