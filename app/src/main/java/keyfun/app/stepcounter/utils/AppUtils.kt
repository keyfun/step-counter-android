package keyfun.app.stepcounter.utils

import java.text.SimpleDateFormat
import java.util.*

object AppUtils {

    fun getFormattedDate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-M-dd HH:mm:ss", Locale.TAIWAN)
        return sdf.format(date)
    }

}