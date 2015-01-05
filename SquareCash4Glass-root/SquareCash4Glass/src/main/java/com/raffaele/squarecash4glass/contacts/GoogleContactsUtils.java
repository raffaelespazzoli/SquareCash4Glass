package com.raffaele.squarecash4glass.contacts;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit.RestAdapter;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.raffaele.squarecash4glass.rest.GoogleContactsService;
import com.raffaele.squarecash4glass.rest.Oauth2CredentialService;
import com.squarecash4glass.rest.data.GoogleContact;
import com.squarecash4glass.rest.data.GoogleContactResult;
import com.squarecash4glass.rest.data.Oauth2Credential;

public class GoogleContactsUtils {
  Logger logger = LoggerFactory.getLogger(GoogleContactsUtils.class);
  private Context context;
  private final static String scopePrefix = "audience:server:client_id:";
  private final static String webclientID = "135749034165-lf5m5cc24pmmfbkfqg0k00e7gmcbociq.apps.googleusercontent.com";
  private final static String scope = scopePrefix + webclientID;
  private static final String ACCOUNTS_TYPE = "com.google";
  private static final DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssZ", Locale.US);

  public GoogleContactsUtils(Context context) {
    this.context = context;
  }

  public String getUserEmail() {
    AccountManager mManager = AccountManager.get(context);

    logger.debug("accounts: " + mManager.getAccounts());

    Account[] accounts = mManager.getAccountsByType(ACCOUNTS_TYPE);

    logger.debug("account[0]" + accounts[0]);

    // TODO at this point the real authentication doesn't work
    // we are going to use the unsecure approach but is is going to be enabled
    // on for myself

    if (!"raffaele.spazzoli@gmail.com".equals(accounts[0].name)) {
      return null;
    }
    return accounts[0].name;
  }

  private String getToken() {

    AccountManager mManager = AccountManager.get(context);

    logger.debug("accounts: " + mManager.getAccounts());

    Account[] accounts = mManager.getAccountsByType(ACCOUNTS_TYPE);

    logger.debug("account[0]" + accounts[0]);

    // TODO at this point the real authentication doesn't work
    // we are going to use the unsecure approach but is is going to be enabled
    // on for myself

    if (!"raffaele.spazzoli@gmail.com".equals(accounts[0].name)) {
      return null;
    }

    // final String token = GoogleAuthUtil.getToken(context, accounts[0].name,
    // scope);

    // return token;
    // TODO implement cross Cross-client Identity
    // https://developers.google.com/accounts/docs/CrossClientAuth
    return null;

  }

  public GoogleContactsService createContactService() throws IOException {
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://squarecash4glass.appspot.com").build();
    GoogleContactsService contactService = restAdapter.create(GoogleContactsService.class);
    return contactService;
  }

  // public void getContacts(ContactEntry entry) throws ServiceException,
  // IOException {
  // if (entry.hasName()) {
  // Name name = entry.getName();
  //
  // if (name.hasFullName()) {
  // String fullName = name.getFullName().getValue();
  // logger.debug(fullName);
  // }
  // }
  //
  // else
  // logger.debug("no name");
  //
  // if (entry.hasPhoneNumbers()) {
  // for (PhoneNumber phone : entry.getPhoneNumbers()) {
  // String phoneNum = phone.getPhoneNumber();
  // logger.debug( phoneNum);
  // }
  // }
  //
  // else
  // logger.debug("no phone");
  //
  // }

  public Pair<Long, Integer> getSynchStatus() throws ParseException {
    SynchStatusDTO synchStatusDTO = new Select().from(SynchStatusDTO.class).executeSingle();
    if (synchStatusDTO != null) {
      Long dateMillis = dateFormat.parse(synchStatusDTO.getLastUpdate()).getTime();
      return new ImmutablePair<Long, Integer>(dateMillis, synchStatusDTO.getPageCompleted());
    }
    return null;
  }

  public GoogleContactResult getContacts(GoogleContactsService myService, Long startTimeMillis, int page, String email) throws IOException {
    return myService.getContacts(email, page, startTimeMillis);
  }

  public void saveContacts(List<GoogleContact> contacts, int page) {
    try {
      logger.debug("started saving contacts");
      logger.debug("transaction begin...");
      ActiveAndroid.beginTransaction();
      for (GoogleContact entry : contacts) {
        logger.debug("saving contact: " + entry);
        ContactDTO contact = new ContactDTO();
        contact.setLastName(entry.getLastName());
        contact.setFirstName(entry.getFirstName());

        From fromOldContacts = new Select().from(ContactDTO.class).where("firstName = ?", contact.getFirstName());

        if (contact.getLastName() != null) {
          fromOldContacts.and("lastName = ?", contact.getLastName());
        } else {
          fromOldContacts.and("lastName IS NULL");
        }

        List<ContactDTO> oldContacts = fromOldContacts.execute();
        // remove any existing contact with the same name
        logger.debug("deleting old contacts with the same name: " + oldContacts);
        for (ContactDTO oldContact : oldContacts) {

          for (EmailDTO email : oldContact.getEmails()) {
            logger.debug("deleting old email of contact with the same name: " + email);
            email.delete();
          }
          for (PhoneNumberDTO phoneNumber : oldContact.getPhoneNumbers()) {
            logger.debug("deleting old phone number contact with the same name: " + phoneNumber);
            phoneNumber.delete();
          }
          logger.debug("deleting old contact with the same name: " + oldContact);
          oldContact.delete();
        }
        logger.debug("saving contact: " + contact);
        contact.save();
        List<String> emails = entry.getEmails();
        for (String email : emails) {
          EmailDTO emailDTO = new EmailDTO(contact, email);
          logger.debug("saving email: " + emailDTO);
          emailDTO.save();
        }
        List<String> phoneNumbers = entry.getPhoneNumbers();
        for (String phoneNumber : phoneNumbers) {
          PhoneNumberDTO phoneNumberDTO = new PhoneNumberDTO(contact, phoneNumber);
          logger.debug("saving phoneNumber: " + phoneNumberDTO);
          phoneNumberDTO.save();
        }
      }
      SynchStatusDTO synchStatusDTO = new Select().from(SynchStatusDTO.class).executeSingle();
      if (synchStatusDTO == null) {
        synchStatusDTO = new SynchStatusDTO(dateFormat.format(new Date(0)), 0);
      }
      synchStatusDTO.setPageCompleted(page + 1);
      logger.debug("saving synchstatus: " + synchStatusDTO);
      synchStatusDTO.save();
      logger.debug("setting transaction successful ...");
      ActiveAndroid.setTransactionSuccessful();
    } finally {
      logger.debug("committing transaction ...");
      ActiveAndroid.endTransaction();
      logger.debug("transaction committed");
    }

  }



  public void updateSynchStatus() {
    // TODO Auto-generated method stub
    SynchStatusDTO synchStatusDTO = new Select().from(SynchStatusDTO.class).executeSingle();
    synchStatusDTO.setPageCompleted(0);
    synchStatusDTO.setLastUpdate(dateFormat.format(new Date()));
    synchStatusDTO.save();
  }



}
