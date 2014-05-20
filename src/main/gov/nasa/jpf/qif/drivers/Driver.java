package gov.nasa.jpf.qif.drivers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/*
 * Interface for all drivers
 */
public class Driver {
	
	protected int getOutput(String target, String method, int H){
		int output = 0;
		try {
			Class<?> c = Class.forName(target);
			Constructor<?> ct = c.getConstructor();
			Object object = ct.newInstance();
			Method meth = c.getMethod(method,int.class);
			Object retobj = meth.invoke(object, H);
			output = (Integer) retobj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
}
