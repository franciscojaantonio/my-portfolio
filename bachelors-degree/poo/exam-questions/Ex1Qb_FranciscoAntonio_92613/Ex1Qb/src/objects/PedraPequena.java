package objects;

import engine.StoneBreaker;
import pt.iul.ista.poo.utils.Point2D;

//TODO
//Pode ser necessario mudar a declaracao da classe e/ou construtor
//
public class PedraPequena extends Pedra{

	public PedraPequena(Point2D position) {
		super("smallstone", position, 1);
	}

	@Override
	public boolean isTransposable() {
		return true;
	}

	@Override
	public void brokenBy(Bulldozer b) {
		StoneBreaker.getInstance().removeObject(this);		
	}
}
