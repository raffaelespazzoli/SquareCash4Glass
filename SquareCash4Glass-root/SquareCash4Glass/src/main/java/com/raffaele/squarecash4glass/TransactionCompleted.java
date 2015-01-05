package com.raffaele.squarecash4glass;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

import retrofit.RestAdapter;
import android.app.Activity;
import android.graphics.drawable.Drawable;
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
import com.raffaele.squarecash4glass.rest.GoogleContactsService;
import com.raffaele.squarecash4glass.rest.Oauth2CredentialService;
import com.squarecash4glass.rest.data.Oauth2Credential;

public class TransactionCompleted extends Activity implements OnClickListener {

  private static final String TAG = "TransactionCompleted";

  private PaymentBean paymentInfo;

  private final static String prodURL = "https://www.dwolla.com/oauth/rest";
  private final static String sandboxURL = "https://uat.dwolla.com/oauth/rest";

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
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(sandboxURL).build();
    DwollaServiceSync dwollaService = restAdapter.create(DwollaServiceSync.class);
    return dwollaService;
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

}
