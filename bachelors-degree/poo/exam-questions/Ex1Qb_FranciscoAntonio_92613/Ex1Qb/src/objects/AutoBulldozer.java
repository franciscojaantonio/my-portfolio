package objects;

import engine.StoneBreaker;
import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

//TODO
//Pode ser necessario mudar a declaracao da classe e/ou construtor
//
public class AutoBulldozer extends Bulldozer {

	public AutoBulldozer(Point2D position) {
		super(position);
		this.setName("autobulldozer_U");
	}

	@Override
	public boolean isTransposable() {
		return false;
	}
	public void move(Direction d) {
		// TODO
		Direction dir = Direction.random();
		correctImage(dir);
		Point2D newPosition = getPosition().plus(dir.asVector());
		if (!StoneBreaker.getInstance().isWithinBounds(newPosition))
			return;
		if (StoneBreaker.getInstance().canMoveTo(newPosition))						
			setPosition(newPosition);
		for (Breakable o: StoneBreaker.getInstance().breakablesAt(newPosition))
			o.brokenBy(this);					
	}
	private void correctImage(Direction direcao) {
		if (direcao==Direction.DOWN)
			setName("autobulldozer_D");
		if (direcao==Direction.UP)
			setName("autobulldozer_U");
		if (direcao==Direction.LEFT)
			setName("autobulldozer_L");
		if (direcao==Direction.RIGHT)
			setName("autobulldozer_R");					
	}
}
