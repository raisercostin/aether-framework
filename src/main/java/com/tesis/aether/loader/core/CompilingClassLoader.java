package com.tesis.aether.loader.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.xml.sax.SAXException;

import com.tesis.aether.loader.conf.ConfigClassLoader;
/********************************************************************************
 * Para que el cargador de clases funcione en la aplicacion                     *
 * se debe agregar com parametro de la vm la siguiente linea:                   *
 *                                                                              *
 * -Djava.system.class.loader=com.tesis.aether.loader.core.CompilingClassLoader *
 ********************************************************************************/


/**
 * Compila las clases en caso de haber sufrido modificaciones
 * o estar desactualizadas y las carga para ejecucion
 */
public class CompilingClassLoader extends ClassLoader {
	/**
	 * Logger utilizado por la clase
	 */
	private static Logger logger = null;
	
	/**
	 * Contiene el mapeo de clases a ser reemplazadas en la carga.
	 * Las clases se deben especificar con incluyendo el paquete,
	 * por ejemplo: "java.util.HashMap"
	 * <Clase a reemplazar, Clase reemplazante>
	 */
	private HashMap<String, String> classExceptions = new HashMap<String, String>();
	
	/**
	 * Contiene el mapeo de paquetes a ser reemplazados en la carga.
	 * <Paquete a reemplazar, Paquete reemplazante>
	 */
	private HashMap<String, String> packageExceptions = new HashMap<String, String>();
	
	/**
	 * Indica si la configuración fue cargada o no.
	 */
	private boolean loadConfigurations = true;

	/**
	 * Constructor de la clase.
	 * @param parent classloader padre
	 */
	public CompilingClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Carga los mapeos de lcases y paquetes
	 * @param fromPath path del archivo de configuracion
	 * @throws SAXException excepcion al parsear el archivo xml
	 * @throws IOException excepcion al intentar obtener acceso al archivo
	 * @throws ParserConfigurationException excepcion al parsear el archivo xml
	 */
	private void loadClassMapper(String fromPath) throws SAXException,
			IOException, ParserConfigurationException {
		ConfigClassLoader conf = new ConfigClassLoader(fromPath);
		classExceptions = conf.getClassExceptions();
	//	packageExceptions = conf.getPackageExceptions();
	}

	/**
	 * Agrega una clase para ser reemplazada en la carga
	 * @param classSrc clase original
	 * @param classDst clase a la cual se debe mapear
	 * @return true si se agrego la excepcion, false si ya estaba agregada
	 */
	public boolean addClassException(String classSrc, String classDst) {
		if (classExceptions.containsKey(classSrc)) {
			return false;
		}
		classExceptions.put(classSrc, classDst);
		return true;
	}

	/**
	 * Agrega un paquete a ser reemplazado en la carga
	 * @param pckSrc paquete original
	 * @param pckDst paquete destino al cual se debe mapear
	 * @return true si se agrego el paquete, false si ya se encontraba agregado
	 */
	public boolean addPackageException(String pckSrc, String pckDst) {
		if (packageExceptions.containsKey(pckSrc)) {
			return false;
		}
		packageExceptions.put(pckSrc, pckDst);
		return true;
	}

	/**
	 * Remueve una clase de la lista de reemplazos
	 * @param classException nombre original de la clase a eliminar del mapeo
	 * @return true si se elimino, false si la clase no se encontraba mapeada
	 */
	public boolean removeClassException(String classException) {
		if (classExceptions.containsKey(classException)) {
			classExceptions.remove(classException);
			return true;
		}
		return false;
	}

	/**
	 * Remueve un paquete de la lista de reemplazos
	 * @param packageException nombre del paquete original a eliminar del mapeo
	 * @return true si se elimino, false si el paquete no se encontraba mapeado
	 */
	public boolean removePackageException(String packageException) {
		if (packageExceptions.containsKey(packageException)) {
			packageExceptions.remove(packageException);
			return true;
		}
		return false;
	}

