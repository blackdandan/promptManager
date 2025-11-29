package com.promptmanager.app.feature.prompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptmanager.app.core.database.entity.PromptEntity
import com.promptmanager.app.feature.prompt.domain.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromptViewModel @Inject constructor(
    private val promptRepository: PromptRepository
) : ViewModel() {

    val prompts: StateFlow<List<PromptEntity>> = promptRepository.prompts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<PromptUiState>(PromptUiState.Idle)
    val uiState: StateFlow<PromptUiState> = _uiState.asStateFlow()

    init {
        refreshPrompts()
    }

    fun refreshPrompts() {
        viewModelScope.launch {
            _uiState.value = PromptUiState.Loading
            promptRepository.getPrompts(forceRefresh = true)
                .fold(
                    onSuccess = { _uiState.value = PromptUiState.Success },
                    onFailure = { _uiState.value = PromptUiState.Error(it.message ?: "Failed to refresh prompts") }
                )
        }
    }

    fun createPrompt(title: String, content: String) {
        viewModelScope.launch {
            _uiState.value = PromptUiState.Loading
            promptRepository.createPrompt(title, content)
                .fold(
                    onSuccess = { _uiState.value = PromptUiState.Success },
                    onFailure = { _uiState.value = PromptUiState.Error(it.message ?: "Failed to create prompt") }
                )
        }
    }
    
    fun deletePrompt(id: String) {
         viewModelScope.launch {
            _uiState.value = PromptUiState.Loading
            promptRepository.deletePrompt(id)
                .fold(
                    onSuccess = { _uiState.value = PromptUiState.Success },
                    onFailure = { _uiState.value = PromptUiState.Error(it.message ?: "Failed to delete prompt") }
                )
        }
    }

    fun resetUiState() {
        _uiState.value = PromptUiState.Idle
    }
}

sealed class PromptUiState {
    object Idle : PromptUiState()
    object Loading : PromptUiState()
    object Success : PromptUiState()
    data class Error(val message: String) : PromptUiState()
}
