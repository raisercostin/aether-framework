package com.tesis.aether.tests;

public class Test1 {
	private String str = "";

	  public String toString () {
		  System.out.println("Clase: Test1");
		  return "Test1";
	  }
	  
	  public int getNumero() {
		  return 1;
	  }
	  
	  public void setString (String str) {
		  str = "pepe1";
	  }
	  
	  public String nombre(String a) {
		  return a + "Test1" + str;
	  }
	  
	  public String aaa() {
		  return "aaa";
	  }
}
