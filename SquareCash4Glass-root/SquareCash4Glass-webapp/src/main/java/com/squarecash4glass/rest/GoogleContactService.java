package com.squarecash4glass.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.commons.configuration.ConfigurationException;
import org.w3._2005.atom.Feed;

import com.google.api.client.auth.oauth2.Credential;
import com.squarecash4glass.contacts.GoogleContactsUtils;
import com.squarecash4glass.rest.data.GoogleContactResult;

@Path("/contacts")
public class GoogleContactService {

  private static final Logger LOG = Logger.getLogger(GoogleContactService.class.getSimpleName());
  @Context
  HttpServletRequest request;
  @Context
  HttpServletResponse response;
  @Context
  ServletContext context;
  
  //format Thu Jan 01 00:00:00 UTC 1970
  private static final SimpleDateFormat googleDataDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
  //private static final SimpleDateFormat googleDataDateFormat=new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
  private static final int pageSize=25;
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{email}")
  public GoogleContactResult getContacts(@PathParam(value = "email") String email, @QueryParam(value="page")int page, @QueryParam(value="startTimeMillis")long startTimeMillis) throws IOException, JAXBException, ConfigurationException {
    LOG.info("getContacts called with: " + email+ " page: "+ page+" startTimeMillis: "+startTimeMillis);
    Credential credential=GoogleContactsUtils.getCredential(email);
    GoogleContactAPIClient googleContactAPIClient=GoogleContactsUtils.createContactService(credential);
    Feed contacts=googleContactAPIClient.getContacts("atom", "", pageSize, page*pageSize+1, googleDataDateFormat.format(new Date(startTimeMillis)),"Bearer "+credential.getAccessToken());
    LOG.info("contacts: "+contacts);
    GoogleContactResult googleContactResult=GoogleContactsUtils.processContacts(contacts);
    LOG.info("contact list: "+googleContactResult);
    return googleContactResult;
  }
  
}
