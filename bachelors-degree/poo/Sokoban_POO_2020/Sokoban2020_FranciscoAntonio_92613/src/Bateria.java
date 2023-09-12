import pt.iul.ista.poo.utils.Point2D;

public class Bateria extends AbstractSObject implements InteractiveObject{
	
	private SokobanGame sokoban;
	private final int valorBateria=100;
	
	public Bateria (Point2D position, SokobanGame sokoban) {	
		super (position, "Bateria");
		this.sokoban=sokoban;
		setLayer(1);
		setGoThrough(false);
	}
	
	public void interact (AbstractSObject object) {
		sokoban.removeObject(this);
		((Empilhadora) object).setBateriaEmpilhadora(valorBateria);
	}

	@Override 
	public String toString() {
		return "Bateria: " + getPosition();
	}	
}
