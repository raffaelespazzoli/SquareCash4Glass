package com.raffaele.squarecash4glass;

import android.app.Activity;
import android.os.Bundle;

public class CVVConfirmActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.cvv_confirm_view);
    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction().add(R.id.container, new NativeCameraFragment()).commit();
    }
  }



}
