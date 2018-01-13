package com.gromaudio.vlineservice;

import com.gromaudio.vlineservice.IBaseServiceListener;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 */
interface IBaseService {

  void addListener(IBaseServiceListener listener);
  void removeListener(IBaseServiceListener listener);
  int  getIAPStatus();
  int  getAOAPStatus();
  int  getCarPlayStatus();
  void activateCarPlay(int activate);
  void sendCarPlayTouchEvent(int x, int y);
}
