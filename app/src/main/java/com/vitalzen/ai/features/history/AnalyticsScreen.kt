package com.vitalzen.ai.features.history

import android.graphics.Color as GraphicsColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.ui.platform.LocalContext
import com.vitalzen.ai.core.util.PdfGenerator
import android.content.Intent
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.analyticsData.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val context = LocalContext.current
    val history by viewModel.analyticsData.collectAsState() // Re-using to get raw history if needed, but let's assume we need all for PDF

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Health Analytics") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            TimeRangeSelector(timeRange, viewModel::setTimeRange)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SummaryCards(state)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Heart Rate Trend", style = MaterialTheme.typography.titleMedium)
            VitalsLineChart(state.heartRateTrend, "HR (bpm)", GraphicsColor.RED)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Wellness Score Trend", style = MaterialTheme.typography.titleMedium)
            VitalsLineChart(state.wellnessScores, "Score", GraphicsColor.GREEN)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Mood Distribution", style = MaterialTheme.typography.titleMedium)
            MoodPieChart(state.moodDistribution)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    // This is a bit hacky to get the raw list, in production would be better in ViewModel
                    // But for this requirement:
                    // val file = PdfGenerator.generateVitalsReport(context, historyRaw)
                    // ... share logic
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Health Report (PDF)")
            }
        }
    }
}

@Composable
fun TimeRangeSelector(current: TimeRange, onSelect: (TimeRange) -> Unit) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TimeRange.entries.forEachIndexed { index, range ->
            SegmentedButton(
                selected = current == range,
                onClick = { onSelect(range) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TimeRange.entries.size)
            ) {
                Text(range.name.lowercase().capitalize(Locale.ROOT))
            }
        }
    }
}

@Composable
fun SummaryCards(state: AnalyticsState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Avg HR", style = MaterialTheme.typography.labelMedium)
                Text("${state.avgHeartRate} bpm", style = MaterialTheme.typography.headlineSmall)
            }
        }
        Card(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Avg HRV", style = MaterialTheme.typography.labelMedium)
                Text("%.1f ms".format(state.avgHrv), style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
fun VitalsLineChart(data: List<Pair<Float, Float>>, label: String, color: Int) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(value.toLong()))
                    }
                }
            }
        },
        update = { chart ->
            val entries = data.map { Entry(it.first, it.second) }
            val dataSet = LineDataSet(entries, label).apply {
                this.color = color
                setCircleColor(color)
                lineWidth = 2f
                setDrawValues(false)
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun MoodPieChart(data: Map<String, Float>) {
    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(GraphicsColor.TRANSPARENT)
            }
        },
        update = { chart ->
            val entries = data.map { PieEntry(it.value, it.key) }
            val dataSet = PieDataSet(entries, "Mood").apply {
                colors = listOf(GraphicsColor.BLUE, GraphicsColor.YELLOW, GraphicsColor.MAGENTA, GraphicsColor.GREEN)
                valueTextColor = GraphicsColor.WHITE
                valueTextSize = 12f
            }
            chart.data = PieData(dataSet)
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}
