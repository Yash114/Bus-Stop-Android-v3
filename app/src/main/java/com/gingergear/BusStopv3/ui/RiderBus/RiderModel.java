package com.gingergear.BusStopv3.ui.RiderBus;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RiderModel extends ViewModel {

    private MutableLiveData<String> mText;

    public RiderModel() {
        mText = new MutableLiveData<>();
        mText.setValue("These are all my buses fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}