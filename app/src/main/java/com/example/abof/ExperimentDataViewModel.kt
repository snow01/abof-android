package com.example.abof

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class ExperimentDataViewModel : ViewModel() {
    val totalTimeTaken: LiveData<Long>
    val experimentData: LiveData<ExperimentResponse>

    init {
        val repository = AbofExperimentRepository.getInstance()
        totalTimeTaken = repository.getTotalTimeTaken()
        experimentData = repository.getExperimentResponse()
    }

}