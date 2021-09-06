package com.example.abof

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.json.JSONObject
import java.time.Instant

class VolleyClient constructor(context: Context) : ExperimentRunnerClient {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context.applicationContext)

    override fun runExperiment(
        repository: AbofExperimentRepository
    ) {
        println("Running experiment with volley")
        val startTime = Instant.now().toEpochMilli()

        val url = BuildConfig.ABOF_API_URL

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(repository.getNewRequestBody()),
            { response ->
                repository.experimentResponse.value =
                    repository.gson.fromJson(response.toString(), ExperimentResponse::class.java)

                repository.totalTimeTaken.value = Instant.now().toEpochMilli() - startTime

                println("!!!!!!!!!!!!!!!!!!!! Took time: %s ms".format(repository.totalTimeTaken.value!!))
            },
            { error ->
                println("!!!!!!!!!!!!!!!!!!!! Error: %s".format(error.toString()))
            }
        )

        jsonObjectRequest.setShouldCache(false)

        // Access the RequestQueue through your singleton class.
        this.requestQueue.add(jsonObjectRequest)
    }
}