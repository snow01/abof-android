package com.example.abof

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import org.json.JSONObject
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
class AbofExperimentRepository constructor(private val volleyRequestQueue: VolleyRequestQueue) {

    companion object {
        private lateinit var INSTANCE: AbofExperimentRepository

        fun create(volleyRequestQueue: VolleyRequestQueue): AbofExperimentRepository {
            INSTANCE = AbofExperimentRepository(volleyRequestQueue)

            return INSTANCE
        }

        fun getInstance(): AbofExperimentRepository {
            return INSTANCE
        }
    }

    private val totalTimeTaken = MutableLiveData<String>()
    private val experimentResponse = MutableLiveData<ExperimentResponse>()

    init {
        runExperiment()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun runExperiment() {
        println("Running experiment")
        val startTime = Instant.now().toEpochMilli()

        val url = "http://15.197.158.50/api/run"

        val gson = Gson()
        val jsonRequest = gson.toJson(ExperimentRequest(user_id = "1234"))

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(jsonRequest),
            { response ->
                experimentResponse.value =
                    gson.fromJson(response.toString(), ExperimentResponse::class.java)

                totalTimeTaken.value = "%d ms".format(Instant.now().toEpochMilli() - startTime)

                println("!!!!!!!!!!!!!!!!!!!! Took time: %s".format(totalTimeTaken.value!!))
            },
            { error ->
                println("!!!!!!!!!!!!!!!!!!!! Error: %s".format(error.toString()))
            }
        )

        jsonObjectRequest.setShouldCache(false)

        // Access the RequestQueue through your singleton class.
        this.volleyRequestQueue.add(jsonObjectRequest)
    }

    fun getTotalTimeTaken(): LiveData<String> {
        return totalTimeTaken
    }

    fun getExperimentResponse(): LiveData<ExperimentResponse> {
        return experimentResponse
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

data class ExperimentResponse(
    val app_id: String,
    val project_id: String,
    val active_experiments: List<ActiveExperiment>,
    val tracking_cookie_name: String,
    val tracking_data: String,
)

data class ActiveExperiment(
    val short_name: String,
    val variation: String,
    val data: JSONObject
)