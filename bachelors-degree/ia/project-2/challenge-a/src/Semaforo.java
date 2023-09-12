import java.util.Scanner;

import net.sourceforge.jFuzzyLogic.FIS;

public class Semaforo {
    public static void main(String[] args) throws Exception {

        String fileName = "covid.fcl";
        FIS fis = FIS.load(fileName,true);

        if( fis == null ) { 
            System.err.println("Can't load file: '" + fileName + "'");
            return;
        }
        
        Scanner in = new Scanner(System.in);
        System.out.println("Qual é a distancia de seguranca da esplanada? ");
        double distancia = in.nextDouble();
        System.out.println("Qual é a percentagem de ocupação da esplanada? ");
        double esplanada = in.nextDouble();
        in.close();
   
        fis.setVariable("distancia", distancia);
        fis.setVariable("esplanada", esplanada);
        fis.evaluate();  

        System.out.println("Valor do semáforo: " + fis.getVariable("semaforo").defuzzify());
    }
}