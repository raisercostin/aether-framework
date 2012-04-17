package com.tesis.aether.core.services.storage;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;

import com.tesis.aether.core.exception.CopyFileException;
import com.tesis.aether.core.exception.CreateContainerException;
import com.tesis.aether.core.exception.DeleteContainerException;
import com.tesis.aether.core.exception.DeleteException;
import com.tesis.aether.core.exception.DownloadException;
import com.tesis.aether.core.exception.FileNotExistsException;
import com.tesis.aether.core.exception.FolderCreationException;
import com.tesis.aether.core.exception.MetadataFetchingException;
import com.tesis.aether.core.exception.MethodNotSupportedException;
import com.tesis.aether.core.exception.MigrationException;
import com.tesis.aether.core.exception.MoveFileException;
import com.tesis.aether.core.exception.URLExtractionException;
import com.tesis.aether.core.exception.UploadException;
import com.tesis.aether.core.services.CloudService;
import com.tesis.aether.core.services.CloudServiceConstants;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;

public abstract class BaseStorageService extends CloudService {

	public BaseStorageService() {
		super();
		setKind(CloudServiceConstants.STORAGE_KIND);
	}

	/**
	 * Verifica si un directorio especifico existe dentro de un container.
	 * 
	 * @param container
	 * @param remotePath
	 * @return
	 * @throws MethodNotSupportedException
	 */
	public abstract boolean checkDirectoryExists(String container, String remotePath) throws MethodNotSupportedException;
	
