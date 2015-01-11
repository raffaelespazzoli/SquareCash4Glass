package com.squarecash4glass.rest;

import org.w3._2005.atom.Feed;

import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Query;

public interface GoogleContactAPIClient {
 
    // https://www.google.com/m8/feeds/contacts/{userEmail}/full
    @GET("/m8/feeds/contacts/default/thin")
    // alt The type of feed to return, such as atom (the default), rss, or json.
    // q Fulltext query on contacts data fields. The API currently supports simple
    // search queries such as q=term1 term2 term3 and exact search queries such as
    // q="term1 term2 term3"
    // max-results The maximum number of entries to return. If you want to receive
    // all of the contacts, rather than only the default maximum, you can specify
    // a very large number for max-results.
    // start-index The 1-based index of the first result to be retrieved (for
    // paging).
    // updated-min
    @Headers({"Gdata-version: 3.0"})
    public Feed getContacts(@Query("alt") String alt, @Query("q") String query, @Query("max-results") int maxResults, @Query("start-index") int startIndex, @Query("updated-min") String dateTime, @Header("Authorization") String auth);
  }


