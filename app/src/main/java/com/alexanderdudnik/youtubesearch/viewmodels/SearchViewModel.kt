package com.alexanderdudnik.youtubesearch.viewmodels

import androidx.lifecycle.*
import com.alexanderdudnik.youtubesearch.network.YoutubeSearchRequest
import java.util.concurrent.atomic.AtomicInteger

class SearchViewModel(private val url:String, private val pageSize:Int):ViewModel() {

    private val _videoList = MutableLiveData<List<String>>(listOf())
    private val _error = MutableLiveData<String?>(null)
    private val _loadingState = MutableLiveData<Boolean>(false)
    private val _page = MutableLiveData<Int>(0)

    val videoList: LiveData<List<String>>
        get() = MediatorLiveData<List<String>>().apply {
            addSource(_videoList){ curList->
                val curPage = _page.value?:0
                postValue(curList.drop(curPage * pageSize).take(pageSize))
            }
            addSource(_page){ curPage->
                val curList = _videoList.value?: emptyList()
                postValue(curList.drop(curPage * pageSize).take(pageSize))
            }
        }
    val error: LiveData<String?>
        get() = _error
    val loading: LiveData<Boolean>
        get() = _loadingState
    val page:LiveData<Int>
        get() = _page

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
                    _page.postValue(0)
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

    fun nextPage(){
        if ((_page.value?:0)*pageSize < (_videoList.value?.size?:0) - 1) {
            _page.postValue((_page.value ?: 0) + 1)
        }
    }

    fun prevPage(){
        if ((_page.value?:0) > 0){
            _page.postValue((_page.value?:0) - 1)
        }
    }

    class SearchViewModelFactory(
        private val url:String,
        private val pageSize:Int = 5
        ):ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SearchViewModel(url, pageSize) as T
        }

    }
}
