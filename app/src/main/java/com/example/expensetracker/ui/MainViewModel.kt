package com.example.expensetracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    categoryRepository: CategoryRepository
) : ViewModel() {

    // App is ready once the first DB query returns (even if empty)
    val isReady = categoryRepository.getAllCategories()
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}
