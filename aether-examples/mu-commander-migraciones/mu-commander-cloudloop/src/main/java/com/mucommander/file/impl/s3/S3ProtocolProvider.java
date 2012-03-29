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
import java.util.StringTokenizer;

import com.cloudloop.Cloudloop;
import com.cloudloop.generated.AdapterType;
import com.cloudloop.generated.CloudloopConfig;
import com.cloudloop.generated.PropertyType;
import com.cloudloop.generated.CloudloopConfig.Adapters;
import com.cloudloop.generated.CloudloopConfig.Stores;
import com.cloudloop.generated.CloudloopConfig.Stores.Store;
import com.cloudloop.storage.CloudStore;
import com.cloudloop.storage.CloudStoreObject;
import com.mucommander.auth.AuthException;
import com.mucommander.auth.Credentials;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.FileURL;
import com.mucommander.file.ProtocolProvider;

/**
 * A file protocol provider for the Amazon S3 protocol.
 *
 * @author Maxence Bernard
 */
public class S3ProtocolProvider implements ProtocolProvider {
	private static CloudStore service = null;

    public AbstractFile getFile(FileURL url, Object... instantiationParams) throws IOException {
        Credentials credentials = url.getCredentials();
        if(credentials==null || credentials.getLogin().equals("") || credentials.getPassword().equals(""))
            throw new AuthException(url);

        String bucketName;

        if(instantiationParams.length==0 && service == null) {
            try {
            	CloudloopConfig cc = new CloudloopConfig();
            	
            	Adapters.Adapter ad = new Adapters.Adapter();
            	ad.setImpl("com.cloudloop.storage.adapter.nirvanix.NirvanixCloudStore");
            	ad.setName("nirvanix");
            	ad.setType(AdapterType.STORAGE);

            	Adapters a = new Adapters();
            	a.getAdapter().add(ad);
            	
               	Store s2 = new Store();
            	s2.setAdapter("nirvanix");
            	s2.setEncrypted(false);
            	s2.setName("amazon");
            	
            	PropertyType pt1 = new PropertyType();
            	pt1.setName("user-login");
            	pt1.setValue(credentials.getLogin());
            	s2.getProperty().add(pt1);
            	
            	PropertyType pt2 = new PropertyType();
            	pt2.setName("user-password");
            	pt2.setValue(credentials.getPassword());
            	s2.getProperty().add(pt2);
            	
            	PropertyType pt3 = new PropertyType();
            	pt3.setName("app-key");
            	pt3.setValue(url.getFilename());
            	s2.getProperty().add(pt3);
            	
            	PropertyType pt4 = new PropertyType();
            	pt4.setName("app-name");
            	pt4.setValue("APP_NAME");
            	s2.getProperty().add(pt4);

            	Stores s = new Stores();
            	
            	s.getStore().add(s2);
            	s.setDefaultStore(s2.getName());
            	
            	cc.setAdapters(a);
            	cc.setEncryption(null);
            	cc.setStores(s);
            	
            	Cloudloop c = new Cloudloop(cc);
            	
            	service = c.getStorage("amazon");
    			
            }
            catch(Exception e) {
                throw new AuthException(url);
            }
        }
//        else {
//            service = (CloudStore)instantiationParams[0];
//        }

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
                return new S3Object(url, service, bucketName, (CloudStoreObject)instantiationParams[1]);

            return new S3Object(url, service, bucketName);
        }

        // Bucket resource
        if(instantiationParams.length==2)
            return new S3Bucket(url, service, (CloudStoreObject)instantiationParams[1], bucketName);

        return new S3Bucket(url, service, bucketName);
    }
}
