package objects;

import engine.StoneBreaker;
import pt.iul.ista.poo.utils.Point2D;

// TODO
// Pode ser necessario mudar a declaracao da classe e/ou construtor
//
public class PedraGrande extends Pedra {

	public PedraGrande(Point2D position) {
		super("bigstone", position, 1);
	}

	@Override
	public boolean isTransposable() {
		return false;
	}

	@Override
	public void brokenBy(Bulldozer b) {
		Point2D position = getPosition();
		StoneBreaker.getInstance().removeObject(this);
		StoneBreaker.getInstance().addObject(new PedraPequena(position));		
	}
}
