package org.jets3t.apps.synchronize;

public class testmain {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		String[] args2 = {"UP", "TEST/", "G:\\Downloads\\IRC Downloads\\honey and clover[niizk]"}; 
		Synchronize.main(args2);
		
		String[] args3 = {"DOWN", "TEST/", "D:\\LALALALALA\\"}; 
		Synchronize.main(args3);
	}

}
