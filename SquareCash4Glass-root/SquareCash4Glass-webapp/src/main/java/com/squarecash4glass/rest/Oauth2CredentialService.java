package com.squarecash4glass.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.mirror.model.TimelineItem;
import com.squarecash4glass.util.AuthUtil;
import com.squarecash4glass.dto.User;
import com.squarecash4glass.rest.data.Oauth2Credential;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Path("/oauth2credential")
public class Oauth2CredentialService {
	
	  private static final Logger LOG = Logger.getLogger(Oauth2CredentialService.class.getSimpleName());
	  @Context
	  HttpServletRequest request;
	  @Context
	  HttpServletResponse response;
	  @Context
	  ServletContext context;
	  
	  /**
	   * insert a bill in the time line.
	   * 
	   * @param bill
	   * @return
	   * @throws IOException
	   * @throws ServletException
	   */
	  @POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Path("/credential/{email}")
	  public List<Oauth2Credential> getCredential(@PathParam(value = "email") String email) throws IOException, ServletException {
	    LOG.fine("getCredential called with: " + email);
	    List<Oauth2Credential> credentials=new ArrayList<Oauth2Credential>();
	    User user=ofy().load().type(User.class).filter("email", email).now();
	    //build get token
	    //build credentials list
	    LOG.fine("getCredential terminated correctly, returning...");
	    return credentials;
	  }

}
