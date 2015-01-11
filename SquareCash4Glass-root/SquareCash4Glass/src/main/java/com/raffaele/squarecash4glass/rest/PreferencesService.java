package com.raffaele.squarecash4glass.rest;

import java.util.Map;

import retrofit.http.GET;
import retrofit.http.Path;

public interface PreferencesService {


  @GET("/rest/preference/{email}")
  public Map<String,String> getPreferences(@Path("email")String email);
}
