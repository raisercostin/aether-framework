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
import com.mucommander.auth.Credentials;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Jdk14Logger;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.BlobStoreContextFactory;
import org.jclouds.blobstore.domain.BlobMetadata;
import org.jclouds.blobstore.domain.StorageMetadata;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * A file protocol provider for the Amazon S3 protocol.
 *
 * @author Maxence Bernard
 */
public class S3ProtocolProvider implements ProtocolProvider {

//    static {
//        // Turn off Jets3t logging: failed (404) HEAD request on non-existing object are logged with a SEVERE level,
//        // even though this is not an error per se. We don't want those to be reported in the log, so we have no
//        // choice but to disable logging entirely.
//        ((Jdk14Logger)LogFactory.getLog(RestS3Service.class)).getLogger().setLevel(Level.INFO);
//    }

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        Credentials credentials = url.getCredentials();
        if(credentials==null || credentials.getLogin().equals("") || credentials.getPassword().equals(""))
            throw new AuthException(url);

    	BlobStoreContext s3Context;
    	BlobStore service;        
        String bucketName;

        if(instantiationParams.length==0) {
            try {
            	s3Context = new BlobStoreContextFactory().createContext("aws-s3", credentials.getLogin(), credentials.getPassword());
            	service = s3Context.getBlobStore();            	
            	
                //Jets3tProperties props = new Jets3tProperties();
                //props.setProperty("s3service.s3-endpoint", url.getHost());
            }
            catch(Exception e) {
                throw S3File.getIOException(e, url);
            }
        }
        else {
            service = (BlobStore)instantiationParams[0];
        }

        String path = url.getPath();

        // Root resource
        if(("/").equals(path))
            return new S3Root(url, service);

        // Fetch the bucket name from the URL
        StringTokenizer st = new StringTokenizer(path, "/");
        bucketName = st.nextToken();

        // Object resource
        if(st.hasMoreTokens()) {
            if(instantiationParams.length==2)
                return new S3Object(url, service, bucketName, (StorageMetadata)instantiationParams[1]);

            return new S3Object(url, service, bucketName);
        }

        //Folder resource
        if(instantiationParams.length==2){
            S3Bucket s3b = new S3Bucket(url, service, (StorageMetadata)instantiationParams[1]);
            //s3b.
        	return s3b;//new S3Bucket(url, service, (StorageMetadata)instantiationParams[1]);
        	//S3Object s3o = new S3Object(url, service, bucketName, (StorageMetadata)instantiationParams[1]);
        	//s3o.setDirectory(true);
        	//return s3o;
        }

        // Bucket resource
        return new S3Bucket(url, service, bucketName);
    }
}
