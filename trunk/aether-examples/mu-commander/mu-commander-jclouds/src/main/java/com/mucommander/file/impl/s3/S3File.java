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

import com.mucommander.auth.AuthException;
import com.mucommander.file.*;
import com.mucommander.io.RandomAccessOutputStream;
import com.mucommander.runtime.JavaVersions;

import org.apache.commons.io.FilenameUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.internal.StorageMetadataImpl;
import org.jclouds.blobstore.options.ListContainerOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Super class of {@link S3Root}, {@link S3Bucket} and {@link S3Object}.
 *
 * @author Maxence Bernard
 */
public abstract class S3File extends ProtocolFile {

    protected BlobStore service;

    protected AbstractFile parent;
    protected boolean parentSet;

    protected S3File(FileURL url, BlobStore service) {
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
        //int code = e.getResponseCode();
        //if(code==401 || code==403)
        //    throw new AuthException(fileURL);
    }
    
    protected AbstractFile[] listObjects(String bucketName, String prefix, S3File parent) throws IOException {
        try {
        	if (prefix != null && !"".equals(prefix.trim())) {
        		if (prefix.endsWith("/") && prefix.length() > 1) {
        			prefix = prefix.substring(0, prefix.length() - 1);
        		}
        		String name = FilenameUtils.getName(prefix);
				String path = FilenameUtils.getPathNoEndSeparator(prefix);     
				prefix = path + (!"".equals(path)?"/":"") + name;
			}
        	else
        		prefix = "";
        
            PageSet<? extends StorageMetadata> chunk;
            if ("".equals(prefix))
            	chunk = service.list(bucketName);//, prefix, "/", Constants.DEFAULT_OBJECT_LIST_CHUNK_SIZE, null, true);
            else
            	chunk = service.list(bucketName, ListContainerOptions.Builder.inDirectory(prefix));
            
            StorageMetadata objects[] = chunk.toArray(new StorageMetadata[]{});
            
            if(objects.length==0 && !"".equals(prefix)) {
                // This happens only when the directory does not exist
                throw new IOException();
            }

            ArrayList <AbstractFile> children = new ArrayList<AbstractFile>();//+commonPrefixes.length];
            FileURL childURL;
            String objectKey;
            StorageMetadata directoryObject;
            for(StorageMetadata object : objects) {
                // Discard the object corresponding to the prefix itself
                objectKey = object.getName();

                String name = FilenameUtils.getName(objectKey);
				String path = FilenameUtils.getPathNoEndSeparator(objectKey);     
				String next = path + (!"".equals(path)?"/":"") + name;

                if(next.equals(prefix))
                    continue;
                
                if (object.getType().equals(StorageType.BLOB)){ //.CONTAINER) || object.getType().equals(StorageType.FOLDER)){
                    childURL = (FileURL)fileURL.clone();
                    childURL.setPath(bucketName + "/" + objectKey);

                    children.add(FileFactory.getFile(childURL, parent, service, object));
                } else { //en este punto el objeto corresponde a una carpeta
                    childURL = (FileURL)fileURL.clone();
                    childURL.setPath(bucketName + "/" + objectKey);

                    directoryObject = new StorageMetadataImpl(StorageType.CONTAINER, null, objectKey, object.getLocation(), object.getUri(),
                    		object.getETag(), new Date(System.currentTimeMillis()), object.getUserMetadata());
                    // Common prefixes are not objects per se, and therefore do not have a date, content-length nor owner.
                    //directoryObject..setContentLength(0);
                    children.add(FileFactory.getFile(childURL, parent, service, directoryObject));
                }
            }

//            StorageMetadata directoryObject;
//            for(String commonPrefix : commonPrefixes) {
//            }

            // Trim the array if an object was discarded.
            // Note: Having to recreate an array sucks (puts pressure on the GC), but I haven't found a reliable way
            // to know in advance whether the prefix will appear in the results or not.
//            if(i<children.length) {
//                AbstractFile[] childrenTrimmed = new AbstractFile[i];
//                System.arraycopy(children, 0, childrenTrimmed, 0, i);
//
//                return childrenTrimmed;
//            }

            return children.toArray(new AbstractFile[]{});
        }
        catch(Exception e) {
        	Logger.getLogger("default").info("Error e: " + e);
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
