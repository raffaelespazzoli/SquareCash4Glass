package com.squarecash4glass.rest.data;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class GoogleContactResult {
  List<GoogleContact> googleContactList;
  int totalResults;
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * @return the googleContactList
   */
  public List<GoogleContact> getGoogleContactList() {
    return googleContactList;
  }

  /**
   * @param googleContactList the googleContactList to set
   */
  public void setGoogleContactList(List<GoogleContact> googleContactList) {
    this.googleContactList = googleContactList;
  }

  /**
   * @return the totalResults
   */
  public int getTotalResults() {
    return totalResults;
  }

  /**
   * @param totalResults the totalResults to set
   */
  public void setTotalResults(int totalResults) {
    this.totalResults = totalResults;
  }

  public GoogleContactResult(List<GoogleContact> googleContactList, int totalResults) {
    super();
    this.googleContactList = googleContactList;
    this.totalResults = totalResults;
  }

  public GoogleContactResult() {
    super();
  }

}
