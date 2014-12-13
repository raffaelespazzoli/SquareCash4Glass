package com.squarecash4glass.contacts;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.w3._2005.atom.Feed;
import org.w3._2005.atom.Feed.Entry;

import retrofit.RestAdapter;

import com.google.api.client.auth.oauth2.Credential;
import com.google.schemas.g._2005.Email;
import com.google.schemas.g._2005.FamilyName;
import com.google.schemas.g._2005.FullName;
import com.google.schemas.g._2005.GivenName;
import com.google.schemas.g._2005.Name;
import com.google.schemas.g._2005.PhoneNumber;
import com.squarecash4glass.dto.User;
import com.squarecash4glass.rest.GoogleContactAPIClient;
import com.squarecash4glass.rest.data.GoogleContact;
import com.squarecash4glass.rest.data.GoogleContactResult;
import com.squarecash4glass.util.AuthUtil;

public class GoogleContactsUtils {
  private static final Logger LOG = Logger.getLogger(GoogleContactsUtils.class.getSimpleName());

  private static final String AUTH_TOKEN_TYPE = "cp";
  private static final String ACCOUNTS_TYPE = "com.google";
  private static final DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmssZ", Locale.US);

  public static Credential getCredential(String email) throws IOException {

    List<User> users = ofy().load().type(User.class).list();
    LOG.info("loaded users: " + users);
    LOG.info("uguali? :" + users.get(0).getEmail().equals(email));
    User user = ofy().load().type(User.class).filter("email", email).first().safe();
    LOG.info("loaded user: " + user);
    Credential credential = AuthUtil.getCredentialFromStore(user.getId());
    return credential;

  }

  public static GoogleContactAPIClient createContactService(Credential credential) throws JAXBException {

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://www.google.com").setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new FeedConverter()).build();
    GoogleContactAPIClient contactService = restAdapter.create(GoogleContactAPIClient.class);
    return contactService;
  }

  public static GoogleContactResult processContacts(Feed contacts) {
    int totalResults = 0;
    GoogleContactResult googleContactResult = new GoogleContactResult();
    List<GoogleContact> contactList = new ArrayList<GoogleContact>();
    for (Object oentry : contacts.getAuthorOrGeneratorOrId()) {
      LOG.info("oentry: " + oentry);
      if (oentry instanceof JAXBElement) {
        JAXBElement jaxbentry = (JAXBElement) oentry;
        LOG.info("jaxbentry class: " + jaxbentry.getDeclaredType());
        Entry entry = null;
        if (Entry.class.isAssignableFrom(jaxbentry.getDeclaredType())) {
          entry = (Entry) jaxbentry.getValue();
        } else {
          if (jaxbentry.getName().getLocalPart().equals("totalResults")) {
            totalResults = (Integer) jaxbentry.getValue();
          }
          continue;
        }
        GoogleContact googleContact = new GoogleContact();
        googleContact.setEmails(new ArrayList<String>());
        googleContact.setPhoneNumbers(new ArrayList<String>());
        LOG.info("entry: " + entry);
        for (Object oattribute : entry.getEditedOrContentOrId()) {
          if (oattribute instanceof Name) {
            Name name = (Name) oattribute;
            String fullNameString = null;
            for (Object nameattribute : name.getAdditionalNameOrFamilyNameOrFullName()) {
              if (nameattribute instanceof GivenName) {
                GivenName givenName = (GivenName) nameattribute;
                googleContact.setFirstName(givenName.getValue());
              }
              if (nameattribute instanceof FamilyName) {
                FamilyName lastName = (FamilyName) nameattribute;
                googleContact.setLastName(lastName.getValue());
              }
              if (nameattribute instanceof FullName) {
                FullName fullName = (FullName) nameattribute;
                fullNameString = fullName.getValue();
              }
            }
            if (googleContact.getFirstName() == null && fullNameString != null) {
              googleContact.setFirstName(fullNameString);
            }
          }
          if (oattribute instanceof Email) {
            Email email = (Email) oattribute;
            googleContact.getEmails().add(email.getAddress());
          }
          if (oattribute instanceof PhoneNumber) {
            PhoneNumber phoneNumber = (PhoneNumber) oattribute;
            googleContact.getPhoneNumbers().add(phoneNumber.getValue());
          }
        }
        if (googleContact.getFirstName() != null) {
          contactList.add(googleContact);
        }
      }
    }
    googleContactResult.setGoogleContactList(contactList);
    googleContactResult.setTotalResults(totalResults);
    return googleContactResult;
  }

}
