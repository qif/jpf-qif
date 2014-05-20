package gov.nasa.jpf.qif.drivers;

import gov.nasa.jpf.jvm.Verify;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DriverBitVector extends Driver {

	public static int DATA_SIZE = 32;
	public static final byte TAINTED = 3;

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Require target and its arguments");
			return;
		}

		DriverBitVector dbv = new DriverBitVector();
		
		String target = args[0];
		String method = args[1];
		int output = 0;

		try {
			byte[] pattern = new byte[DATA_SIZE];
			byte[] bitvector = new byte[DATA_SIZE];
			List<Byte> tainted = new ArrayList<Byte>();
			FileInputStream fin = new FileInputStream("pattern.txt");
			fin.read(pattern);
			fin.close();
			// clean pattern, clear all tainted bits to zero
			for (byte i = 0; i < pattern.length; i++)
				if (pattern[i] == TAINTED) {
					pattern[i] = 0; // clear tainted bit
					tainted.add(i);
				}

			int H = Verify.getInt(0, 255);

			output = dbv.getOutput(target, method, H);

			for (byte i = 0; i < DATA_SIZE; i++) {
				if (tainted.contains(i))
					bitvector[i] = 0;
				else {
					int bit = (output >> i) & 1;
					bitvector[i] = (byte) bit;
				}

			}
			// assert there are no other tainted bits in the output
			assert Arrays.equals(pattern, bitvector);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
