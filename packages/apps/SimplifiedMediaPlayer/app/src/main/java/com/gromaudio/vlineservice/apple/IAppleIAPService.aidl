package com.gromaudio.vlineservice.apple;

import com.gromaudio.vlineservice.apple.IAppleIAPServiceListener;
/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 24.10.17.
 */
interface IAppleIAPService {

  boolean enable();
  boolean disable();
  int onEvent(int origin, int event);

  void addListener(IAppleIAPServiceListener listener);
  void removeListener(IAppleIAPServiceListener listener);

}
