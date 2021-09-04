package com.example.abof

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import org.json.JSONObject

class AbofExperimentRepository constructor(private val volleyRequestQueue: VolleyRequestQueue) {

    fun runExperiment() {
        println("Running experiment")
        val url = "http://15.197.158.50/api/run"

        val gson = Gson()
        val jsonRequest = gson.toJson(ExperimentRequest(user_id = "1234"))

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(jsonRequest),
            { response ->
                println("++++++++++++++++++++ Response: %s".format(response.toString()))
            },
            { error ->
                println("!!!!!!!!!!!!!!!!!!!! Error: %s".format(error.toString()))
            }
        )

        // Access the RequestQueue through your singleton class.
        this.volleyRequestQueue.add(jsonObjectRequest)
    }
}

data class ExperimentRequest(
    val app_id: String = "Ok-cUE-XSGUqyRWiqwKti",
    val project_id: String = "V1NauWW7Qs73MR5J6X0ZF",
    val user_id: String,
    val context: ExperimentContext = ExperimentContext()
) {
    data class ExperimentContext(
        val app_version: String = "4.9.16",
        val new_user: Boolean = false
    )
}