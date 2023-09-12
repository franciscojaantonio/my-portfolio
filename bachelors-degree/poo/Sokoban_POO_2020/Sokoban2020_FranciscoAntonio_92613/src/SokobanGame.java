import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import javax.swing.JOptionPane;

import pt.iul.ista.poo.gui.ImageMatrixGUI;
import pt.iul.ista.poo.gui.ImageTile;
import pt.iul.ista.poo.observer.Observed;
import pt.iul.ista.poo.observer.Observer;
import pt.iul.ista.poo.utils.Direction;
import pt.iul.ista.poo.utils.Point2D;
public class SokobanGame implements Observer {	
	private Empilhadora player; 
	private ArrayList<AbstractSObject> objects = new ArrayList<>();
	private ArrayList<ImageTile> tiles;
	private File [] levels;
	private int level;
	private Score score;
	private ScoreBoard scoreBoard;
	
	public SokobanGame() throws FileNotFoundException {
		File diretorio = new File ("levels");
		this.levels = diretorio.listFiles();	
		this.level=1;		
		if (levels==null || levels.length<1) 						
			throw new FileNotFoundException("Problema no carregamento de ficheiros de nivel!");
		tiles = buildLevel(levels[level-1]);
		if (player==null)
			throw new NullPointerException("Nao existe player para o nivel: "+level+"!");
		tiles.add(player);
		scoreBoard=new ScoreBoard(this);
		ImageMatrixGUI.getInstance().addImages(tiles);	
	}

