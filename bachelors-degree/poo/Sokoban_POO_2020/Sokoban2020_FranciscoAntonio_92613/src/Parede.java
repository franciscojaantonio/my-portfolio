import pt.iul.ista.poo.utils.Point2D;

public class Parede extends AbstractSObject {
	
	public Parede(Point2D position) {
		super(position, "Parede");
		setLayer(1);	
		setGoThrough(false);
	}

	@Override
	public String toString() {
		return "Parede: "+getPosition();
	}
	
	
}