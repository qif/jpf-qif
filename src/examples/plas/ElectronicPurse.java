package plas;

/*
 * Toy program from paper of Backes et. al: "Automatic
 * discovery and quantification of information leaks"
 */

public class ElectronicPurse {
	
	public int func(int H){
	
		int O = 0;
		while (H >= 5 && H < 20){
			H = H - 5;
			O = O + 1;
		}
		return O;

	}
	
	public static void main(String[] args) {
		ElectronicPurse o = new ElectronicPurse();
		System.out.println("Output is: " + o.func(1));
	}
}
