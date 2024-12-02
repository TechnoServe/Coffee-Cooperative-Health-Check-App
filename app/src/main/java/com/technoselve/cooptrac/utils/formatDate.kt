package com.example.cooptrac.utils


import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SimpleDateFormat")
fun formatDate(dateStr: String): String? {
    val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)
    val outputFormat = SimpleDateFormat("yyyy-MM-dd")
    val date = inputFormat.parse(dateStr)

    return date?.let { outputFormat.format(it) }
}

@SuppressLint("SimpleDateFormat")
fun isTodayDate(dateStr: String): Boolean {
    val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)
    val date = dateFormat.parse(dateStr)

    val today = Date()
    val sdf = SimpleDateFormat("yyyyMMdd")
    val formattedDateStr = date?.let { sdf.format(it) }
    val todayStr = sdf.format(today)

    return formattedDateStr == todayStr
}

@SuppressLint("SimpleDateFormat")
fun getCurrentDateFormatted(): String {
    val currentDate = Date()
    val outputFormat = SimpleDateFormat("yyyy-MM-dd")
    return outputFormat.format(currentDate)
}