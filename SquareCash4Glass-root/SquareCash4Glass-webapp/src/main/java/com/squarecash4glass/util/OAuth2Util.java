/*
 * Copyright (C) 2013 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.squarecash4glass.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;

/**
 * A collection of utility functions that simplify common authentication and
 * user identity tasks
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class OAuth2Util {
  private static final Logger LOG = Logger.getLogger(OAuth2Util.class.getSimpleName());

  private JacksonFactory jacksonFactory = new JacksonFactory();
  private HttpTransport httpTransport = new NetHttpTransport();
  private DataStore<StoredCredential> ds;
  
  private Configuration configuration;

  public OAuth2Util (Configuration configuration) throws IOException{
    this.configuration=configuration;
    ds = StoredCredential.getDefaultDataStore(AppEngineDataStoreFactory.getDefaultInstance());
  }
  
  public Credential getCredentialFromToken(TokenResponse tokenResponse) throws IOException {
    if ("secret".equals(configuration.getString("clientAuthorizationType"))){
      return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(httpTransport).setJsonFactory(jacksonFactory)
          .setTokenServerUrl(new GenericUrl(configuration.getString("url") + configuration.getString("tokenEndpoint"))).setClientAuthentication(new ClientParametersAuthentication(configuration.getString("applicationId"),configuration.getString("secret"))).build()
          .setFromTokenResponse(tokenResponse);
    }    
    if ("secret_file".equals(configuration.getString("clientAuthorizationType"))){
      return new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jacksonFactory).setClientSecrets(getSecrets()).build()
          .setFromTokenResponse(tokenResponse);
    }   
    return null;
  }


  public Credential getCredentialFromStore(String userId) throws IOException {
    Credential credential=getCredential(userId);
    if (credential !=null && new Date().getTime() > credential.getExpirationTimeMilliseconds()){
      credential.refreshToken();
    }
    return credential;
  }
  
  private Credential getCredential(String userId) throws IOException {
    if (userId == null) {
      return null;
    } else {
      return newAuthorizationCodeFlow().loadCredential(userId);
    }
  }
  

  /**
   * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
   */
  public AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {


    
    if ("secret".equals(configuration.getString("clientAuthorizationType"))){
      return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), httpTransport, jacksonFactory, new GenericUrl(configuration.getString("url") + configuration.getString("tokenEndpoint")),
          new ClientParametersAuthentication(configuration.getString("applicationId"),configuration.getString("secret")), configuration.getString("applicationId"), new GenericUrl(configuration.getString("url") + configuration.getString("codeEndpoint")).toString()).setScopes(Arrays.asList(configuration.getStringArray("scopes")))
          .setCredentialDataStore(ds).build();
    }
    
    if ("secret_file".equals(configuration.getString("clientAuthorizationType"))){
      return new GoogleAuthorizationCodeFlow.Builder(httpTransport, jacksonFactory, getSecrets()
          ,Arrays.asList(configuration.getStringArray("scopes")))
          .setAccessType("offline")
          .setCredentialDataStore(ds).build();
    }
    
    return null;
    

  }
  
  private GoogleClientSecrets getSecrets() throws IOException {
    return GoogleClientSecrets.load(jacksonFactory, new InputStreamReader(OAuth2Util.class.getResourceAsStream(configuration.getString("jsonSecretFileName"))));
  }
  
  public static String getUserId(HttpServletRequest request) {
    HttpSession session = request.getSession();
    return (String) session.getAttribute("userId");
  }

}
