package followLine;

import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.robotics.SampleProvider;
import lejos.robotics.chassis.Chassis;
import lejos.robotics.chassis.Wheel;
import lejos.robotics.chassis.WheeledChassis;
import lejos.robotics.navigation.MovePilot;
import lejos.utility.Delay;

public class Final {
	static EV3LargeRegulatedMotor LEFT_MOTOR = new EV3LargeRegulatedMotor(MotorPort.C);
	static EV3LargeRegulatedMotor RIGHT_MOTOR = new EV3LargeRegulatedMotor(MotorPort.A);
	static EV3MediumRegulatedMotor ROTATION_MOTOR = new EV3MediumRegulatedMotor(MotorPort.D);
	static EV3 ev3Brick = (EV3) BrickFinder.getLocal();
	static Keys buttons = ev3Brick.getKeys();
	static EV3ColorSensor lcSensor = new EV3ColorSensor(SensorPort.S4);
	static EV3ColorSensor rcSensor = new EV3ColorSensor(SensorPort.S3);
	
	//wheelbase 10.5cm
	//wheel diameter 5.2cm
	
	static Wheel wheel1 = WheeledChassis.modelWheel(LEFT_MOTOR,5.5).offset(-5.2);
	static Wheel wheel2 = WheeledChassis.modelWheel(RIGHT_MOTOR,5.5).offset(5.2);
	static Chassis chassis = new WheeledChassis(new Wheel[] {wheel1,wheel2},WheeledChassis.TYPE_DIFFERENTIAL);
	static MovePilot pilot = new MovePilot(chassis);
	
	
	static SampleProvider leftSampler = lcSensor.getRGBMode();
	static SampleProvider rightSampler = rcSensor.getRGBMode();
	
	static EV3IRSensor IRSensor = new EV3IRSensor(SensorPort.S2);
	static float[] IR = new float[1];
	static SampleProvider IRSampler = IRSensor.getDistanceMode();
	
	public static void main(String[] args) throws Exception {
		pilot.setAngularSpeed(50);
		pilot.setLinearSpeed(10);
		float[][] RGBValues = measure();
		float[] averages = new float[2];
		float[] IRValue = new float[1];
		float IRDistance = 0;
		boolean fwd = false;
		
		LCD.drawString("Ready",0,0);
		buttons.waitForAnyPress();
		while (buttons.getButtons() != Keys.ID_ESCAPE) {
			
			RGBValues = measure();
			averages = measureAverage(RGBValues);
			IRSampler.fetchSample(IRValue, 0);
			IRDistance = IRValue[0];
			
			if (IRDistance < 6) {
				LCD.drawString("Avoid",0,0);
				fwd = avoidMode();
			}
			else if (RGBValues[0][0] > 1.3*averages[0] || RGBValues[0][0] < 0.85*averages[0] || RGBValues[1][0] > 1.3*averages[1] || RGBValues[1][0] < 0.85*averages[1]) {
				LCD.drawString("colourful",0,0);
				fwd = colourfulMode(fwd);
			}
			else {
				LCD.drawString("colour blind",0,0);
				fwd = colourBlindMode(fwd);
			}
			Delay.msDelay(100);
			LCD.clear();
		}
	}
	
	public static boolean colourBlindMode(boolean fwd) {
		float[][] values = measure();
		float[] averages = new float[2];
		float diff = 0;
		//LCD.clear();
		//LCD.drawString(String.valueOf(getTurn(diff)), 0, 0);
		values = measure();
		averages = measureAverage(values);
		diff = averages[0] - averages[1];
		if (Math.abs(diff) < 0.03 && !fwd) {
			pilot.forward();
			fwd=true;
		}
		else if (Math.abs(diff) < 0.03) {
			//	Delay.msDelay(25);
			//do nothing
		}
		else {
			fwd=false;
			pilot.rotate(getTurn(diff));
			Delay.msDelay(50);
		}
		return fwd;
	}
	
