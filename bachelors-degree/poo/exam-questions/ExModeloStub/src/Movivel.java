import pt.iul.ista.poo.utils.Direction;

public interface Movivel {
	default void move(Direction d) {}
	default void move() {}
}
