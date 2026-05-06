package org.safieddine.ablogistics.ui.screen.fleet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.safieddine.ablogistics.data.BRVDTO
import org.safieddine.ablogistics.data.ReceiptResponse
import org.safieddine.ablogistics.data.service.BRVService

class FleetViewModel(
    private val brvService: BRVService = BRVService
) : ViewModel() {

    private val _brvs = MutableStateFlow<List<BRVDTO>>(emptyList())
    val brvs: StateFlow<List<BRVDTO>> = _brvs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _selectedBrvHistory = MutableStateFlow<List<ReceiptResponse>>(emptyList())
    val selectedBrvHistory: StateFlow<List<ReceiptResponse>> = _selectedBrvHistory

    fun loadFleetStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            try {
                val result = brvService.getFleetStatus()
                if (result.isSuccess) {
                    _brvs.value = result.getOrNull()?.data?.brvs ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load fleet"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBRVHistory(brvId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = brvService.getBRVHistory(brvId)
                if (result.isSuccess) {
                    _selectedBrvHistory.value = result.getOrNull()?.data ?: emptyList()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to load history"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBRV(brv: BRVDTO, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = brvService.saveBRV(brv)
                if (result.isSuccess) {
                    loadFleetStatus()
                    onSuccess()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to save BRV"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBRV(brv: BRVDTO, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = brvService.updateBRV(brv.id, brv)
                if (result.isSuccess) {
                    loadFleetStatus()
                    onSuccess()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to update BRV"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBRV(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val result = brvService.deleteBRV(id)
                if (result.isSuccess) {
                    loadFleetStatus()
                    onSuccess()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to delete BRV"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
