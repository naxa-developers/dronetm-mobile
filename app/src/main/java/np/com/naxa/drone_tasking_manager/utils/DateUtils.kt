package np.com.naxa.drone_tasking_manager.utils

import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    fun currentDateAsStr(): String {
        val s = getCurrentISODate()
        Log.d("AMIT", "currentDateAsStr: $s")
        return s
    }

    private fun getCurrentISODate(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now().atOffset(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT)
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }
    }
}