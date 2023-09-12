import pt.iul.ista.poo.utils.Point2D;

public class ParedePartida extends AbstractSObject implements InteractiveObject {
	private SokobanGame sokoban;
	
	public ParedePartida(Point2D position, SokobanGame sokoban) {
		super (position, "Parede_Partida");
		this.sokoban=sokoban;
		setLayer(1);		
		setGoThrough(false);
	}
	
	public void interact(AbstractSObject object) {					
		if (((Empilhadora) object).isMartelo()) 							
			sokoban.removeObject(this);
	}

	@Override
	public String toString() {
		return "ParedePartida: "+getPosition();
	}	
}
