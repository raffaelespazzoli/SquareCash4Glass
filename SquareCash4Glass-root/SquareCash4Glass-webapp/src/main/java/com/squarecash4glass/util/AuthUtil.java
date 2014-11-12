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
import java.util.Collections;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.appengine.api.utils.SystemProperty;

/**
 * A collection of utility functions that simplify common authentication and
 * user identity tasks
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
public class AuthUtil {
	private static final Logger LOG = Logger.getLogger(AuthUtil.class
			.getSimpleName());
	// private static AuthUtil authUtil;
	private static final String jsonSecretFileName = "client_secret_135749034165-lf5m5cc24pmmfbkfqg0k00e7gmcbociq.apps.googleusercontent.com.json";
	private static final String GLASS_SCOPE = "https://www.googleapis.com/auth/glass.timeline "
			+ "https://www.googleapis.com/auth/userinfo.profile";
	private static final String CONTACTS_SCOPE = "https://www.googleapis.com/auth/contacts.readonly";
	private static final String USER_INFO_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
	private static DataStore<StoredCredential> ds;
	private static GoogleClientSecrets secrets;
	private static JacksonFactory jacksonFactory = new JacksonFactory();
	private static HttpTransport httpTransport = new NetHttpTransport();

	private static DataStore<StoredCredential> getCredeDataStore()
			throws IOException {
		if (ds == null) {
			ds = StoredCredential.getDefaultDataStore(AppEngineDataStoreFactory
					.getDefaultInstance());
		}
		return ds;
	}

	public static GoogleClientSecrets getSecrets() throws IOException {
		if (secrets == null) {
			secrets = GoogleClientSecrets.load(
					jacksonFactory,
					new InputStreamReader(AuthUtil.class
							.getResourceAsStream(jsonSecretFileName)));
		}
		return secrets;

	}
	
	public static GoogleCredential getCredentialFromToken(TokenResponse tokenResponse)
			throws IOException {
		GoogleCredential credential=new GoogleCredential.Builder().setTransport(httpTransport)
	      .setJsonFactory(jacksonFactory)
	      .setClientSecrets(AuthUtil.getSecrets())
	      .build()
	      .setFromTokenResponse(tokenResponse);
		return credential;
	}	

	// public static AuthUtil getAuthUtil() throws IOException {
	// if (authUtil == null) {
	// authUtil = new AuthUtil();
	// }
	// return authUtil;
	// }

	/**
	 * Creates and returns a new {@link AuthorizationCodeFlow} for this app.
	 */
	public static AuthorizationCodeFlow newAuthorizationCodeFlow()
			throws IOException {
		// AuthUtil authUtil = AuthUtil.getAuthUtil();

		//LOG.info("google client secrets: " + getSecrets());

		return new GoogleAuthorizationCodeFlow.Builder(httpTransport,
				jacksonFactory, getSecrets(),
				Collections.singleton(CONTACTS_SCOPE + " " + USER_INFO_SCOPE))
				.setAccessType("offline")
				.setCredentialDataStore(getCredeDataStore()).build();
	}

	/**
	 * Get the current user's ID from the session
	 * 
	 * @return string user id or null if no one is logged in
	 */
	public static String getUserId(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return (String) session.getAttribute("userId");
	}

	public static void setUserId(HttpServletRequest request, String userId) {
		HttpSession session = request.getSession();
		session.setAttribute("userId", userId);
	}

	public static Credential getCredential(String userId) throws IOException {
		if (userId == null) {
			return null;
		} else {
			return AuthUtil.newAuthorizationCodeFlow().loadCredential(userId);
		}
	}

	public static Credential getCredential(HttpServletRequest req)
			throws IOException {
		return AuthUtil.newAuthorizationCodeFlow().loadCredential(
				getUserId(req));
	}
}
