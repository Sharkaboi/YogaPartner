package com.sharkaboi.yogapartner.modules.asana_list.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharkaboi.yogapartner.data.DataState
import com.sharkaboi.yogapartner.data.models.Asana
import com.sharkaboi.yogapartner.modules.asana_list.repo.AsanaListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AsanaListViewModel
@Inject
constructor(
    private val asanaListRepository: AsanaListRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errors = MutableLiveData<String>()
    val errors: LiveData<String> = _errors

    private val _asanaList = MutableLiveData<List<Asana>>(emptyList())
    val asanaList: LiveData<List<Asana>> = _asanaList

    private val _currentList = MutableLiveData<List<Asana>>(emptyList())
    val currentList: LiveData<List<Asana>> = _currentList

    init {
        getAsanas()
    }

    private fun getAsanas() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = asanaListRepository.getAsanas()
            _isLoading.value = false
            when (result) {
                is DataState.Failed -> _errors.value = result.message
                is DataState.Success -> {
                    _asanaList.value = result.data
                    _currentList.value = result.data
                }
            }

        }
    }

    fun search(query: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val filteredList = _asanaList.value?.filter {
                it.name.lowercase().contains(query.lowercase())
            }.orEmpty()
            _isLoading.value = false
            _currentList.value = filteredList
        }
    }

    fun resetSearch() {
        _isLoading.value = false
        _currentList.value = _asanaList.value
    }
}
