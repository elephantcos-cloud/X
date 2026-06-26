package com.example.proqrscanner.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.proqrscanner.data.repository.HistoryRepository
import com.example.proqrscanner.domain.model.ScanItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    private val _scans = MutableLiveData<List<ScanItem>>()
    val scans: LiveData<List<ScanItem>> = _scans

    init {
        _scans.value = repository.getAllScans()
    }
}
