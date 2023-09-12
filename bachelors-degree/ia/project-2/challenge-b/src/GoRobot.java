import net.sourceforge.jFuzzyLogic.FIS;

public class GoRobot {
	
    public static void main(String[] args) throws Exception {

        String fileName = "robot.fcl";
        FIS fis = FIS.load(fileName,true);
        if( fis == null ) { 
            System.err.println("Can't load file: '" + fileName + "'");
            return;
        }
        
        Simulator simulator = new Simulator();
        
        goRobot(fis, simulator);      
    }
    
    private static void goRobot(FIS fis, Simulator simulator) {
    	while(true) {
	        Double e = simulator.getDistanceL();
	        Double c = simulator.getDistanceC();
	        Double d = simulator.getDistanceR();        
	        fis.setVariable("esquerda", e);
	        fis.setVariable("centro", c);
	        fis.setVariable("direita", d);
	        fis.evaluate();         
	        simulator.setRobotAngle(fis.getVariable("angulo").defuzzify());
	        simulator.step();
        }
    }
}