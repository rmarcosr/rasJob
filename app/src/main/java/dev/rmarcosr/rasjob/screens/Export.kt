package dev.rmarcosr.rasjob.screens

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.rmarcosr.rasjob.WorkLog
import dev.rmarcosr.rasjob.saveDataToFile

/**
 * Represent the export screen ("export" navigation).
 * @param navController The navigation controller.
 * @param data The list of work logs.
 * @param context The context of the application.
 */
@Composable
fun ExportScreen(navController: NavController, data : List<WorkLog>, context: Context){

    var deleteData by remember { mutableStateOf(false) }

    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = deleteData,
            onCheckedChange = { deleteData = it }
        )
        Text(
            text = "Eliminar datos después de exportarlos",
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    Row(
        Modifier.padding(24.dp)

    ) {
        Button(onClick = {exportData(context, data, deleteData, navController)}, Modifier.padding(24.dp).fillMaxWidth())
        {Text(text = "Exportar")}
    }
}


/**
 * Export the data to a CSV file, saving the file in the download directory.
 * @param context The context of the application.
 * @param data The list of work logs.
 * @param deleteData Indicates if the data should be deleted after exporting (true) or not (false).
 * @param navController The navigation controller.
 * @see saveDataToFile
 */
fun exportData(context: Context, data: List<WorkLog>, deleteData: Boolean, navController: NavController) {
    val csvData = buildString {
        appendLine("day,start,end,duration")
        data.forEach { workLog ->
            appendLine("${workLog.day},${workLog.start},${workLog.end},${workLog.duration}")
        }
    }

    val fileName = "worklog_export.csv"
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            outputStream.write(csvData.toByteArray())
        }

        contentValues.clear()
        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        Toast.makeText(context, "Archivo guardado en Descargas", Toast.LENGTH_LONG).show()
    }

    if (deleteData){
        saveDataToFile(context, emptyList(), navController)
        Toast.makeText(context, "Cierra y abre la app para ver la lista vacia", Toast.LENGTH_LONG).show()

    }
}