	public static boolean avoidMode() {
		//pilot.travel(-9);
		pilot.rotate(-85);
		ROTATION_MOTOR.rotateTo(-90);
		float[] averages = new float[2];
		float[] IRValue = new float[1];
		float IRDistance = 0;
		
		pilot.forward();
		do{
			IRSampler.fetchSample(IRValue, 0);
			IRDistance = IRValue[0];
		} while (IRDistance < 20);
		pilot.travel(20);
		pilot.rotate(90);
		
		pilot.travel(20);
		pilot.forward();
		do{
			IRSampler.fetchSample(IRValue, 0);
			IRDistance = IRValue[0];
		} while (IRDistance < 20);
		
		pilot.travel(30);
		pilot.rotate(90);
		//ROTATION_MOTOR.rotateTo(0);
		pilot.forward();
		do {
			averages = measureAverage();
		}while (averages[0] > 0.05 && averages[1] > 0.05);
		pilot.rotate(-90);
		
		return false;
	}
	
	public static boolean colourfulMode(boolean fwd) {
		//pilot.travel(1);
		float[][] RGBValues = measure();
		//float[] averages = measureAverage(RGBValues);
		String[] colours = detectColour(RGBValues);
		LCD.clear();
		LCD.drawString("Left: "+colours[0],0,0);
		LCD.drawString("Right: "+colours[1],0,1);
		buttons.waitForAnyPress();
		if (colours[0] == "green" && colours[1] == "green") {
			turnExecuter(leftSampler,180);
		}
		else if (colours[0] == "green") {
			turnExecuter(leftSampler,-90);
		}
		else if (colours[1] == "green") {
			turnExecuter(rightSampler,90);
		}
		if (colours[0] == "red" && colours[1] == "red") {
			System.exit(1);
		}
		if (fwd) {
			return true;
		}
		else {
			pilot.forward();
			return true;
		}
	}
	
	public static void turnExecuter(SampleProvider sensor,int angle) {
		float[] rgb = new float[3];
		sensor.fetchSample(rgb, 0);
		String colour = detectColour(rgb);
		pilot.forward();
		while (colour != "black" && colour != "white") {
			sensor.fetchSample(rgb, 0);
			colour = detectColour(rgb);
		}
		
		if (colour == "black") {
			pilot.rotate(angle);
		}
	}
	
	
	
	public static String detectColour(float[] rgb) {
		float average = (rgb[0] + rgb[1] + rgb[2]) / 3.0f;
		if (rgb[0] > 1.3*average) {
			return "red";
		}
		if (rgb[0] < 0.7*average) {
			return "green";
		}
		if (average < 0.03) {
			return "black";
		}
		return "white";
	}
	public static String[] detectColour(float[][] rgb) {
		String[] out = new String[2];
		out[0] = detectColour(rgb[0]);
		out[1] = detectColour(rgb[1]);
		return out;
	}
	
	
	public static float[][] measure() {
		float[] leftReadings = new float[3];
		float[] rightReadings = new float[3];
		leftSampler.fetchSample(leftReadings, 0);
		rightSampler.fetchSample(rightReadings, 0);
		float[][] ret = new float[2][3];
		ret[0] = leftReadings;
		ret[1] = rightReadings;
		return ret;
	}
	//measure averages with sensors
	public static float[] measureAverage() { 
		float[][] readings = measure();
		float leftAverage = (readings[0][0] + readings[0][1] + readings[0][2]) / 3.0f;
		float rightAverage = (readings[1][0] + readings[1][1] + readings[1][2]) / 3.0f;
		return new float[] {leftAverage,rightAverage};
		
	}
	//measure averages from existing readings
	public static float[] measureAverage(float[][] readings) {
		float leftAverage = (readings[0][0] + readings[0][1] + readings[0][2]) / 3.0f;
		float rightAverage = (readings[1][0] + readings[1][1] + readings[1][2]) / 3.0f;
		return new float[] {leftAverage,rightAverage};
	}
	
	public static double getTurn(float val) {
		return (double) val*100;
	}
	
}
