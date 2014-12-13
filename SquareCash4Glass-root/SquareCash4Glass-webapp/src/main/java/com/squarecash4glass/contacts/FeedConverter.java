package com.squarecash4glass.contacts;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3._2005.atom.Feed;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class FeedConverter implements Converter {

  private static final Logger LOG = Logger.getLogger(FeedConverter.class.getSimpleName());
  private static JAXBContext jaxbContext;

  public FeedConverter() throws JAXBException {
    if (jaxbContext == null) {
      jaxbContext = JAXBContext.newInstance("com.a9.__.spec.opensearch._1:com.google.schemas.contact._2008:com.google.schemas.g._2005:org.w3._2005.atom:org.w3._2007.app");
    }
  }

  @Override
  public Object fromBody(TypedInput input, Type type) throws ConversionException {
    LOG.info("type: "+type.getClass() + " " + type.toString());
    if (!type.toString().equals(Feed.class.toString())) {
      throw new IllegalArgumentException("only Feed type is supported: "+type.getClass() + " " + type.toString());
    }
    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      Feed feed = (Feed) unmarshaller.unmarshal(input.in());
      return feed;
    } catch (JAXBException | IOException e) {
      LOG.log(Level.SEVERE, "error unmarshalling input data", e);
      throw new ConversionException(e);
    }
  }

  @Override
  public TypedOutput toBody(final Object output) {
    if (!output.getClass().equals(Feed.class)) {
      throw new IllegalArgumentException("only Feed type is supported");
    }

    TypedOutput typedOutput = new TypedOutput() {

      @Override
      public void writeTo(OutputStream arg0) throws IOException {
        Marshaller marshaller;
        try {
          marshaller = jaxbContext.createMarshaller();
          marshaller.marshal(output, arg0);
        } catch (JAXBException e) {
          LOG.log(Level.SEVERE, "error unmarshalling input data", e);
          throw new IOException(e);
        }

      }

      @Override
      public String mimeType() {
        // TODO Auto-generated method stub
        return "application/xml";
      }

      @Override
      public long length() {
        // TODO Auto-generated method stub
        return -1;
      }

      @Override
      public String fileName() {
        // TODO Auto-generated method stub
        return null;
      }
    };

    return typedOutput;
  }

}
