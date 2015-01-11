package com.raffaele.squarecash4glass;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import retrofit.RestAdapter;
import retrofit.client.Response;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.dwolla.java.sdk.DwollaServiceSync;
import com.dwolla.java.sdk.DwollaTypedBytes;
import com.dwolla.java.sdk.requests.SendRequest;
import com.dwolla.java.sdk.responses.SendResponse;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.Slider;
import com.google.gson.Gson;
import com.raffaele.squarecash4glass.contacts.GoogleContactsUtils;
import com.raffaele.squarecash4glass.payment.PaymentBean;
import com.raffaele.squarecash4glass.payment.PaymentBean.ContactType;
import com.raffaele.squarecash4glass.payment.PaymentBean.PaymentProvider;
import com.raffaele.squarecash4glass.rest.Oauth2CredentialService;
import com.raffaele.squarecash4glass.rest.PreferencesService;
import com.raffaele.squarecash4glass.rest.VenmoService;
import com.squarecash4glass.rest.data.Oauth2Credential;

public class TransactionCompleted extends Activity implements OnClickListener {

  private static final String TAG = "TransactionCompleted";

  private PaymentBean paymentInfo;

  private final static String dwollaProdURL = "https://www.dwolla.com/oauth/rest";
  private final static String dwollaSandboxURL = "https://uat.dwolla.com/oauth/rest";

  private final static String venmoProdURL = "https://api.venmo.com/v1";
  private final static String venmoSandboxURL = "https://sandbox-api.venmo.com/v1";
  
  private final static DecimalFormat venmoAmountFormat=new DecimalFormat("#####.00");
  
  private Slider.Indeterminate mIndeterminate;
  private Slider mSlider;
  
  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      paymentInfo = (PaymentBean) extras.get(PaymentBean.label);
      Log.i(TAG, "deserialized paymentInfo: " + paymentInfo);
    }
    
    View inProgressview=getInProgressCard().getView();
    //inProgressview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    mSlider = Slider.from(inProgressview);
    setContentView(inProgressview);

    new AsyncTask<Void, Void, Boolean>() {

      @Override
      protected void onPreExecute() {
        mIndeterminate = mSlider.startIndeterminate();
      }

      @Override
      protected Boolean doInBackground(Void... args) {
        try {
          return processPaymentRequest();
        } catch (IOException e) {
          e.printStackTrace();
          return false;
        }
      }

      @Override
      protected void onPostExecute(final Boolean success) {
        // Setting data to list adapter
        if (mIndeterminate != null) {
          mIndeterminate.hide();
          mIndeterminate = null;
        }
        CardBuilder cardBuilder = null;
        if (success) {
          cardBuilder = getSuccessCard();
        } else {
          cardBuilder = getFailureCard();
        }
        View view=cardBuilder.getView();
        view.setOnClickListener(TransactionCompleted.this);
        //view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setContentView(view);
      }
    }.execute();

  }

  protected boolean processPaymentRequest() throws IOException {
    // TODO Auto-generated method stub
    if (PaymentProvider.DWOLLA.equals(paymentInfo.getPaymentProvider())){
      return processDwollaPayment();
    }
    if (PaymentProvider.VENMO.equals(paymentInfo.getPaymentProvider())){
      return processVenmoPayment();
    }
    return false;
  }

  private boolean processVenmoPayment() throws IOException {
    String oauth_token = getVenmoOauthToken();
    VenmoService venmoService=createVenmoService();
    Response response=venmoService.makePayment(oauth_token, ContactType.PHONE_NUMBER.equals(paymentInfo.getContactType())?paymentInfo.getContactInfo():null, 
        ContactType.EMAIL.equals(paymentInfo.getContactType())?paymentInfo.getContactInfo():null, 
            null, "payment done with google glass", venmoAmountFormat.format(paymentInfo.getAmount()), "public");
    Log.i(TAG, "venmo response " + response);
    return response.getStatus()==200;
  }

  /**
   * @return
   * @throws IOException
   */
  private boolean processDwollaPayment() throws IOException {
    String oauth_token = getDwollaOauthToken();
    SendRequest sendRequest = new SendRequest(oauth_token, paymentInfo.getAuthCode(), paymentInfo.getContactInfo(), paymentInfo.getAmount().doubleValue());
    Log.i(TAG, "send request " + ToStringBuilder.reflectionToString(sendRequest));
    DwollaServiceSync dwollaService = createDwollaService();
    SendResponse sendResponse = dwollaService.send(new DwollaTypedBytes(new Gson(), sendRequest));
    Log.i(TAG, "send response " + ToStringBuilder.reflectionToString(sendResponse));
    return sendResponse.Success;
  }

  /**
   * @return
   */
  private DwollaServiceSync createDwollaService() {
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(dwollaSandboxURL).build();
    DwollaServiceSync dwollaService = restAdapter.create(DwollaServiceSync.class);
    return dwollaService;
  }
  
  private VenmoService createVenmoService() {
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(venmoSandboxURL).build();
    VenmoService venmoService = restAdapter.create(VenmoService.class);
    return venmoService;
  }

  private CardBuilder getSuccessCard() {
    return new CardBuilder(TransactionCompleted.this, CardBuilder.Layout.COLUMNS_FIXED).setText("congratulations your request has been sent.")
    // add different image whether is phone or email
        .setFootnote("tap to to close").setTimestamp("just now").addImage(R.drawable.success_icon_raster3);
  }

  private CardBuilder getInProgressCard() {
    return new CardBuilder(TransactionCompleted.this, CardBuilder.Layout.COLUMNS_FIXED).setText("payment request in progress ...")
    // add different image whether is phone or email
    // .setFootnote("tap to to close")
        .setTimestamp("just now").addImage(R.drawable.gear_icon_raster3);
  }

  private CardBuilder getFailureCard() {
    return new CardBuilder(TransactionCompleted.this, CardBuilder.Layout.COLUMNS_FIXED).setText("your request was not sent due to an error.")
    // add different image whether is phone or email
        .setFootnote("tap to to close").setTimestamp("just now").addImage(R.drawable.failure_icon_raster3);
  }

  @Override
  public void onClick(View arg0) {
    // TODO Auto-generated method stub
    TransactionCompleted.this.finish();
  }

  private Oauth2CredentialService createOauth2CredentialService() throws IOException {
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://squarecash4glass.appspot.com").build();
    Oauth2CredentialService oauth2CredentialService = restAdapter.create(Oauth2CredentialService.class);
    return oauth2CredentialService;
  }

  private String getDwollaOauthToken() throws IOException {
    Oauth2CredentialService oauth2CredentialService = createOauth2CredentialService();
    String userEmail = new GoogleContactsUtils(this).getUserEmail();
    List<Oauth2Credential> credentials = oauth2CredentialService.getCredentials(userEmail);
    for (Oauth2Credential credential : credentials) {
      if ("dwolla".equals(credential.getType())) {
        return credential.getToken();
      }
    }
    return null;
  }
  
  private String getVenmoOauthToken() throws IOException {
    Oauth2CredentialService oauth2CredentialService = createOauth2CredentialService();
    String userEmail = new GoogleContactsUtils(this).getUserEmail();
    List<Oauth2Credential> credentials = oauth2CredentialService.getCredentials(userEmail);
    for (Oauth2Credential credential : credentials) {
      if ("venmo".equals(credential.getType())) {
        return credential.getToken();
      }
    }
    return null;
  }

}
