package com.tesis.aether.tests;

import com.tesis.aether.loader.core.JavasistClassLoader;

public class LoadTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JavasistClassLoader jacl = new JavasistClassLoader(ClassLoader.getSystemClassLoader());
		try {
			jacl.loadClass("Test2.class");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println("********TEST TERMINADO....");
		
	}

}
