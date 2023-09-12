package objects;

import pt.iul.ista.poo.utils.Point2D;

//TODO
//Pode ser necessario mudar a declaracao da classe e/ou construtor
//
public class Arvore extends GameObject {

	public Arvore(Point2D position) {
		super("pine", position, 1);
	}

	@Override
	public boolean isTransposable() {
		return false;
	}
}
