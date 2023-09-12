import pt.iul.ista.poo.utils.Point2D;

public class Buraco extends AbstractSObject implements InteractiveObject {
	
	private SokobanGame sokoban;
	private boolean isCovered; // Indica se o buraco esta ou nao tapado (uma pedra grande no buraco ira tapa-lo) 
	
	public Buraco(Point2D position, SokobanGame sokoban) {	
		super (position, "Buraco");
		this.sokoban=sokoban;
		this.isCovered=false;
		setLayer(3);
		setGoThrough(true);
	}	

	public void interact (AbstractSObject object) {
		if (this.isCovered)
			return;
		if (object instanceof BigStone) {
			((BigStone) object).setCanMove(false);
			this.isCovered=true;
		}
		else
			sokoban.removeObject(object);
	}

	@Override
	public String toString() {
		return "Buraco: "+getPosition();
	}	
}
		
