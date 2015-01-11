package com.raffaele.squarecash4glass.rest;

import java.util.Map;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public interface VenmoService {

  
  /**
   * access_token REQUIRED  An authorized user's access token.
phone, email or user_id REQUIRED  Provide a valid US phone, email or Venmo User ID.
note REQUIRED A message to accompany the payment.
amount REQUIRED The amount you want to pay. To create a charge, use a negative amount.
audience  The sharing setting for this payment. Possible values are 'public', 'friends' or 'private'.
   * @param email
   * @return
   */
  @FormUrlEncoded
  @POST("/payments")
  public Response makePayment(@Field("access_token")String access_token,@Field("phone")String phone,@Field("email")String email,@Field("user_id")String user_id,@Field("note")String note,@Field("amount")String amount,@Field("audience")String audience);
}
