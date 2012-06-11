package com.tesis.aether.loader.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.SAXException;

import com.tesis.aether.loader.classTools.ClassManipulator;
import com.tesis.aether.loader.classTools.JarResources;
import com.tesis.aether.loader.conf.ConfigClassLoader;

/********************************************************************************
 * Para que el cargador de clases funcione en la aplicacion                     *
 * se debe agregar com parametro de la vm la siguiente linea:                   *
 *                                                                              *
 * -Djava.system.class.loader=com.tesis.aether.loader.core.CompilingClassLoader *
 ********************************************************************************/

/**
 * Compila las clases en caso de haber sufrido modificaciones o estar
 * desactualizadas y las carga para ejecucion
 */
public class JavasistClassLoader extends ClassLoader {
	private static final String RESOURCES = "resources/";

	private static final String LIB = "lib/";

	private static final String MAIN_SRC = "src/main/";
	
	private static final String CONFIG_FILE_NAME = "configClassLoader";
	
	private static final String CONFIG_FILE_EXTENSION = "xml";

	/**
	 * Contiene el mapeo de clases a ser reemplazadas en la carga. Las clases se
	 * deben especificar con incluyendo el paquete, por ejemplo:
	 * "java.util.HashMap" <Clase a reemplazar, Clase reemplazante>
	 */
	private HashMap<String, String> classExceptions = new HashMap<String, String>();

	/**
	 * Indica si se debe cargar la configuracion o no
	 */
	private boolean loadConfigurations = true;

	/**
	 * Constructor de la clase
	 * 
	 * @param parent
	 *            classloader padre
	 */
	public JavasistClassLoader(ClassLoader parent) {
		super(parent);
	}

	/**
	 * Carga el mapeo de clases
	 * 
	 * @param fromPath
	 *            path al archivo de configuracion
	 * @throws SAXException
	 *             excepcion al parsear el archivo xml de configuracion
	 * @throws IOException
	 *             excepcion al intentar tener acceso al archivo de
	 *             configuracion
	 * @throws ParserConfigurationException
	 *             excepcion al parsear el archivo de configuracion
	 */
	private void loadClassMapper(String fromPath) throws SAXException,
			IOException, ParserConfigurationException {
		ConfigClassLoader conf = new ConfigClassLoader(fromPath);
		classExceptions = conf.getClassExceptions();
	}

	/**
	 * Agrega una clase para ser reemplazada en la carga
	 * 
	 * @param classSrc
	 *            clase original
	 * @param classDst
	 *            clase destino a la cual seran mapeados los metodos de la
	 *            original
	 * @return true en caso de haberse agregado la excepcion, false en caso de
	 *         existir la clase en el mapeo
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
	 * 
	 * @param classException
	 *            nombre original de la clase a eliminar del mapeo
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
	 * Retorna el nombre del paquete de una clase completa pasada como parametro
	 * 
	 * @param srcClass
	 *            nombre completo de la clase
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
	 * Retorna el nombre de la clase pasada como parametro, quitandole el nombre
	 * de paquete
	 * 
	 * @param srcClass
	 *            nombre completo de la clase
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
	 * 
	 * @param className
	 *            nombre completo de la clase
	 * @return true en caso de encontrarse la clase en la lista de excepciones
	 */
	private boolean isExceptedClass(String className) {
		return classExceptions.containsKey(className);
	}

	/**
	 * Retorna la clase que reemplaza a la especificada por parametro En caso de
	 * no existir retorna el mismo nombre de clase que se recibio
	 * 
	 * @param className
	 *            nombre de la clase
	 * @return nombre de la clase mapeada en caso de existir. En caso contrario
	 *         retorna el mismo nombre que el pasado por parametro
	 */
	private String replaceClassName(String className) {
		String newName = className;
		if (isExceptedClass(className)) {
			newName = classExceptions.get(className);
			System.out.println("Changing class name '" + className + "' to '"
					+ newName + "'");
		}
		return newName;
	}

