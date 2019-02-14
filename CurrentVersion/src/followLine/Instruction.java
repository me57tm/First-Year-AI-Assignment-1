package followLine;

public class Instruction {
	String message = new String();
	Instruction(){
		message = "NOTHING";
	}
	Instruction(String m){
		message = m;
	}
	public void set(String m) {
		message = m;
	}
	public void run() {
		
	}
}
