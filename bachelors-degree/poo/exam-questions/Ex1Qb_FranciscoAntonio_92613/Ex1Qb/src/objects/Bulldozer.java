package objects;

import engine.StoneBreaker;
import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;


//TODO
//Pode ser necessario mudar a declaracao da classe e/ou construtor
//
public class Bulldozer extends GameObject {

	public Bulldozer(Point2D position) {
		super("bulldozer_U", position, 2);
	}
	

	public void move(Direction d) {
		// TODO
		correctImage(d);
		Point2D newPosition = getPosition().plus(d.asVector());
		if (!StoneBreaker.getInstance().isWithinBounds(newPosition))
			return;
		if (StoneBreaker.getInstance().canMoveTo(newPosition))						
			setPosition(newPosition);
		for (Breakable o: StoneBreaker.getInstance().breakablesAt(newPosition))
			o.brokenBy(this);			
		
		
	}
	private void correctImage(Direction direcao) {
		if (direcao==Direction.DOWN)
			setName("bulldozer_D");
		if (direcao==Direction.UP)
			setName("bulldozer_U");
		if (direcao==Direction.LEFT)
			setName("bulldozer_L");
		if (direcao==Direction.RIGHT)
			setName("bulldozer_R");					
	}

	@Override
	public boolean isTransposable() {
		return false;
	}
}
