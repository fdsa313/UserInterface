package com.example.Partenership_Service;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;

public class UniversityStudentApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Kakao SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_app_key));
    }
}

