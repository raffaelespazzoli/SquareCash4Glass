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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.squarecash4glass.dto.User;
import com.squarecash4glass.util.OAuth2Util;
import com.squarecash4glass.util.Oauth2Factory;

/**
 * This servlet manages the OAuth 2.0 dance
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
@SuppressWarnings("serial")
public class OAuth2AuthorizationServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(OAuth2AuthorizationServlet.class.getSimpleName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    String provider = getProvider(req);
    LOG.info("in oauth flow for: " + provider);
    Configuration oauthConfiguration;
    try {
      oauthConfiguration = Oauth2Factory.getOauth2Configuration(provider, "sandbox");
    } catch (ConfigurationException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      throw new ServletException(e1);
    }
    OAuth2Util oAuth2Util = Oauth2Factory.getOauth2Util(oauthConfiguration);
    if (req.getParameter("error") != null) {
      LOG.severe("Something went wrong during auth: " + req.getParameter("error"));
      res.setContentType("text/plain");
      res.getWriter().write("Something went wrong during auth. Please check your log for details");
      return;
    }

    // If we have a code, finish the OAuth 2.0 dance
    if (req.getParameter("code") != null) {
      LOG.info("Got a code. Attempting to exchange for access token.");

      TokenResponse tokenResponse = null;
      AuthorizationCodeFlow flow = oAuth2Util.newAuthorizationCodeFlow();

      try {

        tokenResponse = flow.newTokenRequest(req.getParameter("code")).setRedirectUri(WebUtil.buildUrl(req, oauthConfiguration.getString("oauthcallback"))).execute();

      } catch (TokenResponseException e) {
        LOG.severe("exception getting token: " + e.getMessage());
        throw e;
      }

      LOG.info("tokenResponse: " + tokenResponse);

      if ("google".equals(provider)) {
        // Extract the Google User ID from the ID token in the auth response
        Payload googlePayload=((GoogleTokenResponse) tokenResponse).parseIdToken().getPayload();
        String userId = googlePayload.getSubject();
        String email=googlePayload.getEmail();
        req.getSession().setAttribute("userId", userId);
        User user=new User(userId,email);
        ofy().save().entities(user).now();
        LOG.info("Code exchange worked. User " + userId + " logged in.");
      }

      String userid = OAuth2Util.getUserId(req);
      flow.createAndStoreCredential(tokenResponse, userid + oauthConfiguration.getString("useridPostfix"));

      // Redirect back to index
      res.sendRedirect(WebUtil.buildUrl(req, "/main"));
      return;
    }

    // Else, we have a new flow. Initiate a new flow.
    LOG.info("No auth context found. Kicking off a new dwolla auth flow.");

    AuthorizationCodeFlow flow = oAuth2Util.newAuthorizationCodeFlow();
    GenericUrl url = flow.newAuthorizationUrl().setRedirectUri(WebUtil.buildUrl(req, oauthConfiguration.getString("oauthcallback")));
    adjustURL(provider, url);
    LOG.info("redirecting to URL: " + url.build());
    res.sendRedirect(url.build());
  }

  /**
   * @param provider
   * @param url
   */
  private void adjustURL(String provider, GenericUrl url) {
    // manage some specific configuration settings
    if ("dwolla".equals(provider)) {
      url.set("request_type", "code");
      url.set("scope", "send");
    }
    if ("google".equals(provider)) {
      url.set("approval_prompt", "force");
    }
    if ("square".equals(provider)) {
      url.set("request_type", "code");
      url.set("state", "ciao");
    }
  }

  /**
   * @param req
   * @param provider
   * @return
   */
  private String getProvider(HttpServletRequest req) {
    LOG.info("getServletPath: " + req.getServletPath());
    if ("/oauth2callbackdwolla".equals(req.getServletPath())) {
      return "dwolla";
    }
    if ("/oauth2callbacksquare".equals(req.getServletPath())) {
      return "square";
    }
    if ("/oauth2callbackvenmo".equals(req.getServletPath())) {
      return "venmo";
    }
    if ("/oauth2callback".equals(req.getServletPath())) {
      return "google";
    }
    return null;
  }
}
