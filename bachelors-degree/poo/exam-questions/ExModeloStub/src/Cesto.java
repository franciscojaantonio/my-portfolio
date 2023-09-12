import pt.iul.ista.poo.utils.Point2D;

public class Cesto extends ObjetoDeJogo implements Interactable {

	public Cesto(Point2D position) {
		super("cesto", position, 1);
	}

	@Override
	public void interact(Agricultor a) {
		a.descarrega();
	}

}
