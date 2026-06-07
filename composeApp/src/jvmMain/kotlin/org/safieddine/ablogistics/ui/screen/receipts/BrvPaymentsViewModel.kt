package org.safieddine.ablogistics.ui.screen.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.BrvPaymentTodoResponse
import org.safieddine.ablogistics.data.service.BRVService
import java.math.BigDecimal

class BrvPaymentsViewModel : ViewModel() {
    private val _payments = MutableStateFlow<List<BrvPaymentTodoResponse>>(emptyList())
    val payments: StateFlow<List<BrvPaymentTodoResponse>> = _payments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    fun loadPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = BRVService.getBrvPayments()
            if (result.isSuccess) {
                _payments.value = result.getOrNull()?.data ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load payments"
            }
            
            _isLoading.value = false
        }
    }

    fun tickPayment(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = BRVService.tickBrvPayment(id)
            if (result.isSuccess) {
                // Refresh list
                loadPayments()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to tick payment"
                _isLoading.value = false
            }
        }
    }

    fun confirmPayments() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = BRVService.confirmBrvPayments()
            if (result.isSuccess) {
                val data = result.getOrNull()?.data
                if (data != null) {
                    _success.value = "Confirmed ${data.confirmedCount} payments for a total of ${data.totalConfirmedAmount}!"
                } else {
                    _success.value = "Payments confirmed successfully"
                }
                loadPayments()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to confirm payments"
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessages() {
        _error.value = null
        _success.value = null
    }
}
