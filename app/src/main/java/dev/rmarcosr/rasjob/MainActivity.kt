package dev.rmarcosr.rasjob

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.rmarcosr.rasjob.screens.AddScreen
import dev.rmarcosr.rasjob.screens.ExportScreen
import dev.rmarcosr.rasjob.screens.MainScreen
import dev.rmarcosr.rasjob.ui.theme.RasJobTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Main activity of the application.
 * @author Marcos Rodríguez
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val data: MutableList<WorkLog> = mutableListOf()
            data.addAll(obtainDataToFile(this))

            RasJobTheme {
                MyApp(data, this)
            }
        }
    }
}

/**
 * Main composable function of the application.
 * Contain a upper navbar section to navigate on different screens.
 * @param data The list of work logs.
 * @param context The context of the application.
 */
@Composable
fun MyApp(data : List<WorkLog>, context: Context) {
    val navController = rememberNavController()
    Column(Modifier.fillMaxSize()) {
        // Upper navbar sections
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 50.dp, start = 50.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Button(onClick = { navController.navigate("home") })
            {Text(text = "Inicio")}
            Button(onClick = { navController.navigate("add") })
            {Text(text = "Añadir")}
            Button(onClick = { navController.navigate("export") })
            {Text(text = "Exportar")}
        }

        Box(modifier = Modifier.weight(1f)) {
            NavHost(navController = navController, startDestination = "home") {
                composable("home") { MainScreen(navController, data) }
                composable("add") { AddScreen(navController, data, context) }
                composable("export") { ExportScreen(navController, data, context) }
            }
        }
    }
}

/**
 * Class representing a work log.
 * @property id The unique identifier of the work log.
 * @property day The day of the work log.
 * @property start The start time of the work log.
 * @property end The end time of the work log.
 * @property duration The duration of the work log.
 *
 */
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class WorkLog(
    val id: Int,
    val day: String,
    val start: String,
    val end: String,
    val duration : Int
)

/**
 * Obtain the data from the internal storage.
 * First try to search the file, if don't exist, copy a placeholder file on assets dir.
 * @param context The context of the application.
 * @return A empty list of work logs.
 */
fun obtainDataToFile(context: Context): List<WorkLog> {
    val filename = "data.json"
    val file = File(context.filesDir, filename)

    try {
        // If the file don't exists, copy from assets
        if (!file.exists()) {
            context.assets.open("data/data.json").use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        // Read the content from internal storage
        val jsonString = file.bufferedReader().use { it.readText() }
        return Json.decodeFromString(jsonString)

    } catch (exception: IOException) {
        println("Error reading or copying the work log list: ${exception.message}")
        exception.printStackTrace()
    } catch (exception: SerializationException) {
        println("Error decoding JSON: ${exception.message}")
        exception.printStackTrace()
    }

    return emptyList()
}

/**
 * Save the data to the internal storage, casting the list to JSON.
 * @param context The context of the application.
 * @param workLogs The list of work logs to save.
 */
fun saveDataToFile(context: Context, workLogs: List<WorkLog>, navController: NavController) {
    val filename = "data.json"
    val file = File(context.filesDir, filename)

    try {
        val jsonString = Json.encodeToString(workLogs)
        file.writeText(jsonString, Charsets.UTF_8)
    } catch (exception: IOException) {
        exception.printStackTrace()
    } catch (exception: SerializationException) {
        exception.printStackTrace()
    }
    return navController.navigate("home")
}

/**
 * Delete a work log from the list using the id.
 * @param id The id of the work log to delete.
 * @param data The list of work logs.
 * @param navController The navigation controller.
 * @return The navigation to the home screen.
 */
fun deleteWorkLog(id : Int, data : MutableList<WorkLog>, navController: NavController){
    data.removeIf { it.id == id }

    saveDataToFile(navController.context, data, navController)

    return navController.navigate("home")

}








