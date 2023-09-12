import pt.iul.ista.poo.utils.Point2D;

public class Martelo extends AbstractSObject implements InteractiveObject {
	private SokobanGame sokoban;
	
	public Martelo(Point2D position, SokobanGame sokoban) {
		super (position, "Martelo");
		this.sokoban=sokoban;
		setLayer(1);		
		setGoThrough(false);
	}
	
	public void interact(AbstractSObject object) {							
		sokoban.removeObject(this);			
		((Empilhadora) object).setMartelo(true);
	}

	@Override
	public String toString() {
		return "Martelo: "+getPosition();
	}	
}