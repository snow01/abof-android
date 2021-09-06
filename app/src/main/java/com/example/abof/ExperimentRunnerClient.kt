package com.example.abof

interface ExperimentRunnerClient {
    fun runExperiment(
        repository: AbofExperimentRepository
    )
}