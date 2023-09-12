public class Score {	
	private String nome;
	private int score;
	private int level;
	
	public Score (String nome, int score, int level) {
		this.nome=nome;
		this.score=score;
		this.level=level;
	}
	
	public Score (int score, int level) {
		this.nome="ND";
		this.score=score;
		this.level=level;
	}

	public String getNome() {
		return nome;
	}

	public int getScore() {
		return score;
	}

	public int getLevel() {
		return level;
	}

	@Override
	public String toString() {
		return nome+":"+score+" Lvl:"+level;
	}
}
