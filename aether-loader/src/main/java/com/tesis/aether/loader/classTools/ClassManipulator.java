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
	
	public static void addClassField (boolean _private, String _type, String _fieldName, 
			String _initialization, CtClass _class) throws CannotCompileException {
		String declaration = (_private)?"private ":"public ";
		declaration += _type + " " + _fieldName + _initialization + ";"; 
		CtField f = CtField.make(declaration, _class);
		_class.addField(f);
	}
	
	public static void addCall (String className, String methodName, boolean retType, 
			CtMethod _method) throws CannotCompileException {
		String _call = "{ " + className + " _new_call = new " + className + "(); ";
		if (retType) {
			_call += "return _new_call." + methodName + "($$);}";
		} else {
			_call += "_new_call." + methodName + "($$); return;}";
		}
		_method.insertBefore(_call);
	}
	
	public static void addSpecificCode(boolean _after, CtMethod _method, String _code) throws CannotCompileException {
		if (_after) {
			_method.insertAfter(_code);
		} else {
			_method.insertBefore(_code);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Class addClassCalls(String pckName, String origName, String nameClassTo) throws CannotCompileException, NotFoundException {
		ClassPool pool = ClassPool.getDefault();
		if (!"".equals(pckName)) {
			pool.importPackage(pckName);
		}
		CtClass cc = pool.get(origName);
		CtMethod[] methods = cc.getDeclaredMethods();
		int i = 0;
		while (i < methods.length) {
			CtMethod method = methods[i];
			try {
				ClassManipulator.addCall(nameClassTo, method.getName(), !method.getReturnType().getName().equals("void"), method);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("No se pudo agregar código en el método: " + method.getName(), e);
			}
			i++;
		}
		return cc.toClass();
	}
}
