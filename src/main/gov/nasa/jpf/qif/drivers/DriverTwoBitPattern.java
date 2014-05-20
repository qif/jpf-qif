package gov.nasa.jpf.qif.drivers;

import gov.nasa.jpf.jvm.Verify;
import plas.SanityCheck;

public class DriverTwoBitPattern extends Driver {

	public static final int LEFT_EQUAL_TO_ZERO_OR_RIGHT_EQUAL_TO_ZERO = 0;
	public static final int LEFT_EQUAL_TO_ONE_OR_RIGHT_EQUAL_TO_ONE = 1;
	public static final int LEFT_GREATER_THAN_OR_EQUAL_TO_RIGHT = 2;
	public static final int LEFT_LESS_THAN_OR_EQUAL_TO_RIGHT = 3;

	/**
	 * Patterns between two bits
	 */
	static boolean pattern(int left, int right, int pattern) {
		switch (pattern) {
		case LEFT_EQUAL_TO_ZERO_OR_RIGHT_EQUAL_TO_ZERO:
			return left == 0 || right == 0;
		case LEFT_EQUAL_TO_ONE_OR_RIGHT_EQUAL_TO_ONE:
			return left == 1 || right == 1;
		case LEFT_GREATER_THAN_OR_EQUAL_TO_RIGHT:
			return left >= right;
		case LEFT_LESS_THAN_OR_EQUAL_TO_RIGHT:
			return left <= right;
		default:
			System.out.println("Error: Unknown pattern");
			return false;
		}
	}

	/**
	 * Driver to detect two-bit patterns
	 */
	public static void main(String[] args) {

		if (args.length < 5) {
			System.out.println("Bit indices and pattern required");
			return;
		}

		int H = Verify.getInt(0, 255);
		int output = 0;
		
		DriverTwoBitPattern d2 = new DriverTwoBitPattern();
		
		String target = args[0];
		String method = args[1];
		output = d2.getOutput(target, method,H);

		// Discover bit patterns
		int mask = 1;
		int twoBitIndex1, twoBitIndex2, firstBit, secondBit, pattern;

		twoBitIndex1 = Integer.parseInt(args[2]);
		twoBitIndex2 = Integer.parseInt(args[3]);
		pattern = Integer.parseInt(args[4]);

		firstBit = (output >> twoBitIndex1) & mask;
		secondBit = (output >> twoBitIndex2) & mask;

		// two-bit patterns
		assert pattern(firstBit, secondBit, pattern);
	}

}
