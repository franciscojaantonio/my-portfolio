package engine;

import pt.iul.ista.poo.gui.ImageMatrixGUI;

public class Main {

/* 

O objetivo deste jogo e' limpar a terra, destruindo todas as pedras.
Todos os bulldozers se movem em cada jogada.
  
- Existem varios bulldozers, mas so' um deles e' controlado pelo jogador;

- A arvore nao se pode tirar nem atravessar;

- As pedras pequenas sao destruidas ao passar por cima (e o bulldozer continua);

- As pedras grandes nao sao atravessadas, mas podem-se partir e nesse caso 
  transformam-se em pedras pequenas (e na jogada seguinte ja' podem ser destruidas);
  
- Os "AutoBulldozers" tem o mesmo comportamento do bulldozer controlado
  pelo jogador, mas movem-se aleatoriamente;

*/

	public static void main(String[] args) {
		ImageMatrixGUI.setSize(StoneBreaker.WIDTH, StoneBreaker.HEIGHT);
		try {
			StoneBreaker s = StoneBreaker.getInstance();
			ImageMatrixGUI.getInstance().registerObserver(s);
			ImageMatrixGUI.getInstance().go();
		} catch (IllegalStateException e) {
			System.err.println("Erro na leitura do ficheiro");
		}

	}

}
