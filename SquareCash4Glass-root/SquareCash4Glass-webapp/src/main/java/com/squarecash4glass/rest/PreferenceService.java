package com.squarecash4glass.rest;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.squarecash4glass.dto.User;

@Path("/preference")
public class PreferenceService {

  private static final Logger LOG = Logger.getLogger(PreferenceService.class.getSimpleName());
  @Context
  HttpServletRequest request;
  @Context
  HttpServletResponse response;
  @Context
  ServletContext context;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{email}")
  public Map<String, String> getPreferences(@PathParam(value = "email") String email) {
    LOG.info("getPreferences called with: " + email);
    User user = ofy().load().type(User.class).filter("email", email).first().safe();
    LOG.info("loaded user: " + user);
    Map<String, String> preferences = new HashMap<String, String>();
    if (user != null) {
      preferences.put("provider", user.getProvider());
    }
    return preferences;
  }

}
