package objects;
import engine.StoneBreaker;

import pt.iul.ista.poo.gui.ImageTile;
import pt.iul.ista.poo.utils.Point2D;


public abstract class GameObject implements ImageTile {

	private String name;
	private Point2D position;
	private int layer;

	public GameObject(String name, Point2D position, int layer) {
		this.name = name;
		this.position = position;
		this.layer = layer;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Point2D getPosition() {
		return position;
	}

	@Override
	public int getLayer() {
		return layer;
	}

	protected void setPosition(Point2D newPosition) {
		this.position = newPosition; 
	}
	
	// indica se o objeto e' ou nao transponivel
	public abstract boolean isTransposable();

	// metodo fabrica
	public static GameObject create(char c, int linha, int coluna) {
		switch (c) {
		case StoneBreaker.PEDRA_GRANDE:
			return new PedraGrande(new Point2D(linha, coluna));
		case StoneBreaker.PEDRA_PEQUENA:
			return new PedraPequena(new Point2D(linha, coluna));
		case StoneBreaker.BULLDOZER:
			return new Bulldozer(new Point2D(linha, coluna));
		case StoneBreaker.ARVORE:
			return new Arvore(new Point2D(linha, coluna));
		case StoneBreaker.AUTOBULLDOZER:
			return new AutoBulldozer(new Point2D(linha, coluna));
		case StoneBreaker.NADA:
			return new Chao(new Point2D(linha, coluna));
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public String toString() {
		return "ObjetoDeJogo [name=" + name + ", position=" + position + ", layer=" + layer + "]";
	}

	
}
