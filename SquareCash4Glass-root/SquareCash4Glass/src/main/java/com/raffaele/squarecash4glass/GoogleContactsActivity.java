package com.raffaele.squarecash4glass;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.activeandroid.query.Select;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.raffaele.squarecash4glass.contacts.ContactDTO;
import com.raffaele.squarecash4glass.contacts.EmailDTO;
import com.raffaele.squarecash4glass.contacts.PhoneNumberDTO;
import com.raffaele.squarecash4glass.payment.PaymentBean;
import com.raffaele.squarecash4glass.payment.PaymentBean.ContactType;

public class GoogleContactsActivity extends Activity {

  private static final String TAG = "GoogleContactsActivity";

  private CardScrollView mGoogleContactsCardScrollView;
  private ContactsCardScrollAdapter mGoogleContactsAdapter;

  private CardScrollView mContactInfoCardScrollView;
  private PhoneAndEmailCardScrollAdapter mContactInfoAdapter;

  private boolean inNestedScrolling;
  private GestureDetector mGestureDetector;

  private List<ContactDTO> contacts;
  List<com.raffaele.squarecash4glass.GoogleContactsActivity.PhoneAndEmailCardScrollAdapter.PhoneAndEmailCardInfo> phoneAndEmailCardInfos = new ArrayList<GoogleContactsActivity.PhoneAndEmailCardScrollAdapter.PhoneAndEmailCardInfo>();

