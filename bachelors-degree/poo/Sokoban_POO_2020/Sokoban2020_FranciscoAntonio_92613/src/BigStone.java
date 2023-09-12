import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class BigStone extends AbstractSObject implements ActiveObject{
	private SokobanGame sokoban;
	private boolean canMove; // Indica se a pedra se pode mover (apos cair no buraco a pedra nao se move)
	
	public BigStone (Point2D position, SokobanGame sokoban) {	
		super (position, "BigStone");
		this.sokoban=sokoban;
		this.canMove=true;
		setLayer(4);
		setGoThrough(false);
	}
	
	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	public void move (Direction direcao) {
		if (!this.canMove) 	
			return;			
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
		return "BigStone: "+getPosition();
	}		
}


