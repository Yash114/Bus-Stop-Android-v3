package com.example.bus_stop_android_v3.ui.BusControl;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class model extends ViewModel {

    private MutableLiveData<String> mText;

    public model() {
        mText = new MutableLiveData<>();
        mText.setValue("This is BusControl fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}