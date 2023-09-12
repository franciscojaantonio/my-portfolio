package engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import objects.Breakable;
import objects.Bulldozer;
import objects.GameObject;
import pt.iul.ista.poo.gui.ImageMatrixGUI;
import pt.iul.ista.poo.observer.Observed;
import pt.iul.ista.poo.observer.Observer;
import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

/* Classe com o motor de jogo - e' um solitao. 
 * 
 * Contem metodos que podem ser uteis:
 * - addObject(...)
 * - removeObject(...)
 * - canMoveTo(...)
 * - breakablesAt(...)
 * - ...
 * 
 * Pode acrescentar ou alterar se achar necessario.
 */

public class StoneBreaker implements Observer {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 10;

	public static final char NADA = ' ';
	public static final char PEDRA_PEQUENA = 'p';
	public static final char PEDRA_GRANDE = 'P';
	public static final char ARVORE = '#';
	public static final char BULLDOZER = 'B';
	public static final char AUTOBULLDOZER = 'A';

	private static StoneBreaker INSTANCE = null;

	private List<GameObject> gameObjects = new ArrayList<>();
	private int level = 0;
	
	private StoneBreaker() {
		readLevel();
		ImageMatrixGUI.getInstance().update();
	}

	public static StoneBreaker getInstance() {
		if (INSTANCE == null)
			INSTANCE = new StoneBreaker();
		return INSTANCE;
	}

	
	@Override
	public void update(Observed arg0) {
		int lastKeyPressed = ((ImageMatrixGUI) arg0).keyPressed();
		
		// Desencadeia o movimento dos bulldozers - nos automaticos a direcao sera' 
		// ignorada e substituida por uma direcao aleatoria
		if (Direction.isDirection(lastKeyPressed)) {
			getBulldozers().forEach(b -> b.move(Direction.directionFor(lastKeyPressed)));
		}
		
		// Verificar se terminou - se sim, apaga a janela
		if (finished())
			ImageMatrixGUI.getInstance().dispose();

		ImageMatrixGUI.getInstance().update();
	}

	// Insere um novo objeto no jogo
	public void addObject(GameObject obj) {
		gameObjects.add(obj);
		ImageMatrixGUI.getInstance().addImage(obj);
	}

	// Remove um objeto do jogo
	public void removeObject(GameObject obj) {
		gameObjects.remove(obj);
		ImageMatrixGUI.getInstance().removeImage(obj);
	}

	// Verifica se e' possivel mover-se para uma dada posicao
	public boolean canMoveTo(Point2D position) {
		if (!isWithinBounds(position)) return false;
		List<GameObject> nonTransposablesAt = select(o -> o.getPosition().equals(position) && !o.isTransposable());
		return nonTransposablesAt.size() == 0;
	}
	
	// Devolve os objetos Breakable que estao numa dada posicao
	public List<Breakable> breakablesAt(Point2D position) {
		return select(o -> o.getPosition().equals(position) && o instanceof Breakable);
	}

	// Verifica se uma posicao esta' dentro dos limites do jogo
	public boolean isWithinBounds(Point2D position) {
		if (position.getX() < 0 || position.getY() < 0 || position.getX() >= WIDTH
				|| position.getY() >= HEIGHT)
			return false;
		return true;
	}

	
	
	// metodos private
	
	@SuppressWarnings("unchecked")
	private <T> List<T> select(Predicate<GameObject> p) {
		List<T> selection = new ArrayList<>();
		for (GameObject obj : gameObjects)
			if (p.test(obj))
				selection.add((T) obj);
		return selection;		
	}
	
	private List<Bulldozer> getBulldozers() {
		return select(obj -> obj instanceof Bulldozer);
	}	
	
	private List<Breakable> getBreakables() {
		return select(obj -> obj instanceof Breakable);
	}
	
	private void readLevel() {

		try {
			Scanner scan = new Scanner(new File("levels/level" + level + ".txt"));
			int y = 0;
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				for (int x = 0; x != line.length(); x++) {
					char c = line.charAt(x);
					
					GameObject o = GameObject.create(c, x, y);
					ImageMatrixGUI.getInstance().addImage(o);
					
					if (c!=NADA) {
						gameObjects.add(o);
						ImageMatrixGUI.getInstance().addImage(GameObject.create(NADA, x, y));
					}
				}
				y++;
			}
			scan.close();
		} catch (FileNotFoundException e) {
			throw new IllegalStateException();
		}
	}

	private boolean finished() {
		return getBreakables().size() == 0;
	}
}
