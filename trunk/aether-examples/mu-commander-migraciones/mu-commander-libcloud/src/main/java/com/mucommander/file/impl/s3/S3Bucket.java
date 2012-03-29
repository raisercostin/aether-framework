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
import java.util.Map;

import simplecloud.storage.interfaces.IStorageAdapter;

import com.mucommander.auth.AuthException;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileAttributes;
import com.mucommander.file.FileLogger;
import com.mucommander.file.FileOperation;
import com.mucommander.file.FilePermissions;
import com.mucommander.file.FileURL;
import com.mucommander.file.SimpleFilePermissions;
import com.mucommander.file.SyncedFileAttributes;
import com.mucommander.file.UnsupportedFileOperation;
import com.mucommander.file.UnsupportedFileOperationException;
import com.mucommander.io.RandomAccessInputStream;

/**
 * <code>S3Bucket</code> represents an Amazon S3 bucket.
 *
 * @author Maxence Bernard
 */
public class S3Bucket extends S3File {

//    private String bucketName;
    private S3BucketFileAttributes atts;

    // TODO: add support for ACL ? (would cost an extra request per bucket)
    /** Default permissions for S3 buckets */
    private final static FilePermissions DEFAULT_PERMISSIONS = new SimpleFilePermissions(448);   // rwx------


    protected S3Bucket(FileURL url, IStorageAdapter service, String bucketName) throws AuthException {
        super(url, service, bucketName);
        atts = new S3BucketFileAttributes();
    }

    protected S3Bucket(FileURL url, IStorageAdapter service, String bucketName, Map<String, String> object) throws AuthException {
        super(url, service, bucketName);
        atts = new S3BucketFileAttributes();
    }

//    protected S3Bucket(FileURL url, S3Adapter service,  Map<String, String> bucket) throws AuthException {
//        super(url, service);
//
//        this.bucketName = bucket;
//        atts = new S3BucketFileAttributes(bucket);
//    }


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
        return atts.getOwner();
    }

    @Override
    public boolean canGetOwner() {
        return true;
    }

    @Override
    public AbstractFile[] ls() throws IOException {
        return listObjects(bucketName, "/", this);
    }

    @Override
    public void delete() throws IOException {
        throw new IOException("Operation not supported.");
    }

    @Override
    public void mkdir() throws IOException {
        throw new IOException("Operation not supported.");
    }


    // Unsupported operations

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


    ///////////////////
    // Inner classes //
    ///////////////////

    /**
     * S3BucketFileAttributes provides getters and setters for S3 bucket attributes. By extending
     * <code>SyncedFileAttributes</code>, this class caches attributes for a certain amount of time
     * after which fresh values are retrieved from the server.
     *
     * @author Maxence Bernard
     */
    private class S3BucketFileAttributes extends SyncedFileAttributes {

        private final static int TTL = 60000;

        private S3BucketFileAttributes() throws AuthException {
            super(TTL, false);      // no initial update

            fetchAttributes();      // throws AuthException if no or bad credentials
            updateExpirationDate(); // declare the attributes as 'fresh'
        }

        private void setAttributes() {
    		setDirectory(true);
      		setDate(System.currentTimeMillis());
            setPermissions(DEFAULT_PERMISSIONS);
            setOwner(null);
        }

        private void fetchAttributes() throws AuthException {
            Exception e = null;
            boolean exist = false;
            try {
                // Note: unlike getObjectDetails, getBucket returns null when the bucket does not exist
                // (that is because the corresponding request is a GET on the root resource, not a HEAD on the bucket).
    			if (files == null)
    				loadFileList(bucketName);
            	
    			exist = files.size()>0;
    			
            } catch(Exception ex) {
                e = ex;
            }

            if(exist) {
                // Bucket exists
                setExists(true);
                setAttributes();
            }
            else {
                // Bucket doesn't exist on the server, or could not be retrieved
                setExists(false);

                setDirectory(false);
                setDate(0);
                setPermissions(FilePermissions.EMPTY_FILE_PERMISSIONS);
                setOwner(null);

                if(e!=null)
                    throw new AuthException(fileURL, e.getMessage());
            }
        }


        /////////////////////////////////////////
        // SyncedFileAttributes implementation //
        /////////////////////////////////////////

        @Override
        public void updateAttributes() {
            try {
                fetchAttributes();
            }
            catch(Exception e) {        // AuthException
                FileLogger.fine("Failed to update attributes", e);
            }
        }
    }
}
