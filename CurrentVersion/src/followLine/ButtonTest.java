package followLine;
import lejos.hardware.BrickFinder;
import lejos.hardware.Keys;
import lejos.hardware.ev3.EV3;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.SampleProvider;

public class ButtonTest {
	public static void main(String[] args) {
		EV3 ev3Brick = (EV3) BrickFinder.getLocal();
		Keys buttons = ev3Brick.getKeys();
		EV3TouchSensor touch = new EV3TouchSensor(SensorPort.S1);
		float[] result = new float[1];
		SampleProvider sampler = touch.getTouchMode();
		while (buttons.getButtons() != Keys.ID_ESCAPE) {
			sampler.fetchSample(result, 0);
			LCD.drawString(String.valueOf(result[0]), 0, 0);
		}
	}

}
