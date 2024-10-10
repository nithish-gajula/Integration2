import android.util.Log
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LOGGING {

    fun INFO(context: String, msg: String) {
        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.size >= 3) {
            val methodName = stackTrace[3].methodName
            val lineNumber = stackTrace[3].lineNumber
            val fileName = stackTrace[3].fileName
            val logContent = "\n[ ${getCurrentDateTime()} ] [INFO] [$fileName] [$methodName] [$lineNumber] \n$msg \n--------------------------------------------"
            val markdownContent = "\n[ *${getCurrentDateTime()}* ] [<font color='blue'>**INFO**</font>] [$fileName] [$methodName] [$lineNumber] \n" +
                    ">$msg \n *** \n"
            reportLog(logContent, markdownContent)
        }
        Log.i(context, msg)

    }

    fun DEBUG(context: String, msg: String) {

        val stackTrace = Thread.currentThread().stackTrace
        if (stackTrace.size >= 3) {
            val methodName = stackTrace[3].methodName
            val lineNumber = stackTrace[3].lineNumber
            val fileName = stackTrace[3].fileName
            val logContent = "\n[ ${getCurrentDateTime()} ] [DEBUG] [$fileName] [$methodName] [$lineNumber] \n$msg \n--------------------------------------------"
            val markdownContent = "\n[ *${getCurrentDateTime()}* ] [<font color='orange'>**DEBUG**</font>] [$fileName] [$methodName] [$lineNumber] \n" +
                    ">$msg \n *** \n"
            reportLog(logContent, markdownContent)
        }
        Log.d(context, msg)
    }

    private fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd HH:mm:ss.SSS")
        return currentDateTime.format(formatter)
    }

    private fun reportLog(log: String, markdown: String) {
        FileWriter(ActivityUtils.reportedLogsFile, true).use { it.write(log) }
        FileWriter(ActivityUtils.reportedReadmeLogsFile, true).use { it.write(markdown) }

    }

}