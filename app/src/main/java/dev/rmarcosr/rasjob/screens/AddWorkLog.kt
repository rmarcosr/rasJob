package dev.rmarcosr.rasjob.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import dev.rmarcosr.rasjob.viewmodels.MainViewModel
import java.util.Calendar
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
    // Variables to obtain a actual Date
    val calendar = Calendar.getInstance()
    val yearCalendar = calendar.get(Calendar.YEAR)
    val monthCalendar = calendar.get(Calendar.MONTH)
    val dayCalendar = calendar.get(Calendar.DAY_OF_MONTH)


    // State variables for the input fields on WorkLog
    var day by remember { mutableStateOf("$dayCalendar/${monthCalendar+1}/$yearCalendar") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    var duration by remember { mutableIntStateOf(0) }
    var isNight by remember { mutableStateOf(false) }


    // Another variables for the dropdown menu
    var expandedStart by remember { mutableStateOf(false) }
    var expandedEnd by remember { mutableStateOf(false) }
    val times = listOf("00:00", "00:30", "01:00", "01:30",
        "02:00", "02:30", "03:00", "03:30",
        "04:00", "04:30", "05:00", "05:30",
        "06:00", "06:30", "07:00", "07:30",
        "08:00", "08:30", "09:00", "09:30",
        "10:00", "10:30", "11:00", "11:30",
        "12:00", "12:30", "13:00", "13:30",
        "14:00", "14:30", "15:00", "15:30",
        "16:00", "16:30", "17:00", "17:30",
        "18:00", "18:30", "19:00", "19:30",
        "20:00", "20:30", "21:00", "21:30",
        "22:00", "22:30", "23:00", "23:30")


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(text = "Añadir nuevo registro")

        // Input field for the Day
        OutlinedTextField(
            value = day ,
            onValueChange = { day = it } ,
            label = { Text("Día") } ,
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown menu for the start time
        ExposedDropdownMenuBox(
            expanded = expandedStart,
            onExpandedChange = { expandedStart = !expandedStart }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = start,
                onValueChange = {},
                label = { Text("Hora de inicio") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStart) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedStart,
                onDismissRequest = { expandedStart = false }
            ) {
                times.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            start = time
                            expandedStart = false
                        }
                    )
                }
            }
        }
        // Dropdown menu for the end time
        ExposedDropdownMenuBox(
            expanded = expandedEnd,
            onExpandedChange = { expandedEnd = !expandedEnd }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = end,
                onValueChange = {},
                label = { Text("Hora de salida") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEnd) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expandedEnd,
                onDismissRequest = { expandedEnd = false }
            ) {
                times.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            end = time
                            expandedEnd = false
                        }
                    )
                }
            }
        }

        // Input field for the Duration (calculated)
        duration = calculateDuration(start, end)
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
            val newWorkLog = WorkLog(day, start, end, duration, isNight)
            addNewWorkLog(newWorkLog, viewModel, context, navController)
        }, modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth())
        {Text(text = "Añadir")}

    }
}


/**
 * Calculate the duration of the work log, using the start and end time.
 * @param start The start time of the work log.
 * @param end The end time of the work log.
 * @return The duration of the work log.
 */
@Composable
fun calculateDuration(start : String, end : String) : Int{
    var totalMinus by remember { mutableIntStateOf(0) }

    if (start != "" && end != ""){

        // First Convert a List and separate the hours and minutes
        var startTime = start.split(":")

        // Convert the hours and minutes to minutes
        var startMinus = (startTime[0].toInt() * 60) + startTime[1].toInt()

        var endTime = end.split(":")

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
    viewModel.saveDataToFile(context)
    return navController.navigate("home")
}
