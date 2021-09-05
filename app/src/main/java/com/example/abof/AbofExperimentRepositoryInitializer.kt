package com.example.abof

import android.content.Context
import androidx.startup.Initializer

class AbofExperimentRepositoryInitializer : Initializer<AbofExperimentRepository> {
    override fun create(context: Context): AbofExperimentRepository {
        println("======> Creating AbofExperimentRepository")

        return AbofExperimentRepository.create(VolleyRequestQueue.getInstance())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(VolleyRequestQueueInitializer::class.java)
    }

}