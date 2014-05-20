package plas;


/*
 * Toy program from paper of Newsome et. al: "Measuring
 * channel capacity to distinguish undue influence"
 */

public class SanityCheck {
	
	public int func(int H){
		int base, O;
		base = 0x00001000;
		if (H < 16)
			O = base + H;
		else
			O = base;
		return O;		
	}
	
	public static void main(String[] args) {
		SanityCheck1 o = new SanityCheck1();
		System.out.println("Output is: " + o.func(1));
	}
}
