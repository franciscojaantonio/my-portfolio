import java.io.FileNotFoundException;

import pt.iul.ista.poo.gui.ImageMatrixGUI;

public class Main {	
	public static void main(String[] args) throws FileNotFoundException {
		SokobanGame s = new SokobanGame();
		ImageMatrixGUI.getInstance().registerObserver(s);
		ImageMatrixGUI.getInstance().go();
	}
}