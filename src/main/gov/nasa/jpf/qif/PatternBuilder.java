package gov.nasa.jpf.qif;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.ChoiceGenerator;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.bytecode.IRETURN;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.ReturnInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.symbc.SymbolicInstructionFactory;
import gov.nasa.jpf.symbc.bytecode.BytecodeUtils;
import gov.nasa.jpf.symbc.concolic.PCAnalyzer;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.numeric.SymbolicConstraintsGeneral;

import java.io.PrintWriter;
import java.util.Hashtable;

public class PatternBuilder extends ListenerAdapter {

	public static final byte EMPTY = 2;
	public static final byte TAINTED = 3;
	public static byte DATA_SIZE = 32;

	// byte[] pattern;
	int N = 0;
	Hashtable<Integer, Integer> h = new Hashtable<Integer, Integer>();

	public PatternBuilder(Config conf, JPF jpf) {
		jpf.addPublisherExtension(ConsolePublisher.class, this);
		// pattern = new byte[DATA_SIZE];
		// Arrays.fill(pattern, EMPTY);
	}

	public void instructionExecuted(JVM vm) {
		// do nothing if this is a matched state
		if (vm.getSystemState().isIgnored())
			return;

		Instruction insn = vm.getLastInstruction();
		ThreadInfo ti = vm.getLastThreadInfo();
		Config conf = vm.getConfig();

		// do nothing in case of intermediate instructions
		if (insn instanceof InvokeInstruction)
			return;

		// process at the return point of class method
		if (insn instanceof ReturnInstruction) {
			MethodInfo mi = insn.getMethodInfo();
			ClassInfo ci = mi.getClassInfo();

			// do nothing if class info is null
			if (null == ci)
				return;

			String className = ci.getName();
			String methodName = mi.getName();
			int numberOfArgs = mi.getNumberOfArguments();

			// /*
			String symMethod = conf.getProperty("symbolic.method");
			// Extract class name and method from symbolic method property
			int pnt = symMethod.lastIndexOf('.');
			int prt = symMethod.indexOf('(');

			if (!className.equals(symMethod.substring(0, pnt))
					|| !methodName.equals(symMethod.substring(pnt + 1, prt))) {
				// Only process at the returning point of function under test
				return;
			}
			// */

			/*
			 * if (!className.equals("gov.nasa.jpf.qif.drivers.DriverForTest")
			 * || !methodName.equals("main")) { // Only process at the returning
			 * point of function under test return; } //
			 */
			// /*
			// TODO: process return values
			if (((BytecodeUtils
					.isClassSymbolic(conf, className, mi, methodName)) || BytecodeUtils
					.isMethodSymbolic(conf, mi.getFullName(), numberOfArgs,
							null))) {
				ChoiceGenerator<?> cg = vm.getChoiceGenerator();
				if (!(cg instanceof PCChoiceGenerator)) {
					ChoiceGenerator<?> prev_cg = cg
							.getPreviousChoiceGenerator();
					while (!((prev_cg == null) || (prev_cg instanceof PCChoiceGenerator))) {
						prev_cg = prev_cg.getPreviousChoiceGenerator();
					}
					cg = prev_cg;
				}
				if ((cg instanceof PCChoiceGenerator)
						&& ((PCChoiceGenerator) cg).getCurrentPC() != null) {
					PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
					// pc.solve(); //we only solve the pc

					if (SymbolicInstructionFactory.concolicMode) { // TODO:
																	// cleaner
						SymbolicConstraintsGeneral solver = new SymbolicConstraintsGeneral();
						PCAnalyzer pa = new PCAnalyzer();
						pa.solve(pc, solver);
					} else
						pc.solve();

					if (insn instanceof IRETURN) {
						IRETURN ireturn = (IRETURN) insn;
						int returnValue = ireturn.getReturnValue();
						IntegerExpression returnAttr = (IntegerExpression) ireturn
								.getReturnAttr(ti);
						if (returnAttr != null) {
							int ret = returnAttr.solution();
							// updateBitPattern(returnAttr.solution());
							if(!h.contains(ret)){
								h.put(ret, ret);
								N++;
							}
						} else { // concrete
							// updateBitPattern(returnValue);
							if(!h.contains(returnValue)){
								h.put(returnValue, returnValue);
								N++;
							}
						}

						// debug
						// int x = 0;
						//System.out.println(">>> Test Symbolic Integer: "
								//+ Debug.getSymbolicIntegerValue(x));

					} else {
						System.out.println("Return data type not supported");
						return;
					}
				}
			}
			// */
		}
	}

	/*
	private void updateBitPattern(int output) {
		for (int i = 0; i < DATA_SIZE; i++) {
			int bit = (output >> i) & 1;
			if (pattern[i] == TAINTED || pattern[i] == bit)
				continue;
			if (pattern[i] == EMPTY)
				pattern[i] = (byte) bit;
			else
				pattern[i] = TAINTED;
		}
	}
	//*/

	/*
	 * Return one-bit pattern in string
	 */
	/*
	public String getBitPattern() {
		String str = "";

		for (byte bit : pattern)
			if (bit == EMPTY)
				str = '-' + str;
			else if (bit == TAINTED)
				str = '*' + str;
			else
				str = String.valueOf(bit) + str;

		return str;
	}
	//*/

	/*
	 * write a clean pattern, i.e. all tainted bit are cleared, to file
	 */
	/*
	private void writePatternToFile() {
		try {
			FileOutputStream fout = new FileOutputStream("pattern.txt");
			fout.write(pattern);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	//*/

	public void publishFinished(Publisher publisher) {
		publisher
				.publishTopicStart(">>> Quantitative Information Flow Analysis: ");
		PrintWriter pw = publisher.getOut();
		// pw.println("One-bit pattern: " + getBitPattern());
		pw.println("Number of possible outputs: " + String.valueOf(N));
		pw.println("Maximum of leakage: " + String.valueOf(Math.log(N)/Math.log(2)));
		// writePatternToFile();
		publisher.publishTopicEnd("One-bit pattern");
	}
}
