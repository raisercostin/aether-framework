package com.tesis.aether.tests;

public class Main {
	  public static void main(String[] args) {
//		  JavasistClassLoader ccl = null;
//		  try {
//			  ccl = new JavasistClassLoader(Thread.currentThread().getContextClassLoader());
//		  } catch (Exception e) {
//			  e.printStackTrace();
//		  }
//		  Thread.currentThread().setContextClassLoader(ccl);
//		  System.out.println("Classpath: " + System.getProperty("java.class.path"));
		  Test1 t = new Test1();
		  t.toString();
		  System.out.println(
				  t.getNumero() + 
				  t.nombre("lala") 
				  );
		  t.setString("un string");
		  System.out.println(
				  t.getNumero() + 
				  t.nombre("pepepepepepepe") 
				  );
	  }
}
