package objects;
import pt.iul.ista.poo.utils.Point2D;

public class Chao extends GameObject {

	public Chao(Point2D position) {
		super("land", position, 0);
	}
	
	@Override
	public boolean isTransposable() {
		return true;
	}

}
