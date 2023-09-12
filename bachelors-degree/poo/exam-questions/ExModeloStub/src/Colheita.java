
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import pt.iul.ista.poo.gui.ImageMatrixGUI;
import pt.iul.ista.poo.gui.ImageTile;
import pt.iul.ista.poo.observer.Observed;
import pt.iul.ista.poo.observer.Observer;
import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class Colheita implements Observer {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 10;

	public static final char CHAO = ' ';
	public static final char TOMATE = 'o';
	public static final char COUVE = 'v';
	public static final char CESTO = 'c';
	public static final char GALINHA = 'g';
	public static final char AGRICULTOR = 'A';

	private static Colheita INSTANCE = null;

	private Agricultor agricultor;
	private List<ObjetoDeJogo> objetosDoJogo = new ArrayList<>();

	private List<ObjetoDeJogo> toRemove = new ArrayList<>();
	private int cicloDeJogo;

	private Colheita() {
		readLevel(); 
		ImageMatrixGUI.getInstance().update();
	}

	@Override
	public void update(Observed arg0) {
		
		// Q5 c)
		// Transformação de tecla em direção do movimento 
		int lastKeyPressed = ((ImageMatrixGUI) arg0).keyPressed();
		// mover o agricultor na direcção indicada

		// mover outros objetos que implementem Movivel  
		
		// remover todos os objetos indicados para remoção (na lista toRemove)
		
		// verificar se o jogo terminou
		if (terminou()) {
			ImageMatrixGUI.getInstance().dispose();
			return;
		}
		
		ImageMatrixGUI.getInstance().update();
		cicloDeJogo++;
	}


	public static Colheita getInstance() {
		if (INSTANCE == null)
			INSTANCE = new Colheita();
		return INSTANCE;
	}

	 void readLevel() {
		 // Q5 b)
	}

	public void addObject(ObjetoDeJogo obj) {
		if (obj instanceof Agricultor)
			agricultor = (Agricultor)obj;
		else 
			objetosDoJogo.add(obj);
		ImageMatrixGUI.getInstance().addImage(obj);
	}

	public void scheduleForRemoval(ObjetoDeJogo obj) {
		toRemove.add(obj);
		ImageMatrixGUI.getInstance().removeImage(obj);
	}

	public List<Apanhavel> catchableObjectsAt(Point2D newPosition) {
		List<Apanhavel> lista = new ArrayList();
		for(ObjetoDeJogo o: objetosDoJogo)
			if (o.getPosition().equals(newPosition) && o instanceof Apanhavel)
				lista.add((Apanhavel)o);
		return lista;
	}

	public List<Interactable> interactableObjectsAt(Point2D newPosition) {
		List<Interactable> lista = new ArrayList();
		for(ObjetoDeJogo o: objetosDoJogo)
			if (o.getPosition().equals(newPosition) && o instanceof Interactable)
				lista.add((Interactable)o);
		return lista;
	}

	public List<ObjetoDeJogo> objectsAt(Point2D newPosition) {
		return objetosDoJogo.stream().filter(o -> o.getPosition().equals(newPosition)).collect(Collectors.toList());
	}

	public boolean isWithinBounds(Point2D newPosition) {
		// Q5 d)
		return true;
	}

	public int cicloDeJogo() {
		return cicloDeJogo;
	}

}
