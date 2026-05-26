package com.vitalzen.ai.features.scan

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(
    onScanFinished: () -> Unit,
    onClose: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val metrics by viewModel.liveMetrics.collectAsState()

    val context = LocalContext.current
    val measurementLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { viewModel.onMeasurementActivityClosed() }
    )
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (!hasCameraPermission) {
        PermissionDeniedScreen(onRetry = { launcher.launch(Manifest.permission.CAMERA) })
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreviewLayer()
        FaceAlignmentOverlay(isScanning = uiState is MeasurementState.Scanning)
        ScanHeader(onClose = onClose)

        if (uiState is MeasurementState.Scanning) {
            ScanningIndicator(progress = (uiState as MeasurementState.Scanning).progress)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                MetricsSection(metrics)
                Spacer(modifier = Modifier.height(16.dp))
                WaveformsSection(metrics)
                Spacer(modifier = Modifier.height(24.dp))
                ActionButtons(
                    uiState = uiState,
                    onStart = {
                        viewModel.startScan()
                        if (viewModel.uiState.value !is MeasurementState.Error) {
                            measurementLauncher.launch(viewModel.createMeasurementIntent())
                        }
                    },
                    onStop = viewModel::stopScan,
                    onFinished = onScanFinished
                )
                Spacer(modifier = Modifier.height(16.dp))
                DisclaimerText()
            }
        }

        if (uiState is MeasurementState.Finished) {
            CompletionOverlay(onNavigate = onScanFinished)
        }
    }
}

@Composable
fun CameraPreviewLayer() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview
                    )
                } catch (_: Exception) {
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun FaceAlignmentOverlay(isScanning: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Darkened background with cutout
        // (Simplified for now, in production use a custom Canvas to draw a cutout)
        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .border(
                    width = 2.dp,
                    brush = Brush.sweepGradient(
                        listOf(Color.Cyan, Color.Magenta, Color.Cyan)
                    ),
                    shape = CircleShape
                )
        )
        
        if (!isScanning) {
            Text(
                "Align your face within the circle",
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp)
            )
        }
    }
}

@Composable
fun ScanHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Text(
            "Live Wellness Scan",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Box(modifier = Modifier.size(48.dp)) // Placeholder for symmetry
    }
}

@Composable
fun ScanningIndicator(progress: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.size(300.dp),
            strokeWidth = 4.dp,
            color = Color.Cyan,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
        Text(
            "$progress%",
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun MetricsSection(metrics: WellnessMetrics) {
    val items = listOf(
        MetricData("Heart Rate", "${metrics.heartRate}", "bpm", Color(0xFFFF5252), metrics.confidenceScores["HR"] ?: 0.9f),
        MetricData("Breathing", "${metrics.breathingRate}", "rpm", Color(0xFF42A5F5), metrics.confidenceScores["BR"] ?: 0.85f),
        MetricData("HRV", "%.1f".format(metrics.hrvRmssd), "ms", Color(0xFF66BB6A), 0.92f),
        MetricData("Mood", metrics.emotions.keys.firstOrNull() ?: "---", "", Color(0xFFFFA726), 0.88f)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(items) { data ->
            PremiumMetricCard(data)
        }
    }
}

@Composable
fun PremiumMetricCard(data: MetricData) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.withAlpha(0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(data.color, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(data.label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(data.value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                if (data.unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(data.unit, fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { data.confidence },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = data.color,
                trackColor = data.color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun WaveformsSection(metrics: WellnessMetrics) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            MiniWaveform("Pulse", metrics.pulseWaveform, Color(0xFFFF5252))
        }
        Box(modifier = Modifier.weight(1f)) {
            MiniWaveform("Breath", metrics.breathingWaveform, Color(0xFF42A5F5))
        }
    }
}

@Composable
fun MiniWaveform(label: String, data: List<Float>, color: Color) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
        Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
            if (data.isEmpty()) return@Canvas
            val path = Path()
            val stepX = size.width / (data.size - 1).coerceAtLeast(1)
            data.forEachIndexed { i, v ->
                val x = i * stepX
                val y = size.height - (v * size.height)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color, style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}

@Composable
fun ActionButtons(
    uiState: MeasurementState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onFinished: () -> Unit
) {
    AnimatedContent(targetState = uiState, label = "Actions") { state ->
        when (state) {
            is MeasurementState.Idle, is MeasurementState.Error -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START WELLNESS SCAN", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
            }
            is MeasurementState.Scanning, is MeasurementState.Initializing -> {
                Button(
                    onClick = onStop,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.withAlpha(0.1f))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STOP SCANNING", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            is MeasurementState.Finished -> {
                Button(
                    onClick = onFinished,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF66BB6A))
                ) {
                    Text("VIEW DETAILED ANALYSIS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CompletionOverlay(onNavigate: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Add a Lottie or generic success animation here
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF66BB6A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", color = Color.White, fontSize = 60.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Scan Complete!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your vitals have been captured.", color = Color.White.copy(alpha = 0.6f))
            
            LaunchedEffect(Unit) {
                delay(2000)
                onNavigate()
            }
        }
    }
}

@Composable
fun DisclaimerText() {
    Text(
        "Disclaimer: This app is for wellness and informational purposes only.",
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Light
    )
}

@Composable
fun PermissionDeniedScreen(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Camera Access Required", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "To measure your vitals, we need access to the front camera.",
            color = Color.White.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry) { Text("Allow Camera Access") }
    }
}

data class MetricData(
    val label: String,
    val value: String,
    val unit: String,
    val color: Color,
    val confidence: Float
)

private fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)
