package gov.nasa.jpf.qif;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.listener.AssertionProperty;
import gov.nasa.jpf.qif.drivers.DriverTwoBitPattern;

import java.util.ArrayList;
import java.util.List;

/*
 * Quantitative Information Flow Analyzer
 */
public class Analyzer {

	public static final int OUTPUT_SIZE = 32;
	public static final int BIT_ZERO = 0;
	public static final int BIT_ONE = 1;
	public static final int TAINTED_BIT = 2;

	String target;
	String method;
	int[] outputBitArray = new int[OUTPUT_SIZE];
	List<Integer> taintedBits = new ArrayList<Integer>();
	List<String> bitPatterns = new ArrayList<String>();
	int numFree = 0;
	
	public Analyzer(String target, String method){
		this.target = target;
		this.method = method;
	}
	
	/*
	 * Debug: print the bit array of output
	 */
	void printBitArray() {
		String bit = "", bitArray = "";
		for (int i = 0; i < OUTPUT_SIZE; i++) {
			switch (outputBitArray[i]) {
			case BIT_ZERO:
				bit = "0";
				break;
			case BIT_ONE:
				bit = "1";
				break;
			case TAINTED_BIT:
				bit = "*";
				break;
			}
			bitArray = bit + bitArray;
		}
		System.out.println(bitArray);
	}

	boolean verifyAssertion(String target, String target_args) {
		// call Java PathFinder to verify bit value
		target_args = this.target + "," + method + "," + target_args;
		String[] args = new String[0];
		Config conf = JPF.createConfig(args);
		conf.setProperty("target", target);
		conf.setProperty("classpath",
				"${jpf-qif}/build/main;${jpf-qif}/build/examples");
		conf.setProperty("target_args", target_args);
			
		AssertionProperty ap = new AssertionProperty(conf);
		JPF jpf = new JPF(conf);
		jpf.addPropertyListener(ap);

		boolean violate = true;
		try {
			jpf.run();
			violate = jpf.foundErrors();
		} catch (JPFConfigException cx) {
			System.out.println("JPFConfigException: ");
			cx.printStackTrace();
		} catch (JPFException jx) {
			System.out.println("JPFException: ");
			jx.printStackTrace();
		}
		return !violate;
	}

	/*
	 * convert bit patterns to CNF formula in DIMACS format
	 */
	public String toDIMACS() {
		// nothing to convert
		if (bitPatterns.isEmpty() || bitPatterns.size() == numFree)
			return null;

		// convert
		String dimacs = "c 2-bit pattern in DIMACS format\n";
		dimacs = dimacs.concat("p cnf " + taintedBits.size() + " "
				+ (bitPatterns.size() - numFree) + "\n");
		for (String pattern : bitPatterns) {
			// parse bit pattern
			int id1 = pattern.indexOf('(');
			int id2 = pattern.indexOf(',');
			int id3 = pattern.indexOf(')');
			String rel = pattern.substring(0, id1);
			int firstBit = Integer.parseInt(pattern.substring(id1 + 1, id2));
			int secondBit = Integer.parseInt(pattern.substring(id2 + 1, id3));
			int firstBitIndex = taintedBits.indexOf(firstBit);
			int secondBitIndex = taintedBits.indexOf(secondBit);

			// Neq(i,j) = (i + j)(-i + -j)
			if (rel.equals("Neq")) {
				dimacs += firstBitIndex + " " + secondBitIndex + " 0\n";
				dimacs += "-" + firstBitIndex + " -" + secondBitIndex + " 0\n";
				continue;
			}

			// Nand(i,j) = (-i + -j)
			if (rel.equals("Nand")) {
				dimacs += "-" + firstBitIndex + " -" + secondBitIndex + " 0\n";
				continue;
			}

			// Eq(i,j) = (i + -j)(-i + j)
			if (rel.equals("Eq")) {
				dimacs += firstBitIndex + " -" + secondBitIndex + " 0\n";
				dimacs += "-" + firstBitIndex + " " + secondBitIndex + " 0\n";
				continue;
			}

			// Geq(i,j) = (i + -j)
			if (rel.equals("Geq")) {
				dimacs += firstBitIndex + " -" + secondBitIndex + " 0\n";
				continue;
			}

			// Leq(i,j) = (-i + j)
			if (rel.equals("Leq")) {
				dimacs += "-" + firstBitIndex + " " + secondBitIndex + " 0\n";
				continue;
			}

			// Or(i,j) = (i + j)
			if (rel.equals("Or")) {
				dimacs += firstBitIndex + " " + secondBitIndex + " 0\n";
				continue;
			}

		}
		return dimacs;
	}

	/*
	 * test if bit at "index" is equal to "value"
	 */
	boolean testBit(int index, int value) {
		// call Java PathFinder to verify bit value
		String target_args = index + "," + value;
		return verifyAssertion("gov.nasa.jpf.qif.drivers.DriverOneBitPattern",
				target_args);
	}

	/*
	 * test if two bits at "index1" and "index2" have the "pattern"
	 */
	boolean testTwoBits(int index1, int index2, int pattern) {
		// call Java PathFinder to verify bit value
		String target_args = index1 + "," + index2 + "," + pattern;
		return verifyAssertion("gov.nasa.jpf.qif.drivers.DriverTwoBitPattern",
				target_args);
	}

