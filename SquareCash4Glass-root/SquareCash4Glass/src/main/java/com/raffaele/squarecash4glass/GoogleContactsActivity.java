package com.raffaele.squarecash4glass;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.activeandroid.query.Select;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.raffaele.squarecash4glass.contacts.ContactDTO;

public class GoogleContactsActivity extends Activity {

  private static final String TAG = "GoogleContactsActivity";

  private CardScrollView mCardScrollView;
  private ContactsCardScrollAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

  
      mCardScrollView = new CardScrollView(this);
      mAdapter = new ContactsCardScrollAdapter();
      mCardScrollView.setAdapter(mAdapter);
      mCardScrollView.activate();
      setupClickListener();
      setContentView(mCardScrollView);
  }
  
  private class ContactsCardScrollAdapter extends CardScrollAdapter {

    private List<ContactDTO> contacts;
    
    ContactsCardScrollAdapter(){
      contacts = new Select()
      .from(ContactDTO.class)
      .orderBy("firstName ASC")
      .execute();
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
      public int getItemViewType(int position){
          return 1;
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
          return getCard(contacts.get(position)).getView(convertView, parent);
      }
      
      private CardBuilder getCard(ContactDTO contact){
        return new CardBuilder(GoogleContactsActivity.this, CardBuilder.Layout.COLUMNS_FIXED)
        .setText(contact.getFirstName()+" "+contact.getLastName())
        .addImage(R.drawable.contacts_icon);
      }
      
  }
  


  private void setupClickListener() {
      mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
              am.playSoundEffect(Sounds.TAP);
          }
      });
  }
  



}
