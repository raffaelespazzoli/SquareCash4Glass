package com.raffaele.squarecash4glass.contacts.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raffaele.squarecash4glass.contacts.GoogleContactsUtils;
import com.raffaele.squarecash4glass.rest.GoogleContactsService;
import com.squarecash4glass.rest.data.GoogleContact;
import com.squarecash4glass.rest.data.GoogleContactResult;

public class GoogleContactsUtilsTest extends TestCase {

  GoogleContactsService contactService;
  GoogleContactsUtils googleContactsUtils;
  Logger logger = LoggerFactory.getLogger(GoogleContactsUtilsTest.class);
  //private static final Logger Log = Logger.getLogger(GoogleContactsUtilsTest.class.getName());
  private static int pageSize = 25;
  private static final SimpleDateFormat googleDataDateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private static Date defaultDate;
  
  @Override
  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
    System.setProperty("http.proxyHost", "proxy.keybank.com");
    System.setProperty("http.proxyPort", "80");
    System.setProperty("https.proxyHost", "proxy.keybank.com");
    System.setProperty("https.proxyPort", "80");
    logger.info("setUp started.");
    googleContactsUtils = new GoogleContactsUtils(null);
//    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://squarecash4glass.appspot.com").build();
//    Oauth2CredentialService credentialService = restAdapter.create(Oauth2CredentialService.class);
//    List<Oauth2Credential> credentials = credentialService.getCredentials("raffaele.spazzoli@gmail.com");
//    String token = credentials.get(0).getToken();
    contactService = googleContactsUtils.createContactService();
    defaultDate=googleDataDateFormat.parse("2000-01-01T00:00:00");
    logger.info("setUp completed.");
  }



  public void testGetContacts() {
    GoogleContactResult contacts;
    long lastUpdate = defaultDate.getTime();
    int page = 0;
    try {
      contacts = googleContactsUtils.getContacts(contactService, lastUpdate, page, "raffaele.spazzoli@gmail.com");
      logger.info(contacts.toString());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }
  
  public void testGetContactsEntireCycle() {
    GoogleContactResult contacts=null;
    long lastUpdate = defaultDate.getTime();;
    int page = 0;;
    do {
      logger.info("getting contacts for page: " + page +" and time: "+SimpleDateFormat.getInstance().format(new Date(lastUpdate)));
      try {
        contacts = googleContactsUtils.getContacts(contactService, lastUpdate, page, "raffaele.spazzoli@gmail.com");
        logger.info(contacts.toString());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        fail(e.getLocalizedMessage());
      }
      logger.info("got contacts for page: " + page);
      logger.info("contacts: " + contacts);
      logger.info("contacts size: "+contacts.getGoogleContactList().size());
      page++;
    } while (page*pageSize < contacts.getTotalResults());
    //googleContactsUtils.updateSynchStatus();
  }

}
