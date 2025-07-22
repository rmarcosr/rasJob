package dev.rmarcosr.rasjob.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dev.rmarcosr.rasjob.WorkLog
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime

/**
 * View model to manage the work logs, on the screens.
 * @see ViewModel
 */
class MainViewModel : ViewModel()  {

    // This is the list of work logs used on the screens.
    var workLogsList: SnapshotStateList<WorkLog> = mutableStateListOf()

    var totalDuration by mutableIntStateOf(0)

    var nightDuration by mutableIntStateOf(0)

    /**
     * Obtain the data from the CVS file and casting to work logs list.
     * @param context The context of the application.
     * @exception IOException If the file is not found.
     * @exception SerializationException If the file is not valid.
     */
    fun obtainDataToFile(context: Context) {
        val filename = "data.json"
        val file = File(context.filesDir, filename)

        try {
            if (!file.exists()) {
                context.assets.open("../data/data.json").use { inputStream ->
                    file.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val jsonString = file.bufferedReader().use { it.readText() }
            val list = Json.decodeFromString<List<WorkLog>>(jsonString)

            workLogsList.clear()
            workLogsList.addAll(list)
            totalDuration = calculateTotalDuration(workLogsList)
            nightDuration = calculateNightDuration(workLogsList)

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: SerializationException) {
            e.printStackTrace()
        }
    }

    /**
     * Save the data to the CVS file.
     * @param context The context of the application.
     * @exception IOException If the file is not found.
     * @exception SerializationException If the file is not valid.
     */
    fun saveDataToFile(context: Context) {
        val filename = "data.json"
        val file = File(context.filesDir, filename)

        try {
            val jsonString = Json.encodeToString(workLogsList.toList())
            file.writeText(jsonString, Charsets.UTF_8)
        } catch (exception: IOException) {
            exception.printStackTrace()

        } catch (exception: SerializationException) {
            exception.printStackTrace()
        }
    }

    /**
     * Delete a work log from the list.
     * @param workLog The work log to delete.
     * @param context The context of the application.
     * @see saveDataToFile
     */
    fun deleteWorkLog(workLog: WorkLog, context: Context){
        workLogsList.remove(workLog)
        saveDataToFile(context)
    }

    /**
     * Delete all work logs from the list.
     * @param context The context of the application.
     * @see saveDataToFile
     */
    fun deleteAll(context: Context){
        workLogsList.clear()
        saveDataToFile(context)
    }

    /**
     * Order the work logs by dates.
     * Used the day and start time to order the work logs.
     */
    fun orderByDates() {
        // Is required used this pattern as  the date format don't uses "0X" numbers
        val dateFormatter = java.time.format.DateTimeFormatterBuilder()
            .appendValue(java.time.temporal.ChronoField.DAY_OF_MONTH)
            .appendLiteral('/')
            .appendValue(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            .appendLiteral('/')
            .appendPattern("yyyy")
            .toFormatter()
        val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")

        val sortedList = workLogsList.sortedWith(
            compareBy(
                { LocalDate.parse(it.day, dateFormatter) },
                { LocalTime.parse(it.start, timeFormatter) }
            )
        )
        workLogsList.clear()
        workLogsList.addAll(sortedList)
    }
}


/**
 * Calculate the total duration of the work logs.
 * @param workLogs The list of work logs.
 * @return The total duration of the work logs.
 */
fun calculateTotalDuration(workLogs: List<WorkLog>): Int {
    var duration = 0

    for (workLog in workLogs) { duration += workLog.duration }
    return duration
}

/**
 * Calculate the total duration of the work logs in night.
 * @param workLogs The list of work logs.
 * @return The total duration of the work logs in night.
 */
fun calculateNightDuration(workLogs: List<WorkLog>): Int {
    var duration = 0

    for (workLog in workLogs) {
        if (workLog.isNight) {
            duration += workLog.duration
        }
    }
    return duration
}