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
package com.squarecash4glass.servlet;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.mirror.Mirror;
import com.google.api.services.mirror.model.Account;
import com.google.api.services.mirror.model.AuthToken;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.Lists;
import com.googlecode.objectify.ObjectifyService;
import com.squarecash4glass.dto.User;
import com.squarecash4glass.util.AuthUtil;

/**
 * Handles POST requests from index.jsp
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
@SuppressWarnings("serial")
public class MainServlet extends HttpServlet {

  public void init() throws ServletException {
    ObjectifyService.register(User.class);
  }

  private static final Logger LOG = Logger.getLogger(MainServlet.class.getSimpleName());
  // ModulesService modulesApi = ModulesServiceFactory.getModulesService();

  /** Email of the Service Account */
  private static final String SERVICE_ACCOUNT_EMAIL = "135749034165-n37dmkvf02comatsgk51qrm5c25m5j3l@developer.gserviceaccount.com";

  /** Path to the Service Account's Private Key file */
  private static final String SERVICE_ACCOUNT_PKCS12_FILE_PATH = "WEB-INF/classes/SquareCash4Glass-2d4996020c1a.p12";

  /** The account type, usually based on your company or app's package. */
  private static final String ACCOUNT_TYPE_GOOGLE = "com.squarecash4glass.google";

  /** The account type, usually based on your company or app's package. */
  private static final String ACCOUNT_TYPE_SQUARE = "com.squarecash4glass.square";

  /** The Mirror API scopes needed to access the API. */
  private static final List<String> MIRROR_ACCOUNT_SCOPES = Arrays.asList(new String[] { "https://www.googleapis.com/auth/glass.thirdpartyauth" });;

  /**
   * Build and returns a Mirror service object authorized with the service
   * accounts.
   *
   * @return Mirror service object that is ready to make requests.
   */
  public static Mirror getMirrorService() throws GeneralSecurityException, IOException, URISyntaxException {
    HttpTransport httpTransport = new NetHttpTransport();
    JacksonFactory jsonFactory = new JacksonFactory();
    GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
        .setServiceAccountScopes(MIRROR_ACCOUNT_SCOPES).setServiceAccountPrivateKeyFromP12File(new java.io.File(SERVICE_ACCOUNT_PKCS12_FILE_PATH)).build();

    LOG.info("mirror service credentials: " + credential);

    Mirror service = new Mirror.Builder(httpTransport, jsonFactory, null).setHttpRequestInitializer(credential).setApplicationName("SquareCash4Glass").build();

    LOG.info("mirror service : " + service);

    return service;
  }

  /**
   * Creates an account and causes it to be synched up with the user's Glass.
   * This example only supports one auth token; modify it if you need to add
   * more than one, or to add features or user data or the password field.
   *
   * @param mirror
   *          the service returned by getMirrorService()
   * @param userToken
   *          the user token sent to your auth callback URL
   * @param accountName
   *          the account name for this particular user
   * @param authTokenType
   *          the type of the auth token (chosen by you)
   * @param authToken
   *          the auth token
   * @throws IOException
   */
  public static void createAccount(Mirror mirror, String userToken, String accountName, String authTokenType, String authToken) throws IOException {
    try {
      Account account = new Account();
      List<AuthToken> authTokens = Lists.newArrayList(new AuthToken().setType(authTokenType).setAuthToken(authToken));

      LOG.info("auth tokens: " + authToken);

      account.setAuthTokens(authTokens);

      LOG.info("account: " + account);
      LOG.info("accountName: " + accountName);
      LOG.info("userToken: " + userToken);

      mirror.accounts().insert(userToken, ACCOUNT_TYPE_GOOGLE, accountName, account).execute();
    } catch (IOException e) {
      LOG.log(Level.SEVERE, "error creating the account on mirror API", e);
      throw e;
    }
  }

  /**
   * Do stuff when buttons on index.jsp are clicked
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {

    // TODO get Token
    Credential credential = AuthUtil.getCredential(req);
    String userEmail = getUserEmail(credential);
    String userid = AuthUtil.getUserId(req);

    storeUserInfo(userid, userEmail);
    pushTokensToGlass(userEmail, credential, (String) req.getSession().getAttribute("authToken"));

  }

  private void storeUserInfo(String userid, String email) {
    User user = new User(userid, email);
    ofy().save().entity(user).now();
  }

  private void pushTokensToGlass(String email, Credential credential, String userToken) throws IOException {

    String accountName = email;
    String authTokenType = "cp";
    String authToken = credential.getAccessToken();
    Mirror mirrorService = null;

    try {
      mirrorService = getMirrorService();
    } catch (GeneralSecurityException e) {
      LOG.log(Level.SEVERE, "initializing mirror API", e);
      throw new IOException(e);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      LOG.log(Level.SEVERE, "initializing mirror API", e);
      throw new IOException(e);
    }
    createAccount(mirrorService, userToken, accountName, authTokenType, authToken);
  }

  private String getUserEmail(Credential credential) throws IOException {
    HttpTransport httpTransport = new UrlFetchTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    Plus.Builder pBuilder = new Plus.Builder(httpTransport, jsonFactory, credential);
    Plus plus = pBuilder.setApplicationName("SquareCash4Glass").build();

    Person mePerson = plus.people().get("me").execute();

    String userId = mePerson.getId();
    String email = mePerson.getEmails().get(0).getValue();

    LOG.info("userid and email: " + userId + " " + email);

    return email;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // TODO Auto-generated method stub
    doPost(req, resp);
  }
}
