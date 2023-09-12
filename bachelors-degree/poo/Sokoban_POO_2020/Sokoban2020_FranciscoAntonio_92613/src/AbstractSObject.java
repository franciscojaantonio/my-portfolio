import pt.iul.ista.poo.gui.ImageTile;
import pt.iul.ista.poo.utils.Point2D;

public abstract class AbstractSObject implements ImageTile {
	
	private Point2D position; 
	private String imageName;
	private int layer;
	private boolean goThrough; // Indica se e ou nao possivel passar por aquele objeto
	
	public AbstractSObject (Point2D position, String imageName) {
		this.position=position;
		this.imageName=imageName;
	}

	public String getName() {
		return imageName;
	}	

	public Point2D getPosition() {
		return position;
	}	
	
	public int getLayer() {
		return layer;
	}
	
	public void setName(String imageName) {
		this.imageName = imageName;
	}
	
	public void setPosition(Point2D position) {		
		this.position = position;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}
	
	public boolean getGoThrough() {
		return this.goThrough;
	}
	
	public void setGoThrough(boolean goThrough ) {
		this.goThrough = goThrough; 
	}
}
