import pt.iul.ista.poo.utils.Point2D;

public class Chao extends AbstractSObject {
	
	public Chao(Point2D position) {
		super(position, "Chao"); 
		setLayer(0);
		setGoThrough(true);
	}
	
	@Override
	public String toString() {
		return "Chao: "+getPosition();
	}
}