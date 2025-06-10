package com.example.enlacesmentales.utils


import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

// utils/DateAxisValueFormatter.kt
class DateAxisValueFormatter : ValueFormatter() {
    private val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())  // antes: "dd/MM HH:mm"

    override fun getFormattedValue(value: Float): String {
        return try {
            val date = Date(value.toLong())
            sdf.format(date)
        } catch (e: Exception) {
            ""
        }
    }
}





