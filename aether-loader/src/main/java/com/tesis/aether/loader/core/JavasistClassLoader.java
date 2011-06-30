package com.tesis.aether.loader.core;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import com.tesis.aether.loader.classTools.ClassManipulator;
import com.tesis.aether.loader.conf.ConfigClassLoader;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
public class JavasistClassLoader extends ClassLoader {
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
	 * Indica si se debe cargar la configuracion o no
	 */
	private boolean loadConfigurations = true;

	/**
	 * Constructor de la clase
	 * @param parent classloader padre
	 */
	public JavasistClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Carga el mapeo de clases
	 * @param fromPath path al archivo de configuracion
	 * @throws SAXException excepcion al parsear el archivo xml de configuracion
	 * @throws IOException excepcion al intentar tener acceso al archivo de configuracion
	 * @throws ParserConfigurationException excepcion al parsear el archivo de configuracion
	 */
	private void loadClassMapper(String fromPath) throws SAXException,
			IOException, ParserConfigurationException {
		ConfigClassLoader conf = new ConfigClassLoader(fromPath);
		classExceptions = conf.getClassExceptions();
	}

	/**
	 * Agrega una clase para ser reemplazada en la carga
	 * @param classSrc clase original
	 * @param classDst clase destino a la cual seran mapeados los metodos de la original
	 * @return true en caso de haberse agregado la excepcion, false en caso de existir la clase en el mapeo
	 */
	public boolean addClassException(String classSrc, String classDst) {
		if (classExceptions.containsKey(classSrc)) {
			return false;
		}
		classExceptions.put(classSrc, classDst);
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
	 * Retorna el nombre del paquete de una clase completa pasada
	 * como parametro
	 * @param srcClass nombre completo de la clase
	 * @return nombre del paquete de la clase
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	 * Retorna si la clase se encuentra en la lista de reemplazos
	 * @param className nombre completo de la clase 
	 * @return true en caso de encontrarse la clase en la lista de excepciones
	 */
	private boolean isExceptedClass(String className) {
		return classExceptions.containsKey(className);
	}

	/**
	 * Retorna la clase que reemplaza a la especificada por parametro
	 * En caso de no existir retorna el mismo nombre de clase que se recibio
	 * @param className nombre de la clase
	 * @return nombre de la clase mapeada en caso de existir. En caso contrario retorna el mismo nombre que el pasado por parametro
	 */
	private String replaceClassName(String className) {
		String newName = className;
		if (isExceptedClass(className)) {
			newName = classExceptions.get(className);
			logger.debug("Changing class name '" + className + "' to '"
					+ newName + "'");
		}
		return newName;
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
	 * @throws ClassNotFoundException en caso de no encontrar la clase
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

	private String findPath(String name, String extension) {
		String classpath = System.getProperty("java.class.path");
		String fileStub = name.replace('.', '/');
		if (classpath != null) {
			String[] classpathItems = classpath.split(";");
			boolean search = true;
			int i = 0;
			String classpathItem = "";
			while (search && i < classpathItems.length) {
				classpathItem = classpathItems[i];
				System.out.println("BUSCANDO PATH EN: " + classpathItem);
				String path = classpathItem.replace("\\", "/").concat("/");
				String fileName = path + fileStub + "." + extension;
				File file = new File(fileName);
				if (file.exists()) {
					System.out.println("PATH DEL ARCHIVO: " + path);
					return path;
				}
				i++;
			}
		}
		System.out.println("PATH DEL ARCHIVO NO ENCONTRADO");
		return null;
	}
	
	/**
	 * Cargador de clases utilizado para las clases que no estan en las excepciones
	 * @param name nombre de la clase a cargar
	 * @return clase cargada
	 * @throws ClassNotFoundException en caso de no encontrarse la clase
	 */
	public Class<?> loadClass2(String name) throws ClassNotFoundException {
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
					
					logger.debug("clas = loadClass(" + javaFilename + ", " + classFilename + ", " + name + ");");
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

		// En caso de haberse encontrado la clase, se retorna
		return clas;
	}
	
	
	/**
	 * Se encarga de cargar las clases y compilar los .java
	 * en caso de que sea necesario.
	 * @param origName nombre original de la clase a cargar
	 */
	@Override
	public Class<?> loadClass(String origName) throws ClassNotFoundException {
		if (loadConfigurations) {
			loadConfigurations = false;
			logger = Logger.getLogger("default");
			PropertyConfigurator.configure("resources/log4j.properties");
			
			//carga de clases a mapear
			String actualPath = new File (".").getAbsolutePath();//System.getProperty("user.dir");
			System.out.println("Current Path: " + actualPath);
			String path = findPath("configClassLoader", "xml");
			if (path != null) {
				try {
					loadClassMapper(path + "configClassLoader.xml");
				} catch (Exception e) {
					logger.error("Error al cargar la configuracion de mapeo de clases y paquetes.");
					logger.error(e);
				}
			} else {
				try {
					loadClassMapper("resources/configClassLoader.xml");
				} catch (Exception e) {
					logger.error("Error al cargar la configuracion de mapeo de clases y paquetes.");
					logger.error(e);
				}
			}
		}
		logger.debug("loading " + origName + "...");
		if (!isExceptedClass(origName)) {
			//Si la clase no esta en la lista de excepciones entonces
			//se carga con un classloader normal
			Class<?> clas = loadClass2(origName);
			return clas;
		}
		String nameClassTo = origName;
		nameClassTo = replaceClassName(origName);
		try {
			return ClassManipulator.addClassCalls(origName, nameClassTo, true);
		} catch (Exception e) {
			logger.error(e);
			throw new ClassNotFoundException(nameClassTo);
		}	
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