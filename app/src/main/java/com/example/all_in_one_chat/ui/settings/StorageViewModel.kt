package com.example.all_in_one_chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.all_in_one_chat.domain.model.ChatServiceType
import com.example.all_in_one_chat.domain.usecase.ClearServiceCacheUseCase
import com.example.all_in_one_chat.domain.usecase.GetStorageUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StorageServiceItem(
    val serviceType: ChatServiceType,
    val usageBytes: Long
)

data class StorageViewState(
    val serviceUsages: List<StorageServiceItem> = emptyList(),
    val totalUsageBytes: Long = 0L,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class StorageViewModel @Inject constructor(
    private val getStorageUsageUseCase: GetStorageUsageUseCase,
    private val clearServiceCacheUseCase: ClearServiceCacheUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(StorageViewState())
    val viewState: StateFlow<StorageViewState> = _viewState.asStateFlow()

    init {
        observeStorageUsage()
    }

    private fun observeStorageUsage() {
        viewModelScope.launch {
            val flows = ChatServiceType.entries.map { type ->
                getStorageUsageUseCase.getServiceUsage(type).map { usage ->
                    StorageServiceItem(type, usage)
                }
            }
            
            combine(flows) { usages ->
                usages.toList()
            }.collect { usages ->
                _viewState.value = _viewState.value.copy(
                    serviceUsages = usages,
                    totalUsageBytes = usages.sumOf { it.usageBytes }
                )
            }
        }
    }

    fun clearCache(serviceType: ChatServiceType) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true)
            val result = clearServiceCacheUseCase(serviceType)
            _viewState.value = _viewState.value.copy(
                isLoading = false,
                message = if (result.isSuccess) "${serviceType.name} 캐시가 삭제되었습니다." else "삭제 실패: ${result.exceptionOrNull()?.message}"
            )
        }
    }
}
