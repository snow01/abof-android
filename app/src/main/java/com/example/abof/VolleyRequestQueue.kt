package com.example.abof

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class VolleyRequestQueue private constructor(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    companion object {
        private lateinit var INSTANCE: VolleyRequestQueue

        fun create(context: Context): VolleyRequestQueue {
            INSTANCE = VolleyRequestQueue(context)

            return INSTANCE
        }

        fun getInstance(): VolleyRequestQueue {
            return INSTANCE
        }
    }

    fun <T> add(req: Request<T>) {
        requestQueue.add(req)
    }
}