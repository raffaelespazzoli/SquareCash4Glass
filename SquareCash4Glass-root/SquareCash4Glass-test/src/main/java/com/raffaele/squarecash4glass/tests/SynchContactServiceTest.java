package com.raffaele.squarecash4glass.tests;

import android.test.ServiceTestCase;

import com.raffaele.squarecash4glass.contacts.synch.ContactSynchService;

public class SynchContactServiceTest extends ServiceTestCase<ContactSynchService> {

  public SynchContactServiceTest(Class<ContactSynchService> serviceClass) {
    super(serviceClass);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
  }
  
  public void testServiceRun(){
    startService(null);
  }
  
  public void testMultipleServiceRun(){
    startService(null);
    startService(null);
    
  }

}
