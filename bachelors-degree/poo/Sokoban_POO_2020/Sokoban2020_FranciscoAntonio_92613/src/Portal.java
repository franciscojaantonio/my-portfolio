import pt.iul.ista.poo.utils.Point2D;

public class Portal extends AbstractSObject implements InteractiveObject {
	
	private SokobanGame sokoban;
	private Point2D nextPortalPosition;
	
	public Portal (Point2D position, SokobanGame sokoban) {
		super (position, "Portal_Verde");
		this.sokoban=sokoban;
		setLayer(1);
		setGoThrough(true);
	}
	// Descobre a posicao do outro portal
	private void findNextPortal() {		
		for (AbstractSObject o: sokoban.getObjects())
			if (o instanceof Portal)
				if (!o.getPosition().equals(getPosition()))
					this.nextPortalPosition=o.getPosition();	
	}
	
	public void interact (AbstractSObject object) {
		findNextPortal();
		if (sokoban.canMove(nextPortalPosition))
			object.setPosition(nextPortalPosition);
	}

	@Override
	public String toString() {
		return "Portal: "+getPosition();
	}	
}
		
