/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.impl.s3;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import com.cloudloop.storage.CloudStore;
import com.cloudloop.storage.CloudStoreDirectory;
import com.cloudloop.storage.CloudStoreObject;
import com.cloudloop.storage.CloudStoreObjectType;
import com.mucommander.auth.AuthException;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileAttributes;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileOperation;
import com.mucommander.file.FilePermissions;
import com.mucommander.file.FileURL;
import com.mucommander.file.PermissionBits;
import com.mucommander.file.ProtocolFile;
import com.mucommander.file.UnsupportedFileOperation;
import com.mucommander.file.UnsupportedFileOperationException;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.runtime.JavaVersions;


/**
 * Super class of {@link S3Root}, {@link S3Bucket} and {@link S3Object}.
 *
 * @author Maxence Bernard
 */
public abstract class S3File extends ProtocolFile {

    protected CloudStore service;

    protected AbstractFile parent;
    protected boolean parentSet;

    protected S3File(FileURL url, CloudStore service) {
        super(url);

        this.service = service;
    }
    
    protected IOException getIOException(Exception e) throws IOException {
        return getIOException(e, fileURL);
    }

    protected static IOException getIOException(Exception e, FileURL fileURL) throws IOException {
        handleAuthException(e, fileURL);

        Throwable cause = e.getCause();
        if(cause instanceof IOException)
            return (IOException)cause;

        if(JavaVersions.JAVA_1_6.isCurrentOrHigher())
            return new IOException(e);

        return new IOException(e.getMessage());
    }

    protected static void handleAuthException(Exception e, FileURL fileURL) throws AuthException {
    	
    }
    
    protected AbstractFile[] listObjects(String bucketName, String prefix, S3File parent) throws IOException {
        try {
        	if ("".equals(prefix))
        		prefix = "/";
        	CloudStoreDirectory directory = service.getDirectory(prefix);
        	CloudStoreObject[] objects = directory.listContents(false);
//            if(objects.length==0 && !prefix.equals("")) {
//                // This happens only when the directory does not exist
//                throw new IOException();
//            }

            ArrayList <AbstractFile> files = new ArrayList<AbstractFile>();
            FileURL childURL;
            String objectKey;
            
        	for (CloudStoreObject object : objects) {
                // Discard the object corresponding to the prefix itself
                objectKey = object.getPath().getAbsolutePath();
                if(objectKey.equals(prefix))
                    continue;
                childURL = (FileURL)fileURL.clone();
                childURL.setPath(bucketName + objectKey);
                
                AbstractFile f = FileFactory.getFile(childURL, parent, service, object);
                files.add(f);
                //FALTA LA PARTE DE DIRECTORIOS Y ESPECIFICAR LOS ATRIBUTOS
                
//                directoryObject.setLastModifiedDate(new Date(System.currentTimeMillis()));
//                directoryObject.setContentLength(0);
//                children[i] = FileFactory.getFile(childURL, parent, service, directoryObject);
        		
			}

            return files.toArray(new AbstractFile[]{});
        }
        catch(Exception e) {
            throw getIOException(e);
        }
    }


    //////////////////////
    // Abstract methods //
    //////////////////////

    public abstract FileAttributes getFileAttributes();


    /////////////////////////////////
    // ProtocolFile implementation //
    /////////////////////////////////

    @Override
    public AbstractFile getParent() {
        if(!parentSet) {
            FileURL parentFileURL = this.fileURL.getParent();
            if(parentFileURL!=null) {
                try {
                    parent = FileFactory.getFile(parentFileURL, null, service);
                }
                catch(IOException e) {
                    // No parent
                }
            }

            parentSet = true;
        }

        return parent;
    }

    @Override
    public void setParent(AbstractFile parent) {
        this.parent = parent;
        this.parentSet = true;
    }


    // Delegates to FileAttributes

    @Override
    public long getDate() {
        return getFileAttributes().getDate();
    }

    @Override
    public long getSize() {
        return getFileAttributes().getSize();
    }

    @Override
    public boolean exists() {
        return getFileAttributes().exists();
    }

    @Override
    public boolean isDirectory() {
        return getFileAttributes().isDirectory();
    }

    @Override
    public FilePermissions getPermissions() {
        return getFileAttributes().getPermissions();
    }

    @Override
    public Object getUnderlyingFileObject() {
        return getFileAttributes();
    }
    

    // Unsupported operations, no matter the kind of resource (object, bucket, service)

    @Override
    public boolean isSymlink() {
        return false;
    }

    @Override
    public PermissionBits getChangeablePermissions() {
        return PermissionBits.EMPTY_PERMISSION_BITS;
    }

    @Override
    @UnsupportedFileOperation
    public void changePermission(int access, int permission, boolean enabled) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_PERMISSION);
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public boolean canGetGroup() {
        return false;
    }

    @Override
    @UnsupportedFileOperation
    public OutputStream getAppendOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.APPEND_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public RandomAccessOutputStream getRandomAccessOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_WRITE_FILE);
    }

    @Override
    @UnsupportedFileOperation
    public long getFreeSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_FREE_SPACE);
    }

    @Override
    @UnsupportedFileOperation
    public long getTotalSpace() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.GET_TOTAL_SPACE);
    }

    @Override
    @UnsupportedFileOperation
    public void changeDate(long lastModified) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CHANGE_DATE);
    }
}
