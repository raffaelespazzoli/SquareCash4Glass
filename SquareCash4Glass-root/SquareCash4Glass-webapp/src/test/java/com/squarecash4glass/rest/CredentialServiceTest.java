package com.squarecash4glass.rest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.Test;

public class CredentialServiceTest {
  private static final Logger log = Logger.getLogger(CredentialServiceTest.class.getName());

  @Test
  public void testGetCredential() throws IOException {

    System.setProperty("http.proxyHost", "proxy.keybank.com");
    System.setProperty("http.proxyPort", "80");
    System.setProperty("https.proxyHost", "proxy.keybank.com");
    System.setProperty("https.proxyPort", "80");

    String email = "raffaele.spazzoli@gmail.com";

    ClientConfig clientConfig = new ClientConfig();

    Client client = ClientBuilder.newClient(clientConfig);

    WebTarget squarecash4glassTarget = client.target("https://squarecash4glass.appspot.com" + "/rest");
    WebTarget credentialServiceTarget = squarecash4glassTarget.path("oauth2credential");
    WebTarget getCredentialsTarget = credentialServiceTarget.path("/credential/" + email);

    Invocation.Builder invocationBuilder = getCredentialsTarget.request(MediaType.APPLICATION_JSON_TYPE);
    Response response = invocationBuilder.get();
    log.fine(response.toString());
    assertEquals(200, response.getStatus());

  }
}
