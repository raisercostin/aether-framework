package com.tesis.aether.tests;

import com.tesis.aether.loader.core.CompilingClassLoader;

public class Main {
	  public static void main(String[] args) {
		  CompilingClassLoader ccl = null;
		  try {
			  ccl = new CompilingClassLoader(Thread.currentThread().getContextClassLoader());
		  } catch (Exception e) {
			  e.printStackTrace();
		  }
		  Thread.currentThread().setContextClassLoader(ccl);
		  System.out.println("Classpath: " + System.getProperty("java.class.path"));
		  Test1 t = new Test1();
		  t.toString();
	  }
}
