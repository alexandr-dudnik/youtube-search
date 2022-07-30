package com.alexanderdudnik.youtubesearch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.alexanderdudnik.youtubesearch.adapters.YoutubeListAdapter
import com.alexanderdudnik.youtubesearch.databinding.ActivityMainBinding
import com.alexanderdudnik.youtubesearch.viewmodels.SearchViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<SearchViewModel> {
        SearchViewModel.SearchViewModelFactory(
            url =  getString(R.string.search_url),
            pageSize = 5
        )
    }
    private lateinit var binder: ActivityMainBinding
    private val adapter = YoutubeListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binder = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        binder.videoList.adapter = adapter
        binder.searchQueryText.addTextChangedListener {
            viewModel.search(it?.toString()?:"")
        }
        binder.nextPage.setOnClickListener { viewModel.nextPage() }
        binder.prevPage.setOnClickListener { viewModel.prevPage() }

        viewModel.error.observe(this){ message ->
            message?.let{
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.loading.observe(this){ state ->
            binder.progress.isVisible = state
        }

        viewModel.videoList.observe(this){ list ->
            adapter.setNewList(list)
            binder.videoList.isVisible = list.isNotEmpty()
        }

        viewModel.page.observe(this){ number ->
            val num = number+1
            binder.pageNumber.text = num.toString()
        }

    }
}