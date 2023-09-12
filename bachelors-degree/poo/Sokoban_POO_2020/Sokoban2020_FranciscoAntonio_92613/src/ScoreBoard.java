import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

import javax.swing.JOptionPane;

public class ScoreBoard {
	private SokobanGame sokoban;
	private ArrayList<Score> scores=new ArrayList<>();
	/* Caso faltem ficheiros score aos niveis, estes serao criados aqui com um padrao default (ND/999/(level)) que sera depois subtituido
	este ScoreBoard esta feito para as 3 melhores pontuacoes */
	public ScoreBoard(SokobanGame sokoban) {
		this.sokoban=sokoban;
		for (int i=1; i<=sokoban.getLevels().length; i++) {
			File file = new File("score/score"+i+".txt");
			try {
				if (file.createNewFile()) {
					FileWriter fw = new FileWriter("score/score"+i+".txt");
					for (int j=0; j<3; j++) {																					
						String nome="ND";									
						int score=999;			
						int level=i;			
						fw.write(nome+" "+score+" "+level+" ");								
						fw.write(System.lineSeparator());																										
					}
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		readScores();
		deleteUnusedFiles();
	}
	
	public ArrayList<Score> getScores() {
		return scores;
	}
	// Adiciona o score obtido se estiver entre os 3 melhores 
	public void addScore(Score score) {
		sortScores();
		highScoreChecker(score);
		sortScores();
		try {
			write();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Mantem na lista de scores apenas os 3 melhores resultados de cada nivel
	public void prepareScoreList() {
		ArrayList<Score> temp = new ArrayList<>();
		for (Score s: scores) {
			if (temp.size()<3*s.getLevel()) 
				temp.add(s);				
		}
		this.scores=temp;
	}
	// Escreve os scores presentes na lista nos respetivos ficheiros txt
	private void write () throws IOException {
		for (int i=1; i<=sokoban.getLevels().length; i++) {		
			FileWriter fw = new FileWriter("score/score"+i+".txt");		
			for (Score s: scores) { 
				if (s.getLevel()==i) {				
					String nome=s.getNome();										
					int score=s.getScore();							
					int level=s.getLevel();						
					fw.write(nome+" "+score+" "+level+" ");						
					fw.write(System.lineSeparator());					
				}
			}			
			fw.close();		
		}
	}
	// Mantem o numero de ficheiros score igual ao de levels 
	public void deleteUnusedFiles() {
		File diretorio = new File ("score");		
		File [] files = diretorio.listFiles();
		if (files.length==sokoban.getLevels().length) 			
			return;
		else {
			for (int i=sokoban.getLevels().length+1; i<=files.length; i++) {
				File file = new File("score/score"+i+".txt"); 
				file.delete();
			}
		}
	}
	// Verifica se o score esta entre os 3 melhores e regista o nome  
	public void highScoreChecker (Score score) {
		String nome="";
		int sco=0;
		int lvl=0;		
		int index= (3*sokoban.getLevel())-1;
		if (scores.get(index).getScore() <= score.getScore())
			return;
		else {
			String n =JOptionPane.showInputDialog("Estas no top 3 de melhores pontuacoes, parabens! Por favor insere o teu nome.");
			if (n==null || n.isEmpty())
				nome="ND";
			else
				nome=n;		
			sco=score.getScore();
			lvl=score.getLevel();
			scores.add(new Score (nome, sco, lvl));
		}
		sortScores();
		prepareScoreList();
	}
	// Le os scores presentes nos ficheiros txt e adiciona-os a lista de Scores 
	public void readScores() {
		Scanner scan=null;
		for (int i=1; i<=sokoban.getLevels().length; i++) {		
			try {		
				scan = new Scanner(new File("score/score"+i+".txt"));		
			} catch (FileNotFoundException e) {		
				e.printStackTrace();		
			}					
			while (scan.hasNext()) {		
				String nome=scan.next();								
				int score=scan.nextInt();									
				int level=scan.nextInt();		
				scores.add(new Score(nome, score, level));			
			}		
		}
		sortScores();
		prepareScoreList();
	}
	// Ordena os scores (menos movimentos primeiro)
	private void sortScores () {
		scores.sort(new ComparadorScore());
	}
	// Enquanto jogamos o nivel aparece na barra de status a melhor pontuacao obtida nesse nivel
	public String displayHS(int level) {
		sortScores();
		prepareScoreList();
		Score score=scores.get((level*3)-3);
		return (score.getNome()+": "+score.getScore());
	}
	// Pressionando a tecla Q obtemos uma lista dos 3 melhores scores no respetivo nivel
	public void showBestScores() {
		File file = new File ("score/score"+sokoban.getLevel()+".txt");
		String scoreList="";
		try {
			scoreList = new String (Files.readAllBytes(Paths.get(file.getPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(null,"(Name/Score/Level)"+System.lineSeparator()+System.lineSeparator()+scoreList,"Best Scores - Level: "+sokoban.getLevel(),JOptionPane.INFORMATION_MESSAGE);
	}
	// Compara os scores (menor nivel primeiro, e depois, menor numero de passos primeiro)
	private class ComparadorScore implements Comparator<Score> {
		public int compare (Score s1, Score s2) {
			if (s1.getLevel()!=s2.getLevel())
				return s1.getLevel()-s2.getLevel();
			else		 
				return s1.getScore()-s2.getScore(); 
		}
	}
}

