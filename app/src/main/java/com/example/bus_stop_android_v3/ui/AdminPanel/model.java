package com.example.bus_stop_android_v3.ui.AdminPanel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class model extends ViewModel {

    private MutableLiveData<String> mText;

    public model() {
        mText = new MutableLiveData<>();
        mText.setValue("admin stuff");
    }

    public LiveData<String> getText() {
        return mText;
    }
}