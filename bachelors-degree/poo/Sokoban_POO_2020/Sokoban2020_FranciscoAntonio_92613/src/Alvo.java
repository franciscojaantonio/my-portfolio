import pt.iul.ista.poo.utils.Point2D;

public class Alvo extends AbstractSObject {
	
	public Alvo(Point2D position) {	
		super(position, "Alvo");
		setLayer(1);
		setGoThrough(true);
	}	 
	
	@Override
	public String toString() {
		return "Alvo: "+getPosition();
	}
	
	
}
