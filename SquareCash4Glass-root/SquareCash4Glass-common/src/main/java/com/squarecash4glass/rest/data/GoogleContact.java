package com.squarecash4glass.rest.data;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class GoogleContact {

  private String firstName;
  private String lastName;
  private List<String> emails;
  private List<String> phoneNumbers;
  
  /**
   * @return the firstName
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param firstName the firstName to set
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return the secondName
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param secondName the secondName to set
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * @return the emails
   */
  public List<String> getEmails() {
    return emails;
  }

  /**
   * @param emails the emails to set
   */
  public void setEmails(List<String> emails) {
    this.emails = emails;
  }

  /**
   * @return the phoneNumbers
   */
  public List<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  /**
   * @param phoneNumbers the phoneNumbers to set
   */
  public void setPhoneNumbers(List<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public GoogleContact() {
    super();
  }

  public GoogleContact(String firstName, String secondName, List<String> emails, List<String> phoneNumbers) {
    super();
    this.firstName = firstName;
    this.lastName = secondName;
    this.emails = emails;
    this.phoneNumbers = phoneNumbers;
  }
}
