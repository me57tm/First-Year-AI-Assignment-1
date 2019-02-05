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
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.*;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

//green - 

public class MazeSolver extends FollowLineBasic{

	public static void main(String[] args) {
		pilot.setAngularSpeed(50);
		pilot.setLinearSpeed(10);
	//	lcSensor.setFloodlight(false);
	//	rcSensor.setFloodlight(false);
		float[][] values = measure(leftSampler,rightSampler);
		float[] av = measureAverage(values);
		while (buttons.getButtons() != Keys.ID_ESCAPE) {
			LCD.drawString(String.valueOf(av[0]),0,0);
			LCD.drawString(String.valueOf(values[0][0]),0,1);
			LCD.drawString(String.valueOf(values[0][1]),0,2);
			LCD.drawString(String.valueOf(values[0][2]),0,3);
			//LCD.drawString(String.valueOf(av[1]),0,4);
			//LCD.drawString(String.valueOf(values[1][0]),0,5);
			//LCD.drawString(String.valueOf(values[1][1]),0,6);
			//LCD.drawString(String.valueOf(values[1][2]),0,7);
			LCD.drawString(detectColour(values[0]), 0, 4);
			values = measure(leftSampler,rightSampler);
			av = measureAverage(values);
			Delay.msDelay(50);
		}
	}
	public static String detectColour(float[] values) {
		float average = (values[0] + values[1] + values[2])/3.0f;
		if (average > 0.07) {
			return "WHITE";
		}
		if (average + 0.04 < values[0]) {
			return "RED";
		}
		if (average < 0.02) {
			return "BLACK";
		}
		if (values[2] > 1.3*values[1] && values[2] > 1.3*values[0]) {
			return "GRAY";
		}
		return "GREEN";
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
