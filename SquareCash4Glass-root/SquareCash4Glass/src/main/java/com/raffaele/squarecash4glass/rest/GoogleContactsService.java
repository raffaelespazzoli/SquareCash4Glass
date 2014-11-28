package com.raffaele.squarecash4glass.rest;

import retrofit.http.GET;
import retrofit.http.Query;

public interface GoogleContactsService {

  // https://www.google.com/m8/feeds/contacts/{userEmail}/full
  @GET("m8/feeds/contacts/default/full")
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
  public String getContacts(@Query("alt") String alt, @Query("q") String query, @Query("max-results") int maxResults, @Query("start-index") int startIndex, @Query("updated-min") String dateTime);
}
