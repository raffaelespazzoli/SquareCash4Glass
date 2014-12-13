package com.raffaele.squarecash4glass.rest;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;

import com.squarecash4glass.rest.data.Oauth2Credential;

public interface Oauth2CredentialService {
  @GET("/rest/oauth2credential/credential/{email}")
  public List<Oauth2Credential> getCredentials(@Path("email") String email);
}
