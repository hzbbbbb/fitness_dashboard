package org.example.project

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getCurrentDate(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy年M月d日"
    formatter.locale = NSLocale("zh_CN")
    return formatter.stringFromDate(NSDate())
}

actual fun getDateInfo(): DateInfo {
    val now = NSDate()
    val fmt = NSDateFormatter()
    // Use a fixed locale so "e" always maps to 1=Sun, 2=Mon … 7=Sat (POSIX convention)
    fmt.locale = NSLocale("en_US_POSIX")

    fmt.dateFormat = "yyyy"
    val year = fmt.stringFromDate(now).toInt()

    fmt.dateFormat = "M"
    val month = fmt.stringFromDate(now).toInt()

    fmt.dateFormat = "d"
    val day = fmt.stringFromDate(now).toInt()

    // "e" with en_US_POSIX: 1=Sunday, 2=Monday … 7=Saturday
    fmt.dateFormat = "e"
    val nsWeekday = fmt.stringFromDate(now).toInt()
    // Convert to ISO 8601: 1=Monday … 7=Sunday
    val dayOfWeek = if (nsWeekday == 1) 7 else nsWeekday - 1

    return DateInfo(year, month, day, dayOfWeek)
}