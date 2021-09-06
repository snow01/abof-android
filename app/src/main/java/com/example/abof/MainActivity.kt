package com.example.abof

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ExperimentDataViewModel by viewModels()
        setContent {
            ExperimentResultScreen(viewModel)
        }

        if (!BuildConfig.RUN_ON_APP_START) {
            AbofExperimentRepository.getInstance().runExperiment()
        }
    }
}

@Composable
fun ExperimentResultScreen(viewModel: ExperimentDataViewModel) {
    val experimentResponse: ExperimentResponse by viewModel.experimentData.observeAsState(
        initial = ExperimentResponse()
    )

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        TimeTaken(kind = "Total", timeTaken = viewModel.totalTimeTaken)
        Spacer(modifier = Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Active Experiments",
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {
            items(experimentResponse.active_experiments) { item ->
                ActiveExperiment(activeExperiment = item)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TrackingData(trackingData = experimentResponse.tracking_data)
        Spacer(modifier = Modifier.height(16.dp))
        Metrics(metrics = viewModel.metrics)
    }
}

@Composable
fun TimeTaken(kind: String, timeTaken: LiveData<Long>) {
    val t: Long by timeTaken.observeAsState(0)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Text(text = "$kind Time Taken: $t ms!", color = MaterialTheme.colors.primary)
    }
}

@Composable
fun ActiveExperiment(activeExperiment: ActiveExperiment) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(3.dp), border = BorderStroke(2.dp, Color.Black)) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(text = "Name = ", style = MaterialTheme.typography.subtitle2)
                Text(text = activeExperiment.short_name, style = MaterialTheme.typography.body2)
            }
            if (activeExperiment.variation != null && activeExperiment.variation.isNotEmpty()) {
                Row {
                    Text(text = "Variation = ", style = MaterialTheme.typography.subtitle2)
                    Text(text = activeExperiment.variation, style = MaterialTheme.typography.body2)
                }
            }
            Row {
                Text(text = "Data = ", style = MaterialTheme.typography.subtitle2)
                Text(text = activeExperiment.data.toString(), style = MaterialTheme.typography.body2)
            }
        }
    }

}

@Composable
fun TrackingData(trackingData: String) {
    Column(Modifier.padding(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Tracking Data",
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2,
            )
        }
        Text(text = trackingData, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun Metrics(metrics: LiveData<List<Gauge>>) {
    val list: List<Gauge> by metrics.observeAsState(emptyList())

    if (list.isEmpty()) {
        return
    }

    Column(Modifier.padding(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                text = "Metrics",
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2,
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),

            ) {
            items(list) { item ->
                Row {
                    Text(text = "${item.kind} = ", style = MaterialTheme.typography.subtitle2)
                    Text(text = "${item.time_taken} ms", style = MaterialTheme.typography.body2)
                }

            }
        }
    }
}