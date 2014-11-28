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

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.squarecash4glass.util.AuthUtil;
import com.squarecash4glass.util.SquareAuthUtil;

/**
 * This servlet manages the OAuth 2.0 dance
 * 
 * @author Jenny Murphy - http://google.com/+JennyMurphy
 */
@SuppressWarnings("serial")
public class SquareAuthServlet extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(SquareAuthServlet.class.getSimpleName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    LOG.info("in square oauth flow");
    manageSquareOauth(req, res);
    // res.sendRedirect(WebUtil.buildUrl(req, "/main"));
  }

  /**
   * @param req
   * @param res
   * @throws IOException
   * @throws ServletException
   */
  private void manageSquareOauth(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
    // If something went wrong, log the error message.
    if (req.getParameter("error") != null) {
      LOG.severe("Something went wrong during auth: " + req.getParameter("error"));
      res.setContentType("text/plain");
      res.getWriter().write("Something went wrong during auth. Please check your log for details");
      return;
    }

    // If we have a code, finish the OAuth 2.0 dance
    if (req.getParameter("code") != null) {
      LOG.info("Got a code. Attempting to exchange for access token.");

      AuthorizationCodeFlow flow = SquareAuthUtil.newAuthorizationCodeFlow();
      TokenResponse tokenResponse = flow.newTokenRequest(req.getParameter("code")).setRedirectUri(WebUtil.buildUrl(req, "/oauth2callbacksquare")).execute();

      LOG.info("tokenResponse: " + tokenResponse);

      String userid = AuthUtil.getUserId(req);
      flow.createAndStoreCredential(tokenResponse, userid + "square");

      // Redirect back to index
      res.sendRedirect(WebUtil.buildUrl(req, "/main"));
      return;
    }

    // Else, we have a new flow. Initiate a new flow.
    LOG.info("No auth context found. Kicking off a new square auth flow.");

    // req.getSession().setAttribute("SquareAuthBean",
    // SquareAuthUtil.getSquareAuthBean());
    // String nextJSP = "/SquareAuth.jsp";
    // RequestDispatcher dispatcher =
    // getServletContext().getRequestDispatcher(nextJSP);
    // dispatcher.forward(req,res);

    AuthorizationCodeFlow flow = SquareAuthUtil.newAuthorizationCodeFlow();
    GenericUrl url = flow.newAuthorizationUrl().setRedirectUri(WebUtil.buildUrl(req, "/oauth2callbacksquare"));
    url.set("request_type", "code");
    url.set("state", "ciao");
    LOG.info("redirecting to URL: " + url.build());
    res.sendRedirect(url.build());
  }

}
