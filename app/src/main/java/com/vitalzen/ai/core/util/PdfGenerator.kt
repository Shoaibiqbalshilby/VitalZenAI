package com.vitalzen.ai.core.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.vitalzen.ai.domain.model.Vitals
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {
    fun generateVitalsReport(context: Context, history: List<Vitals>): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        paint.color = Color.BLACK
        paint.textSize = 24f
        canvas.drawText("VitalZen AI - Wellness Report", 40f, 50f, paint)
        
        paint.textSize = 14f
        canvas.drawText("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", 40f, 80f, paint)
        
        var yPos = 120f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 40f, yPos, paint)
        canvas.drawText("HR", 200f, yPos, paint)
        canvas.drawText("SpO2", 280f, yPos, paint)
        canvas.drawText("Score", 360f, yPos, paint)
        canvas.drawText("Mood", 440f, yPos, paint)
        
        paint.isFakeBoldText = false
        yPos += 20f
        
        history.take(20).forEach { item ->
            canvas.drawText(SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(item.timestamp)), 40f, yPos, paint)
            canvas.drawText("${item.heartRate}", 200f, yPos, paint)
            canvas.drawText("${item.oxygenLevel}%", 280f, yPos, paint)
            canvas.drawText("${item.wellnessScore}", 360f, yPos, paint)
            canvas.drawText(item.mood, 440f, yPos, paint)
            yPos += 20f
        }
        
        pdfDocument.finishPage(page)
        
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "VitalZen_Report_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
