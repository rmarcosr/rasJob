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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.rmarcosr.rasjob.screens.AddScreen
import dev.rmarcosr.rasjob.screens.ExportScreen
import dev.rmarcosr.rasjob.screens.MainScreen
import dev.rmarcosr.rasjob.ui.theme.RasJobTheme
import dev.rmarcosr.rasjob.viewmodels.MainViewModel
import kotlinx.serialization.Serializable


/**
 * Main activity of the application.
 * @author Marcos Rodríguez
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            viewModel.obtainDataToFile(this)

            RasJobTheme {
                MyApp(viewModel, this)
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
fun MyApp(viewModel: MainViewModel, context: Context) {
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
                composable("home") { MainScreen(navController, viewModel, context) }
                composable("add") { AddScreen(navController, viewModel, context) }
                composable("export") { ExportScreen(navController, viewModel, context) }
            }
        }
    }
}

/**
 * Class representing a work log.
 * @property day The day of the work log.
 * @property start The start time of the work log.
 * @property end The end time of the work log.
 * @property duration The duration of the work log.
 * @property isNight Indicate if the work log was done in the night.
 */
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class WorkLog(
    val day: String,
    val start: String,
    val end: String,
    val duration : Int,
    val isNight : Boolean,
)




