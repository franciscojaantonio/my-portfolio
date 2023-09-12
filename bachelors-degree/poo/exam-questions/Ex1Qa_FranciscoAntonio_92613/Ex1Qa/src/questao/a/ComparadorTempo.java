package questao.a;

import java.util.Comparator;

public class ComparadorTempo implements Comparator<Musica> {

	@Override
	public int compare(Musica a, Musica b) {
		if (a.getDuracao().totalSeconds()==b.getDuracao().totalSeconds())
			return a.getTitulo().compareTo(b.getTitulo());
		return a.getDuracao().totalSeconds()-b.getDuracao().totalSeconds();
	}
}
