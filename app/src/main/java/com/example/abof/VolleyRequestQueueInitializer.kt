package com.example.abof

import android.content.Context
import androidx.startup.Initializer
import com.android.volley.VolleyLog

class VolleyRequestQueueInitializer : Initializer<VolleyRequestQueue> {
    override fun create(context: Context): VolleyRequestQueue {
        println("======> Creating VolleyRequestQueue")
        VolleyLog.DEBUG = true
        return VolleyRequestQueue.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }
}