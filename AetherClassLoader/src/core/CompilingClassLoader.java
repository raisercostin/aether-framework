package core;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import conf.ConfigClassLoader;

/**************************************************************
 * Para que el cargador de clases funcione en la aplicacion   *
 * se debe agregar com parametro de la vm la siguiente linea: *
 *                                                            *
 * -Djava.system.class.loader=core.CompilingClassLoader       *
 **************************************************************/


/**
 * Compila las clases en caso de haber sufrido modificaciones
 * o estar desactualizadas y las carga para ejecucion
 */
public class CompilingClassLoader extends ClassLoader {
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
	private boolean loadConfigurations = true;

	public CompilingClassLoader(ClassLoader parent) throws SAXException,
			IOException, ParserConfigurationException {
		super(parent);
	}

	private void loadClassMapper(String fromPath) throws SAXException,
			IOException, ParserConfigurationException {
		ConfigClassLoader conf = new ConfigClassLoader(fromPath);
		classExceptions = conf.getClassExceptions();
		packageExceptions = conf.getPackageExceptions();
	}

	/**
	 * Agrega una clase para ser reemplazada en la carga
	 * @param classSrc
	 * @param classDst
	 * @return
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
	 * @param pckSrc
	 * @param pckDst
	 * @return
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
	 * @param classException
	 * @return
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
	 * @param packageException
	 * @return
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
	 * @param srcClass
	 * @return
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
	 * @param srcClass
	 * @return
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
	 * @param pckName
	 * @return
	 */
	private boolean isExceptedPackage(String pckName) {
		return packageExceptions.containsKey(pckName);
	}

	/**
	 * Retorna se la clase se encuentra en la lista de reemplazos
	 * @param className
	 * @return
	 */
	private boolean isExceptedClass(String className) {
		return classExceptions.containsKey(className);
	}

	/**
	 * Retorna el nombre del paquete que reemplaza al pasado 
	 * por parametro en caso de existir, en caso contrario 
	 * retorna el nombre del paquete pasado por parametro
	 * @param pckName
	 * @return
	 */
	@SuppressWarnings("unused")
	private String replacePackage(String pckName) {
		String newName = pckName;
		if (isExceptedPackage(pckName)) {
			newName = packageExceptions.get(pckName);
			System.out.println("Changing package name '" + pckName + "' to '"
					+ newName + "'");
		}
		return newName;
	}

	/**
	 * Retorna la clase que reemplaza a la especificada por parametro
	 * En caso de no existir retorna el mismo nombre de clase que se recibio
	 * @param className
	 * @return
	 */
	@SuppressWarnings("unused")
	private String replaceClass(String className) {
		String newName = className;
		if (isExceptedClass(className)) {
			newName = classExceptions.get(className);
			System.out.println("Changing class name '" + className + "' to '"
					+ newName + "'");
		}
		return newName;
	}

	/**
	 * Retorna el mapeo al nuevo paquete y nombre de clase
	 * correspondiente a la clase especificada por parametro
	 * @param fullName
	 * @return
	 */
	private String replaceWithExceptions(String fullName) {
		if (isExceptedClass(fullName)) {
			String newClassName = classExceptions.get(fullName);
			System.out.println("Changing class name '" + fullName + "' to '"
					+ newClassName + "'");
			return newClassName;
		}
		String newPckName = getPackageName(fullName);
		String newClassName = getClassName(fullName);
		if (isExceptedPackage(newPckName)) {
			System.out.print("Changing package name '" + newPckName + "' to '");
			newPckName = packageExceptions.get(newPckName);
			System.out.println(newPckName + "'");
		}
		if (newPckName.equals("")) {
			return newClassName;
		}
		if (!fullName.equals(newPckName + "." + newClassName)) {
			System.out.println("Changed full name '" + fullName + "' to '"
					+ newPckName + "." + newClassName + "'");
		}
		return newPckName + "." + newClassName;
	}

	/**
	 * Dado un nombre de archivo, lo lee del disco y lo retorna 
	 * como un array de bytes
	 * @param filename
	 * @return
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
	 * @param javaFile
	 * @return true si la compilacion fue exitosa
	 * @throws IOException
	 */
	private boolean compile(String javaFile) throws IOException {
		System.out.println("CCL: Compiling " + javaFile + "...");
		Process p = Runtime.getRuntime().exec("javac " + javaFile);
		try {
			p.waitFor();
		} catch (InterruptedException ie) {
			System.out.println(ie);
		}
		int ret = p.exitValue();
		return ret == 0;
	}

	/**
	 * Se encarga de cargar las clases y compilar los .java
	 * en caso de que sea necesario.
	 */
	@Override
	public Class<?> loadClass(String origName) throws ClassNotFoundException {
		System.out.println("loading " + origName + "...");
		if (loadConfigurations) {
			loadConfigurations = false;
			try {
				loadClassMapper("resources/configClassLoader.xml");
			} catch (Exception e) {
				System.out
						.println("Error al cargar la configuracion de mapeo de clases y paquetes.");
				e.printStackTrace();
			}
		}
		String name = replaceWithExceptions(origName);
		Class<?> clas = null;
		// Se busca si la clase ya fue cargada
		clas = findLoadedClass(name);
		// Se crea una ruta a la clase
		// Por ejemplo java.lang.Object => java/lang/Object
		String fileStub = name.replace('.', '/');
		// Construimos los objetos que apunten al codigo fuente 
		// y al .class
		String javaFilename = "src/" + fileStub + ".java";
		String classFilename = "bin/" + fileStub + ".class";
		File javaFile = new File(javaFilename);
		File classFile = new File(classFilename);
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
			System.out.println("Error al cargar el arreglo de bytes, se continua con la carga de la clase... Error: " + ie.getMessage());
		}
		if (!origName.equals(name)) {
			if (clas != null)
				return clas;
		}
		//La clase puede estar en una biblioteca, por lo que se intenta
		//cargar de forma normal
		if (clas == null) {
			clas = super.loadClass(name);
		}
		// En caso de no haberse encontrado la clase y estar 'resolve' en true
		// se procede a linkear la clase
			resolveClass(clas);
		// Si no se encontro la clase, entonces ahora si es un error
		if (clas == null)
			throw new ClassNotFoundException(name);
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