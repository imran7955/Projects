package com.example.academiccompanion.ui.Announce;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnnounceViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AnnounceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Announce announce fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}