package com.example.abof

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson

class AbofExperimentRepository constructor(private val httpClient: ExperimentRunnerClient) {

    companion object {
        private lateinit var INSTANCE: AbofExperimentRepository

        fun create(context: Context): AbofExperimentRepository {
            INSTANCE = when (BuildConfig.HTTP_CLIENT) {
                "cronet" -> {
                    AbofExperimentRepository(CronetClient(context))
                }
                "volley" -> {
                    AbofExperimentRepository(VolleyClient(context))
                }
                "okhttp" -> {
                    AbofExperimentRepository(OkHttpExperimentClient())
                }
                else -> {
                    AbofExperimentRepository(VolleyClient(context))
                }
            }

            return INSTANCE
        }

        fun getInstance(): AbofExperimentRepository {
            return INSTANCE
        }
    }

    var startTime: Long = 0
        private set
    private var previousEventTime: Long = 0
    val totalTimeTaken = MutableLiveData<Long>()
    private val _metrics = emptyList<Gauge>().toMutableList()
    val metrics = MutableLiveData<List<Gauge>>(_metrics)
    val experimentResponse = MutableLiveData<ExperimentResponse>()
    val gson = Gson()

    init {
        if (BuildConfig.RUN_ON_APP_START) {
            runExperiment()
        }
    }

    fun runExperiment() {
        httpClient.runExperiment(this)
    }

    fun getTotalTimeTaken(): LiveData<Long> {
        return totalTimeTaken
    }

    fun getExperimentResponse(): LiveData<ExperimentResponse> {
        return experimentResponse
    }

    fun getNewRequestBody(): String {
        return gson.toJson(ExperimentRequest(user_id = "1234"))
    }

    fun addGauge(kind: String, current_time: Long) {
        _metrics.add(Gauge(kind, current_time - previousEventTime))
        this.metrics.postValue(_metrics)
    }

    fun setStartTime(current_time: Long) {
        this.startTime = current_time
        this.previousEventTime = current_time
    }

    fun setTotalTimeTaken(current_time: Long) {
        this.totalTimeTaken.postValue(current_time - startTime)
    }

    fun getMetrics(): LiveData<List<Gauge>> {
        return metrics
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
    val app_id: String = "",
    val project_id: String = "",
    val active_experiments: List<ActiveExperiment> = emptyList(),
    val tracking_cookie_name: String = "",
    val tracking_data: String = "",
)

data class ActiveExperiment(
    val short_name: String,
    val variation: String,
    val data: com.google.gson.JsonObject
)

data class Gauge(
    val kind: String,
    val time_taken: Long
)