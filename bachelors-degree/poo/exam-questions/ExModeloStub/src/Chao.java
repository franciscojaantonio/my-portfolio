import pt.iul.ista.poo.gui.ImageTile;
import pt.iul.ista.poo.utils.Point2D;

public class Chao implements ImageTile {

	private Point2D position;

	public Chao(Point2D position) {
		this.position = position;
	}
	
	@Override
	public String getName() {
		return "land";
	}

	@Override
	public Point2D getPosition() {
		return position;
	}

	@Override
	public int getLayer() {
		return 0;
	}

}
