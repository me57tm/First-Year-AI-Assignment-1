//MOTOR PORT B IS DEAD
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
//rotation calibrated for full turns
public class FollowLineBasic {
	static EV3LargeRegulatedMotor LEFT_MOTOR = new EV3LargeRegulatedMotor(MotorPort.C);
	static EV3LargeRegulatedMotor RIGHT_MOTOR = new EV3LargeRegulatedMotor(MotorPort.A);
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
	
	public static void main(String[] args) throws Exception {
		pilot.setAngularSpeed(50);
		pilot.setLinearSpeed(10);
		lcSensor.setFloodlight(false);
		rcSensor.setFloodlight(false);
		
		
		final float BLACK = 0.03f;
		final float GREY = 0.05f;
		final float WHITE = 0.1f;
		//pilot.forward();
		float[] averages = new float[1];
		float diff = 0;
		boolean fwd = false;
		
		LCD.drawString("Ready",0,0);
		buttons.waitForAnyPress();
		
		while (buttons.getButtons() != Keys.ID_ESCAPE) {
			
			averages = measureAverage(leftSampler,rightSampler);
			diff = averages[0] - averages[1];
			LCD.clear();
			LCD.drawString(String.valueOf(getTurn(diff)), 0, 0);
			fwd = move(diff,fwd);
			
		}
		System.exit(0);
	}
	
	public static boolean move(float diff, boolean fwd) {
		if (Math.abs(diff) < 0.03 && !fwd) {
			pilot.forward();
			fwd=true;
		}
		else if (Math.abs(diff) < 0.03) {
			//Delay.msDelay(25);
			//do nothing
		}
		else {
			fwd=false;
			pilot.rotate(getTurn(diff));
			Delay.msDelay(50);
		}
		return fwd;
		
	}
	
	public static float[][] measure(SampleProvider left, SampleProvider right) {
		float[] leftReadings = new float[3];
		float[] rightReadings = new float[3];
		left.fetchSample(leftReadings, 0);
		right.fetchSample(rightReadings, 0);
		float[][] ret = new float[2][3];
		ret[0] = leftReadings;
		ret[1] = rightReadings;
		return ret;
	}
	//measure averages with sensors
	public static float[] measureAverage(SampleProvider left, SampleProvider right) { 
		float[][] readings = measure(left, right);
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