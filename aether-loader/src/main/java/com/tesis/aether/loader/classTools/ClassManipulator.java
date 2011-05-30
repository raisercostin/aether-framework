package com.tesis.aether.loader.classTools;

import org.apache.log4j.Logger;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class ClassManipulator {
	private static Logger logger = Logger.getLogger("default");
	
	/**
	 * Retorna el nombre de la clase pasada como parametro,
	 * quitandole el nombre de paquete
	 * @param srcClass
	 * @return
	 */
	private static String getClassName(String srcClass) {
		String className = "";
		if (srcClass != null) {
			String[] names = srcClass.split("\\.");
			if (names.length == 1) {
				return names[0];
			}
			className = names[names.length - 1];
		}
		return className;
	}
	
	/**
	 * Retorna el nombre del paquete de una clase completa pasada
	 * como parametro
	 * @param srcClass
	 * @return
	 */
	private static String getPackageName(String srcClass) {
		String pckName = "";
		if (srcClass != null) {
			String[] names = srcClass.split("\\.");
			if (names.length == 1) {
				return "";
			}
			pckName = names[0];
			for (int i = 1; i < names.length - 1; i++) {
				pckName += "." + names[i];
			}
		}
		return pckName;
	}

	public static void addClassField (boolean _private, String _type, String _fieldName, 
			String _initialization, CtClass _class) throws CannotCompileException {
		String declaration = (_private)?"private ":"public ";
		declaration += _type + " " + _fieldName + _initialization + ";"; 
		CtField f = CtField.make(declaration, _class);
		_class.addField(f);
	}
	
	private static String getGeneratedFieldName(String name) {
		return "_aether_" + name + "_fld";
	}
	
	public static void addCall (String className, String methodName, boolean retType, 
			CtMethod _method, boolean useField) throws CannotCompileException {
		String _accessName = "";
		if (useField) {
			_accessName = getGeneratedFieldName(getClassName(className)); 
		} else {
			_accessName = className;
		}
		String _call = "{ ";
		if (useField) {
			// si el atributo es nulo debemos instanciarlo
			_call += "if (" + _accessName + " == null) " +
					_accessName + " = new " + className + "(); ";
		} 
		if (retType) {
			_call += "return " + _accessName + "." + methodName + "($$);}";
		} else {
			_call += _accessName + "." + methodName + "($$); return;}";
		}
		_method.setBody(_call);
	}
	
	public static void addSpecificCode(boolean _after, CtMethod _method, String _code) throws CannotCompileException {
		if (_after) {
			_method.insertAfter(_code);
		} else {
			_method.insertBefore(_code);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Class addClassCalls(String origName, String nameClassTo,
			boolean useField) throws CannotCompileException, NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		String pckName = getPackageName(origName);
		if (!"".equals(pckName)) {
			pool.importPackage(pckName);
		}
		CtClass cc = pool.get(origName);
		if (useField) {
			addClassField(true, nameClassTo, getGeneratedFieldName(getClassName(nameClassTo)), "", cc);
		}
		CtMethod[] methods = cc.getDeclaredMethods();
		int i = 0;
		while (i < methods.length) {
			CtMethod method = methods[i];
			try {
				ClassManipulator.addCall(nameClassTo, method.getName(), !method.getReturnType().getName().equals("void"), 
						method, useField);
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("No se pudo agregar la llamada en el metodo: '" + method.getName() + 
						"' posiblemente no exista en la clase destino: '" + nameClassTo + "'");
				logger.error("No se pudo agregar código en el método: " + method.getName(), e);
			}
			i++;
		}
		return cc.toClass();
	}
}
