package com.raffaele.squarecash4glass.contacts;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


@Table(name = "emails",id = BaseColumns._ID)
public class EmailDTO extends Model{
  @Column(name = "contact")
  public ContactDTO contact;
  @Column
  private String email;

  public EmailDTO() {
    super();
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public EmailDTO(ContactDTO contact, String email) {
    super();
    this.contact = contact;
    this.email = email;
  }

  public ContactDTO getContact() {
    return contact;
  }

  public void setContact(ContactDTO contact) {
    this.contact = contact;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
