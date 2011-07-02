package org.jets3t.apps.synchronize;

public class TestUpDown {

	public static void main(String[] args) throws Exception {
		String[] args2 = {"UP", "TEST/", "D:\\LALALALALA"}; 
		Synchronize.main(args2);
		
		String[] args3 = {"DOWN", "TEST/", "D:\\LALALALALA\\"}; 
		Synchronize.main(args3);

		assert true;
	}

}
