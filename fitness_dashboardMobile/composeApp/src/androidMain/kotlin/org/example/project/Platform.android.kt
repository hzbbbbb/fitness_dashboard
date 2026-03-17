package org.example.project

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("yyyy年M月d日", Locale.CHINA)
    return formatter.format(Date())
}

actual fun getDateInfo(): DateInfo {
    val cal = Calendar.getInstance()
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1  // Calendar.MONTH is 0-based
    val day = cal.get(Calendar.DAY_OF_MONTH)
    // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday … 7=Saturday
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    // Convert to ISO 8601: 1=Monday … 7=Sunday
    val dayOfWeek = if (dow == Calendar.SUNDAY) 7 else dow - 1
    return DateInfo(year, month, day, dayOfWeek)
}