package com.vitalzen.ai.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalzen.ai.domain.model.Vitals
import com.vitalzen.ai.domain.repository.VitalsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: VitalsRepository
) : ViewModel() {

    val vitalsHistory: StateFlow<List<Vitals>> = repository.getVitalsHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
