package com.example.all_in_one_chat.ui.auth.whatsapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.data.repository.whatsapp.WhatsAppServiceAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsAppAuthViewModel @Inject constructor(
    private val whatsappAdapter: WhatsAppServiceAdapter
) : ViewModel() {

    val isConnected: StateFlow<Boolean> = whatsappAdapter.isConnected as StateFlow<Boolean>

    fun login() {
        viewModelScope.launch {
            whatsappAdapter.login()
        }
    }
}
