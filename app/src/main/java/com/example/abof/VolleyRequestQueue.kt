package com.example.abof

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleyRequestQueue private constructor(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    companion object : SingletonHolder<VolleyRequestQueue, Context>(::VolleyRequestQueue)

    fun <T> add(req: Request<T>) {
        requestQueue.add(req)
    }
}