	/**
	 * Dado un nombre de archivo, lo lee del disco y lo retorna como un array de
	 * bytes
	 * 
	 * @param filename
	 *            nombre del archivo
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
	 * Compila un archivo pasado como parametro creando un proceso para realizar
	 * la compilacion
	 * 
	 * @param javaFile
	 *            nombre de la clase java a compilar
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
	 * Carga la clase especificada
	 * 
	 * @param javaFilename
	 *            nombre del archivo fuente
	 * @param classFilename
	 *            nombre del archivo .class
	 * @param name
	 *            nombre de la clase
	 * @return clase cargada
	 * @throws ClassNotFoundException
	 *             en caso de no encontrar la clase
	 */
	private Class<?> loadClass(String javaFilename, String classFilename,
			String name) throws ClassNotFoundException {
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
			// No es un error ya que puede ser que tratemos de cargar
			// una biblioteca
			// System.out.println("Error al cargar el arreglo de bytes, se continua con la carga de la clase... Error: "
			// + ie.getMessage());
		}
		return clas;
	}

	private boolean existFile(String fileName) {
		if (fileName == null)
			return false;
		File file = new File(fileName);
		return file.exists();
	}

	private boolean createFileFromBytes(File toRet, byte[] arr) {
		try {
			FileOutputStream fos = new FileOutputStream(toRet);
			fos.write(arr);
			fos.close();
			return true;
		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException : " + ex);
		} catch (IOException ioe) {
			System.out.println("IOException : " + ioe);
		}
		return false;
	}

	private String findPathInJar(String jarFile, String fileName, String ext) {
		JarResources jr = new JarResources(jarFile);
		byte[] array = jr.getResource(fileName + "." + ext);
		//String pathFile = System.currentTimeMillis() + "";
		File f = null;
		try {
			f = File.createTempFile(fileName, "." + ext);
			createFileFromBytes(f, array);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// try {
		// if( jarFile != null ) {
		// URL jar = (new File (jarFile)).toURI().toURL();
		// ZipInputStream zip = new ZipInputStream(jar.openStream());
		// ZipEntry ze = null;
		// String entryName;
		//
		// while( ( ze = zip.getNextEntry() ) != null ) {
		// entryName = ze.getName();
		// System.out.println("=====>>> jar entry: " + entryName);
		// if( entryName.equals(fileName) ) {
		// //Crear un archivo temporal con la configuracion
		// JarResources jr = new JarResources(jarFile);
		// jr.getResource(fileName);
		// //Retornar el archivo temporal
		// return jarFile + "!";
		// }
		// }
		// }
		// } catch (Exception e) {
		// return null;
		// }
		return f!=null?f.getAbsolutePath():null;
	}

	/**
	 * Busca el path del archivo especificado por parametros
	 * 
	 * @param actualPath
	 *            path actual
	 * @param name
	 *            nombre del archivo a buscar
	 * @param extension
	 *            extension del archivo a buscar (sin el '.')
	 * @return
	 */
	private String findPath(String actualPath, String name, String extension) {
		String fileStub = name.replace('.', '/');

		// Se busca el archivo de configuración en el path actual
		System.out.println("***BUSCANDO ARCHIVO EN: " + actualPath);
		if (existFile(actualPath + fileStub + "." + extension)) {
			return actualPath + fileStub + "." + extension;
		}

		// Si no se encontro se busca en pathActual/resources
		String pathResources = actualPath + RESOURCES;
		System.out.println("BUSCANDO ARCHIVO EN: " + pathResources);
		if (existFile(pathResources + fileStub + "." + extension)) {
			return pathResources + fileStub + "." + extension;
		}

		// Si no se encontro se busca en pathActual/src/main/resources por si es un proy maven
		pathResources = actualPath + MAIN_SRC + RESOURCES;
		System.out.println("BUSCANDO ARCHIVO EN: " + pathResources);
		if (existFile(pathResources + fileStub + "." + extension)) {
			return pathResources + fileStub + "." + extension;
		}

		//Si no se encontro se busca en los jar de aether en 'path actual'/lib
		String libs = LIB;
		System.out.println("libs: " + libs);
		File f = new File (libs);
		System.out.println("Path completo de libs: " + f.getAbsolutePath());
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name.contains("aether-") && name.contains("-adapter"))
					return true;
				return false;
			}
		};
		try {
			String[] libsItems = f.list(filter);
			for (String item : libsItems) {
				item = f.getAbsolutePath() + "/" + item;
				System.out.println("BUSCANDO PATH EN JAR DE AETHER ADAPTER EN LIBS: "
						+ item);
				String path = item.replace('\\', '/');
				if (path.endsWith(".jar")) {
					String retPath = findPathInJar(path, fileStub, extension);
					if (retPath != null)
						return retPath;
				}
			}
		} catch (Exception e) {
			System.out.println("Error al buscar archivo en /lib: " + e.getMessage());
		}
		
		// Se busca en el classpath
		String classpath = System.getProperty("java.class.path");
		System.out.println("Classpath: " + classpath);
		if (classpath != null) {
			ArrayList<String> aetherJars = new ArrayList<String>();
			ArrayList<String> nonAetherJars = new ArrayList<String>();
			String[] classpathItems = classpath.split(";");

			for (String item : classpathItems) {
				if (item.contains("aether-"))
					if (item.contains("-adapter")) {// Si es un adapter se
													// inserta al principio de
													// la lista
						System.out
								.println("***SE AGREGA AL INICIO ADAPTER DE AETHER: "
										+ item);
						aetherJars.add(0, item);
					} else {
						System.out
								.println("****SE AGREGA AL FINAL ELEMENTO DE AETHER: "
										+ item);
						aetherJars.add(item);// Si no es un adapter se inserta
												// al final de la lista
					}
				else {
					System.out.println("**SE AGREGA ELEMENTO FUERA DE AETHER: "
							+ item);
					nonAetherJars.add(item);// Es un jar que no corresponde a
											// aether
				}
			}

			// Se busca en los jars correspondientes a aether
			if (aetherJars != null) {
				for (String item : aetherJars) {
					System.out.println("BUSCANDO PATH EN ELEMENTO DE AETHER: "
							+ item);
					String path = item.replace('\\', '/');
					if (path.endsWith(".jar")) {
						String retPath = findPathInJar(path, fileStub, extension);
						if (retPath != null)
							return retPath;
					} else {
						String fileName = path + fileStub + "." + extension;
						path = path.concat("/");
						if (existFile(fileName)) {
							System.out.println("CARGANDO EL ARCHIVO DESDE: "
									+ path);
							return path;
						}
					}
				}
			}

			// Si no se encontro se busca en el resto de los archivos del
			// classpath
			if (nonAetherJars != null) {
				for (String item : nonAetherJars) {
					System.out
							.println("BUSCANDO PATH EN ELEMENTOS FUERA DE AETHER: "
									+ item);
					String path = item.replace('\\', '/').concat("/");
					String fileName = path + fileStub + "." + extension;
					if (existFile(fileName)) {
						System.out
								.println("CARGANDO EL ARCHIVO DESDE: " + path);
						return path;
					}
				}
			}
		}
		System.out.println("PATH DEL ARCHIVO NO ENCONTRADO");
		return null;
	}

	/**
	 * Cargador de clases utilizado para las clases que no estan en las
	 * excepciones
	 * 
	 * @param name
	 *            nombre de la clase a cargar
	 * @return clase cargada
	 * @throws ClassNotFoundException
	 *             en caso de no encontrarse la clase
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
					String javaFilename = classpathItem.replace("\\", "/")
							.concat("/")
							+ fileStub + ".java";
					String classFilename = classpathItem.replace("\\", "/")
							.concat("/")
							+ fileStub + ".class";
					// Se trata de acargar la clase usando el elemento del
					// classpath

					// System.out.println("clas = loadClass(" + javaFilename +
					// ", " + classFilename + ", " + name + ");");
					clas = loadClass(javaFilename, classFilename, name);
					if (clas != null) {
						search = false;
					}
					i++;
				}
			}
		}

		// La clase puede estar en una biblioteca, por lo que se intenta
		// cargar de forma normal
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
	 * Se encarga de cargar las clases y compilar los .java en caso de que sea
	 * necesario.
	 * 
	 * @param origName
	 *            nombre original de la clase a cargar
	 */
	@Override
	public Class<?> loadClass(String origName) throws ClassNotFoundException {
		if (loadConfigurations) {
			loadConfigurations = false;
			// carga de clases a mapear
			String actualPath;
			actualPath = FilenameUtils.getPathNoEndSeparator(new File(".")
					.getAbsolutePath());

			actualPath = actualPath.replace('\\', '/');

			if (!actualPath.endsWith("/")) // Si el path no termina con / se la
											// agregamos
				actualPath += "/";

			System.out.println("Current Path: " + actualPath);
			String fullPath = findPath(actualPath, CONFIG_FILE_NAME, CONFIG_FILE_EXTENSION);
			if (fullPath != null) {
				try {
					loadClassMapper(fullPath);
				} catch (Exception e) {
					System.out
							.println("Error al cargar la configuracion de mapeo de clases y paquetes.");
					System.out.println(e);
				}
			} else {
				System.out
						.println("Error al cargar la configuracion de mapeo de clases y paquetes. Archivo no encontrado.");
			}
		}
		System.out.println("loading " + origName + "...");
		if (!isExceptedClass(origName)) {
			// Si la clase no esta en la lista de excepciones entonces
			// se carga con un classloader normal
			Class<?> clas = loadClass2(origName);
			return clas;
		}
		String nameClassTo = null;
		nameClassTo = replaceClassName(origName);
		try {
			return ClassManipulator.addClassCalls(origName, nameClassTo, true);
		} catch (Exception e) {
			System.out.println(e);
			throw new ClassNotFoundException(nameClassTo);
		}
	}

	/**
	 * Carga la clase e invoca al metodo especificado
	 * 
	 * @param className
	 *            Clase que se quiere cargar
	 * @param methodName
	 *            Nombre del metodo que se va a invocar
	 * @param args
	 *            Argumentos pasados en la llamada al metodo
	 * @throws Exception
	 */
	public void invoke(String className, String methodName, String args[],
			Class<?>[] argsTypes) throws Exception {
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

	protected URL findResource(String name) {
		try {
			File file = new File(name);
			if (file.exists()) {
				return file.toURI().toURL();

			}
			file = new File("src/main/resources/" + name);
			if (file.exists()) {
				return file.toURI().toURL();
			}

		} catch (Exception e) {
		}
		return null;
	}
}