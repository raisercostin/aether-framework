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

import com.mucommander.file.*;
import com.mucommander.io.RandomAccessInputStream;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * <code>S3Root</code> represents the Amazon S3 root resource, also known as 'service'.
 *
 * @author Maxence Bernard
 */
public class S3Root extends S3File {

    private SimpleFileAttributes atts;

    /** Default permissions for the S3 root */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(448);   // rwx------

    protected S3Root(FileURL url, BlobStore service) {
        super(url, service);

        atts = new SimpleFileAttributes();
        atts.setPath("/");
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
        try {
        	PageSet<? extends StorageMetadata> listBuckets = service.list();
        	StorageMetadata buckets[] = listBuckets.toArray(new StorageMetadata[]{});
        	
            int nbBuckets = buckets.length;
            ArrayList<AbstractFile> bucketFiles = new ArrayList<AbstractFile>();
            FileURL bucketURL;
            for(int i=0; i<nbBuckets; i++) {
                try {
	            	if (buckets[i].getType().equals(StorageType.CONTAINER)) {
		                bucketURL = (FileURL)fileURL.clone();
		                bucketURL.setPath("/"+buckets[i].getName());
		                	AbstractFile af = FileFactory.getFile(bucketURL, null, service, buckets[i]);
			                bucketFiles.add(i, af);
	            	}
                } catch (Exception e) {
                	Logger.getAnonymousLogger().info(e.getMessage() + ": " + buckets[i].getName());
                	e.printStackTrace();
                }

            }

            return bucketFiles.toArray(new AbstractFile[]{});
        }
        catch(Exception e) {
            throw getIOException(e);
        }
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
