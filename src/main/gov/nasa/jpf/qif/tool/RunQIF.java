package gov.nasa.jpf.qif.tool;

import gov.nasa.jpf.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RunQIF extends gov.nasa.jpf.tool.Run {

	public static final int HELP = 1;
	public static final int SHOW = 2;
	public static final int LOG = 4;

	static final String JPF_CLASSNAME = "gov.nasa.jpf.JPF";

	public static void main(String[] args) {
		try {
			int options = getOptions(args);

			if (args.length == 0 || isOptionEnabled(HELP, options)) {
				showUsage();
				return;
			}

			if (isOptionEnabled(LOG, options)) {
				Config.enableLogging(true);
			}

			Config conf = new Config(args);

			if (isOptionEnabled(SHOW, options)) {
				conf.printEntries();
			}

			// configure SQIF-SE
			configSQIFse(conf);
			// conf.printEntries();

			ClassLoader cl = conf
					.initClassLoader(RunQIF.class.getClassLoader());

			Class<?> jpfCls = cl.loadClass(JPF_CLASSNAME);
			if (!call(jpfCls, "start", new Object[] { conf, args })) {
				error("cannot find 'public static start(Config,String[])' in "
						+ JPF_CLASSNAME);
			}

		} catch (NoClassDefFoundError ncfx) {
			ncfx.printStackTrace();
			// /*
		} catch (ClassNotFoundException cnfx) {
			error("cannot find " + JPF_CLASSNAME);
		} catch (InvocationTargetException ix) {
			// should already be handled by JPF
			ix.getCause().printStackTrace();
			// */
		}
	}

	static String[] concat(String[] A, String[] B) {
		String[] C = new String[A.length + B.length];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 0, C, A.length, B.length);

		return C;
	}

	static void configSQIFse(Config conf) {
		// Get class and method under test
		String target = conf.getProperty("target");
		String symMethod = conf.getProperty("symbolic.method");
		int pnt = symMethod.lastIndexOf('.');
		int prt = symMethod.indexOf('(');
		String method = symMethod.substring(pnt + 1, prt);

		conf.setProperty("target", "gov.nasa.jpf.qif.drivers.DriverForTest");
		conf.setProperty("target_args", target + "," + method);
		conf.setProperty("classpath",
				"${jpf-qif}/build/main;${jpf-qif}/build/examples");
		// conf.setProperty("site", "${jpf-qif}/../site.properties");
		conf.setProperty("symbolic.method",
				"gov.nasa.jpf.qif.drivers.DriverForTest.drive(con#con#sym)");
		conf.setProperty("vm.storage.class", "nil");
		conf.setProperty("search.multiple_errors", "true");
		conf.setProperty("symbolic.dp", "cvc3bitvec");
		conf.setProperty("symbolic.min_int", "0");
	}

	public static int getOptions(String[] args) {
		int mask = 0;

		if (args != null) {

			for (int i = 0; i < args.length; i++) {
				String a = args[i];
				if ("-help".equals(a)) {
					args[i] = null;
					mask |= HELP;

				} else if ("-show".equals(a)) {
					args[i] = null;
					mask |= SHOW;

				} else if ("-log".equals(a)) {
					args[i] = null;
					mask |= LOG;

				}
			}
		}

		return mask;
	}

	public static boolean isOptionEnabled(int option, int mask) {
		return ((mask & option) != 0);
	}

	public static void showUsage() {
		// TODO: show QIF usage
		System.out
				.println("Usage: \"java [<vm-option>..] -jar ...RunJPF.jar [<jpf-option>..] [<app> [<app-arg>..]]");
		System.out
				.println("  <jpf-option> : -help : print usage information and exit");
		System.out
				.println("               | -version : print JPF version information");
		System.out
				.println("               | -log : print configuration initialization steps");
		System.out
				.println("               | -show : print configuration dictionary contents");
		System.out
				.println("               | +<key>=<value>  : add or override key/value pair to config dictionary");
		System.out
				.println("  <app>        : *.jpf application properties file pathname | fully qualified application class name");
		System.out
				.println("  <app-arg>    : arguments passed into main() method of application class");
	}

	static void error(String msg) {
		System.err.print("error: ");
		System.err.println(msg);
		System.exit(1);
	}

	static boolean call(Class<?> cls, String mthName, Object[] args)
			throws InvocationTargetException {
		try {
			Class<?>[] argTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				argTypes[i] = args[i].getClass();
			}

			Method m = cls.getMethod(mthName, argTypes);

			int modifiers = m.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				m.invoke(null, args);
				return true;
			}

		} catch (NoSuchMethodException nsmx) {
			return false;
		} catch (IllegalAccessException iax) {
			return false;
		}

		return false;
	}

}
