public class Graphic {

	//Atributos
	private ColorImage graphic;
	private String titulographic;
	private String tituloeixoabcissas;
	private String tituloeixoordenadas;
		
	//Construtores
	public Graphic (ColorImage graphic, String titulographic, String tituloeixoabcissas, String tituloeixoordenadas){
		this.titulographic = titulographic;
		this.tituloeixoabcissas = tituloeixoabcissas;
		this.tituloeixoordenadas = tituloeixoordenadas;
		this.graphic = graphic;
	}
	public Graphic (ColorImage graphic){
		this.graphic = graphic;
	}

	//Métodos
	
	// 1
	public void setTitulographic (String titulographic){
		this.titulographic = titulographic;
	}		
	
	// 2
	public void setTituloeixoabcissas (String tituloeixoabcissas){
		this.tituloeixoabcissas = tituloeixoabcissas;
	}		
	
	// 3
	public void setTituloeixoordenadas (String tituloeixoordenadas){
		this.tituloeixoordenadas = tituloeixoordenadas;
	}		
	
	//Auxiliar alinea 4
	public void definirtransparente (){
		Color transparente = new Color (0,0,0);
		for (int x=0; x<graphic.getWidth(); x++){
			for (int y=0; y<graphic.getHeight(); y++){
				if ( (x%2 != 0 && y%2==0) || (x%2 == 0 && y%2 != 0) )
					graphic.setColor(x,y,transparente);
			}
		}
	}
	
	// 4
	public void transparente (boolean op){
		if (op == true)
			definirtransparente();
	}		
	
	// 5
	public ColorImage graficotransparente (boolean op){
		transparente(op);
		return graphic;
	}
	
	//Auxiliar alinea 6
	public String getTitulographic (){
		return titulographic;
	}
	
	//Auxiliar alinea 6
	public String getTituloeixoabcissas (){
		return tituloeixoabcissas;
	}
	
	//Auxiliar alinea 6
	public String getTituloeixoordenadas (){
		return tituloeixoordenadas;
	}
	
	// 6
	public String Informacaotextual (){
		return getTitulographic() +" / "+ getTituloeixoabcissas() +" / "+ getTituloeixoordenadas();
	}

}