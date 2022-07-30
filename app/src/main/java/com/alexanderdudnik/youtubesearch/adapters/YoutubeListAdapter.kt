package com.alexanderdudnik.youtubesearch.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.alexanderdudnik.youtubesearch.R
import com.alexanderdudnik.youtubesearch.databinding.VideoListItemBinding
import okhttp3.OkHttpClient

class YoutubeListAdapter():RecyclerView.Adapter<YoutubeListAdapter.VideoViewHolder>() {
    private val list = mutableListOf<String>()

    class VideoViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        private val binder = VideoListItemBinding.bind(view)

        fun bind(videoId: String){
            val iframe = view.context.getString(R.string.iframe, videoId)
            with(binder.webView){
                webChromeClient = WebChromeClient()
                settings.javaScriptEnabled = true
                loadData(iframe, null, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.video_list_item, parent,  false)
        )
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun setNewList(newList: List<String>){
        DiffUtil.calculateDiff(object : DiffUtil.Callback(){
            override fun getOldListSize(): Int = list.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = list[oldItemPosition] == newList[newItemPosition]
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean = list[oldItemPosition] == newList[newItemPosition]
        }, true).dispatchUpdatesTo(this)

        list.clear()
        list.addAll(newList)
    }

}