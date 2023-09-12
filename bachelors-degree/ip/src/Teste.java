class Teste {
	static void testeParte1 (){
		int raio =15;
		int [] vetor = {120,0,50,70};
		int largura = 40;
		int espaco = 30;
		int pixeisgradiente=12;
		Color cor1 = new Color(0,0,255);
		Color cor2 = new Color(0,255,0);
		Color cor3 = new Color(255,0,0);
		ColorImage imgex1 = Criargrafico.grafico(vetor, largura, espaco, cor3);
		ColorImage imgex2 = Criargrafico.graficodispersao (vetor, raio, espaco, cor1);
		ColorImage imgex3 = Criargrafico.graficocomgradiente(vetor, largura, espaco, cor2, pixeisgradiente);
		ColorImage imgex4= Criargrafico.rotacao90(Criargrafico.grafico(vetor, largura, espaco, cor1));
	int x=2;
	}
	static void testeParte2(){
		int raio =20;
		int [] vetor = {333,150,39,200};
		int largura = 60;
		int espaco = 30;
		Color cor = new Color(255,0,0);
		Graphic g1 = new Graphic (Criargrafico.grafico(vetor, largura, espaco, cor), 
			"grafico nome original", "eixo dos xx", "eixo dos yy");
		
	}
	static void testeParte3 (){
		int [] vetor1 = {120,0,50,25};
		//int [] vetor2 = {300,400,32,370};
		int largura = 40;
		//int largura2 = 60;
		//int pixeisgradiente= 10;
		//int espaco = 20;
		int espaco2 = 10;
		int raio= 15;
		Color cor1 = new Color(0,0,255);
		Color cor2 = new Color(0,255,0);
		Color cor3 = new Color(255,0,0);
		Graphic g1 = new Graphic (Criargrafico.grafico(vetor1, largura, espaco2, cor2), 
				"aaa", "eixo dos xx", "eixo dos yy");		
		Graphic g2 = new Graphic (Criargrafico.grafico(vetor1, 40, 20, cor1));
		Graphic g3 = new Graphic (Criargrafico.graficocomgradiente(vetor1, 40, 20,cor1,10), "ccc", "xxxx", "yyyy");
		Graphic g4 = new Graphic (Criargrafico.graficodispersao(vetor1,raio, espaco2, cor3));
		g2.definirtransparente();
		Graphic [] vg1 = {g3,g1,g4,g2};
		
		SobreporGraficos vet = new SobreporGraficos (vg1);
		vet.sobreposicao();
	}
}
	
	

