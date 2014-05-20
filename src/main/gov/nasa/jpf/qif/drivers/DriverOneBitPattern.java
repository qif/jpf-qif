package gov.nasa.jpf.qif.drivers;

import gov.nasa.jpf.jvm.Verify;

/**
 * Driver to detect one-bit patterns
 */
public class DriverOneBitPattern extends Driver {

	public static void main(String[] args) {

		if (args.length < 4) {
			System.out.println("Bit-index and test value required");
			return;
		}

		DriverOneBitPattern d1 = new DriverOneBitPattern();
		
		String target = args[0];
		String method = args[1];
		int output = 0;
		
		int H = Verify.getInt(0, 255);

		output = d1.getOutput(target,method,H);

		// Discover one-bit patterns
		int mask = 1;
		int oneBitIndex, test;
		oneBitIndex = Integer.parseInt(args[2]);
		test = Integer.parseInt(args[3]);

		// one-bit patterns
		assert ((output >> oneBitIndex) & mask) == test;
	}

}
