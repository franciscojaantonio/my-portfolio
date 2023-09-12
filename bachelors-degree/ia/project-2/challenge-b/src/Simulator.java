import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;


/**
 * 
 * @author Sancho Oliveira
 * @version 4
 *
 */
public class Simulator {
	JFrame f = new JFrame("Robot simulator");
	private Arena arena;
	private double angle;

	private Point robotPosition = new Point(50, 250);
	private int robotSpeed = 5;
	private double robotDirection = Math.PI / 6;
	private int simulationSpeed = 100;

	private LinkedList<Point> obstacles = new LinkedList<Point>();
	private double distanceC;
	private double obstAngleC;
	private double distanceL;
	private double distanceR;
	private double obstAngleR;
	private double obstAngleL;

	public Simulator() {
		arena = new Arena();
		f.add(arena);
		f.setSize(1000, 1000);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		addObstacles();
	}

	private void addObstacles() {
		obstacles.add(new Point(40, 450));
		obstacles.add(new Point(40, 150));
		obstacles.add(new Point(200, 50));
		obstacles.add(new Point(200, 450));
		obstacles.add(new Point(300, 250));
		obstacles.add(new Point(500, 450));
		obstacles.add(new Point(500, 750));
		obstacles.add(new Point(500, 250));
		obstacles.add(new Point(200, 750));
	}

	public void addObstacle(int x, int y) {
		obstacles.add(new Point(x, y));
	}

	/**
	 * Sets the new angle for the wheels of the robot.
	 * 
	 * @param angle the value of the new angle in degrees
	 */
	public void setRobotAngle(double angle) {
		this.angle = Math.toRadians(angle);
	}

	/**
	 * Getter for the robot speed.
	 * 
	 * @return the current speed of the robot
	 */
	public int getRobotSpeed() {
		return robotSpeed;
	}

	/**
	 * Sets the new robot speed.
	 * 
	 * @param robotSpeed the new value for the robot speed.
	 */
	public void setRobotSpeed(int robotSpeed) {
		this.robotSpeed = robotSpeed;
	}

	/**
	 * Getter for the simulation speed
	 * 
	 * @return time for a simulation step in milliseconds.
	 */
	public int getSimulationSpeed() {
		return simulationSpeed;
	}

	/**
	 * Setter for the time needed for a simulation step.
	 * 
	 * @param simulationSpeed time in milliseconds.
	 */
	public void setSimulationSpeed(int simulationSpeed) {
		this.simulationSpeed = simulationSpeed;
	}

	/**
	 * Moves simulation one time step forward and shows the new state of the
	 * environment in GUI. Uses the current angle of the wheels to move the robot.
	 * After the new position of the robot has been computes the sensor values are
	 * updated.
	 */
	public void step() {
		robotDirection += angle;
		robotDirection = robotDirection % (2 * Math.PI);
		robotPosition.x += (robotSpeed * Math.cos(robotDirection));
		robotPosition.y += (robotSpeed * Math.sin(robotDirection));
		updateSensorValues();
		f.repaint();

		try {
			Thread.sleep(simulationSpeed);
		} catch (InterruptedException e) {
		}

	}

	private void updateSensorValues() {
		distanceL = Double.MAX_VALUE;
		distanceC = Double.MAX_VALUE;
		distanceR = Double.MAX_VALUE;
		for (Point obst : obstacles) {
			double dist = robotPosition.distance(obst);
			double ang = Math.atan2(obst.y - robotPosition.y, obst.x - robotPosition.x);
			double angToRobot = (ang - robotDirection) % (2 * Math.PI);
			if(angToRobot>Math.PI)
				angToRobot-=2*Math.PI;
			if(angToRobot<-Math.PI)
				angToRobot+=2*Math.PI;
			if (Math.abs(angToRobot) <= Math.PI / 6 && distanceC > dist) {
				distanceC = dist;
				obstAngleC = ang;
			} else if (angToRobot > Math.PI / 6 && angToRobot < Math.PI / 2 && distanceR > dist) {
				distanceR = dist;
				obstAngleR = ang;
			} else if (angToRobot < -Math.PI / 6 && angToRobot > -Math.PI / 2 && distanceL > dist) {
				distanceL = dist;
				obstAngleL = ang;
			}
		}

	}

	/**
	 * Getter for the distance of the closest obstacle detected by the central
	 * sensor
	 * 
	 * @return distance in meters of the closest obstacle inside the opening angle
	 *         (60º) of the central sensor.
	 */
	public double getDistanceC() {
		return Math.min(10, distanceC / 30);
	}

	/**
	 * Getter for the distance of the closest obstacle detected by the left sensor
	 * 
	 * @return distance in meters of the closest obstacle inside the opening angle
	 *         (60º) of the left sensor.
	 */
	public double getDistanceL() {
		return Math.min(10, distanceL / 30);
	}

	/**
	 * Getter for the distance of the closest obstacle detected by the right sensor
	 * 
	 * @return distance in meters of the closest obstacle inside the opening angle
	 *         (60º) of the right sensor.
	 */
	public double getDistanceR() {
		return Math.min(10, distanceR / 30);
	}

	private class Arena extends JComponent {
		private static final int ROBOT_RADIUS = 10;
		private static final int OBST_RADIUS = 50;

		@Override
		public void paint(Graphics g) {
			super.paint(g);
			g.setColor(Color.black);
			for (Point p : obstacles) {
				g.fillOval(p.x - OBST_RADIUS / 2, p.y - OBST_RADIUS / 2, OBST_RADIUS, OBST_RADIUS);

			}

			g.drawOval(robotPosition.x- ROBOT_RADIUS / 2, robotPosition.y -ROBOT_RADIUS / 2, ROBOT_RADIUS, ROBOT_RADIUS);

			int x = robotPosition.x; // + ROBOT_RADIUS / 2  ;
			int y = robotPosition.y;//+ ROBOT_RADIUS / 2 

			g.drawLine(x, y, (int) (x + 10 * Math.cos(robotDirection)), (int) (y + 10 * Math.sin(robotDirection)));

			if (distanceC < Double.MAX_VALUE) {
				g.setColor(Color.red);
				g.drawLine(x, y, (int) (x +  distanceC * Math.cos(obstAngleC)),
						(int) (y +  distanceC * Math.sin(obstAngleC)));
			}
			if (distanceL < Double.MAX_VALUE) {
				g.setColor(Color.green);
				g.drawLine(x, y, (int) (x + distanceL * Math.cos(obstAngleL)),
						(int) (y + distanceL * Math.sin(obstAngleL)));
			}

			if (distanceR < Double.MAX_VALUE) {
				g.setColor(Color.blue);
				g.drawLine(x, y, (int) (x +  distanceR * Math.cos(obstAngleR)),
						(int) (y + distanceR * Math.sin(obstAngleR)));
			}
		}
	}
}