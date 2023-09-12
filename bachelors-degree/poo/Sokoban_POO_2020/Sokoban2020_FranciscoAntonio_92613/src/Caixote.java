import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class Caixote extends AbstractSObject implements ActiveObject {
	
	private boolean noAlvo; // Indica se a caixa ja esta posicionada no alvo
	private SokobanGame sokoban;
	
	public Caixote(Point2D position, SokobanGame sokoban) {
		super(position, "Caixote");
		this.sokoban=sokoban;
		this.noAlvo=false;
		setLayer(2);
		setGoThrough(false);
	}
	
	public boolean isNoAlvo() {
		return noAlvo;
	}

	public void move (Direction direcao) {
		Point2D newPosition = getPosition().plus(direcao.asVector());
		for (AbstractSObject o: sokoban.getObjectsInPosition(newPosition)) {
			if (sokoban.canMove(newPosition)) {				
				setPosition(newPosition);				
				if (o instanceof Alvo)					
					this.noAlvo=true;
				else
					this.noAlvo=false;
				if (o instanceof InteractiveObject)
					((InteractiveObject) o).interact(this);
			}	
		}
	}

	@Override
	public String toString() {
		return "Caixote: "+getPosition();
	}
	
	
}
			
