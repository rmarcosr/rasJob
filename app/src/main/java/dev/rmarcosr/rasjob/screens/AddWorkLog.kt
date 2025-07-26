package dev.rmarcosr.rasjob.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.rmarcosr.rasjob.WorkLog
import dev.rmarcosr.rasjob.components.DatePickerFieldToModal
import dev.rmarcosr.rasjob.components.HourField
import dev.rmarcosr.rasjob.viewmodels.MainViewModel
import kotlin.math.abs

/**
 * Screen to add a new work log.
 * @param navController The navigation controller to navigate between screens.
 * @param viewModel The view model to administrate the work logs.
 * @param context The context of the application.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddScreen(navController: NavController, viewModel: MainViewModel, context: Context) {

    // State variables for the input fields on WorkLog
    var duration by remember { mutableIntStateOf(0) }
    var isNight by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "Añadir nuevo registro")

        // Input field for the Day
        DatePickerFieldToModal(viewModel)

        // Input field for the Start (true) and End Time (false)
        HourField("Hora de inicio", viewModel, true)

        HourField("Hora de salida", viewModel, false)


        // Input field for the Duration (calculated)
        duration = calculateDuration(viewModel)
        OutlinedTextField(
            value = duration.toString(),
            onValueChange = { duration = it.toInt() } ,
            label = { Text("Duración") } ,
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        //Checkbox to set the work log as night
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isNight,
                onCheckedChange = { isNight = it }
            )
            Text("Horario nocturno")
        }

        // Button to add the work log
        Button(onClick = {
            val newWorkLog = WorkLog(viewModel.day, viewModel.start, viewModel.end, duration, isNight)
            addNewWorkLog(newWorkLog, viewModel, context, navController)
        }, modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth())
        {Text(text = "Añadir")}

    }
}


/**
 * Calculate the duration of the work log, using the start and end time.
 * @param mainViewModel The view model to administrate the start and end time.
 * @return The duration of the work log.
 */
@Composable
fun calculateDuration(mainViewModel: MainViewModel) : Int{
    var totalMinus by remember { mutableIntStateOf(0) }

    if (mainViewModel.start != "" && mainViewModel.end != ""){

        // First Convert a List and separate the hours and minutes
        var startTime = mainViewModel.start.split(":")

        // Convert the hours and minutes to minutes
        var startMinus = (startTime[0].toInt() * 60) + startTime[1].toInt()

        var endTime = mainViewModel.end.split(":")

        var endMinus = (endTime[0].toInt() * 60) + endTime[1].toInt()

        // Verify if the end time is greater than the start time to pass a new day
        totalMinus = if (endMinus > startMinus){
            endMinus - startMinus
        }
        else(endMinus + 1440) - startMinus

        // Finally convert the result to absolute (delete a negative number)
        totalMinus = abs(totalMinus)

        return totalMinus
    }
    return 0
}


/**
 * Add a new work log to the list of work logs.
 * @param newWorkLog The work log to add.
 * @param viewModel The view model to administrate the work logs.
 * @param context The context of the application.
 * @param navController The navigation controller to navigate between screens.
 * @see MainViewModel
 */
fun addNewWorkLog(newWorkLog: WorkLog, viewModel: MainViewModel, context: Context, navController: NavController) {
    viewModel.workLogsList.add(newWorkLog)
    viewModel.orderByDates()
    viewModel.saveDataToFile(context)
    return navController.navigate("home")
}
