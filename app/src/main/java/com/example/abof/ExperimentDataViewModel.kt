package com.example.abof

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

@RequiresApi(Build.VERSION_CODES.O)
class ExperimentDataViewModel : ViewModel() {
    val totalTimeTaken: LiveData<Long>
    val experimentData: LiveData<ExperimentResponse>

    init {
        val repository = AbofExperimentRepository.getInstance()
        totalTimeTaken = repository.getTotalTimeTaken()
        experimentData = repository.getExperimentResponse()
    }

}