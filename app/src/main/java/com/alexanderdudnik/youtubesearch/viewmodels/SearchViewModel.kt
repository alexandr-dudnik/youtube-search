package com.alexanderdudnik.youtubesearch.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alexanderdudnik.youtubesearch.network.YoutubeSearchRequest
import java.util.concurrent.atomic.AtomicInteger

class SearchViewModel(private val url:String):ViewModel() {

    private val _videoList = MutableLiveData<List<String>>(listOf())
    private val _error = MutableLiveData<String?>(null)
    private val _loadingState = MutableLiveData<Boolean>(false)

    val videoList: LiveData<List<String>>
        get() = _videoList
    val error: LiveData<String?>
        get() = _error
    val loading: LiveData<Boolean>
        get() = _loadingState

    private val requestsInQueue:AtomicInteger = AtomicInteger()
    private val request by lazy { YoutubeSearchRequest(url) }

    fun search(queryString: String){
        _loadingState.postValue(true)
        requestsInQueue.incrementAndGet()

        request.retrieveVideoSearchList(queryString){ result ->
            result
                .onFailure {
                    it.message?.let { message ->
                        _error.postValue(message)
                        _error.postValue(null)
                    }
                    _videoList.postValue(emptyList())
                }
                .onSuccess {
                    _videoList.postValue(it)
                }
            val req = requestsInQueue.decrementAndGet()
            when {
                req > 0 -> _loadingState.postValue(true)
                req == 0 -> _loadingState.postValue(false)
                else -> {
                    _loadingState.postValue(false)
                    requestsInQueue.set(0)
                }
            }
        }
    }

    class SearchViewModelFactory(private val url:String):ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(url) as T
        }

    }
}
