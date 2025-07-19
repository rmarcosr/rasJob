package dev.rmarcosr.rasjob.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
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
import dev.rmarcosr.rasjob.viewmodels.MainViewModel

@Composable
fun ExportScreen(navController: NavController, viewModel: MainViewModel, context: Context){

    // Launch the file picker, necessary to import data
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
               val importedData = importData(context, uri, viewModel)
                if (importedData.isNotEmpty()) {
                    viewModel.saveDataToFile(context)
                    Toast.makeText(context, "Importación completa: ${importedData.size} registros añadidos", Toast.LENGTH_LONG).show()
                    viewModel.workLogsList.addAll(importedData)
                    viewModel.saveDataToFile(context)
                } else {
                    Toast.makeText(context, "No se encontraron datos válidos en el archivo", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    var deleteData by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(24.dp)) {
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


        Button(
            onClick = { exportData(context, viewModel , deleteData, navController) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(text = "Exportar")
        }

        Button(
            onClick = {
                launcher.launch(arrayOf("text/csv", "application/octet-stream", "*/*"))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Importar")
        }
    }
}


fun exportData(context: Context, viewModel: MainViewModel, deleteData: Boolean, navController: NavController) {
    val data = viewModel.workLogsList

    val csvData = buildString {
        appendLine("day,start,end,duration,isNight")
        data.forEach { workLog ->
            appendLine("${workLog.day},${workLog.start},${workLog.end},${workLog.duration},${workLog.isNight}")
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
        viewModel.deleteAll(context)
        return navController.navigate("home")
    }
}

fun importData(context: Context , uri: Uri , viewModel : MainViewModel): List<WorkLog> {
    val resolver = context.contentResolver
    val importData = mutableListOf<WorkLog>()

    resolver.openInputStream(uri)?.bufferedReader()?.useLines { lines ->
        lines.drop(1)
            .forEach { line ->
                val parts = line.split(",")
                if (parts.size == 5) {
                    val workLog = WorkLog(
                        day = parts[0],
                        start = parts[1],
                        end = parts[2],
                        duration = parts[3].toInt(),
                        isNight = parts[4].toBoolean()
                    )
                    importData.add(workLog)

                    if (importData.isNotEmpty()){
                        viewModel.saveDataToFile(context)
                    }
                }
            }
    }
    return importData
}
