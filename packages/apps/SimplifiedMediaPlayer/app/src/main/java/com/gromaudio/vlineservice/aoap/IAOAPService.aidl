package com.gromaudio.vlineservice.aoap;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 */
interface IAOAPService {

  boolean enable();
  boolean disable();
  int onEvent(int origin, int event);

}
