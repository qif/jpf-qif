package gov.nasa.jpf.qif.drivers;

import gov.nasa.jpf.jvm.Verify;
import plas.SanityCheck;

/**
 * Driver for ACSAC10 paper approach
 */
public class DriverNoutputs extends Driver {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Number of outputs required");
			return;
		}

		DriverNoutputs dN = new DriverNoutputs();
		String target = args[0];
		String method = args[1];

		int N = Integer.parseInt(args[0]);
		int[] out = new int[N];
		boolean assume = true;
		boolean found = false;

		int H;

		for (int k = 0; k < N - 1; k++) {
			H = Verify.getInt(0, 255);
			out[k] = dN.getOutput(target, method, H);
			if (k > 0) {
				int index = k;
				for (index = k - 1; index >= 0; index--) {
					// replace this part if JPF supports assume-guarantee model
					// checking
					assume = assume && (out[k] != out[index]);
				}
			}
		}

		H = Verify.getInt(0, 255);
		out[N - 1] = dN.getOutput(target,method,H);

		for (int k = 0; k < N - 1; k++) {
			if (out[N - 1] == out[k]) {
				found = true;
				break;
			}
		}

		if (assume)
			assert found == true;
	}

}
