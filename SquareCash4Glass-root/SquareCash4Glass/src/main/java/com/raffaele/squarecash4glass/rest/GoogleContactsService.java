package com.raffaele.squarecash4glass.rest;

import java.util.List;

import com.squarecash4glass.rest.data.GoogleContact;
import com.squarecash4glass.rest.data.GoogleContactResult;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface GoogleContactsService {

  // https://www.google.com/m8/feeds/contacts/{userEmail}/full
  @GET("/rest/contacts/{email}")
  public GoogleContactResult getContacts(@Path("email")String email,@Query("page") int page, @Query("startTimeMillis") long startTimeMillis);
}
