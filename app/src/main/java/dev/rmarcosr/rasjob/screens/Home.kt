package dev.rmarcosr.rasjob.screens

//noinspection SuspiciousImport
import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import dev.rmarcosr.rasjob.WorkLog
import dev.rmarcosr.rasjob.deleteWorkLog

/**
 * Represents the main screen of the application ("home" navigation).
 * @param navController The navigation controller.
 * @param data The list of work logs.
 */
@Composable
fun MainScreen(navController: NavController, data: List<WorkLog>) {
    CreateTable(data, navController)
}


/**
 * Create a Table using the work logs
 * @param data The list of work logs.
 * @param navController The navigation controller.
 *
 */
@Composable
fun CreateTable(data: List<WorkLog>, navController: NavController) {
    val headerBackground = Color(0xFFEEEEEE)
    val rowBackground = Color(0xFFF9F9F9)
    val borderColor = Color(0xFFDDDDDD)

    val itemsPerPage = 20
    var currentPage by remember { mutableIntStateOf(1) }
    val visibleData = remember { mutableStateListOf<WorkLog>().apply { addAll(data.take(itemsPerPage)) } }
    val listState = rememberLazyListState()


    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (lastVisibleIndex == totalItems - 1 && visibleData.size < data.size) {
                    currentPage++
                    val nextItems = data.drop((currentPage - 1) * itemsPerPage).take(itemsPerPage)
                    visibleData.addAll(nextItems)
                }
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
        LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            itemsIndexed(visibleData) { index, workLog ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) rowBackground else Color.White)
                        .padding(vertical = 10.dp)
                ) {
                    TableCell(workLog.day, Modifier.weight(2f))
                    TableCell(workLog.start.toString(), Modifier.weight(1.5f))
                    TableCell(workLog.end, Modifier.weight(1.25f))
                    TableCell(workLog.duration.toString(), Modifier.weight(1.4f))

                    Box(
                        modifier = Modifier.weight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = {
                            deleteWorkLog(workLog, data as MutableList<WorkLog>, navController)
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Eliminar",
                                tint = Color.Red,
                                modifier = Modifier.size(30.dp)
                                    .padding(start = 0.dp, end = 15.dp, bottom = 15.dp, top = 0.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Represents a table cell.
 * @param text The text to display in the cell.
 * @param modifier The modifier to apply to the cell
 * @param isHeader Indicates if the cell is a header
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