	/**
	 * Retorna el nombre del paquete de una clase completa pasada
	 * como parametro
	 * @param srcClass nombre completo de la clase
	 * @return nombre del paquete de la clase
	 */
	private String getPackageName(String srcClass) {
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

	/**
	 * Retorna el nombre de la clase pasada como parametro,
	 * quitandole el nombre de paquete
	 * @param srcClass nombre completo de la clase
	 * @return nombre de la clase
	 */
	private String getClassName(String srcClass) {
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
	 * Retorna si el paquete se encuentra en la lista de reemplazos
	 * @param pckName nombre del paquete
	 * @return true en caso de que el paquete se encuentre en la lista de excepciones
	 */
	private boolean isExceptedPackage(String pckName) {
		return packageExceptions.containsKey(pckName);
	}

	/**
	 * Retorna si la clase se encuentra en la lista de reemplazos
	 * @param className nombre completo de la clase 
	 * @return true en caso de encontrarse la clase en la lista de excepciones
	 */
	private boolean isExceptedClass(String className) {
		return classExceptions.containsKey(className);
	}

	/**
	 * Retorna el nombre del paquete que reemplaza al pasado 
	 * por parametro en caso de existir, en caso contrario 
	 * retorna el nombre del paquete pasado por parametro
	 * @param pckName nombre del paquete
	 * @return nombre del paquete mapeado en caso de existir. De lo contrario retorna el mismo nombre de paquete pasado por parametro
	 */
	@SuppressWarnings("unused")
	private String replacePackage(String pckName) {
		String newName = pckName;
		if (isExceptedPackage(pckName)) {
			newName = packageExceptions.get(pckName);
			logger.debug("Changing package name '" + pckName + "' to '"
					+ newName + "'");
		}
		return newName;
	}

	/**
	 * Retorna la clase que reemplaza a la especificada por parametro
	 * En caso de no existir retorna el mismo nombre de clase que se recibio
	 * @param className nombre de la clase
	 * @return nombre de la clase mapeada en caso de existir. En caso contrario retorna el mismo nombre que el pasado por parametro
	 */
	@SuppressWarnings("unused")
	private String replaceClass(String className) {
		String newName = className;
		if (isExceptedClass(className)) {
			newName = classExceptions.get(className);
			logger.debug("Changing class name '" + className + "' to '"
					+ newName + "'");
		}
		return newName;
	}

	/**
	 * Retorna el mapeo al nuevo paquete y nombre de clase
	 * correspondiente a la clase especificada por parametro
	 * @param fullName nombre completo de la clase mas el paquete
	 * @return el nombre completo de la clase y paquete en caso de estar mapeada. Caso contrario retorna el mismo nombre pasado por parametro
	 */
	private String replaceWithExceptions(String fullName) {
		if (isExceptedClass(fullName)) {
			String newClassName = classExceptions.get(fullName);
			logger.debug("Changing class name '" + fullName + "' to '"
					+ newClassName + "'");
			return newClassName;
		}
		String newPckName = getPackageName(fullName);
		String newClassName = getClassName(fullName);
		if (isExceptedPackage(newPckName)) {
			System.out.print("Changing package name '" + newPckName + "' to '");
			newPckName = packageExceptions.get(newPckName);
			logger.debug(newPckName + "'");
		}
		if (newPckName.equals("")) {
			return newClassName;
		}
		if (!fullName.equals(newPckName + "." + newClassName)) {
			logger.debug("Changed full name '" + fullName + "' to '"
					+ newPckName + "." + newClassName + "'");
		}
		return newPckName + "." + newClassName;
	}

	/**
	 * Dado un nombre de archivo, lo lee del disco y lo retorna 
	 * como un array de bytes
	 * @param filename nombre del archivo
	 * @return bytes correspondientes al archivo
	 * @throws IOException
	 */
	private byte[] getBytes(String filename) throws IOException {
		File file = new File(filename);
		long len = file.length();
		byte raw[] = new byte[(int) len];
		FileInputStream fin = new FileInputStream(file);
		int r = fin.read(raw);
		if (r != len)
			throw new IOException("Can't read all, " + r + " != " + len);
		fin.close();
		return raw;
	}

	/**
	 * Compila un archivo pasado como parametro creando un proceso
	 * para realizar la compilacion
	 * @param javaFile nombre de la clase java a compilar
	 * @return true si la compilacion fue exitosa
	 * @throws IOException
	 */
	private boolean compile(String javaFile) throws IOException {
		logger.debug("CCL: Compiling " + javaFile + "...");
		Process p = Runtime.getRuntime().exec("javac " + javaFile);
		try {
			p.waitFor();
		} catch (InterruptedException ie) {
			logger.debug(ie);
		}
		int ret = p.exitValue();
		return ret == 0;
	}
	
	/**
	 * Carga la clase especificada
	 * @param javaFilename nombre del archivo fuente
	 * @param classFilename nombre del archivo .class
	 * @param name nombre de la clase
	 * @return clase cargada
	 * @throws ClassNotFoundException en caso de no poder cargarse la clase
	 */
	private Class<?> loadClass (String javaFilename, String classFilename, String name) throws ClassNotFoundException {
		File javaFile = new File(javaFilename);
		File classFile = new File(classFilename);
		Class<?> clas = null;
		// Primero vemos si es necesario compilar. Si existe el codigo
		// java y no existe el .class o el .class esta desactualizado
		// entonces procedemos a compilarlo
		if (javaFile.exists()
				&& (!classFile.exists() || javaFile.lastModified() > classFile
						.lastModified())) {
			try {
				// Si no se puede realizar la compilacion se lanza una excepcion
				if (!compile(javaFilename) || !classFile.exists()) {
					throw new ClassNotFoundException("Compile failed: "
							+ javaFilename);
				}
			} catch (IOException ie) {
				// Lanzamos la excepcion en caso de haber ocurrido algun
				// problema con el archivo
				throw new ClassNotFoundException(ie.toString());
			}
		}
		// Se trata de cargar el archivo en un array de bytes
		try {
			byte raw[] = getBytes(classFilename);
			// Tratamos de convertir el array en una clase
			clas = defineClass(name, raw, 0, raw.length);
		} catch (IOException ie) {
			//No es un error ya que puede ser que tratemos de cargar
			//una biblioteca
			logger.debug("Error al cargar el arreglo de bytes, se continua con la carga de la clase... Error: " + ie.getMessage());
		}
		return clas;
	}

	/**
	 * Se encarga de cargar las clases y compilar los .java
	 * en caso de que sea necesario.
	 * 
	 * @param origName nombre de la clase
	 */
	@Override
	public Class<?> loadClass(String origName) throws ClassNotFoundException {
		if (loadConfigurations) {
			loadConfigurations = false;
			logger = Logger.getLogger("default");
			PropertyConfigurator.configure("resources/log4j.properties");
			logger.debug("loading " + origName + "...");
			try {
				loadClassMapper("resources/configClassLoader.xml");
			} catch (Exception e) {
				System.out
						.println("Error al cargar la configuracion de mapeo de clases y paquetes.");
				e.printStackTrace();
			}
		}
		String name = origName;
		name = replaceWithExceptions(origName);
		
		Class<?> clas = null;
		// Se busca si la clase ya fue cargada
		clas = findLoadedClass(name);
		if (clas == null) {
			// Se crea una ruta a la clase
			// Por ejemplo java.lang.Object => java/lang/Object
			String fileStub = name.replace('.', '/');
			
			String classpath = System.getProperty("java.class.path");
			if (classpath != null) {
				String[] classpathItems = classpath.split(";");
				boolean search = true;
				int i = 0;
				String classpathItem = "";
				while (search && i < classpathItems.length) {
					classpathItem = classpathItems[i];
					// Construimos los objetos que apunten al codigo fuente 
					// y al .class
					String javaFilename = classpathItem.replace("\\", "/").concat("/") + fileStub + ".java";
					String classFilename = classpathItem.replace("\\", "/").concat("/") + fileStub + ".class";
					// Se trata de acargar la clase usando el elemento del classpath
					
					System.out.println("clas = loadClass(" + javaFilename + ", " + classFilename + ", " + name + ");");
					clas = loadClass(javaFilename, classFilename, name);
					if (clas != null) {
						search = false;
					}
					i++;
				}
			}
		}

		//La clase puede estar en una biblioteca, por lo que se intenta
		//cargar de forma normal
		if (clas == null) {
			clas = super.loadClass(name);
		}
		// Si no se encontro la clase, entonces ahora si es un error
		if (clas == null) {
			throw new ClassNotFoundException(name);
		} else {
			// En caso de haberse encontrado la clase
			// se procede a linkearla
			resolveClass(clas);
		}

		// Si la clase original se mapeo a otra ver que es lo que hay que hacer...
		// @TODO queda pendiente resolver esto   
		if (!origName.equals(name)) {
	/*		
			ClassPool pool = ClassPool.getDefault();
			try {
				CtClass cc = pool.get(name);
				cc.setSuperclass(pool.get(origName));
				cc.writeFile();
				return cc.toClass();
			} catch (Exception e) {
				e.printStackTrace();
			}	
		*/		
		}

		// En caso de haberse encontrado la clase, se retorna
		return clas;
	}
	
	/**
	 * Carga la clase e invoca al metodo especificado
	 * 
	 * @param className Clase que se quiere cargar
	 * @param methodName Nombre del metodo que se va a invocar
	 * @param args Argumentos pasados en la llamada al metodo
	 * @throws Exception
	 */
	public void invoke(String className, String methodName, String args[], Class<?>[] argsTypes) throws Exception {
		// Cargamos la clase a traves del class loader
		Class<?> clas = this.loadClass(className);
		// Usamos reflexion para llamar al metodo y pasarle los parametros
		// Buscamos el metodo en la clase
		Method method = clas.getMethod(methodName, argsTypes);
		// Creamos una lista conteniendo los argumentos
		Object argsArray[] = { args };
		// Invocamos al metodo
		method.invoke(null, argsArray);
	}

}