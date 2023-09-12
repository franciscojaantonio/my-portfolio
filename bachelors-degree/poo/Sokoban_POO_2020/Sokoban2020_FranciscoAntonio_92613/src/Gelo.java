import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class Gelo extends AbstractSObject implements InteractiveObject {
				
	private SokobanGame sokoban;		
			
	public Gelo (Point2D position, SokobanGame sokoban) {		
		super (position, "Gelo");	
		this.sokoban=sokoban;	
		setLayer(1);		
		setGoThrough(true);		
	}
	
	public void interact (AbstractSObject object) {
		Direction direcao=sokoban.getPlayer().getDirecao();
		if (object.getPosition().equals(this.getPosition())) {
			if (object instanceof Empilhadora) {											
				((Empilhadora) object).setEmpilhadoraMoves(-1);																			
				((Empilhadora) object).setBateriaEmpilhadora(1);		
			}	
			((ActiveObject) object).move(direcao);																				
		}			
	}

	@Override
	public String toString() {
		return "Gelo: "+getPosition();
	}	
}
		