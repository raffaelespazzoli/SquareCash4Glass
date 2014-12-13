package com.raffaele.squarecash4glass.contacts;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "contacts", id = BaseColumns._ID)
public class ContactDTO extends Model {

  @Column
  private String firstName;
  @Column
  private String lastName;
  
  public List<EmailDTO> getEmails() {
    return getMany(EmailDTO.class, "contacts");
}
  
  public List<PhoneNumberDTO> getPhoneNumbers() {
    return getMany(PhoneNumberDTO.class, "contacts");
}

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public ContactDTO(String firstName, String lastName) {
    super();
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public ContactDTO() {
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
