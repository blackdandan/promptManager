package com.promptmanager.app.feature.prompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptmanager.app.core.database.entity.PromptEntity
import com.promptmanager.app.feature.prompt.domain.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromptViewModel @Inject constructor(
    private val promptRepository: PromptRepository
) : ViewModel() {

    private val _currentFolderId = MutableStateFlow<String?>("all")

    @OptIn(ExperimentalCoroutinesApi::class)
    val prompts: StateFlow<List<PromptEntity>> = _currentFolderId
        .flatMapLatest { folderId ->
            promptRepository.getPromptsByFolder(folderId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<PromptUiState>(PromptUiState.Idle)
    val uiState: StateFlow<PromptUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private var isLastPage = false
    private val pageSize = 20

    init {
        refreshPrompts()
    }

    fun refreshPrompts(folderId: String? = _currentFolderId.value) {
        _currentFolderId.value = folderId
        currentPage = 0
        isLastPage = false
        viewModelScope.launch {
            _uiState.value = PromptUiState.Loading
            promptRepository.getPrompts(folderId = folderId, page = 0, size = pageSize, forceRefresh = true)
                .fold(
                    onSuccess = { _uiState.value = PromptUiState.Success },
                    onFailure = { _uiState.value = PromptUiState.Error(it.message ?: "Failed to refresh prompts") }
                )
        }
    }

    fun loadMorePrompts() {
        if (_uiState.value is PromptUiState.Loading || isLastPage) return

        viewModelScope.launch {
            // We could add a separate LoadingMore state, but for simplicity reusing Loading or keeping Idle
            // ideally we show a footer loader.
            // Let's keep _uiState as is (maybe Success/Idle) and just load.
            // Or add a `isLoadingMore` flag.
            
            currentPage++
            val result = promptRepository.getPrompts(
                folderId = _currentFolderId.value, 
                page = currentPage, 
                size = pageSize, 
                forceRefresh = false
            )
            
            result.onFailure {
                currentPage-- // Revert if failed
                // Optionally show error toast
            }
            // Logic to detect isLastPage would require response metadata (total elements),
            // which we currently don't expose from Repository.
            // Repository returns Result<Unit>.
            // Ideally Repository should return the list or metadata.
            // For now, if no new items are added to DB (Flow doesn't change size much), we assume end?
            // Or simply, if success, we assume there might be more unless we implement full paging metadata.
            // Let's just implement the call for now.
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
