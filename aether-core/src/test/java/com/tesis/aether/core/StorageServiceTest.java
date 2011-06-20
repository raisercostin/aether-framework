package com.tesis.aether.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.tesis.aether.core.factory.ServiceAccountProperties;
import com.tesis.aether.core.services.storage.ExtendedStorageService;
import com.tesis.aether.core.services.storage.constants.StorageServiceConstants;
import com.tesis.aether.core.services.storage.imp.local.LocalStorageService;
import com.tesis.aether.core.services.storage.object.StorageObject;
import com.tesis.aether.core.services.storage.object.StorageObjectMetadata;
import com.tesis.aether.core.services.storage.object.constants.StorageObjectConstants;

public abstract class StorageServiceTest {

	private ExtendedStorageService service;

	@BeforeClass
	public void initialize() {		
		service = getStorageService();
		try {
			service.connect(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public void finish() {		
		service = getStorageService();
		try {
			service.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	@BeforeMethod
	public void initializeMethod() {
		
		try {
			FileUtils.forceMkdir(new File("resources/TEST_FOLDER/"));
			new File("resources/test.1").createNewFile();
			new File("resources/TEST_FOLDER/test.2").createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	@AfterMethod
	public void cleanUpAfterMethod() {
		try {
			service.delete(getContainer(), "resources", true);
			service.delete(getContainer(), "resources1", true);
		} catch (Exception e) {
		}
		
		FileUtils.deleteQuietly(new File("resources/"));
	}
	
	@Test
	public void checkDirectoryExistsTest() {
				
		try {
			assert !service.checkDirectoryExists(getContainer(), "resources/TEST_FOLDER/");
			service.createFolder(getContainer(), "resources/TEST_FOLDER/");
			assert service.checkDirectoryExists(getContainer(), "resources/TEST_FOLDER/");
		} catch(Exception e) {
			assert false;
		}
	}
	
	@Test
	public void checkFileExistsTest() {
				
		try {
			assert !service.checkFileExists(getContainer(), "resources/TEST_FOLDER/test.2");
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			assert service.checkFileExists(getContainer(), "resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void checkObjectExistsTest() {
				
		try {
			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/");
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			assert service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void copyFileTest() {

		try {
			assert !service.checkObjectExists(getContainer(), "resources1");
			service.upload(new File("resources"), getContainer(), "");
			service.copyFile(getContainer(), "resources", getContainer(), "resources1");
			assert service.checkObjectExists(getContainer(), "resources1/resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists(getContainer(), "resources1/resources/test.1");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void createFolderTest() {
				
		try {
			assert !service.checkDirectoryExists(getContainer(), "resources/TEST_FOLDER/");
			service.createFolder(getContainer(), "resources/TEST_FOLDER/");
			assert service.checkDirectoryExists(getContainer(), "resources/TEST_FOLDER/");
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void deleteTest() {
				
		try {
			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/");
			
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			
			assert service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/");
			
			service.delete(getContainer(), "resources/TEST_FOLDER/", true);

			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/");
			
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void downloadDirectoryToDirectoryTest() {

		try {
			File localFile = new File("resources/Downloaded_1/");
			assert !localFile.exists();
			service.upload(new File("resources"), getContainer(), "");
			service.downloadDirectoryToDirectory(getContainer(), "resources", localFile);
			assert new File("resources/Downloaded_1/resources/test.1").exists();
			assert new File("resources/Downloaded_1/resources/TEST_FOLDER/test.2").exists();
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void downloadFileToDirectoryTest() {
		try {
			File localFile = new File("resources/Downloaded_2/test.2");
			assert !localFile.exists();
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			service.downloadFileToDirectory(getContainer(), "resources/TEST_FOLDER/test.2", new File("resources/Downloaded_2"));
			assert localFile.exists();
		} catch(Exception e) {
			assert false;
		}					
	}

	@Test
	public void downloadToDirectoryTest() {

		try {
			File localFile = new File("resources/Downloaded_1/");
			assert !localFile.exists();
			service.upload(new File("resources"), getContainer(), "");
			service.downloadToDirectory(getContainer(), "resources", localFile);
			assert new File("resources/Downloaded_1/resources/test.1").exists();
			assert new File("resources/Downloaded_1/resources/TEST_FOLDER/test.2").exists();
		} catch(Exception e) {
			assert false;
		}
		
		try {
			File localFile = new File("resources/Downloaded_2/test.2");
			assert !localFile.exists();
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			service.downloadToDirectory(getContainer(), "resources/TEST_FOLDER/test.2", new File("resources/Downloaded_2"));
			assert localFile.exists();
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void getInputStreamTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			InputStream inputStream = service.getInputStream(getContainer(), "resources/TEST_FOLDER/test.2");
			assert inputStream != null;			
			inputStream.close();
		} catch(Exception e) {
			assert false;
		}				
	}

	@Test
	public void getMetadataForObjectTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			StorageObjectMetadata metadataForObject = service.getMetadataForObject(getContainer(), "resources/TEST_FOLDER/test.2");
			assert metadataForObject != null;			
			assert metadataForObject.getLastModified() != null;
			assert metadataForObject.getLength() == 0;
			assert metadataForObject.getName().equals("test.2");
			assert metadataForObject.getPath().equals("resources/TEST_FOLDER");
			assert metadataForObject.getPathAndName().equals("resources/TEST_FOLDER/test.2");
			assert metadataForObject.getType().equals(StorageObjectConstants.FILE_TYPE);
			assert metadataForObject.getUri() != null;
			assert metadataForObject.getContainer().equals(getContainer());
			assert metadataForObject.getMd5hash() != null;
		} catch(Exception e) {
			assert false;
		}						
	}

	@Test
	public void getPublicURLForPathTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			assert service.getPublicURLForPath(getContainer(), "resources/TEST_FOLDER/test.2") != null;			
		} catch(Exception e) {
			assert false;
		}		
	}

	@Test
	public void getStorageObjectTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			StorageObject storageObject = service.getStorageObject(getContainer(), "resources/TEST_FOLDER/test.2");
			assert storageObject != null;		
			assert storageObject.getMetadata() != null;
			assert storageObject.getStream() != null;	
			storageObject.getStream().close();
		} catch(Exception e) {
			assert false;
		}						
	}

	@Test
	public void lastModifiedTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			assert service.lastModified(getContainer(), "resources/TEST_FOLDER/test.2") != null;			
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void listFilesTest() {
		
		try {
			assert !service.checkDirectoryExists(getContainer(), "resources");
			service.uploadDirectory(new File("resources"), getContainer(), "");
			assert service.listFiles(getContainer(), "resources", true).size() == 3;
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void migrateDataTest() {
		ServiceAccountProperties properties = new ServiceAccountProperties();
		String tempDirectory = System.getProperty("java.io.tmpdir") + "REMOTE_MOCK_MIGRATE\\";
		properties.putProperty(StorageServiceConstants.LOCAL_BASE_FOLDER, tempDirectory);		
		
		ExtendedStorageService migrateService = new LocalStorageService();
		migrateService.setServiceProperties(properties);

		try {
			assert !service.checkObjectExists(getContainer(), "resources");
			assert !migrateService.checkObjectExists(getContainer(), "resources");
			service.upload(new File("resources"), getContainer(), "");
			service.migrateData(getContainer(), "resources", migrateService, getContainer(), "");
			assert migrateService.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert migrateService.checkObjectExists(getContainer(), "resources/test.1");
			migrateService.delete(getContainer(), "resources", true);
		} catch(Exception e) {
			assert false;
		}
		
		new File(tempDirectory).delete();
	}

	@Test
	public void moveFileTest() {

		try {
			assert !service.checkObjectExists(getContainer(), "resources1");
			service.upload(new File("resources"), getContainer(), "");
			service.moveFile(getContainer(), "resources", getContainer(), "resources1");
			assert !service.checkObjectExists(getContainer(), "resources/TEST_FOLDER/test.2");
			assert !service.checkObjectExists(getContainer(), "resources/test.1");
			assert !service.checkObjectExists(getContainer(), "resources");
			assert service.checkObjectExists(getContainer(), "resources1/resources/TEST_FOLDER/test.2");
			assert service.checkObjectExists(getContainer(), "resources1/resources/test.1");
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void sizeOfTest() {
		try {
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			assert service.sizeOf(getContainer(), "resources/TEST_FOLDER/test.2") == 0;			
		} catch(Exception e) {
			assert false;
		}		
	}

	@Test
	public void uploadTest() {		
		try {
			assert !service.checkFileExists(getContainer(), "resources1/TEST_FOLDER/test.2");
			service.upload(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources1/TEST_FOLDER_2/");
			assert service.checkFileExists(getContainer(), "resources1/TEST_FOLDER_2/test.2");
		} catch(Exception e) {
			assert false;
		}	

		try {
			assert !service.checkDirectoryExists(getContainer(), "resources");
			service.upload(new File("resources"), getContainer(), "");
			assert service.checkFileExists(getContainer(), "resources/test.1");
			assert service.checkFileExists(getContainer(), "resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}
	}

	@Test
	public void uploadDirectoryTest() {
				
		try {
			assert !service.checkDirectoryExists(getContainer(), "resources");
			service.uploadDirectory(new File("resources"), getContainer(), "");
			assert service.checkFileExists(getContainer(), "resources/test.1");
			assert service.checkFileExists(getContainer(), "resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}
		
	}

	@Test
	public void uploadSingleFileTest() {				
		try {
			assert !service.checkFileExists(getContainer(), "resources/TEST_FOLDER/test.2");
			service.uploadSingleFile(new File("resources/TEST_FOLDER/test.2"), getContainer(), "resources/TEST_FOLDER/");
			assert service.checkFileExists(getContainer(), "resources/TEST_FOLDER/test.2");
		} catch(Exception e) {
			assert false;
		}		
	}
	
	@Test
	public void createDeleteContainerTest() {				
		try {
			String container = "tdevtesttemp2";
			service.createContainer(container);
			List<StorageObjectMetadata> listContainers = service.listContainers();
			assert existsContainer(container, listContainers);
			service.deleteContainer(container);
			listContainers = service.listContainers();
			assert !existsContainer(container, listContainers);
		} catch(Exception e) {
			assert false;
		}		
	}
	
	private boolean existsContainer(String container, List<StorageObjectMetadata> listContainers) {
		for (StorageObjectMetadata storageObjectMetadata : listContainers) {
			if(storageObjectMetadata.getName().equals(container)) {
				return true;
			}
		}
		return false;
	}

	protected abstract ExtendedStorageService getStorageService();
	
	protected abstract String getContainer();
}
