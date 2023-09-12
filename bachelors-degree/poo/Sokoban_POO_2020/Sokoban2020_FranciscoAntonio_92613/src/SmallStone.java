import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class SmallStone extends AbstractSObject implements ActiveObject{
	
	private SokobanGame sokoban;
	
	public SmallStone(Point2D position, SokobanGame sokoban) {	
		super (position, "SmallStone");
		this.sokoban=sokoban;
		setLayer(3);
		setGoThrough(false);
	}
	
	public void move (Direction direcao) {			
		Point2D newPosition = getPosition().plus(direcao.asVector());		
		for (AbstractSObject o: sokoban.getObjectsInPosition(newPosition)) {					
			if (sokoban.canMove(newPosition)) {			
				setPosition(newPosition);			
				if (o instanceof InteractiveObject)				
					((InteractiveObject) o).interact(this);		
			}
		}			
	}		
	
	@Override
	public String toString() {
		return "SmallStone: "+getPosition();
	}		
}

