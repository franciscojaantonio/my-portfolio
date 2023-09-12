public class SobreporGraficos {
	
//Atributos
	int topo;
	Graphic [] pilhagraficos;
	private static final int size = 10;
	    
	//1
	//Construtores
	public SobreporGraficos (Graphic objetografico){
		this.pilhagraficos = new Graphic [size];
		this.pilhagraficos [0] = objetografico;
		topo = 0;
	}
	public SobreporGraficos (Graphic [] vetorobjetografico){
		this.pilhagraficos = new Graphic [size];
		int cont=0;
		if (vetorobjetografico.length < size){
			for (int i=0; i < vetorobjetografico.length; i++)
				if (vetorobjetografico[i] != null){
					pilhagraficos[cont] = vetorobjetografico[i];
					cont++;
				}
			topo= cont-1;
		}
		else
		{
			throw new IllegalArgumentException("Dimensão do Conjunto de gráficos inválida!");
		}
	}
	
	// 2 Adicionar gráfico ao topo
	void addtopo (Graphic graph){
		if (graph != null){
			if (topo+1 <= pilhagraficos.length) {
		pilhagraficos [topo+1] = graph;
		topo = topo+1;
			}
			else {
				throw new IllegalArgumentException("Pilha de gráficos cheia");
			}
	}
		else {
			throw new IllegalArgumentException("Gráfico inválido");
		}
	}
	
	
	// 3 Remover gráfico do topo
	void removetopo (){
		pilhagraficos [topo] = null;
		topo = topo -1;
	}
	
	// 4 Obter gráfico do topo
	Graphic getTopo (){
		return pilhagraficos [topo];
	}
	
	// 5 Adicinar grafico (não no topo)
	void add (Graphic graph, int posicao){	
		if ( posicao < topo && graph != null) {
			Graphic aux = pilhagraficos [posicao];
			pilhagraficos [posicao] = graph;
			for (int i = (posicao + 2); i < (topo +2); i++)
				pilhagraficos [i] = pilhagraficos [i-1];
				pilhagraficos [posicao+1] = aux;
				topo=topo+1;
		}
		else {
			throw new IllegalArgumentException("Posição ou gráfico inválido(os)!");
		}
	}
	
	// 6 Trocar gráficos de posicao
	void trocarposicao (int a, int b){
		if ((a <= topo && b <= topo) && (a != b) ){
			Graphic aux = pilhagraficos [a];
			pilhagraficos [a] = pilhagraficos [b];
			pilhagraficos [b] = aux;
		}
		else {
			throw new IllegalArgumentException("Posição(ões) inválida(as) ou estas designam a mesma!");
		}
	}
	
	// 7 Obter gráficos sem titulo 
	Graphic [] getGraficossemtitulo(){
		int a=0;
		Graphic [] semtitulo = new Graphic [topo+1];
		for (int i=0; i < topo+1; i++){
			Graphic aux = pilhagraficos [i];
			if ( aux.getTitulographic() == null && aux.getTituloeixoabcissas() ==null
												&& aux.getTituloeixoordenadas() == null){
				semtitulo[a] = pilhagraficos[i];
				a++;
			}
		}
		return semtitulo;
	}
	
	// 8 Obter graficos por ordem alfabetica
	Graphic [] ordenarportitulo (){
		int a=0;
		for (int i = 0; i < topo+1; i++) {
	        for (int j = i + 1; j < topo+1; j++) {
	        	if (pilhagraficos[i].getTitulographic() == null && pilhagraficos[j] ==null)
	        		a=0;
	        	if (pilhagraficos[i].getTitulographic() == null && pilhagraficos[j].getTitulographic() != null)
	        		a=1;
	        	if (pilhagraficos[i].getTitulographic() != null && pilhagraficos[j].getTitulographic()== null)
	        		a=-1;
	        	if (pilhagraficos[i].getTitulographic() != null && pilhagraficos[j].getTitulographic() != null)
	        		a = pilhagraficos[i].getTitulographic().compareTo(pilhagraficos[j].getTitulographic());
	        	if( a > 0) {
	                Graphic temp = pilhagraficos[i];
	                pilhagraficos[i] = pilhagraficos[j];
	                pilhagraficos[j] = temp;
	            }
	        }
	    }
		return pilhagraficos;
	}
	
	// 9 Obter imagem da sobreposicao de graficos
	ColorImage sobreposicao(){
		int largura = 0;
		int altura = 0;
		boolean op = false;
		for (int i = 0; i < topo+1; i++){
			if ( pilhagraficos[i].graficotransparente(op).getWidth() > largura){
				largura = pilhagraficos[i].graficotransparente(op).getWidth();
			}
			if (pilhagraficos[i].graficotransparente(op).getHeight() > altura){
				altura = pilhagraficos[i].graficotransparente(op).getHeight();
			}
		}
		ColorImage finalimage = new ColorImage (largura, altura);
		for (int i=0; i < topo + 1; i++){		
			for (int x=0;  x<pilhagraficos[i].graficotransparente(op).getWidth(); x++){
				for (int y=0, y1=finalimage.getHeight()-pilhagraficos[i].graficotransparente(op).getHeight(); y<pilhagraficos[i].graficotransparente(op).getHeight(); y++, y1++){
							Color c = pilhagraficos[i].graficotransparente(op).getColor(x,y);
							if ( c.getR()!=0 || c.getG()!=0 || c.getB()!=0){
								finalimage.setColor(x,y1,c);
							}
				}
			}
		}
		return finalimage;
	}	
	
	// 10 Sobreposicao com rotacao 90º
	ColorImage rodarsobreposicao (){
		return Criargrafico.rotacao90(sobreposicao());
	}
}