/**
 * 
 */
package com.raffaele.squarecash4glass.payment;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author SPAZZRA
 *
 */
public class PaymentBean implements Parcelable {

  public static final String label = "payment_info";

  static public enum ContactType {
    EMAIL, PHONE_NUMBER;
  }

  static public enum PaymentProvider {
    SQUARE, DWOLLA, VENMO;
  }

  private ContactType contactType;
  private String contactInfo;

  private PaymentProvider paymentProvider;

  private BigDecimal amount;

  // this represent PIN or CVV or any additional credential needed to complete
  // the payment.
  private String authCode;
  
  /**
   * @return the contactType
   */
  public ContactType getContactType() {
    return contactType;
  }

  /**
   * @param contactType
   *          the contactType to set
   */
  public void setContactType(ContactType contactType) {
    this.contactType = contactType;
  }

  /**
   * @return the contactInfo
   */
  public String getContactInfo() {
    return contactInfo;
  }

  /**
   * @param contactInfo
   *          the contactInfo to set
   */
  public void setContactInfo(String contactInfo) {
    this.contactInfo = contactInfo;
  }

  /**
   * @return the paymentProvider
   */
  public PaymentProvider getPaymentProvider() {
    return paymentProvider;
  }

  /**
   * @param paymentProvider
   *          the paymentProvider to set
   */
  public void setPaymentProvider(PaymentProvider paymentProvider) {
    this.paymentProvider = paymentProvider;
  }

  /**
   * @return the amount
   */
  public BigDecimal getAmount() {
    return amount;
  }

  /**
   * @param amount
   *          the amount to set
   */
  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  /**
   * @return the authCode
   */
  public String getAuthCode() {
    return authCode;
  }

  /**
   * @param authCode
   *          the authCode to set
   */
  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }

  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  /**
   * 
   */
  public PaymentBean() {
    // TODO Auto-generated constructor stub
  }
  
  public PaymentBean(Parcel in) {
    /*
     *  private ContactType contactType;
        private String contactInfo;
        private PaymentProvider paymentProvider;
        private BigDecimal amount;
        private String authCode;
     */
    // TODO Auto-generated constructor stub
    contactType=(ContactType)in.readSerializable();
    contactInfo=in.readString();
    paymentProvider=(PaymentProvider)in.readSerializable();
    amount=(BigDecimal)in.readSerializable();
    authCode=in.readString();
  }

  @Override
  public int describeContents() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int arg1) {
    // TODO Auto-generated method stub
    out.writeSerializable(contactType);
    out.writeString(contactInfo);
    out.writeSerializable(paymentProvider);
    out.writeSerializable(amount);
    out.writeString(authCode);
  }

  public static final Parcelable.Creator<PaymentBean> CREATOR = new Parcelable.Creator<PaymentBean>() {
    public PaymentBean createFromParcel(Parcel in) {
      return new PaymentBean(in);
    }

    public PaymentBean[] newArray(int size) {
      return new PaymentBean[size];
    }
  };

}