  private PaymentBean paymentInfo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      paymentInfo = (PaymentBean) extras.get(PaymentBean.label);
      Log.i(TAG, "deserialized paymentInfo: " + paymentInfo);
    }

    mGoogleContactsCardScrollView = new CardScrollView(this);
    mGoogleContactsAdapter = new ContactsCardScrollAdapter();
    mGoogleContactsCardScrollView.setAdapter(mGoogleContactsAdapter);
    mGoogleContactsCardScrollView.activate();
    setupGoogleContactsClickListener();
    mGestureDetector = createGestureDetector(this);

    setContentView(mGoogleContactsCardScrollView);
  }

  private class ContactsCardScrollAdapter extends CardScrollAdapter {

    ContactsCardScrollAdapter() {
      contacts = new Select().from(ContactDTO.class).orderBy("firstName ASC").execute();
    }

    @Override
    public int getPosition(Object item) {
      return contacts.indexOf(item);
    }

    @Override
    public int getCount() {
      return contacts.size();
    }

    @Override
    public Object getItem(int position) {
      return getCard(contacts.get(position));
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public int getItemViewType(int position) {
      return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = getCard(contacts.get(position)).getView(convertView, parent);
      //view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
      return view;
    }

    private CardBuilder getCard(ContactDTO contact) {
      String name = contact.getFirstName() + (contact.getFirstName().equals(contact.getLastName()) ? "" : (" " + contact.getLastName()));
      return new CardBuilder(GoogleContactsActivity.this, CardBuilder.Layout.COLUMNS_FIXED).setText(name).showStackIndicator(true).setFootnote("tap to select this person").addImage(R.drawable.contact_icon_raster3);
    }

  }

  private void setupGoogleContactsClickListener() {
    mGoogleContactsCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.playSoundEffect(Sounds.TAP);
        mGoogleContactsCardScrollView.deactivate();
        mContactInfoCardScrollView = new CardScrollView(GoogleContactsActivity.this);
        mContactInfoAdapter = new PhoneAndEmailCardScrollAdapter(contacts.get(position));
        mContactInfoCardScrollView.setAdapter(mContactInfoAdapter);
        mContactInfoCardScrollView.activate();
        setupContactInfoClickListener();
        inNestedScrolling = true;
        setContentView(mContactInfoCardScrollView);
      }
    });
  }

  private void setupContactInfoClickListener() {
    mContactInfoCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        am.playSoundEffect(Sounds.TAP);
        doLaunchPinActivity(mContactInfoAdapter.getPhoneAndCardInfoAt(position));
      }
    });
  }

  private class PhoneAndEmailCardScrollAdapter extends CardScrollAdapter {

    class PhoneAndEmailCardInfo {
      private String value;
      private ContactType type;

      /**
       * @return the value
       */
      public String getValue() {
        return value;
      }

      /**
       * @param value
       *          the value to set
       */
      public void setValue(String value) {
        this.value = value;
      }

      /**
       * @return the type
       */
      public ContactType getType() {
        return type;
      }

      /**
       * @param type
       *          the type to set
       */
      public void setType(ContactType type) {
        this.type = type;
      }

      public PhoneAndEmailCardInfo(String value, ContactType type) {
        super();
        this.value = value;
        this.type = type;
      }

      public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
      }

      public String toString() {
        return ToStringBuilder.reflectionToString(this);
      }
    }

    PhoneAndEmailCardScrollAdapter(ContactDTO contact) {
      phoneAndEmailCardInfos=new ArrayList<GoogleContactsActivity.PhoneAndEmailCardScrollAdapter.PhoneAndEmailCardInfo>();
      for (EmailDTO email : contact.getEmails()) {
        phoneAndEmailCardInfos.add(new PhoneAndEmailCardInfo(email.getEmail(), ContactType.EMAIL));
      }
      for (PhoneNumberDTO phoneNumber : contact.getPhoneNumbers()) {
        phoneAndEmailCardInfos.add(new PhoneAndEmailCardInfo(phoneNumber.getPhonenumber(), ContactType.PHONE_NUMBER));
      }
    }

    @Override
    public int getPosition(Object item) {
      return phoneAndEmailCardInfos.indexOf(item);
    }

    @Override
    public int getCount() {
      return phoneAndEmailCardInfos.size();
    }

    @Override
    public Object getItem(int position) {
      return getCard(phoneAndEmailCardInfos.get(position));
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public int getItemViewType(int position) {
      return 1;
    }

    @Override
      public View getView(int position, View convertView, ViewGroup parent) {
        View view=getCard(phoneAndEmailCardInfos.get(position)).getView(convertView, parent);
        //view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return view;
      }

    public PhoneAndEmailCardInfo getPhoneAndCardInfoAt(int position) {
      return phoneAndEmailCardInfos.get(position);
    }

    private CardBuilder getCard(PhoneAndEmailCardInfo contactInfo) {
      return new CardBuilder(GoogleContactsActivity.this, CardBuilder.Layout.COLUMNS_FIXED).setText(contactInfo.getValue())
          // add different image whetehr is phone or email
          .setFootnote("tap to select this " + (contactInfo.getType().equals(ContactType.EMAIL) ? "email" : "phone number"))
          .addImage(contactInfo.getType().equals(ContactType.EMAIL) ? R.drawable.email_icon_raster3 : R.drawable.text_message_icon_raster3);
    }
  }

  private void doLaunchPinActivity(com.raffaele.squarecash4glass.GoogleContactsActivity.PhoneAndEmailCardScrollAdapter.PhoneAndEmailCardInfo phoneAndEmailCardInfo) {
    Intent i = new Intent(this, PinActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    paymentInfo.setContactType(phoneAndEmailCardInfo.getType());
    paymentInfo.setContactInfo(phoneAndEmailCardInfo.getValue());
    i.putExtra(PaymentBean.label, paymentInfo);
    startActivity(i);
  }

  private void doLaunchCVVConfirmActivity() {
    startActivity(new Intent(this, CVVConfirmActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
  }

  // @Override
  // public boolean onKeyDown(int keycode, KeyEvent event) {
  // if (!inNestedScrolling && keycode == KeyEvent.KEYCODE_BACK) {
  // // user tapped touchpad, do something
  // Log.i(TAG, "SWIPE_DOWN is detected");
  // setContentView(mGoogleContactsCardScrollView);
  // inNestedScrolling=false;
  // return true;
  // }
  // return super.onKeyDown(keycode, event);
  // }

  private GestureDetector createGestureDetector(Context context) {
    GestureDetector gestureDetector = new GestureDetector(context);
    gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
      @Override
      public boolean onGesture(Gesture gesture) {
        if (inNestedScrolling && gesture == Gesture.SWIPE_DOWN) {
          Log.i(TAG, "SWIPE_DOWN is detected");
          mContactInfoCardScrollView.deactivate();
          mGoogleContactsCardScrollView.activate();
          setContentView(mGoogleContactsCardScrollView);
          inNestedScrolling = false;
          return true;
        }
        return false;
      }
    });
    return gestureDetector;
  }

  /*
   * Send generic motion events to the gesture detector
   */
  @Override
  public boolean onGenericMotionEvent(MotionEvent event) {
    if (mGestureDetector != null) {
      return mGestureDetector.onMotionEvent(event);
    }
    return false;
  }

}
