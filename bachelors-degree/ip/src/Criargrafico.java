public class Criargrafico {
	
	//Obter máximo do vetor
	static int maxarray (int [] v){
		int max = v[0];
			for (int i=0; i<v.length; i++)
				if (max <= v[i])
					max = v[i]; 
			return max;
	}
	
	//Criar uma coluna (com espaço antes)
	static void criarcoluna (int altura, int largura, int espaco, Color c, ColorImage img){
		if (altura >= 0){
		for (int x=espaco; x<(espaco+largura); x++){
			for (int y=img.getHeight()-1; y>(img.getHeight()-altura-1); y--){			
				img.setColor(x,y,c);
			}
		}
		}
		else {
			throw new IllegalArgumentException("Altura inválida");
		}
	}
	
	// 1 Criar gráfico de colunas
	static ColorImage grafico (int [] vetor, int largura, int espaco, Color cor){
		if (largura >0 && espaco >= 0){
		ColorImage img = new ColorImage ( vetor.length*(espaco+largura)+espaco, maxarray(vetor) );
		for (int i=0; i<vetor.length; i++)
			criarcoluna(vetor[i], largura,(espaco+largura)*i + espaco, cor, img);
		return img; 
	}
		else {
			throw new IllegalArgumentException("Argumentos inválidos!");
		}
	}
			
	
	//Efeito gradiente		
	static Color efeitogradiente (Color cor, int pixeis, int i){
		double r = cor.getR()-(((cor.getR()*0.98)/pixeis)*i);
		double g = cor.getG()-(((cor.getG()*0.98)/pixeis)*i);
		double b = cor.getB()-(((cor.getB()*0.98)/pixeis)*i);
		int r1= (int) r;
		int g1= (int) g;
		int b1= (int) b;
	if (r1>255)
		r1=255;
	if (g1>255)
		g1=255;
	if (b1>255)
		b1=255;
	if (r1<0)
		r1=0;
	if (g1<0)
		g1=0;
	if (b1<0)
		b1=0;
	Color c = new Color(r1,g1,b1);
	return c;
	}
	
	//Criar coluna com gradiente
	static void criarcolunagradiente (int altura, int largura, int espaco, Color cor, ColorImage img, int pixeisgradiente){		
		if ( pixeisgradiente<(largura/2)){			
		criarcoluna (altura, largura, espaco, cor, img);
		for (int x=espaco, a=pixeisgradiente; x<espaco+pixeisgradiente; x++, a--){
			for (int y=img.getHeight()-1; y>(img.getHeight()-altura-1); y--){
			img.setColor(x,y,efeitogradiente(cor,pixeisgradiente,a));
			}
		}
		for (int x=(espaco+largura-pixeisgradiente), a=0; x<espaco+largura; x++, a++){
			for (int y=img.getHeight()-1; y>(img.getHeight()-altura-1); y--){
				img.setColor(x,y,efeitogradiente(cor,pixeisgradiente,a));
			}
		}
		for (int y=img.getHeight()-altura, a=pixeisgradiente, l=0; y<img.getHeight(); y++, a--, l++){
			for (int x=espaco+l; x<espaco+largura-l; x++){			
				img.setColor(x,y,efeitogradiente(cor,pixeisgradiente,a));
			}
		}		
	}
		else {
			throw new IllegalArgumentException("Número de pixes de gradiente inválido!");
		}
	}
	
	// 2 Criar gráfico com gradiente
	static ColorImage graficocomgradiente (int [] vetor, int largura, int espaco, Color cor, int pixeisgradiente){
		ColorImage img = new ColorImage ( vetor.length*(espaco+largura)+espaco, maxarray(vetor) );
		for (int i=0; i<vetor.length; i++)
			criarcolunagradiente(vetor[i], largura,(espaco+largura)*i + espaco, cor, img, pixeisgradiente);
		return img; 
	}
	
	//Criar circulo
	static void circulo (int altura, int raio, int espaco, Color cor, ColorImage img){
		for (int x=espaco; x<img.getWidth();x++){
			for(int y=0; y<img.getHeight();y++){
				if(((x-((espaco+(2*raio))-raio-1))*(x-((espaco+(2*raio))-raio-1))+(y-(img.getHeight()-altura))*(y-(img.getHeight()-altura)))<=raio*raio){
						img.setColor(x,y,cor);
				}
			}
		}
	}
		
	// 3 Criar gráfico dispersão
	static ColorImage graficodispersao (int [] vetor, int raio, int espaco, Color cor){
		if (raio > 0){
		ColorImage img = new ColorImage ( ((espaco+(2*raio))*vetor.length)+espaco , maxarray(vetor)+raio );
		for (int i=0; i<vetor.length; i++)
			circulo(vetor[i], raio,((espaco+(2*raio))*i) + espaco, cor, img);
		return img; 
	}
		else{
			throw new IllegalArgumentException("Argumento inválido!");
		}
	}
	
	
	// 4 Rodar imagem 90graus
	static ColorImage rotacao90 (ColorImage imagem){
		ColorImage img = new ColorImage( imagem.getHeight(), imagem.getWidth() );
		for (int x=0; x<imagem.getWidth(); x++){
			for (int y=0; y<imagem.getHeight(); y++){
				Color c = imagem.getColor(x,y);
				img.setColor(img.getWidth()-1-y, x, c);
			}
		}
		return img;
	}
}