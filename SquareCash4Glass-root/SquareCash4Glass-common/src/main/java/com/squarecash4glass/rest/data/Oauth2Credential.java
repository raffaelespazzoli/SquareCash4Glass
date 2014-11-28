package com.squarecash4glass.rest.data;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Oauth2Credential implements Serializable {
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  String token;
  String refreshtoken;
  String type;

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getRefreshtoken() {
    return refreshtoken;
  }

  public void setRefreshtoken(String refreshtoken) {
    this.refreshtoken = refreshtoken;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  Long expirationTime;

  public Oauth2Credential(String token, String refreshtoken, Long expirationTime, String type) {
    super();
    this.token = token;
    this.refreshtoken = refreshtoken;
    this.expirationTime = expirationTime;
    this.type = type;
  }

  public Long getExpirationTime() {
    return expirationTime;
  }

  public void setExpirationTime(Long expirationTime) {
    this.expirationTime = expirationTime;
  }

  public Oauth2Credential() {
    super();
  }
}
