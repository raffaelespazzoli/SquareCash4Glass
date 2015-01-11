package com.squarecash4glass.util;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Oauth2Factory {

  public static final String GOOGLE="google";
  public static final String SQUARE="square";
  public static final String DWOLLA="dwolla";
  public static final String VENMO="venmo";
  
  private static final Logger LOG = Logger.getLogger(Oauth2Factory.class.getSimpleName());
  
  public static Configuration getOauth2Configuration(String provider, String env) throws ConfigurationException{
      URL url=Oauth2Factory.class.getClassLoader().getResource("com/squarecash4glass/util/"+provider+"oauth_"+env+".properties");
      LOG.info("loading file at url: "+url);
      PropertiesConfiguration propertiesConfiguration=new PropertiesConfiguration(url);
      return propertiesConfiguration;
  }
  
  public static OAuth2Util getOauth2Util(Configuration configuration) throws IOException{
    return new OAuth2Util(configuration);
  }
  
  public static OAuth2Util getOauth2Util(String provider, String env) throws ConfigurationException, IOException{
   return getOauth2Util(getOauth2Configuration(provider, env));
  }
}
