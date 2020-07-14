package com.gingertech.BusStopv3.ui.AdminPanel;

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