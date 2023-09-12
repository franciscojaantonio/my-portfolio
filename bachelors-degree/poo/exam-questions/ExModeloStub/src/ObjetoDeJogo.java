import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pt.iul.ista.poo.gui.ImageTile;
import pt.iul.ista.poo.utils.Point2D;

public class ObjetoDeJogo implements ImageTile {

	private String name;
	private Point2D position;
	private int layer;

	public ObjetoDeJogo(String name, Point2D position, int layer) {
		this.name = name;
		this.position = position;
		this.layer = layer;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Point2D getPosition() {
		return position;
	}

	@Override
	public int getLayer() {
		return layer;
	}

	protected void setPosition(Point2D newPosition) {
		this.position = newPosition; 
	}
	
	public static ObjetoDeJogo fabrica(char c, int linha, int coluna) {
		// Q5 a)
	}

	@Override
	public String toString() {
		return "ObjetoDeJogo [name=" + name + ", position=" + position + ", layer=" + layer + "]";
	}

	
}
