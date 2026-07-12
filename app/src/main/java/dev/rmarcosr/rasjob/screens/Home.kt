package dev.rmarcosr.rasjob.screens

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.rmarcosr.rasjob.R
import dev.rmarcosr.rasjob.WorkLog
import dev.rmarcosr.rasjob.viewmodels.MainViewModel
import kotlinx.serialization.json.Json
import kotlin.math.ceil

/**
 * Main screen of the application.
 * Show a table with the work logs.
 * @param navController The navigation controller to navigate between screens.
 * @param viewModel The view model to administrate the work logs.
 * @param context The context of the application.
 */
@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel, context: Context) {
    val data = viewModel.workLogsList
    CreateTable(data, navController, context, viewModel)
}


/**
 * Create a extensible table with the work logs.
 * @param data The list of work logs to show.
 * @param navController The navigation controller to navigate between screens.
 * @param context The context of the application.
 * @param viewModel The view model to administrate the work logs.
 */
@Composable
fun CreateTable(data: List<WorkLog>, navController: NavController, context: Context, viewModel: MainViewModel) {
    val headerBackground = Color(0xFFEEEEEE)
    val rowBackground = Color(0xFFF9F9F9)
    val borderColor = Color(0xFFDDDDDD)

    val itemsPerPage = 20
    var currentPage by remember { mutableIntStateOf(1) }

    val totalPages = remember(data.size) {
        maxOf(1, ceil(data.size.toDouble() / itemsPerPage).toInt())
    }

    val visibleData by remember(data, currentPage) {
        derivedStateOf {
            val startIndex = (currentPage - 1) * itemsPerPage
            data.drop(startIndex).take(itemsPerPage)
        }
    }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .border(1.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .shadow(2.dp, RoundedCornerShape(8.dp))
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .background(headerBackground)
                .padding(vertical = 12.dp)
        ) {
            TableCell("Día", Modifier.weight(2f), isHeader = true)
            TableCell("Entrada", Modifier.weight(1.5f), isHeader = true)
            TableCell("Salida", Modifier.weight(1.25f), isHeader = true)
            TableCell("Minutos", Modifier.weight(1.5f), isHeader = true)
            TableCell(" ", Modifier.weight(0.5f), isHeader = true)
        }

        HorizontalDivider(thickness = 1.dp, color = borderColor)

        // Table body
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            itemsIndexed(visibleData) { index, workLog ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val jsonString = Json.encodeToString(WorkLog.serializer(), workLog)
                            val encodedJson = Uri.encode(jsonString)
                            navController.navigate("edit/$encodedJson")
                        }
                        .background(if (index % 2 == 0) rowBackground else Color.White)
                        .padding(vertical = 10.dp)
                ) {
                    TableCell(workLog.day, Modifier.weight(2f))
                    if (workLog.isNight) TableCell("${workLog.start} \uD83C\uDF19", Modifier.weight(1.5f))
                    else TableCell(workLog.start.toString(), Modifier.weight(1.5f))

                    TableCell(workLog.end, Modifier.weight(1.25f))
                    TableCell(workLog.duration.toString(), Modifier.weight(1.4f))

                    Box(
                        modifier = Modifier.weight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            viewModel.deleteWorkLog(workLog, context)
                            if (visibleData.size == 1 && currentPage > 1) {
                                currentPage--
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete_icon),
                                contentDescription = "Eliminar",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(thickness = 1.dp, color = borderColor)

        // Pagination control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
        ) {
            Text(
                text = "Página $currentPage de $totalPages",
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            IconButton(
                onClick = { currentPage-- },
                enabled = currentPage > 1
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_left_arrow),
                    contentDescription = "Anterior"
                )
            }
            IconButton(
                onClick = { currentPage++ },
                enabled = currentPage < totalPages
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_right_arrow),
                    contentDescription = "Siguiente"
                )
            }
        }
    }
}
/**
 * Create a table cell.
 * @param text The text to show in the cell.
 * @param modifier The modifier to apply to the cell.
 * @param isHeader Indicate if the cell is a header.
 * @see CreateTable
 */
@Composable
fun TableCell(text: String, modifier: Modifier = Modifier, isHeader: Boolean = false) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 16.sp else 14.sp,
            textAlign = TextAlign.Center,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
    }
}
