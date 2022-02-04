package com.example.guestprofile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wacinfo.wacextrathaiid.Data.NativeCardInfo

class SharedViewModel : ViewModel() {
    val nativeCardInfo = MutableLiveData<NativeCardInfo>()
    fun setNativeCardInfo(CardInfo: NativeCardInfo) {
        nativeCardInfo.value = CardInfo
    }
}