	public File[] getLevels() {
		return levels;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public Score getScore() {
		return score;
	}

	public Empilhadora getPlayer() {
		return player;
	}

	public ArrayList<AbstractSObject> getObjects() {
		return objects;
	}

	public int getLevel() {
		return level;
	}

	public void setPlayer(Empilhadora player) {
		this.player = player;
	}
	// Efectua o resize necessario na ImageMatrixGUI (nao devem haver espacos para la dos limites do mapa no txt)
	public void setGameDimension() {
		int width=0;
		int height=0;
		Scanner scan = scanFileLevel(levels[level-1]);
		String linha="";
		while (scan.hasNextLine()) {
			height++;
			String temp = scan.nextLine();
			if (temp.length()>linha.length())
				linha=temp;
		}
		width=linha.length();
		ImageMatrixGUI.setSize(width, height);
	}
	
	//devolve o Scan efectuado ao ficheiro do nivel
	private Scanner scanFileLevel(File level) {
		Scanner scanner=null;
		try {
			scanner = new Scanner (level);
		} 		
		catch (FileNotFoundException e) {						
			System.out.println("Erro: Ficheiro nao encontrado");
		}
		return scanner;		
	}
	//auxilia o buildLevel() - boolean withFloor pois certos objetos precisam de chao na mesma posicao
	private void createObjects(ArrayList<ImageTile> LevelTiles, int x, int y, AbstractSObject object, boolean withFloor) {
		if (object instanceof Empilhadora)
			this.player=(Empilhadora)object;
		LevelTiles.add(object);
		objects.add(object);
		if (withFloor==true) {		
			Chao chao=new Chao(new Point2D(x,y));		
			LevelTiles.add(chao);    	    	
			objects.add(chao);
		}   				
	}
	//cria os objetos e preenche o quadro de jogo 
	private ArrayList<ImageTile> buildLevel(File level){
		ArrayList<ImageTile> LevelTiles = new ArrayList<ImageTile>();
		Scanner scanner=scanFileLevel(level);
		int y=0;
		String linha="";
		while (scanner.hasNextLine()) {			
			linha=scanner.nextLine();			
			for (int x=0; x<linha.length(); x++) {
				char item = linha.charAt(x);
				switch(item) {
	                case '#':
	                	createObjects(LevelTiles,x,y,new Parede( new Point2D (x, y)),false);
	                    break;
	                case 'C':
	                	createObjects(LevelTiles,x,y,new Caixote (new Point2D(x,y), this),true);
	                    break;
	                case 'X':
	                	createObjects(LevelTiles,x,y,new Alvo (new Point2D(x,y)),false);
	                    break;
	                case 'E':
	                    createObjects(LevelTiles,x,y,new Empilhadora (new Point2D(x,y),this),true);
	                    break;
	                case 'O':
	                	createObjects(LevelTiles,x,y,new Buraco (new Point2D(x,y), this),false);
	                    break;
	                case 'p':
	                	createObjects(LevelTiles,x,y,new SmallStone(new Point2D(x,y), this),true);
	                	break;
	                case 'P':
	                	createObjects(LevelTiles,x,y,new BigStone(new Point2D(x,y), this),true);
	                	break;
	                case 'b':
	                	createObjects(LevelTiles,x,y,new Bateria (new Point2D(x,y), this),true);
	                	break;
	                case 'g':
	                	createObjects(LevelTiles,x,y,new Gelo(new Point2D(x,y), this),false);
	                	break;
	                case '%':
	                	createObjects(LevelTiles,x,y,new ParedePartida (new Point2D(x,y), this),true);
	                	break;
	                case 'm':
	                	createObjects(LevelTiles,x,y,new Martelo (new Point2D(x,y), this),true);
	                	break;
	                case 't':
	                	createObjects(LevelTiles,x,y,new Portal (new Point2D(x,y), this),false);
	                	break;
	                case ' ':
	                	createObjects(LevelTiles,x,y,new Chao (new Point2D(x,y)),false);
	                    break;
	                default:
	                	ImageMatrixGUI.getInstance().dispose();
	                	throw new IllegalArgumentException("Presenca de elemento invalido!");
	            }
			}
			y++;
		}
		setGameDimension();
		scanner.close();
		return LevelTiles;
	}
	 
	public void update(Observed arg0) {
		int lastKeyPressed = ((ImageMatrixGUI)arg0).keyPressed();
		System.out.println("KeyPressed: "+lastKeyPressed);
		keyReader(lastKeyPressed);
		if (Direction.isDirection(lastKeyPressed)){
			if (player != null) 
				player.move(Direction.directionFor(lastKeyPressed));										
			if (player==null || !canIWin()) {
				restartLevel();
				return;
			}
			if (player.getBateriaEmpilhadora()==0) {
				if (!levelCompleted())				
					restartLevel();	
				else					//caso o nivel seja concluido no ultimo movimento 
					loadNextLevel();				 
			}
			if (levelCompleted()) 
				loadNextLevel();										
		}							
		if (player!=null)
			ImageMatrixGUI.getInstance().setStatusMessage("Nivel: "+getLevel()+" | Martelo: "+player.isMartelo()+" | Bateria: "+player.getBateriaEmpilhadora()+" | Movimentos: "+getPlayer().getEmpilhadoraMoves()+" | BestScore: "+ scoreBoard.displayHS(level));
	}
	private void loadNextLevel() {
		ImageMatrixGUI.getInstance().setStatusMessage("Level Completed!");
		scoreBoard.addScore(new Score (player.getEmpilhadoraMoves(), level));
		nextLevel();		
	}
	//de acordo com a tecla pressionada chama a respetiva funcao
	private void keyReader(int lastKeyPressed) {
		if (lastKeyPressed==81)//Tecla Q para ver pontuacoes 
			scoreBoard.showBestScores();			
		if (lastKeyPressed==27)//Tecla Esc para sair
			exitGame();
		if (lastKeyPressed==82)//Tecla R para restart do nivel
			restartLevel();
		if (lastKeyPressed==74) {
			level--;          //Tecla J para carregar nivel anterior
			prepareLevel();
		}
		if (lastKeyPressed==75) {
			level++;          //Tecla K para carregar proximo nivel
			prepareLevel();
		}		
	}
	//obtem todos os objetos na posicao dada 			
	public ArrayList<AbstractSObject> getObjectsInPosition(Point2D position) {
		ArrayList<AbstractSObject> ovec = new ArrayList<>();
		for (AbstractSObject o: objects)
			if (o.getPosition().equals(position))								
				ovec.add(o);		
		ovec.sort(new ComparadorObject());
		return ovec;
	}
	//verifica se e possivel mover para a posicao dada
	public boolean canMove (Point2D position) {
		for (AbstractSObject o: objects)
			if (o.getPosition().equals(position))
				if (!o.getGoThrough())
					return false;
		return true;
	}
	//verifica se o nivel esta completo
	public boolean levelCompleted() {
		int alvo=0;
		int caixotesNoAlvo=0;
		for (AbstractSObject o: objects) {
			if (o instanceof Alvo)
				alvo++;
			if (o instanceof Caixote)
				if (((Caixote) o).isNoAlvo())
					caixotesNoAlvo++;
		}
		return alvo==caixotesNoAlvo;		
	}
	//sair do jogo
	public void exitGame() {
		int r = JOptionPane.showConfirmDialog(null, "Queres mesmo sair?", "Sair", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (r==JOptionPane.YES_OPTION)			
			ImageMatrixGUI.getInstance().dispose();
		else
			return;				
	}
	//remover o objecto dado
	public void removeObject(AbstractSObject object) {
		tiles.remove(object);
		ImageTile image=object;
		ImageMatrixGUI.getInstance().removeImage(image);
		if (object instanceof Empilhadora)
			this.player=null;
		else 
			objects.remove(object);
	}
	//verificar valor do nivel
	public void validateLevelIndex() {
		if (level<1)
			level=1;
		if (level>levels.length)
			level=levels.length;
	}
	//preparar o quadro de jogo para restart/proximo nivel/etc
	public void prepareLevel() {
		validateLevelIndex();
		objects.clear();
		this.player=null;
		ImageMatrixGUI.getInstance().clearImages();
		tiles=buildLevel(levels[level-1]);
		if (player==null)
			throw new NullPointerException("Nao existe player para o nivel: "+level+"!");
		tiles.add(player);
		score=null;
		ImageMatrixGUI.getInstance().addImages(tiles);
		ImageMatrixGUI.getInstance().update();		
	}
	//avancar para o proximo nivel
	public void nextLevel() {
		if (level==levels.length)
			level=1;
		else			
			level++;
		prepareLevel();		
	}
	//restart do nivel
	public void restartLevel() {
		if (player==null || !canIWin())
			ImageMatrixGUI.getInstance().setStatusMessage("Game Over!");
		else if (player.getBateriaEmpilhadora()==0)						
			ImageMatrixGUI.getInstance().setStatusMessage("Player out of battery!");
		int r = JOptionPane.showConfirmDialog(null, "Deseja reiniciar?"+System.lineSeparator()+"Pressionar 'No' ou fechar a janela ira terminar o jogo.", "Reiniciar", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (r!=JOptionPane.YES_OPTION)
			ImageMatrixGUI.getInstance().dispose();
		else 
			prepareLevel();	
	}
	//verifica se existem caixas suficientes para concluir o nivel (false apenas se as caixas nao forem suficientes para o numero de Alvos) 
	public boolean canIWin() {
		int a=0;
		int c=0;
		for (AbstractSObject o: objects) {
			if (o instanceof Caixote)
				c++;
			if (o instanceof Alvo)
				a++;
		}
		return c>=a;												
	}	
	//especie de hierarquia definida por: ActiveObject>InteractiveObject>outros
	public class ComparadorObject implements Comparator<AbstractSObject> {
		public int compare (AbstractSObject s1, AbstractSObject s2) {
			int o1=0;
			int o2=0;		
			if (s1 instanceof ActiveObject)
				o1=3;
			else if (s1 instanceof InteractiveObject)
				o1=2;
			else 
				o1=1;
			if (s2 instanceof ActiveObject)
				o2=3;
			else if (s2 instanceof InteractiveObject)
				o2=2;
			else
				o2=1;
			return o2-o1;
		}
	}		
}