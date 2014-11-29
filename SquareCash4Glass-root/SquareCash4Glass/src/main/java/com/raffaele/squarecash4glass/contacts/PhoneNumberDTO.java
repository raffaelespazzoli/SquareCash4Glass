package com.raffaele.squarecash4glass.contacts;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "phone_numbers", id = BaseColumns._ID)
public class PhoneNumberDTO extends Model{
  @Column(name = "contact")
  private ContactDTO contact;
  @Column
  private String phonenumber;

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public ContactDTO getContact() {
    return contact;
  }

  public void setContact(ContactDTO contact) {
    this.contact = contact;
  }

  public String getPhonenumber() {
    return phonenumber;
  }

  public void setPhonenumber(String phonenumber) {
    this.phonenumber = phonenumber;
  }

  public PhoneNumberDTO(ContactDTO contact, String phonenumber) {
    super();
    this.contact = contact;
    this.phonenumber = phonenumber;
  }

  public PhoneNumberDTO() {
    super();
  }

}