	/*
	 * detect bit patterns between two bits at "index1" and "index2"
	 */
	void detectTwoBitPatterns(int index1, int index2) {
		if (testTwoBits(index1, index2,
				DriverTwoBitPattern.LEFT_EQUAL_TO_ZERO_OR_RIGHT_EQUAL_TO_ZERO)) {
			if (testTwoBits(index1, index2,
					DriverTwoBitPattern.LEFT_EQUAL_TO_ONE_OR_RIGHT_EQUAL_TO_ONE)) {
				bitPatterns.add("Neq(" + index1 + "," + index2 + ")");
			} else {
				bitPatterns.add("Nand(" + index1 + "," + index2 + ")");
			}
		} else {
			if (testTwoBits(index1, index2,
					DriverTwoBitPattern.LEFT_GREATER_THAN_OR_EQUAL_TO_RIGHT)) {
				if (testTwoBits(index1, index2,
						DriverTwoBitPattern.LEFT_LESS_THAN_OR_EQUAL_TO_RIGHT)) {
					bitPatterns.add("Eq(" + index1 + "," + index2 + ")");
				} else {
					bitPatterns.add("Geq(" + index1 + "," + index2 + ")");
				}
			} else {
				if (testTwoBits(index1, index2,
						DriverTwoBitPattern.LEFT_LESS_THAN_OR_EQUAL_TO_RIGHT)) {
					bitPatterns.add("Leq(" + index1 + "," + index2 + ")");
				} else {
					if (testTwoBits(
							index1,
							index2,
							DriverTwoBitPattern.LEFT_EQUAL_TO_ONE_OR_RIGHT_EQUAL_TO_ONE)) {
						bitPatterns.add("Or(" + index1 + "," + index2 + ")");
					} else {
						bitPatterns.add("Free(" + index1 + "," + index2 + ")");
						numFree++;
					}
				}
			}
		}
	}

	/*
	 * detect bit patterns in each pair of tainted bits
	 */
	void detectTwoBitPatterns() {
		for (Integer i : taintedBits) {
			for (Integer j : taintedBits) {
				if (i < j)
					detectTwoBitPatterns(i, j);
			}
		}
	}

	/*
	 * Detect bits that are tainted by high data
	 */
	void detectTaintedBits() {
		// testing all bits of output
		for (int i = 0; i < OUTPUT_SIZE; i++) {
			if (testBit(i, BIT_ZERO)) {
				outputBitArray[i] = BIT_ZERO;
			} else if (testBit(i, BIT_ONE)) {
				outputBitArray[i] = BIT_ONE;
			} else {
				outputBitArray[i] = TAINTED_BIT;
				taintedBits.add(new Integer(i));
			}
		}
	}

	/*
	 * PLAS11 paper approach
	 */
	public void quantifyUsingTwoBitPatterns() {
		long startTime1 = System.currentTimeMillis();

		detectTaintedBits();

		long endTime1 = System.currentTimeMillis();

		if (taintedBits.isEmpty()) {
			System.out.println("Program is secure");
			return;
		}

		long startTime2 = System.currentTimeMillis();

		detectTwoBitPatterns();

		long endTime2 = System.currentTimeMillis();
		System.out.println("\nTotal elapsed time to detect tainted bits is "
				+ (endTime1 - startTime1) / 100 + " seconds");
		System.out.println("One-bit patterns: ");
		printBitArray();
		System.out
				.println("\nTotal elapsed time to detect two-bit patterns is "
						+ (endTime2 - startTime2) / 100 + " seconds");
		System.out.println("Two-bit patterns: ");
		for (String pattern : bitPatterns)
			System.out.println(pattern);

		if (bitPatterns.size() == numFree)
			System.out.println("\nProgram leaks at most " + taintedBits.size()
					+ " bits");
		else{
			// TODO: write to file
			System.out.println("\nPlease use RelSat to count the number of solutions for the following formula:\n");
			System.out.println(toDIMACS());
		}
	}

	/*
	 * ACSAC10 paper approach
	 */
	public void leakMoreThan(int k) {
		int N = (int) Math.pow(2, k);
		String target_args = Integer.toString(N);
		boolean result = verifyAssertion(
				"gov.nasa.jpf.qif.drivers.DriverNoutputs", target_args);

		if (result)
			System.out
					.println("Program does not leak more than " + k + " bits");
		else
			System.out.println("Program leaks at least " + k + " bits");
	}
	
	/*
	 * Check soundness of one-bit pattern
	 */
	boolean isSound() {
		String[] args = new String[0];
		Config conf = JPF.createConfig(args);
		conf.setProperty("target",
			"gov.nasa.jpf.qif.drivers.DriverBitVector");
		conf.setProperty("classpath",
				"${jpf-qif}/bin/main;${jpf-qif}/bin/examples");
		conf.setProperty("site", "${jpf-qif}../site.properties");
		conf.setProperty("vm.storage.class", "nil");
		conf.setProperty("search.multiple_errors", "true");
		// conf.setProperty("symbolic.debug", "true");
		JPF jpf = new JPF(conf);

		boolean violate = true;
		try {
			jpf.run();
			violate = jpf.foundErrors();
		} catch (JPFConfigException cx) {
			System.out.println("JPFConfigException: ");
			cx.printStackTrace();
		} catch (JPFException jx) {
			System.out.println("JPFException: ");
			jx.printStackTrace();
		}
		
		return !violate;
	}	

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Require target and its arguments");
			return;
		}
		
		Analyzer qif = new Analyzer(args[0],args[1]);
		
		// PLAS 2011 approach
		qif.quantifyUsingTwoBitPatterns();
		
		// ACSAC 2010 approach
		// qif.leakMoreThan(3);
	}

}
