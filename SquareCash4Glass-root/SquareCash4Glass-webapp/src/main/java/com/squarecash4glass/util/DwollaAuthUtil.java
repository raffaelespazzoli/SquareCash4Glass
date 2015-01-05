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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
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
public class DwollaAuthUtil {
  private static final Logger LOG = Logger.getLogger(DwollaAuthUtil.class.getSimpleName());
  // private static AuthUtil authUtil;
  private final static String secret = "/VFDSCtzCuQtuWWQGxmsOKWE4peNH/ChTaib357kTRG9MBGyGA";
  private final static String applicationId = "Ts+36ymI6m/5ANzdjdtY7HWGkjohGpmaRCGyeYKg7kELT8lIUW";
  private final static String tokenEndpointURI = "/oauth/v2/token";
  private final static String codeEndpointURI = "/oauth/v2/authenticate";
  private final static String prodURL = "https://www.dwolla.com";
  private final static String sandboxURL = "https://uat.dwolla.com/";
  private final static Collection<String> scopes = Arrays.asList("send");
  private static JacksonFactory jacksonFactory = new JacksonFactory();
  private static HttpTransport httpTransport = new NetHttpTransport();
  private static DataStore<StoredCredential> ds;

  public static SquareAuthBean getSquareAuthBean() {
    return new SquareAuthBean(sandboxURL, tokenEndpointURI, DwollaAuthUtil.scopes.toArray()[0].toString(), applicationId);
  }

  private static DataStore<StoredCredential> getCredentialDataStore() throws IOException {
    if (ds == null) {
      ds = StoredCredential.getDefaultDataStore(AppEngineDataStoreFactory.getDefaultInstance());
    }
    return ds;
  }

  public static Credential getCredentialFromToken(TokenResponse tokenResponse) throws IOException {
    Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(httpTransport).setJsonFactory(jacksonFactory)
        .setTokenServerUrl(new GenericUrl(sandboxURL + tokenEndpointURI)).setClientAuthentication(new ClientParametersAuthentication(applicationId, secret)).build()
        .setFromTokenResponse(tokenResponse);
    return credential;
  }


  public static Credential getCredentialFromStore(String userId) throws IOException {
    Credential credential=getCredential(userId);
    if (new Date().getTime() > credential.getExpirationTimeMilliseconds()){
      credential.refreshToken();
    }
    return credential;
  }
  
  public static Credential getCredential(String userId) throws IOException {
    if (userId == null) {
      return null;
    } else {
      return newAuthorizationCodeFlow().loadCredential(userId);
    }
  }
  

  /**
   * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
   */
  public static AuthorizationCodeFlow newAuthorizationCodeFlow() throws IOException {
    // AuthUtil authUtil = AuthUtil.getAuthUtil();

    // LOG.info("google client secrets: " + getSecrets());

    // AuthorizationCodeFlow.Builder(Credential.AccessMethod method,
    // com.google.api.client.http.HttpTransport transport,
    // com.google.api.client.json.JsonFactory jsonFactory,
    // com.google.api.client.http.GenericUrl tokenServerUrl,
    // com.google.api.client.http.HttpExecuteInterceptor
    // clientAuthentication,
    // String clientId,
    // String authorizationServerEncodedUrl)

    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(), httpTransport, jacksonFactory, new GenericUrl(sandboxURL + tokenEndpointURI),
        new ClientParametersAuthentication(applicationId, secret), applicationId, new GenericUrl(sandboxURL + codeEndpointURI).toString()).setScopes(scopes)
        .setCredentialDataStore(getCredentialDataStore()).build();
  }

  /**
   * Get the current user's ID from the session
   * 
   * @return string user id or null if no one is logged in
   */
  // public static String getUserId(HttpServletRequest request) {
  // HttpSession session = request.getSession();
  // return (String) session.getAttribute("userId");
  // }
  //
  // public static void setUserId(HttpServletRequest request, String userId) {
  // HttpSession session = request.getSession();
  // session.setAttribute("userId", userId);
  // }
  //
  // public static Credential getCredential(String userId) throws IOException {
  // if (userId == null) {
  // return null;
  // } else {
  // return SquareAuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
  // }
  // }
  //
  // public static Credential getCredential(HttpServletRequest req)
  // throws IOException {
  // return SquareAuthUtil.newAuthorizationCodeFlow().loadCredential(
  // getUserId(req));
  // }
}
