package com.tesis.aether.adapters.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.tesis.aether.adapters.file.config.ConfigFileAdapter;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DownloadException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.framework.adapter.AetherFrameworkAdapter;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public class FileAetherFrameworkAdapter extends AetherFrameworkAdapter {
	private static FileAetherFrameworkAdapter INSTANCE = null;
	private static String container = "raiz";
	private static String tempDirectory = "/tmp";
	private static String CONTAINER = "container";
	private static String TEMPDIRECTORY = "tempDirectory";
	private static final String CONFIG_FILE = "src/main/resources/configFileAdapter.xml";
		
	protected FileAetherFrameworkAdapter() {
		super();
	}

	public static FileAetherFrameworkAdapter getInstance() {
		if (INSTANCE == null) {
			loadConfig(CONFIG_FILE);
			INSTANCE = new FileAetherFrameworkAdapter();
			if (!INSTANCE.service.existsContainer(container))
				try {
					INSTANCE.service.createContainer(container);
				} catch (CreateContainerException e) {
					e.printStackTrace();
				}
		}
		return INSTANCE;
	}

	//Cargar la configuracion de un xml o algo por el estilo
	private static void loadConfig(String xmlfile) {
		ConfigFileAdapter cfa;
		Map<String, String> map = null;
		try {
			cfa = new ConfigFileAdapter(xmlfile);
			map = cfa.getProperties();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		if (map != null) {
			if (map.containsKey(CONTAINER))
				container = map.get(CONTAINER);
			if (map.containsKey(TEMPDIRECTORY))
				tempDirectory = map.get(TEMPDIRECTORY);
		}
	}

	public boolean isDirectory(String path) {
		try {
			return service.checkDirectoryExists(container, path);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean canRead(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean canWrite(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean exists(String path) {
		try {
			return service.checkObjectExists(container, path);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isFile(String path) {
		try {
			return service.checkFileExists(container, path);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean isHidden(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	public long lastModified(String path) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long length(String path) {
		try {
			return service.sizeOf(container, path);
		} catch (MetadataFetchingException e) {
			e.printStackTrace();
			return 0;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return 0;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public boolean createNewFile(String path) {
		try {
			if (service.checkFileExists(container, path))
				return false;
		} catch (MethodNotSupportedException e1) {
			e1.printStackTrace();
		}
		boolean exists = false;
		try {
			exists = service.checkDirectoryExists(container, path);
		} catch (MethodNotSupportedException e1) {
			e1.printStackTrace();
		} 
		if (!exists) {
			String fileName = getFileName(path);
			if (fileName != null && !"".equals(fileName)) {
				File tmp = new File(tempDirectory + (fileName.startsWith("/")?fileName:"/"+fileName));
				try {
					if (!tmp.createNewFile())
						return false;
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				try {
					service.uploadSingleFile(tmp, container, getDirectories(path));
					return true;
				} catch (UploadException e) {
					e.printStackTrace();
				} catch (MethodNotSupportedException e) {
					e.printStackTrace();
				} catch (FileNotExistsException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public boolean delete(String path) {
		try {
			service.delete(container, path, true);
		} catch (DeleteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String[] list(String path) {
		try {
			if (!service.checkDirectoryExists(container, path)) 
				return null;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		List<StorageObjectMetadata> listObjects = null;
		try {
			listObjects = service.listFiles(container, path, false);
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		if (listObjects != null) {
			List<String> strObjects = new ArrayList<String>();
			for (StorageObjectMetadata som : listObjects) {
				strObjects.add(som.getName());
			}
			return strObjects.toArray(new String[0]);
		}
		return null;
	}

	public String[] canRead(String path, FilenameFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public File[] listFiles(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public File[] listFiles(String path, FilenameFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public File[] listFiles(String path, FileFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean mkdir(String path) {
		if (path == null || "".equals(path))
			return false;
		try {
			if (service.checkDirectoryExists(container, path)) 
				return false;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
		try {
			if (service.checkFileExists(container, path))
				return false;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
		try {
			service.createFolder(container, path);
		} catch (FolderCreationException e) {
			e.printStackTrace();
			return false;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean mkdirs(String path) {
		return mkdir(path);
	}

	public boolean renameTo(String path, File dest) {
		try {
			if (!service.checkFileExists(container, path))
				return false;
			if (service.checkFileExists(container, dest.getPath()))
				return false;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
		}
		try {
			if (service.checkDirectoryExists(container, dest.getPath()))
				return false;
		} catch (MethodNotSupportedException e1) {
			e1.printStackTrace();
		}
		String nameTime = String.valueOf(System.currentTimeMillis());
		File fileTmp = new File(tempDirectory + "/" + nameTime + "tmpFile"); 
		try {
			service.downloadFileToDirectory(container, path, fileTmp);
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return false;
		} catch (DownloadException e) {
			e.printStackTrace();
			return false;
		}
		try {
			service.uploadSingleFile(fileTmp, container, dest.getPath());
		} catch (UploadException e) {
			e.printStackTrace();
			return false;
		} catch (MethodNotSupportedException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotExistsException e) {
			e.printStackTrace();
			return false;
		}
		try {
			service.deleteFile(container, path);
		} catch (DeleteException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public File[] listRoots() {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(String path, File pathname) {
		long f1 = 0, f2 = 0;
		f1 = this.length(path);
		f2 = pathname.length();
		if (f1 > f2)
			return 1;
		if (f1 < f2)
			return -1;
		return 0;
	}

	public boolean equals(String path, Object obj) {
		if ((obj != null) && (obj instanceof File))
			return compareTo(path, (File)obj) == 0;
		return false;
	}

	public String toString(String path) {
		return path;
	}

	private static String getFileName(String fileName) {
		String name = "";
		if (fileName != null && !getInstance().isDirectory(fileName)) {
			String[] names = fileName.split("/");
			if (names.length == 1) {
				return names[0];
			}
			name = names[names.length - 1];
		}
		return name;
	}

	/**
	 * Retorna el nombre del paquete de una clase completa pasada como parametro
	 * 
	 * @param srcClass
	 *            nombre completo de la clase (incluyendo el nombre de paquete)
	 * @return nombre del paquete correspondiente a la clase
	 */
	private static String getDirectories(String path) {
		String directories = "";
		if (getInstance().isDirectory(path) || path.endsWith("/"))
			return path;
		if (path != null) {
			String[] names = path.split("/");
			for (int i = 0; i < names.length - 1; i++) {  //tomamos siempre la ultima posicion como el nombre de archivo
				directories += "/" + names[i];
			}
		}
		return directories;
	}

}
