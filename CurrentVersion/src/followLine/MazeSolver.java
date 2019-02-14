package followLine;

import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.*;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

//green - 

public class MazeSolver extends FollowLineBasic{

	public static void main(String[] args) {
		//EV3TouchSensor touchSensor = new EV3TouchSensor(SensorPort.S1);
		//float[] pressed = new float[1];
		//SampleProvider touchSampler = touchSensor.getTouchMode();
		EV3IRSensor IRSensor = new EV3IRSensor(SensorPort.S2);
		float[] IR = new float[1];
		SampleProvider IRSampler = IRSensor.getDistanceMode();
		
		pilot.setAngularSpeed(50);
		pilot.setLinearSpeed(10);
		float[][] values = measure(leftSampler,rightSampler);
		float[] av = measureAverage(values);
		float diff = 0;
		boolean fwd = false;
		String[] colours;
		int nextAngle = 0;
		
		LCD.drawString("Ready",0,0);
		buttons.waitForAnyPress();
		
		boolean flag = false;
		
		while (buttons.getButtons() != Keys.ID_ESCAPE) {
			flag = false;
			LCD.clear();
			LCD.drawString(String.valueOf(av[1]),0,0);
			LCD.drawString(String.valueOf(values[1][0]),0,1);
			LCD.drawString(String.valueOf(values[1][1]),0,2);
			LCD.drawString(String.valueOf(values[1][2]),0,3);
			LCD.drawString(detectColour(values[1]), 0, 4);
			
			touchSampler.fetchSample(pressed, 0);
			values = measure(leftSampler,rightSampler);
			colours = detectColour(values);
			av = measureAverage(values);
			
			if (pressed[0] == 1.0f) {
				avoid();
			}
			
			if (colours[0].equals("RED") && colours[1].equals("RED")) {
				System.exit(0);
			}
			else if (colours[0].equals("GREEN") && !colours[1].equals("GREEN")) {
				nextAngle = -90; //turn left if green on left sensor
				fwd = moveMaze(colours,fwd);
				flag = true;
			}
			
			else if (colours[1].equals("GREEN") && !colours[0].equals("GREEN")) {
				nextAngle = 90; //turn right if green on right sensor
				fwd = moveMaze(colours,fwd);
				flag = true;
			}
			
			else if (colours[0].equals("WHITE") && colours[1].equals("WHITE")) {
				nextAngle = 0;
				fwd = move(av[0]-av[1],fwd);
			}
			
			else if (colours[0].equals("GREEN") && colours[1].equals("GREEN")) {
				flag = true;
				nextAngle = 180; // turn around for 2 greens
				fwd = moveMaze(colours,fwd);
			}
			
			if (colours[0].equals("RED") || colours[0].equals("GREEN") || colours[1].equals("RED") || colours[1].equals("GREEN"))
			{
				pilot.travel(4.5);
			}
			else if (colours[0].equals("BLACK") || colours[1].equals("BLACK")) {
				if (nextAngle != 0) {
					pilot.travel(5);
					pilot.rotate(nextAngle);
					nextAngle = 0;
					Delay.msDelay(20);
					pilot.forward();
					Delay.msDelay(200);
					fwd = true;
				}
				else  fwd = move(av[0]-av[1],fwd);
			}
			else {
				
				pilot.travel(1);
				fwd = move(av[0]-av[1],fwd);
				
			}
			Delay.msDelay(10);
			LCD.drawString(String.valueOf(nextAngle), 0, 5);
		}
		System.exit(0);
	}
	
	public static boolean moveMaze(String[] colours, boolean fwd) {
		if (colours[0].equals("GRAY") && !colours[1].equals("GRAY")) {
			pilot.rotate(-5);
			Delay.msDelay(30);
			return false;
		}
		else if (colours[1].equals("GRAY") && !colours[0].equals("GRAY")) {
			pilot.rotate(5);
			Delay.msDelay(30);
			return false;
			
		}
		else if (!fwd) {
			pilot.forward();
			return true;
		}
		return true;
		
	}
	
	public static void avoid() {
		LCD.clear();
		LCD.drawString("AAAAAAAAAAAAAAA",0,0);
		buttons.waitForAnyPress();
	}
	
	public static String detectColour(float[] values) {
		float average = (values[0] + values[1] + values[2])/3.0f;
		if (average > 0.07) {
			return "WHITE";
		}
		if (average + 0.04 < values[0]) {
			return "RED";
		}
		if (average < 0.019) {
			return "BLACK";
		}
		if (/*values[1] > 1.2*average && */values[0] < 0.02) {
			return "GREEN";
		}
		/*if (values[2] > 1.3*values[1] && values[2] > 1.3*values[0]) {
			return "GRAY";
		}*/
		return "OOF";
	}
	
	public static String[] detectColour(float[][] values){
		String left = detectColour(values[0]);
		String right = detectColour(values[1]);
		return new String[] {left,right};
	}
	
	
	public static double nextAngle(String[] colours) {
		return -1;
	}
}
