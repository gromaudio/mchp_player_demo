package com.gromaudio.vlineservice;

/**
 * Created by Vitaly Kuznetsov <v.kuznetsov.work@gmail.com> on 28.06.17.
 */
interface IBaseServiceListener {

    void onAOAPStatus(int key, int value);
    void onCarPlayStatus(int key, int value);
    void onIAPStatus(int key, int value);
}


