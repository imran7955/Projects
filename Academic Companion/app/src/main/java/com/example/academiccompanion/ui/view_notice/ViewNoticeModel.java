package com.example.academiccompanion.ui.view_notice;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ViewNoticeModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ViewNoticeModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is view notice fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
