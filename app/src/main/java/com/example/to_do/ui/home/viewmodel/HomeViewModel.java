package com.example.to_do.ui.home.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> _text = new MutableLiveData<>("This is home Fragment");
    public LiveData<String> getText() {
        return _text;
    }
}
