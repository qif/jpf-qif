package gov.nasa.jpf.qif.drivers;

import java.util.BitSet;

public class DriverForTest extends Driver{

	public static int DATA_SIZE = 32;

	/*
	 * drive the test
	 */
	public int drive(String target, String method, int H) {

		int output = getOutput(target,method,H);
		
		BitSet bs = new BitSet(DATA_SIZE);
		for (byte i = 0; i < DATA_SIZE; i++) {
			byte bit = (byte) ((output >> i) & 1);
			if (bit == 0)
				bs.set(i, false);
			else
				bs.set(i, true);
		}

		return toInt(bs);

	}

	/*
	 * convert integer to bit-set
	 */
	static BitSet toBitSet(int i) {
		BitSet bs = new BitSet(Integer.SIZE);
		for (int k = 0; k < Integer.SIZE; k++) {
			if ((i & (1 << k)) != 0) {
				bs.set(k);
			}
		}
		return bs;
	}

	/*
	 * convert integer to bit-set
	 */
	static int toInt(BitSet bs) {
		int i = 0;
		for (int pos = -1; (pos = bs.nextSetBit(pos + 1)) != -1;) {
			i |= (1 << pos);
		}
		return i;
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Require target and its arguments");
			return;
		}

		String target = args[0];
		String method = args[1];

		DriverForTest driver = new DriverForTest();
		System.out.println("Output is: " + driver.drive(target, method, 1));
	}
}
