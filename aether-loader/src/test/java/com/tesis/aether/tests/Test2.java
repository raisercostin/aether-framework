package com.tesis.aether.tests;

public class Test2 {
	private String str = "";
	  public String toString () {
		  System.out.println("Clase: Test2");
		  return "Test2";
	  }
	  
	  public int getNumero() {
		  return 2;
	  }
	  
	  public void setString (String str) {
		  str = "pepe2";
	  }
	  
	  public String nombre(String a) {
		  return a + "Test2" + str;
	  }
}
