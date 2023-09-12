package objects;

import pt.iul.ista.poo.utils.Point2D;

public abstract class Pedra extends GameObject implements Breakable {

	public Pedra(String name, Point2D position, int layer) {
		super(name, position, layer);
	}

}
