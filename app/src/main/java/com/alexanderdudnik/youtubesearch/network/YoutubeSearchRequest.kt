package com.alexanderdudnik.youtubesearch.network

import okhttp3.*
import java.io.IOException
import java.util.regex.Pattern

class YoutubeSearchRequest(private val baseURL:String) {
    private val httpClient by lazy {
        OkHttpClient.Builder().build()
    }

    fun retrieveVideoSearchList(query: String, callback: (Result<List<String>>)->Unit){
        if (query.isEmpty()) {
            callback.invoke(Result.success(listOf()))
            return
        }

        val request = Request.Builder()
            .url(baseURL+query)
            .build()

        httpClient.newCall(request).enqueue(object:Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback.invoke(Result.failure(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
                    val page = response.body?.string()?: run {
                        callback.invoke(Result.success(emptyList()))
                        return
                    }
                    val result = mutableSetOf<String>()

                    val pattern = Pattern.compile("\"videoId\":\"([a-z0-9]+?)\"", Pattern.MULTILINE or Pattern.CASE_INSENSITIVE)
                    val matcher = pattern.matcher(page)
                    while (matcher.find()) {
                        val matchResult = matcher.toMatchResult()
                        result.add(matchResult.group().split(":")[1].drop(1).dropLast(1))
                    }
                    callback.invoke(Result.success(result.toList()))
                } else {
                    callback.invoke(Result.failure(IOException(response.message)))
                }
            }

        })
    }
}