package com.aone.menurandomchoice;

import android.app.Application;

import com.aone.menurandomchoice.data.oauth.KakaoSDKAdapter;
import com.kakao.auth.KakaoSDK;

public class GlobalApplication extends Application {
    private static volatile GlobalApplication instance = null;

    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null) {
            throw new IllegalStateException("this application does not extends Application.class");
        }

        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        setUp();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        instance = null;
    }

    private void setUp() {
        instance = this;
        KakaoSDK.init(new KakaoSDKAdapter());
    }
}