import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;

public class Agricultor extends ObjetoDeJogo implements Movivel {

	private Direction lastDirection = Direction.UP;

	private static final double MAX_CARGA = 2.0;

	List<Apanhavel> apanhados = new ArrayList<>();

	public Agricultor(Point2D position) {
		super("Agricultor", position, 2);
	}

	public void move(Direction direction) {
		lastDirection = direction;

		// Q5 d)

		// Calcular nova posição
		// Verificar se nova posição está dentro dos limites


		// Apanhar todos os objetos apanháveis na nova posição

		
		// Interagir com todos os objetos que implementam Interactable na nova posição

		// atualizar posição
		setPosition(newPosition);
	}


	private void apanhar(Apanhavel o) {
		if (podeApanhar(o)) {
			apanhados.add(o);
			o.apanha(this);
		}
	}

	public boolean podeApanhar(Apanhavel v) {
		return (carga() + v.getPeso() < MAX_CARGA) && !temGalinha();
	}

	private boolean temGalinha() {
		for (Apanhavel a : apanhados)
			if (a instanceof Galinha)
				return true;
		return false;
	}

	private double carga() {
		double s = 0.0;
		for (Apanhavel a : apanhados)
			s += a.getPeso();
		return s;
	}

	public void descarrega() {
		apanhados.clear();
	}

}
