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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.dasein.cloud.storage.BlobStoreSupport;
import org.dasein.cloud.storage.CloudStoreObject;

import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileAttributes;
import com.mucommander.file.FileFactory;
import com.mucommander.file.FileOperation;
import com.mucommander.file.FilePermissions;
import com.mucommander.file.FileURL;
import com.mucommander.file.SimpleFileAttributes;
import com.mucommander.file.SimpleFilePermissions;
import com.mucommander.file.UnsupportedFileOperation;
import com.mucommander.file.UnsupportedFileOperationException;
import com.mucommander.io.RandomAccessInputStream;

/**
 * <code>S3Root</code> represents the Amazon S3 root resource, also known as 'service'.
 *
 * @author Maxence Bernard
 */
public class S3Root extends S3File {

    private SimpleFileAttributes atts;

    /** Default permissions for the S3 root */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(448);   // rwx------
    protected S3Root(FileURL url, BlobStoreSupport service) {
        super(url, service);
        atts = new SimpleFileAttributes();
        atts.setPath(url.getPath());
        atts.setExists(true);
        atts.setDate(0);
        atts.setSize(0);
        atts.setDirectory(true);
        atts.setPermissions(DEFAULT_PERMISSIONS);
        atts.setOwner(null);
        atts.setGroup(null);
    }


    ///////////////////////////
    // S3File implementation //
    ///////////////////////////

    @Override
    public FileAttributes getFileAttributes() {
        return atts;
    }


    /////////////////////////////////
    // ProtocolFile implementation //
    /////////////////////////////////

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public boolean canGetOwner() {
        return false;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
    	String bucketName = atts.getPath();
    	if (bucketName.startsWith("/"))
    		bucketName = bucketName.substring(1);
    	loadFileList(bucketName);
    	ArrayList<CloudStoreObject> listCso = files.get(bucketName);
        FileURL bucketURL;
        ArrayList<AbstractFile> bucketFiles = new ArrayList<AbstractFile>();
        for(CloudStoreObject object : listCso) {
        	if (object.getDirectory() == bucketName && "".equals(object.getName())) 
        		continue;
            bucketURL = (FileURL)fileURL.clone();
            bucketURL.setPath(object.getName());
            object.setName(bucketName);
            bucketFiles.add(FileFactory.getFile(bucketURL, null, service, object));
        }
        return bucketFiles.toArray(new AbstractFile[]{});
    	
    	//    	CloudStoreObject cso = new CloudStoreObject();
//    	cso.setName("tesismarcos");
//    	
//    	
//    	return new AbstractFile[] {FileFactory.getFile((FileURL)fileURL.clone(), null, service, cso)};
    	
//        try {
//        	org.dasein.cloud.platform.CDNSupport a = new org.dasein.cloud.platform.
//        	a.list();
        	
        	
//        	
//        	String bucketName = atts.getPath();
//        	if (bucketName.startsWith("/"))
//        		bucketName = bucketName.substring(1);
//            Iterable<CloudStoreObject> buckets = service.listFiles(bucketName);
//            ArrayList<AbstractFile> bucketFiles = new ArrayList<AbstractFile>();
//            FileURL bucketURL;
//            for(CloudStoreObject object : buckets) {
//                bucketURL = (FileURL)fileURL.clone();
//                bucketURL.setPath(bucketName+"/"+object.getName());
//                object.setName(bucketName);
//
//                bucketFiles.add(FileFactory.getFile(bucketURL, null, service, object));
//            }
//
//            return bucketFiles.toArray(new AbstractFile[]{});
//        }
//        catch(Exception e) {
//            throw getIOException(e);
//        }
    }

    // Unsupported operations

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public void mkdir() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.CREATE_DIRECTORY);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public InputStream getInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.READ_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public OutputStream getOutputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.WRITE_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public RandomAccessInputStream getRandomAccessInputStream() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RANDOM_READ_FILE);
    }

    /**
     * Always throws an {@link UnsupportedFileOperationException}.
     */
    @Override
    @UnsupportedFileOperation
    public void delete() throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.DELETE);
    }

    @Override
    @UnsupportedFileOperation
    public void copyRemotelyTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.COPY_REMOTELY);
    }

    @Override
    @UnsupportedFileOperation
    public void renameTo(AbstractFile destFile) throws UnsupportedFileOperationException {
        throw new UnsupportedFileOperationException(FileOperation.RENAME);
    }
}