	public boolean checkDirectoryExists(String remotePath) throws MethodNotSupportedException {
		return checkDirectoryExists(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Verifica si un archivo especifico existe dentro de un container.
	 * 
	 * @param container
	 * @param remotePath
	 * @return
	 * @throws MethodNotSupportedException
	 */
	public abstract boolean checkFileExists(String container, String remotePath) throws MethodNotSupportedException;
	public boolean checkFileExists(String remotePath) throws MethodNotSupportedException {
		return checkFileExists(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Verifica si un objeto, sea archivo o directorio, existe dentro de un
	 * container.
	 * 
	 * @param container
	 * @param remotePath
	 * @return
	 * @throws MethodNotSupportedException
	 */
	public abstract boolean checkObjectExists(String container, String remotePath) throws MethodNotSupportedException;
	public boolean checkObjectExists(String remotePath) throws MethodNotSupportedException {
		return checkObjectExists(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Copia el archivo “from” del container “fromContainer” hacia el directorio
	 * “toDirectory” del container “toContainer”. Ambos containers pueden ser
	 * idénticos.
	 * 
	 * @param fromContainer
	 * @param from
	 * @param toContainer
	 * @param toDirectory
	 * @throws CopyFileException
	 */
	public abstract void copyFile(String fromContainer, String from, String toContainer, String toDirectory) throws CopyFileException;

	/**
	 * Crea el container con el nombre deseado. Según el tipo de servicio que se
	 * esté utilizando, pueden existir restricciones. Por ejemplo en S3 se tiene
	 * un índice global de containers, no por cuenta. Es decir, dos usuarios no
	 * pueden crear containers con el mismo nombre.
	 * 
	 * @param name
	 * @throws CreateContainerException
	 */
	public abstract void createContainer(String name) throws CreateContainerException;

	/**
	 * Crea la carpeta “remotePath” de manera recursiva dentro de “container”
	 * 
	 * @param container
	 * @param remotePath
	 * @throws FolderCreationException
	 * @throws MethodNotSupportedException
	 */
	public abstract void createFolder(String container, String remotePath) throws FolderCreationException, MethodNotSupportedException;
	public void createFolder(String remotePath) throws FolderCreationException, MethodNotSupportedException {
		createFolder(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Método de acceso genérico a “delete”. Elimina un objeto, sea archivo o
	 * directorio, del container especifico. La variable “recursive” indica si
	 * el borrado debe ser recursivo.
	 * 
	 * @param container
	 * @param file
	 * @param recursive
	 * @throws DeleteException
	 */
	public abstract void delete(String container, StorageObjectMetadata file, boolean recursive) throws DeleteException;
	public void delete(StorageObjectMetadata file, boolean recursive) throws DeleteException {
		delete(this.getServiceProperty(StorageServiceConstants.CONTAINER), file, recursive);
	}

	/**
	 * Método de acceso genérico a “delete”. Elimina un objeto, sea archivo o
	 * directorio, del container especifico. La variable “recursive” indica si
	 * el borrado debe ser recursivo.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @param recursive
	 * @throws DeleteException
	 */
	public abstract void delete(String container, String remotePathFile, boolean recursive) throws DeleteException;
	public void delete(String remotePathFile, boolean recursive) throws DeleteException {
		delete(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile, recursive);
	}

	/**
	 * Elimina un container completo de la cuenta del usuario. El contenido del
	 * container es eliminado.
	 * 
	 * @param name
	 * @throws DeleteContainerException
	 */
	public abstract void deleteContainer(String name) throws DeleteContainerException;

	/**
	 * Elimina el archivo pasado por parámetro del contenedor especificado.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @throws DeleteException
	 */
	public abstract void deleteFile(String container, String remotePathFile) throws DeleteException;
	public void deleteFile(String remotePathFile) throws DeleteException {
		deleteFile(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile);
	}

	/**
	 * Elimina el directorio pasado por parámetro del contenedor especificado.
	 * 
	 * @param container
	 * @param remotePath
	 * @throws DeleteException
	 */
	public abstract void deleteFolder(String container, String remotePath) throws DeleteException;
	public void deleteFolder(String remotePath) throws DeleteException {
		deleteFolder(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Descarga el directorio “remoteDirectory” dentro del path local
	 * especificado. La descarga del directorio es recursiva.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @param localDirectory
	 * @throws FileNotExistsException
	 * @throws DownloadException
	 * @throws MethodNotSupportedException
	 */
	public abstract void downloadDirectoryToDirectory(String container, String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException;
	public void downloadDirectoryToDirectory(String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException, MethodNotSupportedException {
		downloadDirectoryToDirectory(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile, localDirectory);
	}

	/**
	 * Descarga el archivo remoto pasado por parámetro dentro del path local
	 * especificado.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @param localDirectory
	 * @throws FileNotExistsException
	 * @throws DownloadException
	 */
	public abstract void downloadFileToDirectory(String container, String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException;
	public void downloadFileToDirectory(String remotePathFile, File localDirectory) throws FileNotExistsException, DownloadException {
		downloadFileToDirectory(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile, localDirectory);
	}

	/**
	 * Método genérico de acceso a “get”. Descarga el archivo o directorio
	 * completo pasado por parámetro dentro del path local especificado. En caso
	 * de ser un directorio, la descarga es recursiva.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @param localDirectory
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 * @throws DownloadException
	 */
	public abstract void downloadToDirectory(String container, String remotePathFile, File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException;
	public void downloadToDirectory(String remotePathFile, File localDirectory) throws MethodNotSupportedException, FileNotExistsException, DownloadException {
		downloadToDirectory(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile, localDirectory);
	}

	/**
	 * Verifica si el container con nombre “name” existe.
	 * 
	 * @param name
	 * @return
	 */
	public abstract boolean existsContainer(String name);

	/**
	 * Base de las facilidades de “get”. Obtiene un InputStream para un archivo
	 * específico dentro del container “container”.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @return
	 * @throws FileNotExistsException
	 */
	public abstract InputStream getInputStream(String container, String remotePathFile) throws FileNotExistsException;
	public InputStream getInputStream(String remotePathFile) throws FileNotExistsException {
		return getInputStream(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile);
	}

	/**
	 * Retorna los metadatos de un archivo particular dentro del container
	 * “container”.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @return
	 */
	public abstract StorageObjectMetadata getMetadataForObject(String container, String remotePathFile);
	public StorageObjectMetadata getMetadataForObject(String remotePathFile) {
		return getMetadataForObject(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile);
	}

	/**
	 * Retorna una URI pública para el archivo deseado dentro de “container”
	 * 
	 * @param container
	 * @param remotePath
	 * @return
	 * @throws FileNotExistsException
	 * @throws MethodNotSupportedException
	 * @throws URLExtractionException
	 */
	public abstract URI getPublicURLForPath(String container, String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException;
	public URI getPublicURLForPath(String remotePath) throws FileNotExistsException, MethodNotSupportedException, URLExtractionException {
		return getPublicURLForPath(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Retorna un StorageObject completo con los datos del archivo apuntado por
	 * “remotePathFile. La respuesta contiene todos los metadatos del archivo
	 * más un InputStream por el cual se puede acceder a los datos.
	 * 
	 * @param container
	 * @param remotePathFile
	 * @return
	 * @throws FileNotExistsException
	 */
	public abstract StorageObject getStorageObject(String container, String remotePathFile) throws FileNotExistsException;
	public StorageObject getStorageObject(String remotePathFile) throws FileNotExistsException {
		return getStorageObject(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePathFile);
	}

	/**
	 * Retorna la fecha de última modificación del archivo “remotePath”
	 * contenido en “container”.
	 * 
	 * @param container
	 * @param remotePath
	 * @return
	 * @throws MetadataFetchingException
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 */
	public abstract Date lastModified(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;
	public Date lastModified(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		return lastModified(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Retorna una lista de todos los containers disponibles para el las
	 * credenciales configuradas por el usuario. Estos containers pueden
	 * utilizarse con el resto de los métodos de BaseStorageService.
	 * 
	 * @return
	 */
	public abstract List<StorageObjectMetadata> listContainers();

	/**
	 * Lista los archivos dentro de cierto directorio en un container. La
	 * búsqueda no es recursiva salvo que la variable “recursive” sea “true”. En
	 * caso de que el path a listar sea en realidad un archivo, se retornara el
	 * mismo archivo como resultado único. El resultado es una colección de
	 * StorageObjectMetadata que describen las propiedades de cada archivo o
	 * directorio listado.
	 * 
	 * @param container
	 * @param remotePath
	 * @param recursive
	 * @return
	 * @throws MethodNotSupportedException
	 */
	public abstract List<StorageObjectMetadata> listFiles(String container, String remotePath, boolean recursive) throws MethodNotSupportedException;
	public List<StorageObjectMetadata> listFiles(String remotePath, boolean recursive) throws MethodNotSupportedException {
		return listFiles(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath, recursive);
	}

	/**
	 * Migra recursivamente los datos del servicio actual, comenzando desde el
	 * directorio “startingPath”, hacia el servicio pasado como parámetro.
	 * 
	 * @param container
	 * @param startingPath
	 * @param target
	 * @param targetContainer
	 * @param targetPath
	 * @throws MigrationException
	 */
	public abstract void migrateData(String container, String startingPath, ExtendedStorageService target, String targetContainer, String targetPath) throws MigrationException;

	/**
	 * Mueve el archivo “from” del container “fromContainer” hacia el directorio
	 * “toDirectory” del container “toContainer”. Ambos containers pueden ser
	 * idénticos. Este método está compuesto de una operación “copy” y una
	 * operación “delete”.
	 * 
	 * @param fromContainer
	 * @param from
	 * @param toContainer
	 * @param toDirectory
	 * @throws MoveFileException
	 */
	public abstract void moveFile(String fromContainer, String from, String toContainer, String toDirectory) throws MoveFileException;

	/**
	 * Obtiene el tamaño en bytes del archivo referenciado por la variable
	 * remotePath en el container especificado.
	 * 
	 * @param container
	 * @param remotePath
	 * @return
	 * @throws MetadataFetchingException
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 */
	public abstract Long sizeOf(String container, String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException;
	public Long sizeOf(String remotePath) throws MetadataFetchingException, MethodNotSupportedException, FileNotExistsException {
		return sizeOf(this.getServiceProperty(StorageServiceConstants.CONTAINER), remotePath);
	}

	/**
	 * Método de acceso genérico al “put”. Realiza un “put” de un archivo o
	 * directorio completo dentro del container y directorio del servicio
	 * deseados.
	 * 
	 * @param localPath
	 * @param container
	 * @param remoteDirectory
	 * @throws UploadException
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 */
	public abstract void upload(File localPath, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;
	public void upload(File localPath, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		upload(localPath, this.getServiceProperty(StorageServiceConstants.CONTAINER), remoteDirectory);
	}

	/**
	 * Realiza un “put” de un directorio completo dentro del container y
	 * directorio del servicio deseados.
	 * 
	 * @param localDirectory
	 * @param container
	 * @param remoteDirectory
	 * @throws UploadException
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 */
	public abstract void uploadDirectory(File localDirectory, String container, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException;
	public void uploadDirectory(File localDirectory, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {		
		uploadDirectory(localDirectory, this.getServiceProperty(StorageServiceConstants.CONTAINER), remoteDirectory);
	}

	/**
	 * Realiza un “put” en el servicio de storage de un inputStream especifico
	 * en el directorio y container deseado. El stream pasado como parámetro
	 * puede tener cualquier origen, no necesariamente un archivo local.
	 * 
	 * @param stream
	 * @param container
	 * @param remoteDirectory
	 * @param filename
	 * @param contentLength
	 * @throws UploadException
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 */
	public abstract void uploadInputStream(InputStream stream, String container, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException;
	public void uploadInputStream(InputStream stream, String remoteDirectory, String filename, Long contentLength) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		uploadInputStream(stream, this.getServiceProperty(StorageServiceConstants.CONTAINER), remoteDirectory, filename, contentLength);
	}

	/**
	 * Realiza un “put” de un archivo local en el container y directorio
	 * específicos.
	 * 
	 * @param localFile
	 * @param container
	 * @param remoteDirectory
	 * @throws UploadException
	 * @throws MethodNotSupportedException
	 * @throws FileNotExistsException
	 */
	public abstract void uploadSingleFile(File localFile, String container, String remoteDirectory, String fileName) throws UploadException, MethodNotSupportedException, FileNotExistsException;
	public void uploadSingleFile(File localFile, String remoteDirectory) throws UploadException, MethodNotSupportedException, FileNotExistsException {
		uploadSingleFile(localFile, this.getServiceProperty(StorageServiceConstants.CONTAINER), remoteDirectory, localFile.getName());
	}
}