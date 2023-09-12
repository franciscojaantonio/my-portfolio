package questao.a;

public class RemoveMoreThan6m implements FiltroDeMusicas{

	@Override
	public boolean excluir(Musica m) {
		if (m.getDuracao().totalSeconds()>6*60)		
			return true;
		return false;
	}

}
