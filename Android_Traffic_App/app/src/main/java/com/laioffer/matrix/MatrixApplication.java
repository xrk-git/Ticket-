package com.laioffer.matrix;


import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;
import com.google.firebase.messaging.FirebaseMessaging;

public class MatrixApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        Stetho.initializeWithDefaults(this);
        FirebaseMessaging.getInstance().subscribeToTopic("Android_Traffic_App");
    }
}
