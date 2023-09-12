import pt.iul.ista.poo.gui.ImageMatrixGUI;
import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class Empilhadora extends AbstractSObject implements ActiveObject {
	
	private SokobanGame sokoban;
	private int empilhadoraMoves;
	private int bateriaEmpilhadora=100;
	private boolean martelo=false;
	private Direction direcao;
	
	public Empilhadora (Point2D position, SokobanGame sokoban) {
		super(position, "Empilhadora_U");
		this.sokoban=sokoban;
		this.empilhadoraMoves=0;
		setLayer(2);
		setGoThrough(false);
		ImageMatrixGUI.getInstance().setStatusMessage("Nivel: "+sokoban.getLevel()+" | Martelo: "+martelo+" | Bateria: "+getBateriaEmpilhadora()+" | Movimentos: "+getEmpilhadoraMoves()+" | Carregando BestScore...");
	}
	
	public int getEmpilhadoraMoves() {
		return empilhadoraMoves;
	}

	public void setEmpilhadoraMoves(int empilhadoraMoves) {
		this.empilhadoraMoves = this.empilhadoraMoves+empilhadoraMoves;
	}

	public boolean isMartelo() {
		return martelo;
	}

	public void setMartelo(boolean martelo) {
		this.martelo = martelo;
	}

	public int getBateriaEmpilhadora() {
		return bateriaEmpilhadora;
	}

	public Direction getDirecao() {
		return direcao;
	}

	public void setBateriaEmpilhadora(int bateria) {
		if (this.bateriaEmpilhadora+bateria>=100)
			this.bateriaEmpilhadora=100;
		else 
			this.bateriaEmpilhadora=bateriaEmpilhadora+bateria;
	}

	private boolean hasBateria() {
		if (bateriaEmpilhadora>0)
			return true;
		return false;
	}
	
	private void correctImage(Direction direcao) {
		if (direcao==Direction.DOWN)
			setName("Empilhadora_D");
		if (direcao==Direction.UP)
			setName("Empilhadora_U");
		if (direcao==Direction.LEFT)
			setName("Empilhadora_L");
		if (direcao==Direction.RIGHT)
			setName("Empilhadora_R");					
	}
	
	private void statusUpdate (Direction direcao) {
		correctImage(direcao);			
		setEmpilhadoraMoves(1);			
		setBateriaEmpilhadora(-1);	
	}
	
	public void move (Direction direcao) {
		Point2D newPosition = getPosition().plus(direcao.asVector());
		this.direcao=direcao;
		if (!hasBateria()) 
			return;	
		if (ImageMatrixGUI.getInstance().isWithinBounds(newPosition)){
			statusUpdate(direcao);								
			for (AbstractSObject o: sokoban.getObjectsInPosition(newPosition)) {			
				System.out.println(o);																								
				if (sokoban.canMove(newPosition)) 					
					setPosition(newPosition);					
				if (o instanceof ActiveObject) 											
					((ActiveObject) o).move(direcao);																																			
				if (o instanceof InteractiveObject) 								
					((InteractiveObject) o).interact(this);								
			}
		}
		ImageMatrixGUI.getInstance().update();			
	}
}
	
	
	
	
	
	
	
	
	
	
	
	
