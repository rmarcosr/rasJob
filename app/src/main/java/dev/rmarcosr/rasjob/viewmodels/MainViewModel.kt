package dev.rmarcosr.rasjob.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import dev.rmarcosr.rasjob.WorkLog
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class MainViewModel : ViewModel()  {
    var workLogsList: SnapshotStateList<WorkLog> = mutableStateListOf()


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

        } catch (e: IOException) {
            println("ERROR OBTENER")
            e.printStackTrace()
        } catch (e: SerializationException) {
            println("ERROR OBTENER")
            e.printStackTrace()
        }
    }

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


    fun deleteWorkLog(workLog: WorkLog, context: Context){
        workLogsList.remove(workLog)
        saveDataToFile(context)
    }

    fun deleteAll(context: Context){
        workLogsList.clear()
        saveDataToFile(context)
    }
}