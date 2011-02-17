package com.tesis.aether.core.auth.authenticator;
import com.tesis.aether.core.auth.authenticable.Authenticable;
import com.tesis.aether.core.exception.AuthenticationException;
import com.tesis.aether.core.exception.InvalidAuthMethodException;
import com.tesis.aether.core.factory.ServiceAccountProperties;


public abstract class Authenticator {
	
	public abstract void authenticate(Authenticable auth, ServiceAccountProperties authData) throws AuthenticationException, InvalidAuthMethodException;
	
}
